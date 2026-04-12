package cakir.saga_orchestrator.service;

public interface SagaInstanceService {
    boolean saveSagaState(Long orderId, String idempotencyKey, String state, String status);

    void updateSagaState(String idempotencyKey, String lastState, String result);

    void handleCompensatingActions(Long orderId, String idempotencyKey);
}
