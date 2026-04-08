package com.lubover.singularity.order.listener;

import com.alibaba.fastjson2.JSON;
import com.lubover.singularity.order.dto.StockMessage;
import com.lubover.singularity.order.service.StockService;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * 库存消息异步消费者
 * 
 * 削峰填谷实现：
 * 1. 承载高并发的库存扣减请求通过MQ消息队列缓冲
 * 2. 异步消费消息，按顺序处理
 * 3. 最终落库到MySQL，确保数据一致性
 * 
 * 消费者组：stock-consumer-group
 * Topic：stock-topic
 */
@Component
@RocketMQMessageListener(
    topic = "stock-topic",
    consumerGroup = "stock-consumer-group",
    selectorExpression = "*",
    consumeMode = org.apache.rocketmq.spring.annotation.ConsumeMode.ORDERLY
)
public class StockConsumer implements RocketMQListener<String> {

    private static final Logger logger = LoggerFactory.getLogger(StockConsumer.class);

    private final StockService stockService;

    public StockConsumer(StockService stockService) {
        this.stockService = stockService;
    }

    @Override
    public void onMessage(String message) {
        logger.info("消费库存消息: {}", message);
        
        try {
            // 解析消息
            StockMessage stockMsg = JSON.parseObject(message, StockMessage.class);
            
            if (stockMsg == null) {
                logger.warn("库存消息解析失败: {}", message);
                return;
            }

            // 根据消息类型进行处理
            processStockMessage(stockMsg);

        } catch (Exception e) {
            logger.error("处理库存消息异常: {}", message, e);
            throw new RuntimeException("库存消息处理失败: " + e.getMessage(), e);
        }
    }

    /**
     * 处理库存消息
     * 支持多种类型: 初始化、扣减、回退等
     */
    private void processStockMessage(StockMessage stockMsg) {
        String stockId = stockMsg.getStockId();
        
        if (stockMsg.getTotalStock() != null && stockMsg.getOrderId() == null) {
            // 库存初始化消息
            logger.info("初始化库存: stockId={}, totalStock={}", stockId, stockMsg.getTotalStock());
            
            stockService.initializeStock(
                stockId,
                stockMsg.getProductId(),
                stockMsg.getSlotId(),
                stockMsg.getTotalStock()
            );
            
            logger.info("库存初始化成功: stockId={}", stockId);
            
        } else if (stockMsg.getOrderId() != null && stockMsg.getQuantity() != null) {
            // 库存扣减消息
            String orderId = stockMsg.getOrderId();
            Integer quantity = stockMsg.getQuantity();
            
            logger.info("扣减库存: stockId={}, orderId={}, quantity={}", stockId, orderId, quantity);
            
            boolean success = stockService.decreaseStock(stockId, orderId, quantity);
            
            if (success) {
                logger.info("库存扣减成功: stockId={}, orderId={}, quantity={}", stockId, orderId, quantity);
            } else {
                logger.warn("库存扣减失败: stockId={}, orderId={}, quantity={}", stockId, orderId, quantity);
                // 可选: 发送死信或重试
                throw new RuntimeException("库存扣减失败: " + stockId);
            }
        } else {
            logger.warn("无法识别的库存消息: {}", stockMsg);
        }
    }
}
