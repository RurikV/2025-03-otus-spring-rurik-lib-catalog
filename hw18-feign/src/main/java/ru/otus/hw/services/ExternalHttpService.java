package ru.otus.hw.services;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import ru.otus.hw.external.HttpBinClient;

@Service
@RequiredArgsConstructor
public class ExternalHttpService {

    private final HttpBinClient httpBinClient;

    @CircuitBreaker(name = "httpbin", fallbackMethod = "statusFallback")
    @Retry(name = "httpbin")
    public String getStatus(int code) {
        try {
            return httpBinClient.status(code);
        } catch (Throwable ex) {
            return statusFallback(code, ex);
        }
    }

    public String statusFallback(int code, Throwable ex) {
        return "fallback-status:" + code + ", reason=" + ex.getClass().getSimpleName();
    }

    @CircuitBreaker(name = "httpbin", fallbackMethod = "delayFallback")
    @Retry(name = "httpbin")
    public String getDelayed(int seconds) {
        try {
            return httpBinClient.delay(seconds);
        } catch (Throwable ex) {
            return delayFallback(seconds, ex);
        }
    }

    public String delayFallback(int seconds, Throwable ex) {
        return "fallback-delay:" + seconds + ", reason=" + ex.getClass().getSimpleName();
    }
}
