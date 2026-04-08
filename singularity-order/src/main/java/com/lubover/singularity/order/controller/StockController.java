package com.lubover.singularity.order.controller;

import com.lubover.singularity.order.entity.Stock;
import com.lubover.singularity.order.producer.StockProducer;
import com.lubover.singularity.order.service.StockService;
import org.springframework.web.bind.annotation.*;

/**
 * 库存管理API控制器
 */
@RestController
@RequestMapping("/api/stock")
public class StockController {

    private final StockService stockService;
    private final StockProducer stockProducer;

    public StockController(StockService stockService, StockProducer stockProducer) {
        this.stockService = stockService;
        this.stockProducer = stockProducer;
    }

    /**
     * 初始化库存
     */
    @PostMapping("/init")
    public Stock initStock(
            @RequestParam String stockId,
            @RequestParam String productId,
            @RequestParam String slotId,
            @RequestParam Integer totalStock) {
        
        // 发送初始化消息到MQ
        stockProducer.sendStockInitMessage(stockId, productId, slotId, totalStock);
        
        // 同步等待消费（实际应该异步，这里仅作示例）
        try {
            Thread.sleep(500);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        
        return stockService.getStock(stockId);
    }

    /**
     * 获取库存信息
     */
    @GetMapping("/{stockId}")
    public Stock getStock(@PathVariable String stockId) {
        return stockService.getStock(stockId);
    }

    /**
     * 获取库存信息（按槽ID）
     */
    @GetMapping("/slot/{slotId}")
    public Stock getStockBySlot(@PathVariable String slotId) {
        return stockService.getStockBySlotId(slotId);
    }

    /**
     * 扣减库存（异步方式）
     * 将扣减请求发送到MQ，由消费者异步处理
     */
    @PostMapping("/decrease-async")
    public String decreaseStockAsync(
            @RequestParam String stockId,
            @RequestParam String orderId,
            @RequestParam Integer quantity) {
        
        // 检查库存充足
        if (!stockService.decreaseStockAsync(stockId, orderId, quantity)) {
            return "库存不足, 无法生成扣减消息";
        }
        
        // 发送扣减消息到MQ，由StockConsumer异步处理
        stockProducer.sendStockDecreaseMessage(stockId, orderId, quantity);
        
        return "库存扣减消息已发送，等待异步处理";
    }

    /**
     * 库存回退
     */
    @PostMapping("/return")
    public String returnStock(
            @RequestParam String stockId,
            @RequestParam String orderId,
            @RequestParam Integer quantity) {
        
        // 发送回退消息到MQ
        stockProducer.sendStockReturnMessage(stockId, orderId, quantity);
        
        return "库存回退消息已发送，等待异步处理";
    }
}
