package ru.otus.hw.services;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.ResourceAccessException;
import ru.otus.hw.external.HttpBinClient;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SpringRetryServiceTest {

    @Mock
    private HttpBinClient httpBinClient;

    private SpringRetryService springRetryService;

    @BeforeEach
    void setUp() {
        springRetryService = new SpringRetryService(httpBinClient);
    }

    @Test
    void testGetStatusWithRetry_Success() {
        // Given
        int statusCode = 200;
        String expectedResponse = "OK";
        when(httpBinClient.status(statusCode)).thenReturn(expectedResponse);

        // When
        String result = springRetryService.getStatusWithRetry(statusCode);

        // Then
        assertEquals(expectedResponse, result);
        verify(httpBinClient, times(1)).status(statusCode);
    }

    @Test
    void testGetStatusWithRetry_SuccessAfterRetries() {
        // Given
        int statusCode = 500;
        String expectedResponse = "Internal Server Error";
        when(httpBinClient.status(statusCode))
                .thenThrow(new HttpServerErrorException(org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR))
                .thenThrow(new HttpServerErrorException(org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR))
                .thenReturn(expectedResponse);

        // When
        String result = springRetryService.getStatusWithRetry(statusCode);

        // Then
        assertEquals(expectedResponse, result);
        verify(httpBinClient, times(3)).status(statusCode);
    }

    @Test
    void testGetStatusWithRetry_ExhaustsRetriesAndRecovers() {
        // Given
        int statusCode = 500;
        HttpServerErrorException exception = new HttpServerErrorException(org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR);
        when(httpBinClient.status(statusCode)).thenThrow(exception);

        // When
        String result = springRetryService.getStatusWithRetry(statusCode);

        // Then
        assertTrue(result.startsWith("Recovery: Failed to get status " + statusCode));
        verify(httpBinClient, times(3)).status(statusCode); // maxAttempts = 3
    }

    @Test
    void testGetStatusWithRetry_NetworkErrorRecovery() {
        // Given
        int statusCode = 200;
        ResourceAccessException exception = new ResourceAccessException("Connection timeout");
        when(httpBinClient.status(statusCode)).thenThrow(exception);

        // When
        String result = springRetryService.getStatusWithRetry(statusCode);

        // Then
        assertTrue(result.startsWith("Recovery: Network error for status " + statusCode));
        verify(httpBinClient, times(3)).status(statusCode);
    }

    @Test
    void testGetDelayedWithRetry_Success() {
        // Given
        int delaySeconds = 1;
        String expectedResponse = "Delayed response";
        when(httpBinClient.delay(delaySeconds)).thenReturn(expectedResponse);

        // When
        String result = springRetryService.getDelayedWithRetry(delaySeconds);

        // Then
        assertEquals(expectedResponse, result);
        verify(httpBinClient, times(1)).delay(delaySeconds);
    }

    @Test
    void testGetDelayedWithRetry_RetriesAndRecovers() {
        // Given
        int delaySeconds = 2;
        HttpServerErrorException exception = new HttpServerErrorException(org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR);
        when(httpBinClient.delay(delaySeconds)).thenThrow(exception);

        // When
        String result = springRetryService.getDelayedWithRetry(delaySeconds);

        // Then
        assertTrue(result.startsWith("Recovery: Failed to get delay " + delaySeconds));
        verify(httpBinClient, times(2)).delay(delaySeconds); // maxAttempts = 2
    }

    @Test
    void testSimulateFailingOperation_EventualSuccess() {
        // Given
        String input = "test-input";
        
        // When - call multiple times to test the random failure
        boolean eventuallySucceeds = false;
        for (int i = 0; i < 10; i++) {
            String result = springRetryService.simulateFailingOperation(input);
            if (result.startsWith("Success:")) {
                eventuallySucceeds = true;
                assertEquals("Success: " + input, result);
                break;
            }
        }
        
        // Then - at least one should succeed or recover
        assertTrue(eventuallySucceeds || 
                springRetryService.simulateFailingOperation(input).startsWith("Recovery:"));
    }

    @Test
    void testSimulateFailingOperation_Recovery() {
        // Given
        String input = "always-fail";
        
        // Mock Math.random() to always return high value (always fail)
        // Since we can't easily mock Math.random(), we'll test the recovery by 
        // running multiple times and checking that recovery eventually happens
        int recoveryCount = 0;
        for (int i = 0; i < 20; i++) {
            String result = springRetryService.simulateFailingOperation(input);
            if (result.startsWith("Recovery:")) {
                recoveryCount++;
            }
        }
        
        // Then - should have some recovery cases
        assertTrue(recoveryCount > 0, "Should have at least some recovery cases");
    }
}