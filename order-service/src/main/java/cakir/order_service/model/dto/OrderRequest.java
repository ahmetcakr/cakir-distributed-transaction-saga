package cakir.order_service.model.dto;

public class OrderRequest {
    private String productId;
    private Integer quantity;
    private Double price;
    private Long userId;

    public OrderRequest() {
    }

    public OrderRequest(String productId, Integer quantity, Double price, Long userId) {
        this.productId = productId;
        this.quantity = quantity;
        this.price = price;
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

    public Double getPrice() {
        return price;
    }

    public void setPrice(Double price) {
        this.price = price;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }
}