package cakir.saga_orchestrator.config;

import cakir.saga_orchestrator.messaging.SagaOrchestratorMessagePublisher;
import cakir.saga_orchestrator.model.dto.*;
import cakir.saga_orchestrator.service.SagaInstanceService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.stream.function.StreamBridge;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.function.Consumer;

@Slf4j
@Configuration
public class SagaOrchestratorProcessorConfig {
    private final SagaInstanceService sagaInstanceService;
    private final SagaOrchestratorMessagePublisher publisher;

    public SagaOrchestratorProcessorConfig(SagaInstanceService sagaInstanceService, StreamBridge streamBridge, SagaOrchestratorMessagePublisher publisher) {
        this.sagaInstanceService = sagaInstanceService;
        this.publisher = publisher;
    }


    @Bean
    public Consumer<OrderEvent> orderProcessor() {
        return event -> {
            log.info(event.getOrderStatus() + " orderEvent received for orderId: {}", event.getOrderId());

            if ("ORDER_CREATED".equals(event.getOrderStatus())) {
                sagaInstanceService.saveSagaState(event.getOrderId(), "ORDER_CREATED", "PROCESSING");

                publisher.sendStockReserveCommand(event);
            }
        };
    }

    @Bean
    public Consumer<StockEvent> stockResponseProcessor() {
        return event -> {
            log.info(event.getOrderStatus() + " stockEvent received for orderId: {}", event.getOrderId());

            if ("STOCK_RESERVED".equals(event.getOrderStatus())) {
                sagaInstanceService.updateSagaState(event.getOrderId(), "STOCK_RESERVED", "PROCESSING");

                publisher.sendPaymentCommand(event);
            } else if ("STOCK_RELEASED".equals(event.getOrderStatus())) {
                sagaInstanceService.updateSagaState(event.getOrderId(), "STOCK_RELEASED", "FAILED");

                publisher.sendPaymentFailCommand(event.getOrderId(),  event.getPrice());
            } else if ("STOCK_NOT_FOUND".equals(event.getOrderStatus()) || "INSUFFICIENT_STOCK".equals(event.getOrderStatus())) {
                sagaInstanceService.updateSagaState(event.getOrderId(), "STOCK_FAILED", "FAILED");

                publisher.sendPaymentFailCommand(event.getOrderId(),  event.getPrice());
            }
        };
    }
    @Bean
    public Consumer<PaymentEvent> paymentResponseProcessor() {
        return event -> {
            log.info(event.getStatus() + " paymentEvent received for orderId: {}", event.getOrderId());

            if ("PAYMENT_SUCCESS".equals(event.getStatus())) {
                sagaInstanceService.updateSagaState(event.getOrderId(), "PAYMENT_COMPLETED", "SUCCESS");

                publisher.sendOrderCompleteCommand(event.getOrderId(), event.getPrice());
            } else if ("PAYMENT_FAILED".equals(event.getStatus())) {
                sagaInstanceService.handleCompensatingActions(event.getOrderId());
            }
        };
    }

}
