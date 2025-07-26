package ru.otus.hw.handlers;

import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;
import ru.otus.hw.services.AuthorService;

@Component
@RequiredArgsConstructor
public class AuthorHandler {

    private final AuthorService authorService;

    public Mono<ServerResponse> listAuthors(ServerRequest request) {
        return authorService.findAll()
                .collectList()
                .flatMap(authors -> ServerResponse.ok()
                        .contentType(MediaType.TEXT_HTML)
                        .render("author/list", "authors", authors));
    }
}