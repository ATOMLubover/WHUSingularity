package com.lubover.singularity.order.entity;

import java.time.LocalDateTime;

/**
 * 库存变更日志实体类
 * 记录库存的所有变更操作用于审计和问题追踪
 */
public class StockChangeLog {

    private Long id;
    private String stockId;
    private String orderId;
    private Integer changeType;  // 1-初始化, 2-锁定, 3-解锁, 4-扣减, 5-退货
    private Integer changeAmount;
    private Integer beforeAvailable;
    private Integer afterAvailable;
    private LocalDateTime createTime;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getStockId() {
        return stockId;
    }

    public void setStockId(String stockId) {
        this.stockId = stockId;
    }

    public String getOrderId() {
        return orderId;
    }

    public void setOrderId(String orderId) {
        this.orderId = orderId;
    }

    public Integer getChangeType() {
        return changeType;
    }

    public void setChangeType(Integer changeType) {
        this.changeType = changeType;
    }

    public Integer getChangeAmount() {
        return changeAmount;
    }

    public void setChangeAmount(Integer changeAmount) {
        this.changeAmount = changeAmount;
    }

    public Integer getBeforeAvailable() {
        return beforeAvailable;
    }

    public void setBeforeAvailable(Integer beforeAvailable) {
        this.beforeAvailable = beforeAvailable;
    }

    public Integer getAfterAvailable() {
        return afterAvailable;
    }

    public void setAfterAvailable(Integer afterAvailable) {
        this.afterAvailable = afterAvailable;
    }

    public LocalDateTime getCreateTime() {
        return createTime;
    }

    public void setCreateTime(LocalDateTime createTime) {
        this.createTime = createTime;
    }

    @Override
    public String toString() {
        return "StockChangeLog{" +
                "id=" + id +
                ", stockId='" + stockId + '\'' +
                ", orderId='" + orderId + '\'' +
                ", changeType=" + changeType +
                ", changeAmount=" + changeAmount +
                ", beforeAvailable=" + beforeAvailable +
                ", afterAvailable=" + afterAvailable +
                ", createTime=" + createTime +
                '}';
    }
}
