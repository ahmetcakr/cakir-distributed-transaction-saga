package cakir.payment_service.messaging;

import cakir.payment_service.model.dto.PaymentEvent;
import org.springframework.cloud.stream.function.StreamBridge;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component
public class PaymentMessagePublisher {
    private final StreamBridge streamBridge;
    private static final String BINDING_NAME = "paymentResponse-out-0";

    public PaymentMessagePublisher(StreamBridge streamBridge) {
        this.streamBridge = streamBridge;
    }

    public void publishPaymentSuccess(Long orderId, BigDecimal amount) {
        PaymentEvent event = new PaymentEvent(orderId, "PAYMENT_SUCCESS", amount);
        streamBridge.send(BINDING_NAME, event);
    }

    public void publishPaymentFailed(Long orderId) {
        PaymentEvent event = new PaymentEvent(orderId, "PAYMENT_FAILED");
        streamBridge.send(BINDING_NAME, event);
    }

    public void publishRefundSuccess(Long orderId) {
        PaymentEvent event = new PaymentEvent(orderId, "REFUND_SUCCESS");
        streamBridge.send(BINDING_NAME, event);
    }

    public void publishRefundFailed(Long orderId) {
        PaymentEvent event = new PaymentEvent(orderId, "REFUND_FAILED");
        streamBridge.send(BINDING_NAME, event);
    }
}
