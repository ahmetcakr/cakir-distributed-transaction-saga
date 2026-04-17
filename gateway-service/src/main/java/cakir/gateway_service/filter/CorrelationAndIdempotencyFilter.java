package cakir.gateway_service.filter;

import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.HttpMethod;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.UUID;

import static org.springframework.http.HttpStatus.BAD_REQUEST;

@Component
public class CorrelationAndIdempotencyFilter implements GlobalFilter, Ordered {

    public static final String CORRELATION_ID_HEADER = "X-Correlation-Id";
    public static final String IDEMPOTENCY_KEY_HEADER = "X-Idempotency-Key";

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        String correlationId = exchange.getRequest().getHeaders().getFirst(CORRELATION_ID_HEADER);
        if (!StringUtils.hasText(correlationId)) {
            correlationId = UUID.randomUUID().toString();
        }

        String idempotencyKey = exchange.getRequest().getHeaders().getFirst(IDEMPOTENCY_KEY_HEADER);
        if (isOrderCreation(exchange) && !StringUtils.hasText(idempotencyKey)) {
            return Mono.error(new ResponseStatusException(
                    BAD_REQUEST,
                    "X-Idempotency-Key header is required for POST /api/orders"
            ));
        }

        ServerHttpRequest.Builder requestBuilder = exchange.getRequest().mutate()
                .header(CORRELATION_ID_HEADER, correlationId);

        ServerWebExchange mutatedExchange = exchange.mutate()
                .request(requestBuilder.build())
                .build();

        mutatedExchange.getResponse().getHeaders().set(CORRELATION_ID_HEADER, correlationId);
        if (StringUtils.hasText(idempotencyKey)) {
            mutatedExchange.getResponse().getHeaders().set(IDEMPOTENCY_KEY_HEADER, idempotencyKey);
        }

        return chain.filter(mutatedExchange);
    }

    @Override
    public int getOrder() {
        return Ordered.HIGHEST_PRECEDENCE;
    }

    private boolean isOrderCreation(ServerWebExchange exchange) {
        return HttpMethod.POST.equals(exchange.getRequest().getMethod())
                && exchange.getRequest().getPath().value().startsWith("/api/orders");
    }
}
