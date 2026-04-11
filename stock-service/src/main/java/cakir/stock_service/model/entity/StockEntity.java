package cakir.stock_service.model.entity;

import jakarta.persistence.*;

import java.math.BigDecimal;

@Entity
@Table(name = "stock")
public class StockEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String productId;
    private Integer remainingStock;
    private BigDecimal singleUnitPrice;

    public BigDecimal getSingleUnitPrice() {
        return singleUnitPrice;
    }

    public void setSingleUnitPrice(BigDecimal singleUnitPrice) {
        this.singleUnitPrice = singleUnitPrice;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getProductId() {
        return productId;
    }

    public void setProductId(String productId) {
        this.productId = productId;
    }

    public Integer getRemainingStock() {
        return remainingStock;
    }

    public void setRemainingStock(Integer remainingStock) {
        this.remainingStock = remainingStock;
    }
}