package ru.otus.hw.repositories.r2dbc;

import lombok.RequiredArgsConstructor;
import org.springframework.data.r2dbc.core.R2dbcEntityOperations;
import org.springframework.data.relational.core.query.Criteria;
import org.springframework.data.relational.core.query.Query;
import org.springframework.r2dbc.core.DatabaseClient;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import ru.otus.hw.models.r2dbc.AuthorEntity;
import ru.otus.hw.models.r2dbc.BookEntity;
import ru.otus.hw.models.r2dbc.BookWithRelations;
import ru.otus.hw.models.r2dbc.GenreEntity;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Repository
@RequiredArgsConstructor
public class BookR2dbcRepositoryImpl implements BookR2dbcRepository {

    private final R2dbcEntityOperations entityOperations;

    private final DatabaseClient databaseClient;

    @Override
    public Flux<BookEntity> findAll() {
        return entityOperations.select(BookEntity.class).all();
    }

    @Override
    public Mono<BookEntity> findById(Long id) {
        return entityOperations.selectOne(Query.query(Criteria.where("id").is(id)), BookEntity.class);
    }

    @Override
    public Mono<BookEntity> save(BookEntity book) {
        if (book.getId() == null) {
            return entityOperations.insert(book);
        } else {
            return entityOperations.update(book);
        }
    }

    @Override
    public Mono<Void> deleteById(Long id) {
        return entityOperations.delete(Query.query(Criteria.where("id").is(id)), BookEntity.class)
                .then();
    }

    @Override
    public Flux<BookEntity> findAllWithAuthorsAndGenres() {
        return findAll()
                .collectList()
                .flatMapMany(books -> {
                    if (books.isEmpty()) {
                        return Flux.empty();
                    }
                    
                    // Fetch all authors in one query to avoid N+1
                    List<Long> authorIds = books.stream()
                            .map(BookEntity::getAuthorId)
                            .distinct()
                            .collect(Collectors.toList());
                    
                    return entityOperations.select(AuthorEntity.class)
                            .matching(Query.query(Criteria.where("id").in(authorIds)))
                            .all()
                            .collectMap(AuthorEntity::getId)
                            .flatMapMany(authorMap -> Flux.fromIterable(books));
                });
    }

    @Override
    public Mono<BookEntity> findByIdWithAuthorAndGenres(Long id) {
        return findById(id)
                .flatMap(book -> {
                    // Fetch author
                    Mono<AuthorEntity> authorMono = entityOperations.selectOne(
                            Query.query(Criteria.where("id").is(book.getAuthorId())), 
                            AuthorEntity.class);
                    
                    // Fetch genres through book_genres junction table
                    Flux<GenreEntity> genresFlux = databaseClient.sql("""
                            SELECT g.* FROM genres g 
                            JOIN book_genres bg ON g.id = bg.genre_id 
                            WHERE bg.book_id = :bookId
                            """)
                            .bind("bookId", book.getId())
                            .map((row, metadata) -> new GenreEntity(
                                    row.get("id", Long.class),
                                    row.get("name", String.class)))
                            .all();
                    
                    return Mono.zip(authorMono, genresFlux.collectList())
                            .map(tuple -> book); // Return the book, relations are fetched separately
                });
    }

    public Flux<BookWithRelations> findAllBooksWithRelations() {
        return executeBookRelationsQuery()
                .collectList()
                .flatMapMany(this::processRowsToBooks);
    }

    private Flux<Map<String, Object>> executeBookRelationsQuery() {
        return databaseClient.sql("""
                SELECT b.id as book_id, b.title, b.author_id,
                       a.id as author_id, a.full_name,
                       g.id as genre_id, g.name as genre_name
                FROM books b
                LEFT JOIN authors a ON b.author_id = a.id
                LEFT JOIN book_genres bg ON b.id = bg.book_id
                LEFT JOIN genres g ON bg.genre_id = g.id
                ORDER BY b.id
                """)
                .map(this::mapRowToMap)
                .all();
    }

    private Map<String, Object> mapRowToMap(io.r2dbc.spi.Row row, io.r2dbc.spi.RowMetadata metadata) {
        Map<String, Object> map = new HashMap<>();
        map.put("bookId", row.get("book_id", Long.class));
        map.put("title", row.get("title", String.class));
        map.put("authorId", row.get("author_id", Long.class));
        map.put("authorName", row.get("full_name", String.class));
        map.put("genreId", row.get("genre_id", Long.class));
        map.put("genreName", row.get("genre_name", String.class));
        return map;
    }

    private Flux<BookWithRelations> processRowsToBooks(List<Map<String, Object>> rows) {
        Map<Long, BookWithRelations> bookMap = rows.stream()
                .collect(Collectors.groupingBy(
                        row -> (Long) row.get("bookId"),
                        Collectors.collectingAndThen(
                                Collectors.toList(),
                                this::createBookWithRelations
                        )
                ));
        return Flux.fromIterable(bookMap.values());
    }

    private BookWithRelations createBookWithRelations(List<Map<String, Object>> list) {
        Map<String, Object> first = list.get(0);
        BookWithRelations book = new BookWithRelations();
        book.setId((Long) first.get("bookId"));
        book.setTitle((String) first.get("title"));
        book.setAuthor(new AuthorEntity(
                (Long) first.get("authorId"),
                (String) first.get("authorName")));
        book.setGenres(createGenresList(list));
        return book;
    }

    private List<GenreEntity> createGenresList(List<Map<String, Object>> list) {
        return list.stream()
                .filter(row -> row.get("genreId") != null)
                .map(row -> new GenreEntity(
                        (Long) row.get("genreId"),
                        (String) row.get("genreName")))
                .distinct()
                .collect(Collectors.toList());
    }
}