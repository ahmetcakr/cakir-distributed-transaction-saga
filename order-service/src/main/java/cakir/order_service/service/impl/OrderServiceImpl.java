package cakir.order_service.service.impl;

import cakir.order_service.model.dto.OrderEvent;
import cakir.order_service.model.dto.OrderRequest;
import cakir.order_service.model.entity.OrderEntity;
import cakir.order_service.model.enums.OrderStatus;
import cakir.order_service.repository.OrderRepository;
import cakir.order_service.service.OrderService;
import org.springframework.cloud.stream.function.StreamBridge;
import org.springframework.stereotype.Service;

@Service
public class OrderServiceImpl implements OrderService {

    private final OrderRepository orderRepository;
    private final StreamBridge streamBridge;

    public OrderServiceImpl(OrderRepository orderRepository, StreamBridge streamBridge) {
        this.orderRepository = orderRepository;
        this.streamBridge = streamBridge;
    }

    @Override
    public OrderEntity createOrder(OrderRequest orderRequest) {
        OrderEntity orderEntity = new OrderEntity();
        orderEntity.setPrice(orderRequest.getPrice());
        orderEntity.setQuantity(orderRequest.getQuantity());
        orderEntity.setProductId(orderRequest.getProductId());
        orderEntity.setStatus(OrderStatus.PENDING);

        OrderEntity savedOrder = orderRepository.save(orderEntity);

        streamBridge.send("orderSource-out-0", new OrderEvent(savedOrder.getId(), "ORDER_CREATED"));

        return savedOrder;
    }
}
