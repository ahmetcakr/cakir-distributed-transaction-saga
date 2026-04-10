package cakir.stock_service.service.impl;

import cakir.stock_service.model.dto.StockCommand;
import cakir.stock_service.model.dto.StockEvent;
import cakir.stock_service.repository.StockRepository;
import cakir.stock_service.service.StockService;
import jakarta.transaction.Transactional;
import org.springframework.cloud.stream.function.StreamBridge;
import org.springframework.stereotype.Service;

@Service
public class StockServiceImpl implements StockService {
    private final StockRepository repository;
    private final StreamBridge streamBridge;

    public StockServiceImpl(StockRepository repository, StreamBridge streamBridge) {
        this.repository = repository;
        this.streamBridge = streamBridge;
    }

    @Override
    @Transactional
    public void handleStockReserve(StockCommand command) {
        // Normalde burada DB'den productId ile stok çekilir,
        // miktar kontrol edilir. Şimdilik "OK" diyelim.

        System.out.println("Stok rezerve ediliyor... Order ID: " + command.getOrderId());

        // Stok işlemi başarılıysa Orchestrator'a haber ver
        streamBridge.send("stockResponse-out-0", new StockEvent(command.getOrderId(), "STOCK_RESERVED"));
    }

    @Override
    @Transactional
    public void handleStockRelease(StockCommand command) {
        System.out.println("Stok geri yükleniyor (Compensating)... Order ID: " + command.getOrderId());
        // Burada DB'ye gidip miktarı geri arttırıyoruz...

        streamBridge.send("stockResponse-out-0", new StockEvent(command.getOrderId(), "STOCK_RELEASED"));
    }
}
