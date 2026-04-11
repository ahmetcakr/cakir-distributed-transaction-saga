package cakir.saga_orchestrator.model.dto;

import java.math.BigDecimal;

public class PaymentEvent {
    private Long orderId;
    private String status;
    private BigDecimal price;

    public PaymentEvent() {
    }

    public PaymentEvent(Long orderId, String status) {
        this.orderId = orderId;
        this.status = status;
    }

    public PaymentEvent(Long orderId, String status, BigDecimal price) {
        this.orderId = orderId;
        this.status = status;
        this.price = price;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public void setPrice(BigDecimal price) {
        this.price = price;
    }

    public Long getOrderId() {
        return orderId;
    }

    public void setOrderId(Long orderId) {
        this.orderId = orderId;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}