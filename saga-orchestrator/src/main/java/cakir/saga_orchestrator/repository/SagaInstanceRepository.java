package cakir.saga_orchestrator.repository;

import cakir.saga_orchestrator.model.entity.SagaInstanceEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface SagaInstanceRepository extends JpaRepository<SagaInstanceEntity, String> {
    Optional<SagaInstanceEntity> findByOrderId(Long orderId);

    Optional<SagaInstanceEntity> findByIdempotencyKey(String idempotencyKey);
}