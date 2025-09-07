package ru.otus.hw.services;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.TestPropertySource;
import ru.otus.hw.external.HttpBinClient;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.BDDMockito.given;

@SpringBootTest(classes = {ExternalHttpService.class}, properties = {
        // Load minimal context with only ExternalHttpService to avoid scanning Mongo configs
        "spring.autoconfigure.exclude=org.springframework.boot.autoconfigure.mongo.MongoAutoConfiguration,org.springframework.boot.autoconfigure.data.mongo.MongoDataAutoConfiguration"
})
@TestPropertySource(properties = {
        // Use short retry to speed up tests if retry is enabled
        "resilience4j.retry.instances.httpbin.maxAttempts=2",
        "resilience4j.retry.instances.httpbin.waitDuration=10ms",
        "resilience4j.circuitbreaker.instances.httpbin.minimumNumberOfCalls=1"
})
@DisplayName("ExternalHttpService with Resilience4j should")
class ExternalHttpServiceTest {

    @Autowired
    private ExternalHttpService service;

    @MockBean
    private HttpBinClient httpBinClient;

    @Test
    @DisplayName("return fallback for status when client throws exception")
    void shouldReturnFallbackForStatusOnError() {
        given(httpBinClient.status(anyInt())).willThrow(new RuntimeException("boom"));

        String result = service.getStatus(500);

        assertThat(result)
                .startsWith("fallback-status:500")
                .contains("reason=");
    }

    @Test
    @DisplayName("return fallback for delay when client throws exception")
    void shouldReturnFallbackForDelayOnError() {
        given(httpBinClient.delay(anyInt())).willThrow(new RuntimeException("boom"));

        String result = service.getDelayed(2);

        assertThat(result)
                .startsWith("fallback-delay:2")
                .contains("reason=");
    }

    @Test
    @DisplayName("pass through successful responses without fallback")
    void shouldPassThroughOnSuccess() {
        given(httpBinClient.status(200)).willReturn("OK");
        given(httpBinClient.delay(1)).willReturn("DELAYED");

        assertThat(service.getStatus(200)).isEqualTo("OK");
        assertThat(service.getDelayed(1)).isEqualTo("DELAYED");
    }
}
