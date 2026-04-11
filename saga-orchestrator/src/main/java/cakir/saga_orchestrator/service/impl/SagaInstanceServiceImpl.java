package cakir.saga_orchestrator.service.impl;

import cakir.saga_orchestrator.messaging.SagaOrchestratorMessagePublisher;
import cakir.saga_orchestrator.model.entity.SagaInstanceEntity;
import cakir.saga_orchestrator.repository.SagaInstanceRepository;
import cakir.saga_orchestrator.service.SagaInstanceService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
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

        log.info("Saga durumu kaydedildi -> Order: {} | State: {} | Status: {}", orderId, state, status);
    }

    @Override
    public void updateSagaState(Long orderId, String lastState, String result) {
        SagaInstanceEntity sagaInstance = sagaInstanceRepository.findById(orderId)
                .orElse(new SagaInstanceEntity(orderId));

        if ("SUCCESS".equals(sagaInstance.getSagaStatus()) || "FAILED".equals(sagaInstance.getSagaStatus())) {
            log.info("Saga zaten tamamlanmış veya başarısız durumda. Güncelleme atlandı -> Order: {} | Current Status: {}", orderId, sagaInstance.getSagaStatus());
            return;
        }

        sagaInstance.setLastState(lastState);
        sagaInstance.setSagaStatus(result);

        sagaInstanceRepository.save(sagaInstance);

        log.info("Saga durumu güncellendi -> Order: {} | Last State: {} | Result: {}", orderId, lastState, result);
    }

    @Override
    public void handleCompensatingActions(Long orderId) {
        updateSagaState(orderId, "COMPENSATING", "FAILED");

        sagaOrchestratorMessagePublisher.sendStockReleaseCommand(orderId);

        log.info("Telafi işlemleri başlatıldı -> Order: {}", orderId);

        sagaOrchestratorMessagePublisher.sendOrderCancelCommand(orderId);
    }
}
