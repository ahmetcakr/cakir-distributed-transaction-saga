package cakir.payment_service.service.impl;

import cakir.payment_service.messaging.PaymentMessagePublisher;
import cakir.payment_service.model.dto.PaymentCommand;
import cakir.payment_service.model.entity.PaymentEntity;
import cakir.payment_service.repository.PaymentRepository;
import cakir.payment_service.service.PaymentService;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class PaymentServiceImpl implements PaymentService {

    private final PaymentRepository repository;
    private final PaymentMessagePublisher paymentMessagePublisher;

    public PaymentServiceImpl(PaymentRepository repository, PaymentMessagePublisher paymentMessagePublisher) {
        this.repository = repository;
        this.paymentMessagePublisher = paymentMessagePublisher;
    }

    @Override
    @Transactional
    public void processPayment(PaymentCommand command) {
        PaymentEntity payment = repository.findByUserId(command.getUserId())
                .orElse(null);

        if (payment == null) {
            paymentMessagePublisher.publishPaymentFailed(command.getOrderId());
            return;
        }

        // use compareTo, because balance and amount are BigDecimal
        if (payment.getBalance().compareTo(command.getAmount()) >= 0) {
            // Bakiyeyi düş
            payment.setBalance(payment.getBalance().subtract(command.getAmount()));
            repository.save(payment);

            log.info("Payment successful! Order ID: {}, New balance: {}", command.getOrderId(), payment.getBalance());

            paymentMessagePublisher.publishPaymentSuccess(command.getOrderId(), command.getAmount());
        } else {
            log.warn("Payment failed due to insufficient balance! Order ID: {}, Available balance: {}, Required amount: {}",
                        command.getOrderId(), payment.getBalance(), command.getAmount());


            paymentMessagePublisher.publishPaymentFailed(command.getOrderId());
        }
    }

    @Override
    public void refundPayment(PaymentCommand command) {
        PaymentEntity payment = repository.findByUserId(command.getUserId())
                    .orElse(null);

        if (payment == null) {
            log.warn("Refund failed! No payment record found for user ID: {}", command.getUserId());

            paymentMessagePublisher.publishRefundFailed(command.getOrderId());
            return;
        }

        payment.setBalance(payment.getBalance().add(command.getAmount()));
        repository.save(payment);

        log.info("Refund successful! Order ID: {}, Refunded amount: {}, New balance: {}",
                    command.getOrderId(), command.getAmount(), payment.getBalance());

        paymentMessagePublisher.publishRefundSuccess(command.getOrderId());
    }
}
