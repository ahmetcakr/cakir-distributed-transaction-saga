package cakir.payment_service.service.impl;

import cakir.payment_service.lock.RedisDistributedLockService;
import cakir.payment_service.messaging.PaymentMessagePublisher;
import cakir.payment_service.model.dto.PaymentCommand;
import cakir.payment_service.model.entity.PaymentEntity;
import cakir.payment_service.model.entity.PaymentTransactionEntity;
import cakir.payment_service.repository.PaymentRepository;
import cakir.payment_service.repository.PaymentTransactionRepository;
import cakir.payment_service.service.PaymentService;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Slf4j
@Service
public class PaymentServiceImpl implements PaymentService {

    private final PaymentRepository repository;
    private final PaymentTransactionRepository paymentTransactionRepository;
    private final PaymentMessagePublisher paymentMessagePublisher;
    private final RedisDistributedLockService lockService;

    public PaymentServiceImpl(PaymentRepository repository,
                              PaymentTransactionRepository paymentTransactionRepository,
                              PaymentMessagePublisher paymentMessagePublisher,
                              RedisDistributedLockService lockService) {
        this.repository = repository;
        this.paymentTransactionRepository = paymentTransactionRepository;
        this.paymentMessagePublisher = paymentMessagePublisher;
        this.lockService = lockService;
    }

    @Override
    @Transactional
    public void processPayment(PaymentCommand command) {
        if (!StringUtils.hasText(command.getIdempotencyKey())) {
            throw new IllegalArgumentException("idempotencyKey is required for payment processing");
        }
        lockService.executeWithLock("payment:process:" + command.getIdempotencyKey(), () -> {
            PaymentTransactionEntity existingTransaction = paymentTransactionRepository
                    .findByIdempotencyKey(command.getIdempotencyKey())
                    .orElse(null);

            if (existingTransaction != null) {
                log.info("Duplicate payment command skipped for idempotencyKey: {}", command.getIdempotencyKey());
                paymentMessagePublisher.publishPaymentSuccess(
                        existingTransaction.getOrderId(),
                        existingTransaction.getAmount(),
                        existingTransaction.getIdempotencyKey());
                return null;
            }

            PaymentEntity payment = repository.findByUserId(command.getUserId())
                    .orElse(null);

            if (payment == null) {
                paymentMessagePublisher.publishPaymentFailed(command.getOrderId(), command.getIdempotencyKey());
                return null;
            }

            if (payment.getBalance().compareTo(command.getAmount()) >= 0) {
                payment.setBalance(payment.getBalance().subtract(command.getAmount()));
                repository.save(payment);

                PaymentTransactionEntity transaction = new PaymentTransactionEntity();
                transaction.setOrderId(command.getOrderId());
                transaction.setAmount(command.getAmount());
                transaction.setIdempotencyKey(command.getIdempotencyKey());
                paymentTransactionRepository.save(transaction);

                log.info("Payment successful! Order ID: {}, New balance: {}", command.getOrderId(), payment.getBalance());

                paymentMessagePublisher.publishPaymentSuccess(command.getOrderId(), command.getAmount(), command.getIdempotencyKey());
            } else {
                log.warn("Payment failed due to insufficient balance! Order ID: {}, Available balance: {}, Required amount: {}",
                        command.getOrderId(), payment.getBalance(), command.getAmount());

                paymentMessagePublisher.publishPaymentFailed(command.getOrderId(), command.getIdempotencyKey());
            }

            return null;
        });
    }

    @Override
    public void refundPayment(PaymentCommand command) {
        if (!StringUtils.hasText(command.getIdempotencyKey())) {
            throw new IllegalArgumentException("idempotencyKey is required for refund processing");
        }

        lockService.executeWithLock("payment:refund:" + command.getIdempotencyKey(), () -> {
            PaymentEntity payment = repository.findByUserId(command.getUserId())
                    .orElse(null);

            if (payment == null) {
                log.warn("Refund failed! No payment record found for user ID: {}", command.getUserId());

                paymentMessagePublisher.publishRefundFailed(command.getOrderId(), command.getIdempotencyKey());
                return null;
            }

            payment.setBalance(payment.getBalance().add(command.getAmount()));
            repository.save(payment);

            log.info("Refund successful! Order ID: {}, Refunded amount: {}, New balance: {}",
                    command.getOrderId(), command.getAmount(), payment.getBalance());

            paymentMessagePublisher.publishRefundSuccess(command.getOrderId(), command.getIdempotencyKey());
            return null;
        });
    }
}
