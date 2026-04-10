package cakir.saga_orchestrator.model.dto;

public class StockCommand {
    private Long orderId;
    private String action; // RESERVE_STOCK, RELEASE_STOCK (Compensating için)

    public StockCommand() {
    }

    public StockCommand(Long orderId, String action) {
        this.orderId = orderId;
        this.action = action;
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
}