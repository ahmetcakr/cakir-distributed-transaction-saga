package cakir.saga_orchestrator.model.dto;

import java.math.BigDecimal;

public class StockEvent {
    private Long orderId;
    private String orderStatus;
    private BigDecimal price;
    private Long userId;
    private String idempotencyKey;

    public StockEvent() {
    }

    public StockEvent(Long orderId, String orderStatus, String idempotencyKey) {
        this.orderId = orderId;
        this.orderStatus = orderStatus;
        this.idempotencyKey = idempotencyKey;
    }

    public StockEvent(Long orderId, String orderStatus, BigDecimal price, Long userId, String idempotencyKey) {
        this.orderId = orderId;
        this.orderStatus = orderStatus;
        this.price = price;
        this.userId = userId;
        this.idempotencyKey = idempotencyKey;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public void setPrice(BigDecimal price) {
        this.price = price;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public Long getOrderId() {
        return orderId;
    }

    public void setOrderId(Long orderId) {
        this.orderId = orderId;
    }

    public String getOrderStatus() {
        return orderStatus;
    }

    public void setOrderStatus(String orderStatus) {
        this.orderStatus = orderStatus;
    }

    public String getIdempotencyKey() {
        return idempotencyKey;
    }

    public void setIdempotencyKey(String idempotencyKey) {
        this.idempotencyKey = idempotencyKey;
    }
}