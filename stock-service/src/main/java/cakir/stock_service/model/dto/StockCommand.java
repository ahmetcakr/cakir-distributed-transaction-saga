package cakir.stock_service.model.dto;


public class StockCommand {
    private Long orderId;
    private String productId;
    private Integer quantity;
    private String action;

    public StockCommand() {
    }

    public StockCommand(Long orderId, String productId, Integer quantity, String action) {
        this.orderId = orderId;
        this.productId = productId;
        this.quantity = quantity;
        this.action = action;
    }

    public Long getOrderId() {
        return orderId;
    }

    public void setOrderId(Long orderId) {
        this.orderId = orderId;
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

    public String getAction() {
        return action;
    }

    public void setAction(String action) {
        this.action = action;
    }
}