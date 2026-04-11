package cakir.order_service.config;

import cakir.order_service.model.dto.OrderEvent;
import cakir.order_service.model.enums.OrderStatus;
import cakir.order_service.repository.OrderRepository;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.function.Consumer;

@Configuration
public class OrderProcessorConfig {

    @Bean
    public Consumer<OrderEvent> orderFinalizer(OrderRepository repository) {
        return event -> {
            System.out.println("Received last event for orderId: " + event.getOrderId() + ", status: " + event.getOrderStatus());

            repository.findById(event.getOrderId()).ifPresent(order -> {
                if ("ORDER_COMPLETED".equals(event.getOrderStatus())) {
                    order.setStatus(OrderStatus.COMPLETED);
                    order.setPrice(event.getPrice());
                    System.out.println("Sipariş başarıyla COMPLETED yapıldı.");
                } else if ("ORDER_CANCELLED".equals(event.getOrderStatus())) {
                    order.setStatus(OrderStatus.CANCELLED);
                    System.out.println("Sipariş iptal durumuna (CANCELLED) çekildi.");
                } else if ("ORDER_FAILED".equals(event.getOrderStatus())) {
                    order.setStatus(OrderStatus.FAILED);
                    System.out.println("Sipariş başarısız durumuna (FAILED) çekildi.");
                } else {
                    System.out.println("Bilinmeyen sipariş durumu: " + event.getOrderStatus());
                }

                repository.save(order);
            });
        };
    }
}
