package cakir.stock_service.config;

import cakir.stock_service.model.dto.StockCommand;
import cakir.stock_service.service.StockService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.function.Consumer;

@Configuration
public class StockProcessorConfig {

    @Bean
    public Consumer<StockCommand> stockProcessor(StockService stockService) {
        return command -> {
            if ("RESERVE_STOCK".equals(command.getAction())) {
                stockService.handleStockReserve(command);
            } else if ("RELEASE_STOCK".equals(command.getAction())) {
                stockService.handleStockRelease(command);
            }
        };
    }
}