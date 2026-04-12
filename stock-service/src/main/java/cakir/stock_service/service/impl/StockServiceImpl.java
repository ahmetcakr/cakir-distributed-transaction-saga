package cakir.stock_service.service.impl;

import cakir.stock_service.messaging.StockMessagePublisher;
import cakir.stock_service.model.dto.StockCommand;
import cakir.stock_service.model.entity.StockEntity;
import cakir.stock_service.repository.StockRepository;
import cakir.stock_service.service.StockService;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Slf4j
@Service
public class StockServiceImpl implements StockService {
    private final StockRepository stockRepository;
    private final StockMessagePublisher publisher;

    public StockServiceImpl(StockRepository stockRepository, StockMessagePublisher publisher) {
        this.stockRepository = stockRepository;
        this.publisher = publisher;
    }

    @Override
    @Transactional
    public void handleStockReserve(StockCommand command) {
        StockEntity stockEntity = stockRepository.findByProductId(command.getProductId()).orElse(null);

        if (stockEntity == null) {
            publisher.publishStockError(command.getOrderId(), "STOCK_NOT_FOUND", command.getIdempotencyKey());
            return;
        }

        if (stockEntity.getRemainingStock() < command.getQuantity()) {
            publisher.publishStockError(command.getOrderId(), "INSUFFICIENT_STOCK", command.getIdempotencyKey());
            return;
        }

        BigDecimal totalAmount = stockEntity.getSingleUnitPrice().multiply(BigDecimal.valueOf(command.getQuantity()));

        Integer newRemainingStock = stockEntity.getRemainingStock() - command.getQuantity();
        stockEntity.setRemainingStock(newRemainingStock);
        stockRepository.save(stockEntity);

        log.info("Stock reserved. Order ID: {}, Product ID: {}, Quantity: {}, Remaining Stock: {}", command.getOrderId(), command.getProductId(), command.getQuantity(), newRemainingStock);

        publisher.publishStockReserved(command.getOrderId(), totalAmount, command.getUserId(), command.getIdempotencyKey());
    }

    @Override
    @Transactional
    public void handleStockRelease(StockCommand command) {
        StockEntity stockEntity = stockRepository.findByProductId(command.getProductId()).orElse(null);

        if (stockEntity == null) {
            publisher.publishStockError(command.getOrderId(), "STOCK_NOT_FOUND", command.getIdempotencyKey());
            return;
        }

        BigDecimal totalAmount = stockEntity.getSingleUnitPrice().multiply(BigDecimal.valueOf(command.getQuantity()));

        Integer newRemainingStock = stockEntity.getRemainingStock() + command.getQuantity();
        stockEntity.setRemainingStock(newRemainingStock);
        stockRepository.save(stockEntity);

        log.info("Stock released. Order ID: {}, Product ID: {}, Quantity: {}, Remaining Stock: {}", command.getOrderId(), command.getProductId(), command.getQuantity(), newRemainingStock);

        publisher.publishStockReleased(command.getOrderId(), totalAmount, command.getUserId(), command.getIdempotencyKey());
    }
}
