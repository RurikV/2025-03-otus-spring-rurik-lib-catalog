package ru.otus.hw.external;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

@Service
public class HttpBinClientRest implements HttpBinClient {

    private final RestClient restClient;

    public HttpBinClientRest(
            @Value("${external.httpbin.base-url:https://httpbin.org}") String baseUrl
    ) {
        this.restClient = RestClient.builder()
                .baseUrl(baseUrl)
                .build();
    }

    @Override
    public String status(int code) {
        return restClient.get()
                .uri("/status/{code}", code)
                .retrieve()
                .body(String.class);
    }

    @Override
    public String delay(int seconds) {
        return restClient.get()
                .uri("/delay/{seconds}", seconds)
                .retrieve()
                .body(String.class);
    }
}
