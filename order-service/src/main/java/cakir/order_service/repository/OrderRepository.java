package cakir.order_service.repository;

import cakir.order_service.model.entity.OrderEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface OrderRepository extends JpaRepository<OrderEntity, Long> {
	Optional<OrderEntity> findByIdempotencyKey(String idempotencyKey);
}