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

    public Mono<ServerResponse> listAuthors(@SuppressWarnings("unused") ServerRequest request) {
        log.info("[DEBUG_LOG] AuthorHandler.listAuthors() called");
        
        return ServerResponse.ok()
                .contentType(MediaType.TEXT_HTML)
                .render("author/list");
    }

    public Mono<ServerResponse> getAllAuthors(@SuppressWarnings("unused") ServerRequest request) {
        log.info("[DEBUG_LOG] AuthorHandler.getAllAuthors() API called");
        
        return ServerResponse.ok()
                .contentType(MediaType.APPLICATION_JSON)
                .body(authorService.findAll(), ru.otus.hw.dto.AuthorDto.class);
    }
}