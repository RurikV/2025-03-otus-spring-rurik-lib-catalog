package ru.otus.hw.services;

import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.ResourceAccessException;
import ru.otus.hw.external.HttpBinClient;

@Service
@RequiredArgsConstructor
public class SpringRetryService {

    private static final Logger LOG = LoggerFactory.getLogger(SpringRetryService.class);

    private final HttpBinClient httpBinClient;

    public String getStatusWithRetry(int code) {
        int maxAttempts = 3;
        for (int attempt = 1; attempt <= maxAttempts; attempt++) {
            LOG.info("Attempting to get status for code: {}", code);
            try {
                return httpBinClient.status(code);
            } catch (HttpServerErrorException ex) {
                if (attempt == maxAttempts) {
                    LOG.error("Failed to get status after retries for code: {}, error: {}", code, ex.getMessage());
                    return "Recovery: Failed to get status " + code + " after retries";
                }
            } catch (ResourceAccessException ex) {
                if (attempt == maxAttempts) {
                    LOG.error("Failed to get status after retries for code: {}, error: {}",
                            code, ex.getMessage());
                    return "Recovery: Network error for status " + code + " after retries";
                }
            }
        }
        // Should never reach here
        return "Recovery: Failed to get status " + code + " after retries";
    }

    public String getDelayedWithRetry(int seconds) {
        int maxAttempts = 2;
        for (int attempt = 1; attempt <= maxAttempts; attempt++) {
            LOG.info("Attempting to get delayed response for {} seconds", seconds);
            try {
                return httpBinClient.delay(seconds);
            } catch (HttpServerErrorException ex) {
                if (attempt == maxAttempts) {
                    LOG.error("Failed to get delayed response after retries for {} seconds, error: {}",
                            seconds, ex.getMessage());
                    return "Recovery: Failed to get delay " + seconds + " after retries";
                }
            } catch (ResourceAccessException ex) {
                if (attempt == maxAttempts) {
                    LOG.error("Failed to get delayed response after retries for {} seconds, error: {}",
                            seconds, ex.getMessage());
                    return "Recovery: Network error for delay " + seconds + " after retries";
                }
            }
        }
        return "Recovery: Failed to get delay " + seconds + " after retries";
    }

    public String simulateFailingOperation(String input) {
        int maxAttempts = 4;
        for (int attempt = 1; attempt <= maxAttempts; attempt++) {
            LOG.info("Attempting failing operation with input: {}", input);
            try {
                if (Math.random() < 0.7) { // 70% chance to fail
                    throw new RuntimeException("Simulated failure for: " + input);
                }
                return "Success: " + input;
            } catch (RuntimeException ex) {
                if (attempt == maxAttempts) {
                    LOG.error("Failed operation after retries for input: {}, error: {}",
                            input, ex.getMessage());
                    return "Recovery: Operation failed for " + input + " after all retries";
                }
            }
        }
        return "Recovery: Operation failed for " + input + " after all retries";
    }
}