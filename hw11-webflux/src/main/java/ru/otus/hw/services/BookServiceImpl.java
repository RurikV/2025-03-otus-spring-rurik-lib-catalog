package ru.otus.hw.services;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import ru.otus.hw.dto.AuthorDto;
import ru.otus.hw.dto.BookCreateDto;
import ru.otus.hw.dto.BookDto;
import ru.otus.hw.dto.BookUpdateDto;
import ru.otus.hw.dto.GenreDto;
import ru.otus.hw.exceptions.EntityNotFoundException;
import ru.otus.hw.models.Book;
import ru.otus.hw.repositories.AuthorRepository;
import ru.otus.hw.repositories.BookRepository;
import ru.otus.hw.repositories.GenreRepository;

import static org.springframework.util.CollectionUtils.isEmpty;

@RequiredArgsConstructor
@Service
public class BookServiceImpl implements BookService {
    private final AuthorRepository authorRepository;

    private final GenreRepository genreRepository;

    private final BookRepository bookRepository;

    @Override
    public Mono<BookDto> findById(String id) {
        return bookRepository.findById(id)
                .switchIfEmpty(Mono.error(new EntityNotFoundException("Book with id %s not found".formatted(id))))
                .map(this::toBookDto);
    }

    @Override
    public Flux<BookDto> findAll() {
        return bookRepository.findAll()
                .map(this::toBookDto);
    }

    @Override
    public Mono<BookDto> create(BookCreateDto bookCreateDto) {
        return authorRepository.findById(bookCreateDto.getAuthorId())
                .switchIfEmpty(Mono.error(new EntityNotFoundException(
                        "Author with id %s not found".formatted(bookCreateDto.getAuthorId()))))
                .flatMap(author -> 
                    genreRepository.findAllByIds(bookCreateDto.getGenreIds())
                            .collectList()
                            .flatMap(genres -> {
                                if (isEmpty(genres) || bookCreateDto.getGenreIds().size() != genres.size()) {
                                    return Mono.error(new EntityNotFoundException(
                                            "One or all genres with ids %s not found"
                                                    .formatted(bookCreateDto.getGenreIds())));
                                }
                                var book = new Book(null, bookCreateDto.getTitle(), author, genres);
                                return bookRepository.save(book);
                            })
                )
                .map(this::toBookDto);
    }

    @Override
    public Mono<BookDto> update(BookUpdateDto bookUpdateDto) {
        return bookRepository.findById(bookUpdateDto.getId())
                .switchIfEmpty(Mono.error(new EntityNotFoundException(
                        "Book with id %s not found".formatted(bookUpdateDto.getId()))))
                .flatMap(existingBook -> updateBookWithAuthorAndGenres(bookUpdateDto))
                .map(this::toBookDto);
    }

    private Mono<Book> updateBookWithAuthorAndGenres(BookUpdateDto bookUpdateDto) {
        return authorRepository.findById(bookUpdateDto.getAuthorId())
                .switchIfEmpty(Mono.error(new EntityNotFoundException(
                        "Author with id %s not found".formatted(bookUpdateDto.getAuthorId()))))
                .flatMap(author -> updateBookWithGenres(bookUpdateDto, author));
    }

    private Mono<Book> updateBookWithGenres(BookUpdateDto bookUpdateDto, 
                                           ru.otus.hw.models.Author author) {
        return genreRepository.findAllByIds(bookUpdateDto.getGenreIds())
                .collectList()
                .flatMap(genres -> {
                    if (isEmpty(genres) || bookUpdateDto.getGenreIds().size() != genres.size()) {
                        return Mono.error(new EntityNotFoundException(
                                "One or all genres with ids %s not found"
                                        .formatted(bookUpdateDto.getGenreIds())));
                    }
                    var book = new Book(bookUpdateDto.getId(), bookUpdateDto.getTitle(), 
                                       author, genres);
                    return bookRepository.save(book);
                });
    }

    private BookDto toBookDto(Book book) {
        AuthorDto authorDto = new AuthorDto(book.getAuthor().getId(), book.getAuthor().getFullName());
        var genreDtos = book.getGenres().stream()
                .map(genre -> new GenreDto(genre.getId(), genre.getName()))
                .toList();
        return new BookDto(book.getId(), book.getTitle(), authorDto, genreDtos);
    }

    @Override
    public Mono<Void> deleteById(String id) {
        return bookRepository.deleteById(id);
    }
}
