package cakir.saga_orchestrator.service;

import cakir.saga_orchestrator.model.dto.OrderEvent;
import cakir.saga_orchestrator.model.dto.PaymentCommand;
import cakir.saga_orchestrator.model.dto.StockCommand;
import cakir.saga_orchestrator.model.dto.StockEvent;
import cakir.saga_orchestrator.model.entity.SagaInstanceEntity;
import cakir.saga_orchestrator.repository.SagaInstanceRepository;
import org.springframework.cloud.stream.function.StreamBridge;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Service;

import java.util.function.Consumer;

@Service
public class SagaOrchestratorAdapter {
    private final StreamBridge streamBridge;
    private final SagaInstanceRepository sagaInstanceRepository;

    public SagaOrchestratorAdapter(StreamBridge streamBridge, SagaInstanceRepository sagaInstanceRepository) {
        this.streamBridge = streamBridge;
        this.sagaInstanceRepository = sagaInstanceRepository;
    }

    @Bean
    public Consumer<OrderEvent> orderProcessor() {
        return event -> {
            if ("ORDER_CREATED".equals(event.getOrderStatus())) {
                System.out.println("Yeni sipariş geldi, Stok servisine emir veriliyor... Order ID: " + event.getOrderId());

                saveSagaState(event.getOrderId(), "ORDER_CREATED", "PROCESSING");

                streamBridge.send("stock-out-0", new StockCommand(event.getOrderId(), "RESERVE_STOCK"));
            }
        };
    }

    @Bean
    public Consumer<StockEvent> stockResponseProcessor() {
        return event -> {
            if ("STOCK_RESERVED".equals(event.getOrderStatus())) {
                System.out.println("Stok tamam, şimdi ödemeye geçiyoruz. Order ID: " + event.getOrderId());

                // Payment Service'e emir fırlat
                streamBridge.send("payment-out-0", new PaymentCommand(event.getOrderId(), 101L, 85000.0, "PROCESS_PAYMENT"));
            }
        };
    }

    private void saveSagaState(Long orderId, String state, String status) {
        SagaInstanceEntity instance = new SagaInstanceEntity();
        instance.setOrderId(orderId);
        instance.setLastState(state);
        instance.setSagaStatus(status);
        sagaInstanceRepository.save(instance);
    }
}
