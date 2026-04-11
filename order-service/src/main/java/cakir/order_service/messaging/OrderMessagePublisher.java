package cakir.order_service.messaging;

import cakir.order_service.model.dto.OrderEvent;
import cakir.order_service.model.entity.OrderEntity;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.stream.function.StreamBridge;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class OrderMessagePublisher {

    private final StreamBridge streamBridge;
    private static final String BINDING_NAME = "orderSource-out-0";

    public OrderMessagePublisher(StreamBridge streamBridge) {
        this.streamBridge = streamBridge;
    }

    public void publishOrderCreatedMessage(OrderEntity orderEntity) {
        streamBridge.send(BINDING_NAME, new OrderEvent(orderEntity.getId(),
                "ORDER_CREATED",
                orderEntity.getProductId(),
                orderEntity.getQuantity(),
                orderEntity.getUserId()));

        log.info(BINDING_NAME + " - Published ORDER_CREATED event for Order ID: " + orderEntity.getId());
    }

}
