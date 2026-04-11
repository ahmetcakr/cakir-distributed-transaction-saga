package cakir.order_service.model.dto;

public class OrderRequest {
    private String productId;
    private Integer quantity;
    private Long userId;

    public OrderRequest() {
    }

    public OrderRequest(String productId, Integer quantity, Long userId) {
        this.productId = productId;
        this.quantity = quantity;
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

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }
}