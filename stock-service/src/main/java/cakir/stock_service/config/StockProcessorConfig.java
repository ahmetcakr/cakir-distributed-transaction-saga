package cakir.stock_service.config;

import cakir.stock_service.model.dto.StockCommand;
import cakir.stock_service.service.StockService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.function.Consumer;

@Slf4j
@Configuration
public class StockProcessorConfig {

    @Bean
    public Consumer<StockCommand> stockProcessor(StockService stockService) {
        return command -> {
            log.info(command.getAction() + " command received");

            if ("RESERVE_STOCK".equals(command.getAction())) {
                stockService.handleStockReserve(command);
            } else if ("RELEASE_STOCK".equals(command.getAction())) {
                stockService.handleStockRelease(command);
            }
        };
    }
}