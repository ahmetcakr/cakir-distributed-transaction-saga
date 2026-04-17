package cakir.gateway_service.model;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.time.Instant;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record ApiErrorResponse(
        String code,
        String message,
        int status,
        String path,
        String correlationId,
        Instant timestamp
) {
}
