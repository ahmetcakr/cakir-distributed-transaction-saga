package cakir.saga_orchestrator.model.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Column;

@Entity
@Table(name = "saga_instances")
public class SagaInstanceEntity {
    @Id
    @Column(name = "idempotency_key", length = 100)
    private String idempotencyKey;

    @Column(name = "order_id", nullable = false, unique = true)
    private Long orderId;

    private String lastState;

    private String sagaStatus;

    public SagaInstanceEntity() {
    }

    public SagaInstanceEntity(String idempotencyKey, Long orderId) {
        this.idempotencyKey = idempotencyKey;
        this.orderId = orderId;
    }

    public SagaInstanceEntity(String idempotencyKey, Long orderId, String lastState, String sagaStatus) {
        this.idempotencyKey = idempotencyKey;
        this.orderId = orderId;
        this.lastState = lastState;
        this.sagaStatus = sagaStatus;
    }

    public String getIdempotencyKey() {
        return idempotencyKey;
    }

    public void setIdempotencyKey(String idempotencyKey) {
        this.idempotencyKey = idempotencyKey;
    }

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