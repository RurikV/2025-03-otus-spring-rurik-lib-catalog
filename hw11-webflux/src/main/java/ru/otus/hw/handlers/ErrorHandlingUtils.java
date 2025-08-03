package ru.otus.hw.handlers;

import org.springframework.http.HttpStatus;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;

import java.net.URI;
import java.util.function.Function;

/**
 * Utility class for common error handling in WebFlux handlers
 */
public final class ErrorHandlingUtils {

    private ErrorHandlingUtils() {
        // Utility class
    }

    /**
     * Common error handling for API endpoints that return JSON responses
     * Handles IllegalArgumentException as BadRequest and other exceptions as InternalServerError
     */
    public static Function<Throwable, Mono<ServerResponse>> handleApiErrors() {
        return throwable -> {
            if (throwable instanceof IllegalArgumentException) {
                return ServerResponse.badRequest().bodyValue(throwable.getMessage());
            }
            return ServerResponse.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        };
    }

    /**
     * Common error handling for API endpoints with custom IllegalArgumentException handling
     */
    public static Function<Throwable, Mono<ServerResponse>> handleApiErrors(
            Function<IllegalArgumentException, Mono<ServerResponse>> illegalArgHandler) {
        return throwable -> {
            if (throwable instanceof IllegalArgumentException) {
                return illegalArgHandler.apply((IllegalArgumentException) throwable);
            }
            return ServerResponse.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        };
    }

    /**
     * Common error handling for page endpoints that redirect on errors
     */
    public static Function<Throwable, Mono<ServerResponse>> handlePageErrors(String redirectUrl) {
        return throwable -> ServerResponse.seeOther(URI.create(redirectUrl)).build();
    }

    /**
     * Common error handling for general exceptions only (returns InternalServerError)
     */
    public static Function<Throwable, Mono<ServerResponse>> handleGeneralErrors() {
        return throwable -> ServerResponse.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
    }

    /**
     * Applies common API error handling to a Mono
     */
    public static <T> Mono<T> withApiErrorHandling(Mono<T> mono) {
        return mono.onErrorResume(IllegalArgumentException.class, 
                        e -> Mono.error(e))
                .onErrorResume(Exception.class, 
                        e -> Mono.error(e));
    }
}