package cakir.stock_service.messaging;

import cakir.stock_service.model.dto.StockEvent;
import org.springframework.cloud.stream.function.StreamBridge;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component
public class StockMessagePublisher {
    private final StreamBridge streamBridge;
    private static final String BINDING_NAME = "stockResponse-out-0";

    public StockMessagePublisher(StreamBridge streamBridge) {
        this.streamBridge = streamBridge;
    }

    public void publishStockReserved(Long orderId, BigDecimal totalAmount, Long userId) {
        StockEvent event = new StockEvent(orderId, "STOCK_RESERVED", totalAmount, userId);
        streamBridge.send(BINDING_NAME, event);
    }

    public void publishStockReleased(Long orderId, BigDecimal totalAmount, Long userId) {
        StockEvent event = new StockEvent(orderId, "STOCK_RELEASED", totalAmount, userId);
        streamBridge.send(BINDING_NAME, event);
    }

    public void publishStockError(Long orderId, String status) {
        StockEvent event = new StockEvent(orderId, status);
        streamBridge.send(BINDING_NAME, event);
    }
}