package ru.otus.hw.repositories;

import org.springframework.data.mongodb.repository.Query;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import reactor.core.publisher.Flux;
import ru.otus.hw.models.Genre;

import java.util.Set;

public interface GenreRepository extends ReactiveMongoRepository<Genre, String> {
    @Query("{ '_id' : { $in: ?0 } }")
    Flux<Genre> findAllByIds(Set<String> ids);
}
