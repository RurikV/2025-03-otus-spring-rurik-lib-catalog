package ru.otus.hw.services;

import ru.otus.hw.dto.BookCreateDto;
import ru.otus.hw.dto.BookUpdateDto;
import ru.otus.hw.models.Book;

import java.util.List;

public interface BookService {
    Book findById(String id);

    List<Book> findAll();

    Book create(BookCreateDto bookCreateDto);

    Book update(BookUpdateDto bookUpdateDto);

    void deleteById(String id);
}
