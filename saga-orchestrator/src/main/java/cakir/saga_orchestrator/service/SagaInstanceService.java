package cakir.saga_orchestrator.service;

public interface SagaInstanceService {
    void saveSagaState(Long orderId, String state, String status);

    void updateSagaState(Long orderId, String lastState, String result);

    void handleCompensatingActions(Long orderId);
}
