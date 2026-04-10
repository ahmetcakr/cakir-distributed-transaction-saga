package cakir.stock_service.service;

import cakir.stock_service.model.dto.StockCommand;

public interface StockService {
    void handleStockReserve(StockCommand command);

    void handleStockRelease(StockCommand command);
}
