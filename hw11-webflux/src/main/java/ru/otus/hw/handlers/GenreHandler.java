package ru.otus.hw.handlers;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;
import ru.otus.hw.services.GenreService;

@Slf4j
@Component
@RequiredArgsConstructor
public class GenreHandler {

    private final GenreService genreService;

    public Mono<ServerResponse> listGenres(ServerRequest request) {
        log.info("[DEBUG_LOG] GenreHandler.listGenres() called");
        
        return genreService.findAll()
                .doOnNext(genre -> log.info("[DEBUG_LOG] Found genre: {}", genre.getName()))
                .collectList()
                .doOnSuccess(genres -> log.info("[DEBUG_LOG] Retrieved {} genres from service: {}", 
                        genres.size(), genres.stream().map(genre -> genre.getName()).toList()))
                .doOnError(error -> log.error("[DEBUG_LOG] Error retrieving genres", error))
                .flatMap(genres -> {
                    log.info("[DEBUG_LOG] Rendering genre/list template with {} genres", genres.size());
                    log.info("[DEBUG_LOG] Genres data structure: {}", genres);
                    log.info("[DEBUG_LOG] First genre details: id={}, name={}", 
                            genres.isEmpty() ? "N/A" : genres.get(0).getId(),
                            genres.isEmpty() ? "N/A" : genres.get(0).getName());
                    log.info("[DEBUG_LOG] Genres list isEmpty: {}", genres.isEmpty());
                    log.info("[DEBUG_LOG] About to render template with model key 'genres' using Map approach");
                    
                    // Use Map-based approach for better Thymeleaf compatibility
                    java.util.Map<String, Object> model = new java.util.HashMap<>();
                    model.put("genres", genres);
                    log.info("[DEBUG_LOG] Model map created with genres key, map size: {}", model.size());
                    
                    return ServerResponse.ok()
                            .contentType(MediaType.TEXT_HTML)
                            .render("genre/list", model);
                })
                .doOnSuccess(response -> log.info("[DEBUG_LOG] Successfully rendered genres page"))
                .doOnError(error -> log.error("[DEBUG_LOG] Error rendering genres page", error));
    }
}