package cakir.saga_orchestrator.service.impl;

import cakir.saga_orchestrator.lock.RedisDistributedLockService;
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
    private final RedisDistributedLockService lockService;

    public SagaInstanceServiceImpl(SagaInstanceRepository sagaInstanceRepository,
                                   SagaOrchestratorMessagePublisher sagaOrchestratorMessagePublisher,
                                   RedisDistributedLockService lockService) {
        this.sagaInstanceRepository = sagaInstanceRepository;
        this.sagaOrchestratorMessagePublisher = sagaOrchestratorMessagePublisher;
        this.lockService = lockService;
    }

    @Override
    public boolean saveSagaState(Long orderId, String idempotencyKey, String state, String status) {
        if (!StringUtils.hasText(idempotencyKey)) {
            throw new IllegalArgumentException("idempotencyKey is required for saga state creation");
        }

        return lockService.executeWithLock("saga:create:" + idempotencyKey, () -> {
            if (sagaInstanceRepository.findByIdempotencyKey(idempotencyKey).isPresent()) {
                log.info("Duplicate ORDER_CREATED ignored. Saga already exists for idempotencyKey: {}", idempotencyKey);
                return false;
            }

            SagaInstanceEntity instance = new SagaInstanceEntity(idempotencyKey, orderId, state, status);
            sagaInstanceRepository.save(instance);

            log.info("Saga durumu kaydedildi -> Key: {} | Order: {} | State: {} | Status: {}", idempotencyKey, orderId, state, status);
            return true;
        });
    }

    @Override
    public void updateSagaState(String idempotencyKey, String lastState, String result) {
        if (!StringUtils.hasText(idempotencyKey)) {
            throw new IllegalArgumentException("idempotencyKey is required for saga state update");
        }

        lockService.executeWithLock("saga:update:" + idempotencyKey, () -> {
            SagaInstanceEntity sagaInstance = sagaInstanceRepository.findByIdempotencyKey(idempotencyKey)
                    .orElseThrow(() -> new IllegalStateException("Saga instance not found for key: " + idempotencyKey));

            if ("SUCCESS".equals(sagaInstance.getSagaStatus()) || "FAILED".equals(sagaInstance.getSagaStatus())) {
                log.info("Saga zaten tamamlanmış veya başarısız durumda. Güncelleme atlandı -> Key: {} | Current Status: {}", idempotencyKey, sagaInstance.getSagaStatus());
                return null;
            }

            sagaInstance.setLastState(lastState);
            sagaInstance.setSagaStatus(result);

            sagaInstanceRepository.save(sagaInstance);

            log.info("Saga durumu güncellendi -> Key: {} | Last State: {} | Result: {}", idempotencyKey, lastState, result);
            return null;
        });
    }

    @Override
    public void handleCompensatingActions(Long orderId, String idempotencyKey) {
        if (!StringUtils.hasText(idempotencyKey)) {
            throw new IllegalArgumentException("idempotencyKey is required for compensating actions");
        }

        lockService.executeWithLock("saga:compensate:" + idempotencyKey, () -> {
            SagaInstanceEntity sagaInstance = sagaInstanceRepository.findByIdempotencyKey(idempotencyKey)
                    .orElseThrow(() -> new IllegalStateException("Saga instance not found for key: " + idempotencyKey));

            if ("SUCCESS".equals(sagaInstance.getSagaStatus()) || "FAILED".equals(sagaInstance.getSagaStatus())) {
                log.info("Compensation skipped. Saga already finalized -> Key: {} | Current Status: {}",
                        idempotencyKey, sagaInstance.getSagaStatus());
                return null;
            }

            sagaInstance.setLastState("COMPENSATING");
            sagaInstance.setSagaStatus("FAILED");
            sagaInstanceRepository.save(sagaInstance);

            sagaOrchestratorMessagePublisher.sendStockReleaseCommand(orderId, idempotencyKey);
            sagaOrchestratorMessagePublisher.sendOrderCancelCommand(orderId, idempotencyKey);

            log.info("Telafi işlemleri başlatıldı -> Key: {} | Order: {}", idempotencyKey, orderId);
            return null;
        });
    }
}
