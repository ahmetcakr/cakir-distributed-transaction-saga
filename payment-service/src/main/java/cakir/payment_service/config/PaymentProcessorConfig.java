package cakir.payment_service.config;

import cakir.payment_service.model.dto.PaymentCommand;
import cakir.payment_service.model.entity.PaymentEntity;
import cakir.payment_service.repository.PaymentRepository;
import cakir.payment_service.service.PaymentService;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.math.BigDecimal;
import java.util.function.Consumer;

@Configuration
public class PaymentProcessorConfig {

    private final PaymentService paymentService;

    public PaymentProcessorConfig(PaymentService paymentService) {
        this.paymentService = paymentService;
    }

    @Bean
    public Consumer<PaymentCommand> paymentProcessor() {
        return command -> {
            System.out.println("Şeften ödeme emri geldi: Order ID: " + command.getOrderId());
            
            if ("PROCESS_PAYMENT".equals(command.getAction())) {
                paymentService.processPayment(command);
            } else if ("REFUND_PAYMENT".equals(command.getAction())) {
                paymentService.refundPayment(command);
            }
        };
    }

    @Bean
    public CommandLineRunner initData(PaymentRepository repository) {
        return args -> {
            if (repository.count() == 0) {
                PaymentEntity richUser = new PaymentEntity();
                richUser.setUserId(101L);
                richUser.setBalance(BigDecimal.valueOf(100000)); // bol keseden
                repository.save(richUser);
            }
        };
    }
}