package cakir.gateway_service.web;

import cakir.gateway_service.model.ApiErrorResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ServerWebExchange;

import java.time.Instant;

@RestController
public class GatewayFallbackController {

    @RequestMapping("/fallback/order-service")
    public ResponseEntity<ApiErrorResponse> orderServiceFallback(ServerWebExchange exchange) {
        String correlationId = exchange.getRequest().getHeaders().getFirst("X-Correlation-Id");

        ApiErrorResponse response = new ApiErrorResponse(
                "DOWNSTREAM_UNAVAILABLE",
                "Order service is temporarily unavailable. Please retry shortly.",
                HttpStatus.SERVICE_UNAVAILABLE.value(),
                exchange.getRequest().getPath().value(),
                correlationId,
                Instant.now()
        );

        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(response);
    }
}
