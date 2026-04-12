package cakir.saga_orchestrator.messaging;

import cakir.saga_orchestrator.model.dto.OrderEvent;
import cakir.saga_orchestrator.model.dto.PaymentCommand;
import cakir.saga_orchestrator.model.dto.StockCommand;
import cakir.saga_orchestrator.model.dto.StockEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.stream.function.StreamBridge;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Slf4j
@Component
public class SagaOrchestratorMessagePublisher {
    private final StreamBridge streamBridge;

    private static final String STOCK_OUT = "stock-out-0";
    private static final String PAYMENT_OUT = "payment-out-0";
    private static final String ORDER_RESPONSE_OUT = "orderResponse-out-0";


    public SagaOrchestratorMessagePublisher(StreamBridge streamBridge) {
        this.streamBridge = streamBridge;
    }

    public void sendStockReserveCommand(OrderEvent event) {
        StockCommand command = new StockCommand(event.getOrderId(), "RESERVE_STOCK",
            event.getProductId(), event.getQuantity(), event.getUserId(), event.getIdempotencyKey());
        streamBridge.send(STOCK_OUT, command);

        log.info(STOCK_OUT + "RESERVE_STOCK command sent for Order ID: {}", event.getOrderId());
    }

    public void sendStockReleaseCommand(Long orderId, String idempotencyKey) {
        StockCommand command = new StockCommand(orderId, "RELEASE_STOCK", idempotencyKey);
        streamBridge.send(STOCK_OUT, command);

        log.info(STOCK_OUT + "RELEASE_STOCK command sent for Order ID: {}", orderId);
    }

    public void sendPaymentCommand(StockEvent event) {
        PaymentCommand command = new PaymentCommand(event.getOrderId(), event.getUserId(),
            event.getPrice(), "PROCESS_PAYMENT", event.getIdempotencyKey());
        streamBridge.send(PAYMENT_OUT, command);

        log.info(PAYMENT_OUT + "PROCESS_PAYMENT command sent for Order ID: {}", event.getOrderId());
    }

    public void sendPaymentFailCommand(Long orderId, BigDecimal price, String idempotencyKey) {
        OrderEvent command = new OrderEvent(orderId, "ORDER_FAILED", price, idempotencyKey);
        streamBridge.send(ORDER_RESPONSE_OUT, command);

        log.info(ORDER_RESPONSE_OUT + "ORDER_FAILED command sent for Order ID: {}", orderId);
    }

    public void sendOrderCompleteCommand(Long orderId, BigDecimal price, String idempotencyKey) {
        OrderEvent command = new OrderEvent(orderId, "ORDER_COMPLETED", price, idempotencyKey);
        streamBridge.send(ORDER_RESPONSE_OUT, command);

        log.info(ORDER_RESPONSE_OUT + "ORDER_COMPLETED command sent for Order ID: {}", orderId);
    }

    public void sendOrderCancelCommand(Long orderId, String idempotencyKey) {
        OrderEvent command = new OrderEvent(orderId, "ORDER_CANCELLED", idempotencyKey);
        streamBridge.send(ORDER_RESPONSE_OUT, command);

        log.info(ORDER_RESPONSE_OUT + "ORDER_CANCELLED command sent for Order ID: {}", orderId);
    }

}
