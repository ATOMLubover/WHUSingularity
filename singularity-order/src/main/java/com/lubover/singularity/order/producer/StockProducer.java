package com.lubover.singularity.order.producer;

import com.alibaba.fastjson2.JSON;
import com.lubover.singularity.order.dto.StockMessage;
import org.apache.rocketmq.spring.core.RocketMQTemplate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * 库存消息生产者
 * 
 * 负责将库存操作事件发送到MQ队列
 * 支持:
 * 1. 库存初始化消息
 * 2. 库存扣减消息
 * 3. 库存回退消息
 */
@Component
public class StockProducer {

    private static final Logger logger = LoggerFactory.getLogger(StockProducer.class);

    private final RocketMQTemplate rocketMQTemplate;

    private static final String STOCK_TOPIC = "stock-topic";
    private static final String STOCK_TAG_INIT = "init";
    private static final String STOCK_TAG_DECREASE = "decrease";
    private static final String STOCK_TAG_RETURN = "return";

    public StockProducer(RocketMQTemplate rocketMQTemplate) {
        this.rocketMQTemplate = rocketMQTemplate;
    }

    /**
     * 发送库存初始化消息
     */
    public void sendStockInitMessage(String stockId, String productId, String slotId, Integer totalStock) {
        StockMessage message = new StockMessage(stockId, productId, slotId, totalStock, 0);
        
        String destination = STOCK_TOPIC + ":" + STOCK_TAG_INIT;
        String payload = JSON.toJSONString(message);
        
        logger.info("发送库存初始化消息: stockId={}, destination={}", stockId, destination);
        
        try {
            rocketMQTemplate.convertAndSend(destination, payload);
            logger.info("库存初始化消息发送成功: stockId={}", stockId);
        } catch (Exception e) {
            logger.error("库存初始化消息发送失败: stockId={}", stockId, e);
            throw new RuntimeException("发送库存初始化消息失败", e);
        }
    }

    /**
     * 发送库存扣减消息
     * 这是削峰填谷的核心: 高并发的扣减请求通过MQ异步处理
     */
    public void sendStockDecreaseMessage(String stockId, String orderId, Integer quantity) {
        StockMessage message = new StockMessage();
        message.setStockId(stockId);
        message.setOrderId(orderId);
        message.setQuantity(quantity);
        message.setTimestamp(System.currentTimeMillis());
        
        String destination = STOCK_TOPIC + ":" + STOCK_TAG_DECREASE;
        String payload = JSON.toJSONString(message);
        
        logger.info("发送库存扣减消息: stockId={}, orderId={}, quantity={}", stockId, orderId, quantity);
        
        try {
            // 使用相同的 key 保证消息顺序性
            rocketMQTemplate.convertAndSend(destination, payload, msg -> {
                msg.setKeys(stockId); // 用 stockId 作为 key 保证单个库存的消息顺序
                return msg;
            });
            logger.info("库存扣减消息发送成功: stockId={}, orderId={}", stockId, orderId);
        } catch (Exception e) {
            logger.error("库存扣减消息发送失败: stockId={}, orderId={}", stockId, orderId, e);
            throw new RuntimeException("发送库存扣减消息失败", e);
        }
    }

    /**
     * 发送库存回退消息
     */
    public void sendStockReturnMessage(String stockId, String orderId, Integer quantity) {
        StockMessage message = new StockMessage();
        message.setStockId(stockId);
        message.setOrderId(orderId);
        message.setQuantity(quantity);
        message.setTimestamp(System.currentTimeMillis());
        
        String destination = STOCK_TOPIC + ":" + STOCK_TAG_RETURN;
        String payload = JSON.toJSONString(message);
        
        logger.info("发送库存回退消息: stockId={}, orderId={}, quantity={}", stockId, orderId, quantity);
        
        try {
            rocketMQTemplate.convertAndSend(destination, payload);
            logger.info("库存回退消息发送成功: stockId={}, orderId={}", stockId, orderId);
        } catch (Exception e) {
            logger.error("库存回退消息发送失败: stockId={}, orderId={}", stockId, orderId, e);
            throw new RuntimeException("发送库存回退消息失败", e);
        }
    }
}
