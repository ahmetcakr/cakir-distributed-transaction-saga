package cakir.payment_service.messaging;

import cakir.payment_service.model.dto.PaymentEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.stream.function.StreamBridge;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Slf4j
@Component
public class PaymentMessagePublisher {
    private final StreamBridge streamBridge;
    private static final String BINDING_NAME = "paymentResponse-out-0";

    public PaymentMessagePublisher(StreamBridge streamBridge) {
        this.streamBridge = streamBridge;
    }

    public void publishPaymentSuccess(Long orderId, BigDecimal amount, String idempotencyKey) {
        PaymentEvent event = new PaymentEvent(orderId, "PAYMENT_SUCCESS", amount, idempotencyKey);
        streamBridge.send(BINDING_NAME, event);

        log.info(BINDING_NAME + "PAYMENT_SUCCESS event published for orderId: " + orderId + " with amount: " + amount);
    }

    public void publishPaymentFailed(Long orderId, String idempotencyKey) {
        PaymentEvent event = new PaymentEvent(orderId, "PAYMENT_FAILED", idempotencyKey);

        streamBridge.send(BINDING_NAME, event);

        log.info(BINDING_NAME + "PAYMENT_FAILED event published for orderId: " + orderId);
    }

    public void publishRefundSuccess(Long orderId, String idempotencyKey) {
        PaymentEvent event = new PaymentEvent(orderId, "REFUND_SUCCESS", idempotencyKey);
        streamBridge.send(BINDING_NAME, event);

        log.info(BINDING_NAME + "REFUND_SUCCESS event published for orderId: " + orderId);
    }

    public void publishRefundFailed(Long orderId, String idempotencyKey) {
        PaymentEvent event = new PaymentEvent(orderId, "REFUND_FAILED", idempotencyKey);
        streamBridge.send(BINDING_NAME, event);

        log.info(BINDING_NAME + "REFUND_FAILED event published for orderId: " + orderId);
    }
}
