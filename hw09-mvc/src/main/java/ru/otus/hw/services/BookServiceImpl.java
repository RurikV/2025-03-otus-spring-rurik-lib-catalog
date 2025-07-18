package ru.otus.hw.services;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.otus.hw.dto.BookCreateDto;
import ru.otus.hw.dto.BookUpdateDto;
import ru.otus.hw.exceptions.EntityNotFoundException;
import ru.otus.hw.models.Book;
import ru.otus.hw.repositories.AuthorRepository;
import ru.otus.hw.repositories.BookRepository;
import ru.otus.hw.repositories.GenreRepository;

import java.util.List;

import static org.springframework.util.CollectionUtils.isEmpty;

@RequiredArgsConstructor
@Service
public class BookServiceImpl implements BookService {
    private final AuthorRepository authorRepository;

    private final GenreRepository genreRepository;

    private final BookRepository bookRepository;

    @Override
    public Book findById(String id) {
        return bookRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Book with id %s not found".formatted(id)));
    }

    @Override
    public List<Book> findAll() {
        return bookRepository.findAll();
    }

    @Override
    public Book create(BookCreateDto bookCreateDto) {
        if (isEmpty(bookCreateDto.getGenreIds())) {
            throw new IllegalArgumentException("Genres ids must not be null");
        }

        var author = authorRepository.findById(bookCreateDto.getAuthorId())
                .orElseThrow(() -> new EntityNotFoundException("Author with id %s not found".formatted(bookCreateDto.getAuthorId())));
        var genres = genreRepository.findAllByIds(bookCreateDto.getGenreIds());
        if (isEmpty(genres) || bookCreateDto.getGenreIds().size() != genres.size()) {
            throw new EntityNotFoundException("One or all genres with ids %s not found".formatted(bookCreateDto.getGenreIds()));
        }

        var book = new Book(null, bookCreateDto.getTitle(), author, genres);
        return bookRepository.save(book);
    }

    @Override
    public Book update(BookUpdateDto bookUpdateDto) {
        findById(bookUpdateDto.getId());
        
        if (isEmpty(bookUpdateDto.getGenreIds())) {
            throw new IllegalArgumentException("Genres ids must not be null");
        }

        var author = authorRepository.findById(bookUpdateDto.getAuthorId())
                .orElseThrow(() -> new EntityNotFoundException("Author with id %s not found".formatted(bookUpdateDto.getAuthorId())));
        var genres = genreRepository.findAllByIds(bookUpdateDto.getGenreIds());
        if (isEmpty(genres) || bookUpdateDto.getGenreIds().size() != genres.size()) {
            throw new EntityNotFoundException("One or all genres with ids %s not found".formatted(bookUpdateDto.getGenreIds()));
        }

        var book = new Book(bookUpdateDto.getId(), bookUpdateDto.getTitle(), author, genres);
        return bookRepository.save(book);
    }

    @Override
    public void deleteById(String id) {
        bookRepository.deleteById(id);
    }
}
