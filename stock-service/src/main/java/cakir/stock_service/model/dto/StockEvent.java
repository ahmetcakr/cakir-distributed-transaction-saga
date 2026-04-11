package cakir.stock_service.model.dto;

import java.math.BigDecimal;

public class StockEvent {
    private Long orderId;
    private String orderStatus;
    private BigDecimal price;
    private Long userId;

    public StockEvent() {
    }

    public StockEvent(Long orderId, String orderStatus) {
        this.orderId = orderId;
        this.orderStatus = orderStatus;
    }

    public StockEvent(Long orderId, String orderStatus, BigDecimal price, Long userId) {
        this.orderId = orderId;
        this.orderStatus = orderStatus;
        this.price = price;
        this.userId = userId;
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
}