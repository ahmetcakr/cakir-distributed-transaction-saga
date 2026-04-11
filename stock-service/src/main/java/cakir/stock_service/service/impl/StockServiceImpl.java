package cakir.stock_service.service.impl;

import cakir.stock_service.messaging.StockMessagePublisher;
import cakir.stock_service.model.dto.StockCommand;
import cakir.stock_service.model.dto.StockEvent;
import cakir.stock_service.model.entity.StockEntity;
import cakir.stock_service.repository.StockRepository;
import cakir.stock_service.service.StockService;
import jakarta.transaction.Transactional;
import org.springframework.cloud.stream.function.StreamBridge;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

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
            publisher.publishStockError(command.getOrderId(), "STOCK_NOT_FOUND");
            return;
        }

        if (stockEntity.getRemainingStock() < command.getQuantity()) {
            publisher.publishStockError(command.getOrderId(), "INSUFFICIENT_STOCK");
            return;
        }

        System.out.println("Stok rezerve ediliyor... Order ID: " + command.getOrderId());

        BigDecimal totalAmount = stockEntity.getSingleUnitPrice().multiply(BigDecimal.valueOf(command.getQuantity()));

        Integer newRemainingStock = stockEntity.getRemainingStock() - command.getQuantity();
        stockEntity.setRemainingStock(newRemainingStock);
        stockRepository.save(stockEntity);

        publisher.publishStockReserved(command.getOrderId(), totalAmount, command.getUserId());
    }

    @Override
    @Transactional
    public void handleStockRelease(StockCommand command) {
        StockEntity stockEntity = stockRepository.findByProductId(command.getProductId()).orElse(null);

        if (stockEntity == null) {
            System.out.println("Ürün bulunamadı. Order ID: " + command.getOrderId());
            publisher.publishStockError(command.getOrderId(), "STOCK_NOT_FOUND");
            return;
        }

        System.out.println("Stok serbest bırakılıyor... Order ID: " + command.getOrderId());

        BigDecimal totalAmount = stockEntity.getSingleUnitPrice().multiply(BigDecimal.valueOf(command.getQuantity()));

        Integer newRemainingStock = stockEntity.getRemainingStock() + command.getQuantity();
        stockEntity.setRemainingStock(newRemainingStock);
        stockRepository.save(stockEntity);

        publisher.publishStockReleased(command.getOrderId(), totalAmount, command.getUserId());
    }
}
