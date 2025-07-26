package ru.otus.hw.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.codec.ServerCodecConfigurer;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.http.server.reactive.ServerHttpResponseDecorator;
import org.springframework.web.reactive.config.WebFluxConfigurer;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.ServerWebExchangeDecorator;
import org.springframework.web.server.WebFilter;

@Configuration
public class WebFluxConfig implements WebFluxConfigurer {

    @Override
    public void configureHttpMessageCodecs(ServerCodecConfigurer configurer) {
        configurer.defaultCodecs().enableLoggingRequestDetails(true);
        configurer.defaultCodecs().maxInMemorySize(1024 * 1024);
    }

    @Bean
    public WebFilter charsetFilter() {
        return (exchange, chain) -> {
            ServerWebExchange decoratedExchange = new ServerWebExchangeDecorator(exchange) {
                @Override
                public ServerHttpResponse getResponse() {
                    return new ServerHttpResponseDecorator(super.getResponse()) {
                        @Override
                        public reactor.core.publisher.Mono<Void> writeWith(org.reactivestreams.Publisher<? extends org.springframework.core.io.buffer.DataBuffer> body) {
                            String contentType = getHeaders().getFirst("Content-Type");
                            if (contentType != null && contentType.equals("text/html")) {
                                getHeaders().set("Content-Type", "text/html;charset=UTF-8");
                            }
                            return super.writeWith(body);
                        }
                    };
                }
            };
            return chain.filter(decoratedExchange);
        };
    }

}