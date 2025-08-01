package ru.otus.hw.services;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.otus.hw.exceptions.EntityNotFoundException;
import ru.otus.hw.models.Book;
import ru.otus.hw.repositories.AuthorRepository;
import ru.otus.hw.repositories.BookRepository;
import ru.otus.hw.repositories.GenreRepository;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.springframework.util.CollectionUtils.isEmpty;

@RequiredArgsConstructor
@Service
public class BookServiceImpl implements BookService {
    private final AuthorRepository authorRepository;

    private final GenreRepository genreRepository;

    private final BookRepository bookRepository;

    @Override
    @Transactional(readOnly = true)
    public Optional<Book> findById(long id) {
        Optional<Book> bookOpt = bookRepository.findById(id);
        // Принудительно загружаем комментарии, вызывая size() для инициализации lazy-коллекции
        bookOpt.ifPresent(book -> {
            int commentsCount = book.getComments().size(); // Используем результат для принудительной загрузки
        });
        return bookOpt;
    }

    @Override
    @Transactional(readOnly = true)
    public List<Book> findAll() {
        List<Book> books = bookRepository.findAll();
        // Принудительно загружаем жанры, вызывая size() для инициализации lazy-коллекции
        books.forEach(book -> {
            int genresCount = book.getGenres().size(); // Используем результат для принудительной загрузки
        });
        return books;
    }

    @Override
    @Transactional
    public Book insert(String title, long authorId, Set<Long> genresIds) {
        if (isEmpty(genresIds)) {
            throw new IllegalArgumentException("Genres ids must not be null");
        }

        var author = authorRepository.findById(authorId)
                .orElseThrow(() -> new EntityNotFoundException("Author with id %d not found".formatted(authorId)));
        var genres = genreRepository.findAllByIdIn(genresIds);
        if (isEmpty(genres) || genresIds.size() != genres.size()) {
            throw new EntityNotFoundException("One or all genres with ids %s not found".formatted(genresIds));
        }

        var book = new Book(0, title, author, genres, new ArrayList<>());
        return bookRepository.save(book);
    }

    @Override
    @Transactional
    public Book update(long id, String title, long authorId, Set<Long> genresIds) {
        if (isEmpty(genresIds)) {
            throw new IllegalArgumentException("Genres ids must not be null");
        }

        var book = bookRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Book with id %d not found".formatted(id)));

        var author = authorRepository.findById(authorId)
                .orElseThrow(() -> new EntityNotFoundException("Author with id %d not found".formatted(authorId)));
        var genres = genreRepository.findAllByIdIn(genresIds);
        if (isEmpty(genres) || genresIds.size() != genres.size()) {
            throw new EntityNotFoundException("One or all genres with ids %s not found".formatted(genresIds));
        }

        book.setTitle(title);
        book.setAuthor(author);
        book.setGenres(genres);

        return bookRepository.save(book);
    }

    @Override
    @Transactional
    public void deleteById(long id) {
        bookRepository.deleteById(id);
    }

}
