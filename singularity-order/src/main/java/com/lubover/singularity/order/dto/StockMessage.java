package com.lubover.singularity.order.dto;

import java.io.Serializable;

/**
 * 库存消息DTO
 * 用于MQ消息传输
 */
public class StockMessage implements Serializable {

    private static final long serialVersionUID = 1L;

    private String stockId;
    private String productId;
    private String slotId;
    private Integer totalStock;
    private Integer status;
    private Long timestamp;
    
    // 库存扣减相关
    private String orderId;
    private Integer quantity;

    public StockMessage() {
    }

    public StockMessage(String stockId, String productId, String slotId, Integer totalStock, Integer status) {
        this.stockId = stockId;
        this.productId = productId;
        this.slotId = slotId;
        this.totalStock = totalStock;
        this.status = status;
        this.timestamp = System.currentTimeMillis();
    }

    public String getStockId() {
        return stockId;
    }

    public void setStockId(String stockId) {
        this.stockId = stockId;
    }

    public String getProductId() {
        return productId;
    }

    public void setProductId(String productId) {
        this.productId = productId;
    }

    public String getSlotId() {
        return slotId;
    }

    public void setSlotId(String slotId) {
        this.slotId = slotId;
    }

    public Integer getTotalStock() {
        return totalStock;
    }

    public void setTotalStock(Integer totalStock) {
        this.totalStock = totalStock;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    public Long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Long timestamp) {
        this.timestamp = timestamp;
    }

    public String getOrderId() {
        return orderId;
    }

    public void setOrderId(String orderId) {
        this.orderId = orderId;
    }

    public Integer getQuantity() {
        return quantity;
    }

    public void setQuantity(Integer quantity) {
        this.quantity = quantity;
    }

    @Override
    public String toString() {
        return "StockMessage{" +
                "stockId='" + stockId + '\'' +
                ", productId='" + productId + '\'' +
                ", slotId='" + slotId + '\'' +
                ", totalStock=" + totalStock +
                ", status=" + status +
                ", timestamp=" + timestamp +
                ", orderId='" + orderId + '\'' +
                ", quantity=" + quantity +
                '}';
    }
}
