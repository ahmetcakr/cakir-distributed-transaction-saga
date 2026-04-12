package cakir.saga_orchestrator.model.dto;

public class StockCommand {
    private Long orderId;
    private String action;
    private String productId;
    private Integer quantity;
    private Long userId;
    private String idempotencyKey;

    public StockCommand() {
    }

    public StockCommand(Long orderId, String action, String idempotencyKey) {
        this.orderId = orderId;
        this.action = action;
        this.idempotencyKey = idempotencyKey;
    }

    public StockCommand(Long orderId, String action, String productId, Integer quantity, Long userId, String idempotencyKey) {
        this.orderId = orderId;
        this.action = action;
        this.productId = productId;
        this.quantity = quantity;
        this.userId = userId;
        this.idempotencyKey = idempotencyKey;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getProductId() {
        return productId;
    }

    public void setProductId(String productId) {
        this.productId = productId;
    }

    public Integer getQuantity() {
        return quantity;
    }

    public void setQuantity(Integer quantity) {
        this.quantity = quantity;
    }

    public Long getOrderId() {
        return orderId;
    }

    public void setOrderId(Long orderId) {
        this.orderId = orderId;
    }

    public String getAction() {
        return action;
    }

    public void setAction(String action) {
        this.action = action;
    }

    public String getIdempotencyKey() {
        return idempotencyKey;
    }

    public void setIdempotencyKey(String idempotencyKey) {
        this.idempotencyKey = idempotencyKey;
    }
}