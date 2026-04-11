package cakir.saga_orchestrator.service.impl;

import cakir.saga_orchestrator.messaging.SagaOrchestratorMessagePublisher;
import cakir.saga_orchestrator.model.dto.OrderEvent;
import cakir.saga_orchestrator.model.dto.StockCommand;
import cakir.saga_orchestrator.model.entity.SagaInstanceEntity;
import cakir.saga_orchestrator.repository.SagaInstanceRepository;
import cakir.saga_orchestrator.service.SagaInstanceService;
import org.springframework.cloud.stream.function.StreamBridge;
import org.springframework.stereotype.Service;

@Service
public class SagaInstanceServiceImpl implements SagaInstanceService {
    private final SagaInstanceRepository sagaInstanceRepository;
    private final SagaOrchestratorMessagePublisher sagaOrchestratorMessagePublisher;

    public SagaInstanceServiceImpl(SagaInstanceRepository sagaInstanceRepository,
                                   SagaOrchestratorMessagePublisher sagaOrchestratorMessagePublisher) {
        this.sagaInstanceRepository = sagaInstanceRepository;
        this.sagaOrchestratorMessagePublisher = sagaOrchestratorMessagePublisher;
    }

    @Override
    public void saveSagaState(Long orderId, String state, String status) {
        SagaInstanceEntity instance = new SagaInstanceEntity(orderId, state, status);
        sagaInstanceRepository.save(instance);
    }

    @Override
    public void updateSagaState(Long orderId, String lastState, String result) {
        SagaInstanceEntity sagaInstance = sagaInstanceRepository.findById(orderId)
                .orElse(new SagaInstanceEntity(orderId));

        if ("SUCCESS".equals(sagaInstance.getSagaStatus()) || "FAILED".equals(sagaInstance.getSagaStatus())) {
            System.out.println("LOG: " + orderId + " zaten mühürlenmiş, eski mesaj pas geçiliyor.");
            return;
        }

        sagaInstance.setLastState(lastState);
        sagaInstance.setSagaStatus(result);

        sagaInstanceRepository.save(sagaInstance);
        System.out.println("Saga durumu güncellendi -> Order: " + orderId + " | State: " + lastState + " | Status: " + result);
    }

    @Override
    public void handleCompensatingActions(Long orderId) {
        updateSagaState(orderId, "COMPENSATING", "FAILED");

        sagaOrchestratorMessagePublisher.sendStockReleaseCommand(orderId);

        System.out.println("Telafi işlemi başlatıldı: Stok iadesi emri gönderildi. Order: " + orderId);

        sagaOrchestratorMessagePublisher.sendOrderCancelCommand(orderId);
    }
}
