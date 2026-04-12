package cakir.order_service.service.impl;

import cakir.order_service.messaging.OrderMessagePublisher;
import cakir.order_service.model.dto.OrderRequest;
import cakir.order_service.model.entity.OrderEntity;
import cakir.order_service.model.enums.OrderStatus;
import cakir.order_service.repository.OrderRepository;
import cakir.order_service.service.OrderService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class OrderServiceImpl implements OrderService {

    private final OrderRepository orderRepository;
    private final OrderMessagePublisher orderMessagePublisher;

    public OrderServiceImpl(OrderRepository orderRepository, OrderMessagePublisher orderMessagePublisher) {
        this.orderRepository = orderRepository;
        this.orderMessagePublisher = orderMessagePublisher;
    }

    @Override
    public OrderEntity createOrder(OrderRequest orderRequest, String idempotencyKey) {
        OrderEntity existingOrder = orderRepository.findByIdempotencyKey(idempotencyKey).orElse(null);
        if (existingOrder != null) {
            log.info("Duplicate order request skipped for idempotencyKey: {}. Existing orderId: {}", idempotencyKey, existingOrder.getId());
            return existingOrder;
        }

        OrderEntity orderEntity = new OrderEntity();
        orderEntity.setPrice(null);
        orderEntity.setQuantity(orderRequest.getQuantity());
        orderEntity.setProductId(orderRequest.getProductId());
        orderEntity.setUserId(orderRequest.getUserId());
        orderEntity.setStatus(OrderStatus.PENDING);
        orderEntity.setIdempotencyKey(idempotencyKey);

        OrderEntity savedOrder;
        try {
            savedOrder = orderRepository.save(orderEntity);
        } catch (DataIntegrityViolationException ex) {
            OrderEntity duplicateOrder = orderRepository.findByIdempotencyKey(idempotencyKey).orElseThrow(() -> ex);
            log.info("Concurrent duplicate order request skipped for idempotencyKey: {}. Existing orderId: {}", idempotencyKey, duplicateOrder.getId());
            return duplicateOrder;
        }

        log.info("Order created with ID: {}", savedOrder.getId());

        orderMessagePublisher.publishOrderCreatedMessage(savedOrder);

        return savedOrder;
    }
}
