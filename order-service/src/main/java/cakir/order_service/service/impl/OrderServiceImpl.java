package cakir.order_service.service.impl;

import cakir.order_service.messaging.OrderMessagePublisher;
import cakir.order_service.model.dto.OrderRequest;
import cakir.order_service.model.entity.OrderEntity;
import cakir.order_service.model.enums.OrderStatus;
import cakir.order_service.repository.OrderRepository;
import cakir.order_service.service.OrderService;
import org.springframework.stereotype.Service;

@Service
public class OrderServiceImpl implements OrderService {

    private final OrderRepository orderRepository;
    private final OrderMessagePublisher orderMessagePublisher;

    public OrderServiceImpl(OrderRepository orderRepository, OrderMessagePublisher orderMessagePublisher) {
        this.orderRepository = orderRepository;
        this.orderMessagePublisher = orderMessagePublisher;
    }

    @Override
    public OrderEntity createOrder(OrderRequest orderRequest) {
        OrderEntity orderEntity = new OrderEntity();
        orderEntity.setPrice(null);
        orderEntity.setQuantity(orderRequest.getQuantity());
        orderEntity.setProductId(orderRequest.getProductId());
        orderEntity.setUserId(orderRequest.getUserId());
        orderEntity.setStatus(OrderStatus.PENDING);

        OrderEntity savedOrder = orderRepository.save(orderEntity);

        orderMessagePublisher.publishOrderCreatedMessage(savedOrder);

        return savedOrder;
    }
}
