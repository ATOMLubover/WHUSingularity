package com.lubover.singularity.order.service;

import com.lubover.singularity.order.entity.Stock;
import com.lubover.singularity.order.entity.StockChangeLog;

import java.util.List;

/**
 * 库存业务逻辑层接口
 */
public interface StockService {

    /**
     * 初始化库存
     */
    Stock initializeStock(String stockId, String productId, String slotId, Integer totalStock);

    /**
     * 获取库存信息
     */
    Stock getStock(String stockId);

    /**
     * 按时间槽获取库存
     */
    Stock getStockBySlotId(String slotId);

    /**
     * 异步扣减库存
     * @param stockId 库存ID
     * @param orderId 订单ID
     * @param quantity 扣减数量
     * @return 是否成功
     */
    boolean decreaseStockAsync(String stockId, String orderId, Integer quantity);

    /**
     * 扣减库存（同步版本，用于MQ消费）
     */
    boolean decreaseStock(String stockId, String orderId, Integer quantity);

    /**
     * 库存回退
     */
    boolean returnStock(String stockId, String orderId, Integer quantity);

    /**
     * 查询库存变更日志
     */
    List<StockChangeLog> getStockChangeLogs(String stockId, int limit);

    /**
     * 查询订单相关的库存变更
     */
    List<StockChangeLog> getOrderStockChangeLogs(String orderId);
}
