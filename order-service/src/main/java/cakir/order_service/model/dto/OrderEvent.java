package cakir.order_service.model.dto;

import java.math.BigDecimal;

public class OrderEvent {
    private Long orderId;
    private String orderStatus;
    private String productId;
    private Integer quantity;
    private Long userId;
    private BigDecimal price;

    public OrderEvent() {
    }

    public OrderEvent(Long orderId, String orderStatus, String productId, Integer quantity,  Long userId) {
        this.orderId = orderId;
        this.orderStatus = orderStatus;
        this.productId = productId;
        this.quantity = quantity;
        this.userId = userId;
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

    public String getOrderStatus() {
        return orderStatus;
    }

    public void setOrderStatus(String orderStatus) {
        this.orderStatus = orderStatus;
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