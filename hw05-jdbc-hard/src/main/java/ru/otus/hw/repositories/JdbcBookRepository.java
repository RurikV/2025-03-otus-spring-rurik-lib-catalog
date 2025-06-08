package ru.otus.hw.repositories;

import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.ResultSetExtractor;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.stereotype.Repository;
import ru.otus.hw.exceptions.EntityNotFoundException;
import ru.otus.hw.models.Author;
import ru.otus.hw.models.Book;
import ru.otus.hw.models.Genre;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Repository
@RequiredArgsConstructor
public class JdbcBookRepository implements BookRepository {

    private final GenreRepository genreRepository;
    private final NamedParameterJdbcTemplate jdbcTemplate;
    private final AuthorRepository authorRepository;

    @Override
    public Optional<Book> findById(long id) {
        MapSqlParameterSource params = new MapSqlParameterSource();
        params.addValue("id", id);

        String sql = """
            SELECT b.id, b.title, b.author_id, a.full_name, g.id as genre_id, g.name as genre_name
            FROM books b
            JOIN authors a ON b.author_id = a.id
            LEFT JOIN books_genres bg ON b.id = bg.book_id
            LEFT JOIN genres g ON bg.genre_id = g.id
            WHERE b.id = :id
        """;

        return Optional.ofNullable(jdbcTemplate.query(sql, params, new BookResultSetExtractor()));
    }

    @Override
    public List<Book> findAll() {
        var genres = genreRepository.findAll();
        var relations = getAllGenreRelations();
        var books = getAllBooksWithoutGenres();
        mergeBooksInfo(books, genres, relations);
        return books;
    }

    @Override
    public Book save(Book book) {
        if (book.getId() == 0) {
            return insert(book);
        }
        return update(book);
    }

    @Override
    public void deleteById(long id) {
        MapSqlParameterSource params = new MapSqlParameterSource();
        params.addValue("id", id);

        // First delete from books_genres
        jdbcTemplate.update("DELETE FROM books_genres WHERE book_id = :id", params);

        // Then delete the book
        jdbcTemplate.update("DELETE FROM books WHERE id = :id", params);
    }

    private List<Book> getAllBooksWithoutGenres() {
        String sql = """
            SELECT b.id, b.title, b.author_id, a.full_name
            FROM books b
            JOIN authors a ON b.author_id = a.id
        """;

        return jdbcTemplate.query(sql, new BookRowMapper());
    }

    private List<BookGenreRelation> getAllGenreRelations() {
        String sql = "SELECT book_id, genre_id FROM books_genres";

        return jdbcTemplate.query(sql, (rs, rowNum) -> 
            new BookGenreRelation(rs.getLong("book_id"), rs.getLong("genre_id")));
    }

    private void mergeBooksInfo(List<Book> booksWithoutGenres, List<Genre> genres,
                                List<BookGenreRelation> relations) {
        // Create a map of genre id to genre
        Map<Long, Genre> genreMap = genres.stream()
                .collect(Collectors.toMap(Genre::getId, genre -> genre));

        // Group relations by book id
        Map<Long, List<Long>> bookGenreRelations = new HashMap<>();
        for (BookGenreRelation relation : relations) {
            bookGenreRelations.computeIfAbsent(relation.bookId(), k -> new ArrayList<>())
                    .add(relation.genreId());
        }

        // Add genres to books
        for (Book book : booksWithoutGenres) {
            List<Long> genreIds = bookGenreRelations.getOrDefault(book.getId(), Collections.emptyList());
            List<Genre> bookGenres = genreIds.stream()
                    .map(genreMap::get)
                    .collect(Collectors.toList());
            book.setGenres(bookGenres);
        }
    }

    private Book insert(Book book) {
        var keyHolder = new GeneratedKeyHolder();

        MapSqlParameterSource params = new MapSqlParameterSource();
        params.addValue("title", book.getTitle());
        params.addValue("authorId", book.getAuthor().getId());

        jdbcTemplate.update(
                "INSERT INTO books (title, author_id) VALUES (:title, :authorId)",
                params,
                keyHolder,
                new String[]{"id"}
        );

        //noinspection DataFlowIssue
        book.setId(keyHolder.getKeyAs(Long.class));
        batchInsertGenresRelationsFor(book);
        return book;
    }

    private Book update(Book book) {
        MapSqlParameterSource params = new MapSqlParameterSource();
        params.addValue("id", book.getId());
        params.addValue("title", book.getTitle());
        params.addValue("authorId", book.getAuthor().getId());

        int updatedCount = jdbcTemplate.update(
                "UPDATE books SET title = :title, author_id = :authorId WHERE id = :id",
                params
        );

        if (updatedCount == 0) {
            throw new EntityNotFoundException("Book with id %d not found".formatted(book.getId()));
        }

        removeGenresRelationsFor(book);
        batchInsertGenresRelationsFor(book);

        return book;
    }

    private void batchInsertGenresRelationsFor(Book book) {
        List<MapSqlParameterSource> batchParams = new ArrayList<>();

        for (Genre genre : book.getGenres()) {
            MapSqlParameterSource params = new MapSqlParameterSource();
            params.addValue("bookId", book.getId());
            params.addValue("genreId", genre.getId());
            batchParams.add(params);
        }

        jdbcTemplate.batchUpdate(
                "INSERT INTO books_genres (book_id, genre_id) VALUES (:bookId, :genreId)",
                batchParams.toArray(new MapSqlParameterSource[0])
        );
    }

    private void removeGenresRelationsFor(Book book) {
        MapSqlParameterSource params = new MapSqlParameterSource();
        params.addValue("bookId", book.getId());

        jdbcTemplate.update("DELETE FROM books_genres WHERE book_id = :bookId", params);
    }

    private static class BookRowMapper implements RowMapper<Book> {

        @Override
        public Book mapRow(ResultSet rs, int rowNum) throws SQLException {
            long id = rs.getLong("id");
            String title = rs.getString("title");
            long authorId = rs.getLong("author_id");
            String authorFullName = rs.getString("full_name");

            Author author = new Author(authorId, authorFullName);
            return new Book(id, title, author, new ArrayList<>());
        }
    }

    // Использовать для findById
    @SuppressWarnings("ClassCanBeRecord")
    @RequiredArgsConstructor
    private static class BookResultSetExtractor implements ResultSetExtractor<Book> {

        @Override
        public Book extractData(ResultSet rs) throws SQLException, DataAccessException {
            Book book = null;
            List<Genre> genres = new ArrayList<>();

            while (rs.next()) {
                if (book == null) {
                    long id = rs.getLong("id");
                    String title = rs.getString("title");
                    long authorId = rs.getLong("author_id");
                    String authorFullName = rs.getString("full_name");

                    Author author = new Author(authorId, authorFullName);
                    book = new Book(id, title, author, new ArrayList<>());
                }

                long genreId = rs.getLong("genre_id");
                if (!rs.wasNull()) {
                    String genreName = rs.getString("genre_name");
                    genres.add(new Genre(genreId, genreName));
                }
            }

            if (book != null) {
                book.setGenres(genres);
            }

            return book;
        }
    }

    private record BookGenreRelation(long bookId, long genreId) {
    }
}
