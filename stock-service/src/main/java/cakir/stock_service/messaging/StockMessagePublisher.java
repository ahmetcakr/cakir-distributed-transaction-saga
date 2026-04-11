package cakir.stock_service.messaging;

import cakir.stock_service.model.dto.StockEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.stream.function.StreamBridge;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Slf4j
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

        log.info("Published STOCK_RESERVED event for orderId: {}, totalAmount: {}, userId: {}", orderId, totalAmount, userId);
    }

    public void publishStockReleased(Long orderId, BigDecimal totalAmount, Long userId) {
        StockEvent event = new StockEvent(orderId, "STOCK_RELEASED", totalAmount, userId);
        streamBridge.send(BINDING_NAME, event);

        log.info("Published STOCK_RELEASED event for orderId: {}, totalAmount: {}, userId: {}", orderId, totalAmount, userId);
    }

    public void publishStockError(Long orderId, String status) {
        StockEvent event = new StockEvent(orderId, status);
        streamBridge.send(BINDING_NAME, event);

        log.info("Published {} event for orderId: {}", status, orderId);
    }
}