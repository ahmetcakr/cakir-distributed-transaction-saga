package cakir.order_service.config;

import cakir.order_service.model.dto.OrderEvent;
import cakir.order_service.model.enums.OrderStatus;
import cakir.order_service.repository.OrderRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.function.Consumer;

@Slf4j
@Configuration
public class OrderProcessorConfig {

    @Bean
    public Consumer<OrderEvent> orderFinalizer(OrderRepository repository) {
        return event -> {
            repository.findById(event.getOrderId()).ifPresent(order -> {
                if ("ORDER_COMPLETED".equals(event.getOrderStatus())) {
                    order.setStatus(OrderStatus.COMPLETED);
                    order.setPrice(event.getPrice());

                    log.info("ORDER_COMPLETED event received for orderId: {}, price: {}", event.getOrderId(), event.getPrice());
                } else if ("ORDER_CANCELLED".equals(event.getOrderStatus())) {
                    order.setStatus(OrderStatus.CANCELLED);
                    System.out.println("ORDER_CANCELLED event received, order status set to CANCELLED.");
                } else if ("ORDER_FAILED".equals(event.getOrderStatus())) {
                    order.setStatus(OrderStatus.FAILED);
                    System.out.println("ORDER_FAILED event received, order status set to FAILED.");
                } else {
                    order.setStatus(OrderStatus.FAILED);
                    System.out.println("Unknown order status received: " + event.getOrderStatus());
                }

                repository.save(order);
            });
        };
    }
}
