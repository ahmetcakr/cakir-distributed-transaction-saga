package cakir.gateway_service.config;

import cakir.gateway_service.model.ApiErrorResponse;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.boot.web.reactive.error.ErrorWebExceptionHandler;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.time.Instant;

@Component
@Order(-2)
public class GatewayExceptionHandler implements ErrorWebExceptionHandler {

    private final ObjectMapper objectMapper;

    public GatewayExceptionHandler(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Override
    public Mono<Void> handle(ServerWebExchange exchange, Throwable ex) {
        if (exchange.getResponse().isCommitted()) {
            return Mono.error(ex);
        }

        HttpStatus status = HttpStatus.INTERNAL_SERVER_ERROR;
        String code = "GATEWAY_INTERNAL_ERROR";
        String message = "Unexpected gateway error occurred.";

        if (ex instanceof ResponseStatusException rse) {
            status = HttpStatus.resolve(rse.getStatusCode().value());
            if (status == null) {
                status = HttpStatus.INTERNAL_SERVER_ERROR;
            }
            code = "GATEWAY_DOWNSTREAM_ERROR";
            message = rse.getReason() != null ? rse.getReason() : "Gateway downstream error.";
        }

        String correlationId = exchange.getRequest().getHeaders().getFirst("X-Correlation-Id");

        ApiErrorResponse response = new ApiErrorResponse(
                code,
                message,
                status.value(),
                exchange.getRequest().getPath().value(),
                correlationId,
                Instant.now()
        );

        exchange.getResponse().setStatusCode(status);
        exchange.getResponse().getHeaders().setContentType(MediaType.APPLICATION_JSON);

        byte[] body;
        try {
            body = objectMapper.writeValueAsBytes(response);
        } catch (JsonProcessingException serializationException) {

            ApiErrorResponse serializationErrorResponse = new ApiErrorResponse(
                    "GATEWAY_SERIALIZATION_ERROR",
                    "Failed to build error response",
                    HttpStatus.INTERNAL_SERVER_ERROR.value(),
                    exchange.getRequest().getPath().value(),
                    correlationId,
                    Instant.now()
            );

            String fallbackBody = String.format(
                    "{\"code\":\"%s\",\"message\":\"%s\",\"status\":%d,\"path\":\"%s\",\"correlationId\":\"%s\",\"timestamp\":\"%s\"}",
                    serializationErrorResponse.code(),
                    serializationErrorResponse.message(),
                    serializationErrorResponse.status(),
                    serializationErrorResponse.path(),
                    serializationErrorResponse.correlationId(),
                    serializationErrorResponse.timestamp()
            );

            body = fallbackBody.getBytes();

        }

        return exchange.getResponse().writeWith(Mono.just(exchange.getResponse().bufferFactory().wrap(body)));
    }
}
