package cakir.order_service.service;

import cakir.order_service.model.dto.OrderRequest;
import cakir.order_service.model.entity.OrderEntity;

public interface OrderService {
    OrderEntity createOrder(OrderRequest orderRequest, String idempotencyKey);
}
