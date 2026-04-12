package cakir.saga_orchestrator.model.dto;

import java.math.BigDecimal;

public class PaymentEvent {
    private Long orderId;
    private String status;
    private BigDecimal price;
    private String idempotencyKey;

    public PaymentEvent() {
    }

    public PaymentEvent(Long orderId, String status, String idempotencyKey) {
        this.orderId = orderId;
        this.status = status;
        this.idempotencyKey = idempotencyKey;
    }

    public PaymentEvent(Long orderId, String status, BigDecimal price, String idempotencyKey) {
        this.orderId = orderId;
        this.status = status;
        this.price = price;
        this.idempotencyKey = idempotencyKey;
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

    public String getIdempotencyKey() {
        return idempotencyKey;
    }

    public void setIdempotencyKey(String idempotencyKey) {
        this.idempotencyKey = idempotencyKey;
    }
}