package com.lubover.singularity.order.service.impl;

import com.lubover.singularity.order.entity.Stock;
import com.lubover.singularity.order.entity.StockChangeLog;
import com.lubover.singularity.order.mapper.StockChangeLogMapper;
import com.lubover.singularity.order.mapper.StockMapper;
import com.lubover.singularity.order.service.StockService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * 库存业务逻辑实现
 * 支持异步落库和库存管理
 */
@Service
public class StockServiceImpl implements StockService {

    private final StockMapper stockMapper;
    private final StockChangeLogMapper changeLogMapper;

    public StockServiceImpl(StockMapper stockMapper, StockChangeLogMapper changeLogMapper) {
        this.stockMapper = stockMapper;
        this.changeLogMapper = changeLogMapper;
    }

    /**
     * 初始化库存
     * 一般由库存初始化生产者调用
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Stock initializeStock(String stockId, String productId, String slotId, Integer totalStock) {
        Stock stock = new Stock();
        stock.setStockId(stockId);
        stock.setProductId(productId);
        stock.setSlotId(slotId);
        stock.setTotalStock(totalStock);
        stock.setAvailableStock(totalStock);
        stock.setLockedStock(0);
        stock.setSoldStock(0);
        stock.setStatus(0); // 未开始
        stock.setCreateTime(LocalDateTime.now());
        stock.setUpdateTime(LocalDateTime.now());
        stock.setIsDeleted(0);
        stock.setVersion(0);

        stockMapper.insert(stock);

        // 记录初始化日志
        StockChangeLog log = new StockChangeLog();
        log.setStockId(stockId);
        log.setChangeType(1); // 初始化
        log.setChangeAmount(totalStock);
        log.setBeforeAvailable(0);
        log.setAfterAvailable(totalStock);
        log.setCreateTime(LocalDateTime.now());
        changeLogMapper.insert(log);

        return stock;
    }

    /**
     * 获取库存信息
     */
    @Override
    public Stock getStock(String stockId) {
        return stockMapper.selectByStockId(stockId);
    }

    /**
     * 按时间槽获取库存
     */
    @Override
    public Stock getStockBySlotId(String slotId) {
        return stockMapper.selectBySlotId(slotId);
    }

    /**
     * 异步扣减库存
     * 仅返回是否成功提交到MQ，实际扣减在消费者处理
     */
    @Override
    public boolean decreaseStockAsync(String stockId, String orderId, Integer quantity) {
        // 这里仅做了逻辑校验，实际异步操作由StockConsumer处理
        Stock stock = stockMapper.selectByStockId(stockId);
        if (stock == null || stock.getAvailableStock() < quantity) {
            return false;
        }
        return true;
    }

    /**
     * 扣减库存（同步版本，用于MQ消费）
     * 这是削峰填谷的核心异步落库方法
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean decreaseStock(String stockId, String orderId, Integer quantity) {
        Stock stock = stockMapper.selectByStockId(stockId);
        if (stock == null || stock.getIsDeleted() == 1) {
            return false;
        }

        // 检查库存足够且库存状态有效
        if (stock.getAvailableStock() < quantity) {
            // 记录失败日志
            recordStockChangeLog(stock, orderId, 4, quantity, false);
            return false;
        }

        // 使用乐观锁更新库存
        int oldAvailable = stock.getAvailableStock();
        stock.setAvailableStock(oldAvailable - quantity);
        stock.setSoldStock(stock.getSoldStock() + quantity);
        stock.setUpdateTime(LocalDateTime.now());

        int updated = stockMapper.updateByVersion(stock);
        if (updated == 0) {
            // 并发冲突，版本号不匹配
            return false;
        }

        // 记录成功的库存变更日志
        recordStockChangeLog(stock, orderId, 4, quantity, true);

        return true;
    }

    /**
     * 库存回退
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean returnStock(String stockId, String orderId, Integer quantity) {
        Stock stock = stockMapper.selectByStockId(stockId);
        if (stock == null || stock.getIsDeleted() == 1) {
            return false;
        }

        // 使用乐观锁更新库存
        int oldSold = stock.getSoldStock();
        if (oldSold < quantity) {
            return false;
        }

        int oldAvailable = stock.getAvailableStock();
        stock.setAvailableStock(oldAvailable + quantity);
        stock.setSoldStock(oldSold - quantity);
        stock.setUpdateTime(LocalDateTime.now());

        int updated = stockMapper.updateByVersion(stock);
        if (updated == 0) {
            return false;
        }

        // 记录退货日志
        recordStockChangeLog(stock, orderId, 5, quantity, true);

        return true;
    }

    /**
     * 查询库存变更日志
     */
    @Override
    public List<StockChangeLog> getStockChangeLogs(String stockId, int limit) {
        return changeLogMapper.selectByStockId(stockId, limit);
    }

    /**
     * 查询订单相关的库存变更
     */
    @Override
    public List<StockChangeLog> getOrderStockChangeLogs(String orderId) {
        return changeLogMapper.selectByOrderId(orderId);
    }

    /**
     * 记录库存变更日志
     */
    private void recordStockChangeLog(Stock stock, String orderId, Integer changeType, Integer changeAmount, boolean success) {
        StockChangeLog log = new StockChangeLog();
        log.setStockId(stock.getStockId());
        log.setOrderId(orderId);
        log.setChangeType(changeType);
        log.setChangeAmount(changeAmount);
        
        if (success) {
            log.setBeforeAvailable(stock.getAvailableStock() + changeAmount);
            log.setAfterAvailable(stock.getAvailableStock());
        } else {
            log.setBeforeAvailable(stock.getAvailableStock());
            log.setAfterAvailable(stock.getAvailableStock());
        }
        
        log.setCreateTime(LocalDateTime.now());
        changeLogMapper.insert(log);
    }
}
