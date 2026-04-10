package cakir.payment_service.service;

import cakir.payment_service.model.dto.PaymentCommand;

public interface PaymentService {
    void processPayment(PaymentCommand command);
}
