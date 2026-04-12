package cakir.saga_orchestrator.service.impl;

import cakir.saga_orchestrator.messaging.SagaOrchestratorMessagePublisher;
import cakir.saga_orchestrator.model.entity.SagaInstanceEntity;
import cakir.saga_orchestrator.repository.SagaInstanceRepository;
import cakir.saga_orchestrator.service.SagaInstanceService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

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
    public boolean saveSagaState(Long orderId, String idempotencyKey, String state, String status) {
        if (!StringUtils.hasText(idempotencyKey)) {
            throw new IllegalArgumentException("idempotencyKey is required for saga state creation");
        }

        if (sagaInstanceRepository.findByIdempotencyKey(idempotencyKey).isPresent()) {
            log.info("Duplicate ORDER_CREATED ignored. Saga already exists for idempotencyKey: {}", idempotencyKey);
            return false;
        }

        SagaInstanceEntity instance = new SagaInstanceEntity(idempotencyKey, orderId, state, status);
        sagaInstanceRepository.save(instance);

        log.info("Saga durumu kaydedildi -> Key: {} | Order: {} | State: {} | Status: {}", idempotencyKey, orderId, state, status);
        return true;
    }

    @Override
    public void updateSagaState(String idempotencyKey, String lastState, String result) {
        if (!StringUtils.hasText(idempotencyKey)) {
            throw new IllegalArgumentException("idempotencyKey is required for saga state update");
        }

        SagaInstanceEntity sagaInstance = sagaInstanceRepository.findByIdempotencyKey(idempotencyKey)
                .orElseThrow(() -> new IllegalStateException("Saga instance not found for key: " + idempotencyKey));

        if ("SUCCESS".equals(sagaInstance.getSagaStatus()) || "FAILED".equals(sagaInstance.getSagaStatus())) {
            log.info("Saga zaten tamamlanmış veya başarısız durumda. Güncelleme atlandı -> Key: {} | Current Status: {}", idempotencyKey, sagaInstance.getSagaStatus());
            return;
        }

        sagaInstance.setLastState(lastState);
        sagaInstance.setSagaStatus(result);

        sagaInstanceRepository.save(sagaInstance);

        log.info("Saga durumu güncellendi -> Key: {} | Last State: {} | Result: {}", idempotencyKey, lastState, result);
    }

    @Override
    public void handleCompensatingActions(Long orderId, String idempotencyKey) {
        updateSagaState(idempotencyKey, "COMPENSATING", "FAILED");

        sagaOrchestratorMessagePublisher.sendStockReleaseCommand(orderId, idempotencyKey);

        log.info("Telafi işlemleri başlatıldı -> Key: {} | Order: {}", idempotencyKey, orderId);

        sagaOrchestratorMessagePublisher.sendOrderCancelCommand(orderId, idempotencyKey);
    }
}
