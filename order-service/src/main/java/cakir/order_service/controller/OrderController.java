package cakir.order_service.controller;

import cakir.order_service.model.dto.OrderRequest;
import cakir.order_service.model.entity.OrderEntity;
import cakir.order_service.service.OrderService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/orders")
public class OrderController {

    private final OrderService orderService;

    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    @PostMapping
    public ResponseEntity<OrderEntity> createOrder(@RequestBody OrderRequest request,
                                                   @RequestHeader("X-Idempotency-Key") String idempotencyKey) {
        OrderEntity order = orderService.createOrder(request, idempotencyKey);

        return ResponseEntity.ok(order);
    }
}