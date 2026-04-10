package cakir.payment_service.service.impl;

import cakir.payment_service.model.dto.PaymentCommand;
import cakir.payment_service.model.dto.PaymentEvent;
import cakir.payment_service.repository.PaymentRepository;
import cakir.payment_service.service.PaymentService;
import jakarta.transaction.Transactional;
import org.springframework.cloud.stream.function.StreamBridge;
import org.springframework.stereotype.Service;

@Service
public class PaymentServiceImpl implements PaymentService {

    private final PaymentRepository repository;
    private final StreamBridge streamBridge;

    public PaymentServiceImpl(PaymentRepository repository, StreamBridge streamBridge) {
        this.repository = repository;
        this.streamBridge = streamBridge;
    }

    @Override
    @Transactional
    public void processPayment(PaymentCommand command) {
        System.out.println("Ödeme alınıyor... Tutar: " + command.getAmount());

        streamBridge.send("paymentResponse-out-0", new PaymentEvent(command.getOrderId(), "PAYMENT_SUCCESS"));
    }
}
