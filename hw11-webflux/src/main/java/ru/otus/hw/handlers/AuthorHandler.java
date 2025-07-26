package ru.otus.hw.handlers;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;
import ru.otus.hw.services.AuthorService;

@Slf4j
@Component
@RequiredArgsConstructor
public class AuthorHandler {

    private final AuthorService authorService;

    public Mono<ServerResponse> listAuthors(ServerRequest request) {
        log.info("[DEBUG_LOG] AuthorHandler.listAuthors() called");
        
        return authorService.findAll()
                .doOnNext(author -> log.info("[DEBUG_LOG] Found author: {}", author.getFullName()))
                .collectList()
                .doOnSuccess(authors -> log.info("[DEBUG_LOG] Retrieved {} authors from service: {}", 
                        authors.size(), authors.stream().map(author -> author.getFullName()).toList()))
                .doOnError(error -> log.error("[DEBUG_LOG] Error retrieving authors", error))
                .flatMap(authors -> {
                    log.info("[DEBUG_LOG] Rendering author/list template with {} authors", authors.size());
                    log.info("[DEBUG_LOG] About to render template with model key 'authors' using Map approach");
                    
                    // Use Map-based approach for better Thymeleaf compatibility
                    java.util.Map<String, Object> model = new java.util.HashMap<>();
                    model.put("authors", authors);
                    log.info("[DEBUG_LOG] Model map created with authors key, map size: {}", model.size());
                    
                    return ServerResponse.ok()
                            .contentType(MediaType.TEXT_HTML)
                            .render("author/list", model);
                })
                .doOnSuccess(response -> log.info("[DEBUG_LOG] Successfully rendered authors page"))
                .doOnError(error -> log.error("[DEBUG_LOG] Error rendering authors page", error));
    }
}