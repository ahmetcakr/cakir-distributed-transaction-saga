package cakir.saga_orchestrator.messaging;

import cakir.saga_orchestrator.model.dto.OrderEvent;
import cakir.saga_orchestrator.model.dto.PaymentCommand;
import cakir.saga_orchestrator.model.dto.StockCommand;
import cakir.saga_orchestrator.model.dto.StockEvent;
import org.springframework.cloud.stream.function.StreamBridge;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

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
                event.getProductId(), event.getQuantity(), event.getUserId());
        streamBridge.send(STOCK_OUT, command);
    }

    public void sendStockReleaseCommand(Long orderId) {
        StockCommand command = new StockCommand(orderId, "RELEASE_STOCK");
        streamBridge.send(STOCK_OUT, command);
    }

    public void sendPaymentCommand(StockEvent event) {
        PaymentCommand command = new PaymentCommand(event.getOrderId(), event.getUserId(),
                event.getPrice(), "PROCESS_PAYMENT");
        streamBridge.send(PAYMENT_OUT, command);
    }

    public void sendPaymentFailCommand(Long orderId, BigDecimal price) {
        OrderEvent command = new OrderEvent(orderId, "ORDER_FAILED", price);
        streamBridge.send(ORDER_RESPONSE_OUT, command);
    }

    public void sendOrderCompleteCommand(Long orderId, BigDecimal price) {
        OrderEvent command = new OrderEvent(orderId, "ORDER_COMPLETED", price);
        streamBridge.send(ORDER_RESPONSE_OUT, command);
    }

    public void sendOrderCancelCommand(Long orderId) {
        OrderEvent command = new OrderEvent(orderId, "ORDER_CANCELLED");
        streamBridge.send(ORDER_RESPONSE_OUT, command);
    }

}
