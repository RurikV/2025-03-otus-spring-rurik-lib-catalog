package ru.otus.hw.handlers;

import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;
import ru.otus.hw.services.GenreService;

@Component
@RequiredArgsConstructor
public class GenreHandler {

    private final GenreService genreService;

    public Mono<ServerResponse> listGenres(ServerRequest request) {
        return genreService.findAll()
                .collectList()
                .flatMap(genres -> ServerResponse.ok()
                        .contentType(MediaType.TEXT_HTML)
                        .render("genre/list", "genres", genres));
    }
}