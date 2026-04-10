package cakir.saga_orchestrator.model.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "saga_instances")
public class SagaInstanceEntity {
    @Id
    private Long orderId;

    private String lastState;

    private String sagaStatus;

    public Long getOrderId() {
        return orderId;
    }

    public void setOrderId(Long orderId) {
        this.orderId = orderId;
    }

    public String getLastState() {
        return lastState;
    }

    public void setLastState(String lastState) {
        this.lastState = lastState;
    }

    public String getSagaStatus() {
        return sagaStatus;
    }

    public void setSagaStatus(String sagaStatus) {
        this.sagaStatus = sagaStatus;
    }
}