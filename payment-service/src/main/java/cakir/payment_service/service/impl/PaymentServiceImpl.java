package cakir.payment_service.service.impl;

import cakir.payment_service.messaging.PaymentMessagePublisher;
import cakir.payment_service.model.dto.PaymentCommand;
import cakir.payment_service.model.entity.PaymentEntity;
import cakir.payment_service.repository.PaymentRepository;
import cakir.payment_service.service.PaymentService;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

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
            System.out.println("Kullanıcı bulunamadı! Order ID: " + command.getOrderId());

            paymentMessagePublisher.publishPaymentFailed(command.getOrderId());
            return;
        }

        // use compareTo, because balance and amount are BigDecimal
        if (payment.getBalance().compareTo(command.getAmount()) >= 0) {
            // Bakiyeyi düş
            payment.setBalance(payment.getBalance().subtract(command.getAmount()));
            repository.save(payment);

            System.out.println("Ödeme başarılı! Kalan bakiye: " + payment.getBalance());

            paymentMessagePublisher.publishPaymentSuccess(command.getOrderId(), command.getAmount());
        } else {
            System.out.println("Bakiye yetersiz!");

            paymentMessagePublisher.publishPaymentFailed(command.getOrderId());
        }
    }

    @Override
    public void refundPayment(PaymentCommand command) {
        System.out.println("Ödeme iade ediliyor... Order ID: " + command.getOrderId());

        PaymentEntity payment = repository.findByUserId(command.getUserId())
                    .orElse(null);

        if (payment == null) {
            System.out.println("Kullanıcı bulunamadı! Order ID: " + command.getOrderId());

            paymentMessagePublisher.publishRefundFailed(command.getOrderId());
            return;
        }

        // Bakiyeyi geri ekle
        payment.setBalance(payment.getBalance().add(command.getAmount()));
        repository.save(payment);

        System.out.println("İade başarılı! Yeni bakiye: " + payment.getBalance());

        paymentMessagePublisher.publishRefundSuccess(command.getOrderId());
    }
}
