package ru.otus.hw.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.i18n.LocaleContext;
import org.springframework.context.i18n.SimpleLocaleContext;
import org.springframework.http.codec.ServerCodecConfigurer;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.http.server.reactive.ServerHttpResponseDecorator;
import org.springframework.lang.NonNull;
import org.springframework.web.reactive.config.WebFluxConfigurer;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.ServerWebExchangeDecorator;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.i18n.LocaleContextResolver;
import org.springframework.web.server.i18n.AcceptHeaderLocaleContextResolver;

import java.util.Arrays;
import java.util.Locale;

@Configuration
public class WebFluxConfig implements WebFluxConfigurer {

    @Override
    public void configureHttpMessageCodecs(ServerCodecConfigurer configurer) {
        configurer.defaultCodecs().enableLoggingRequestDetails(true);
        configurer.defaultCodecs().maxInMemorySize(1024 * 1024);
    }

    @Bean
    public LocaleContextResolver localeContextResolver() {
        AcceptHeaderLocaleContextResolver resolver = new AcceptHeaderLocaleContextResolver() {
            @Override
            @NonNull
            public LocaleContext resolveLocaleContext(@NonNull ServerWebExchange exchange) {
                String lang = exchange.getRequest().getQueryParams().getFirst("lang");
                if (lang != null) {
                    Locale locale = Locale.forLanguageTag(lang);
                    return new SimpleLocaleContext(locale);
                }
                return super.resolveLocaleContext(exchange);
            }
        };
        resolver.setSupportedLocales(Arrays.asList(Locale.ENGLISH, new Locale("ru")));
        resolver.setDefaultLocale(Locale.ENGLISH);
        return resolver;
    }

    @Bean
    public WebFilter charsetFilter() {
        return (exchange, chain) -> {
            ServerWebExchange decoratedExchange = createDecoratedExchange(exchange);
            return chain.filter(decoratedExchange);
        };
    }

    private ServerWebExchange createDecoratedExchange(ServerWebExchange exchange) {
        return new ServerWebExchangeDecorator(exchange) {
            @Override
            @NonNull
            public ServerHttpResponse getResponse() {
                return createDecoratedResponse(super.getResponse());
            }
        };
    }

    private ServerHttpResponse createDecoratedResponse(ServerHttpResponse response) {
        return new ServerHttpResponseDecorator(response) {
            @Override
            @NonNull
            public reactor.core.publisher.Mono<Void> writeWith(
                    @NonNull org.reactivestreams.Publisher<? extends 
                    org.springframework.core.io.buffer.DataBuffer> body) {
                setCharsetForHtmlContent();
                return super.writeWith(body);
            }

            private void setCharsetForHtmlContent() {
                String contentType = getHeaders().getFirst("Content-Type");
                if (contentType != null && contentType.equals("text/html")) {
                    getHeaders().set("Content-Type", "text/html;charset=UTF-8");
                }
            }
        };
    }

}