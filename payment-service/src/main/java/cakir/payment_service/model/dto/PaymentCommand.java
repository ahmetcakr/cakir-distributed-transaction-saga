package cakir.payment_service.model.dto;

public class PaymentCommand {
    private Long orderId;
    private Long userId;
    private Double amount;
    private String action;

    public PaymentCommand() {
    }

    public PaymentCommand(Long orderId, Long userId, Double amount, String action) {
        this.orderId = orderId;
        this.userId = userId;
        this.amount = amount;
        this.action = action;
    }

    public Long getOrderId() {
        return orderId;
    }

    public void setOrderId(Long orderId) {
        this.orderId = orderId;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public Double getAmount() {
        return amount;
    }

    public void setAmount(Double amount) {
        this.amount = amount;
    }

    public String getAction() {
        return action;
    }

    public void setAction(String action) {
        this.action = action;
    }
}