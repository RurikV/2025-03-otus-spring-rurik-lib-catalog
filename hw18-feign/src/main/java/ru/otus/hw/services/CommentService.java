package ru.otus.hw.services;

import ru.otus.hw.dto.CommentCreateDto;
import ru.otus.hw.dto.CommentUpdateDto;
import ru.otus.hw.models.Comment;

import java.util.List;

public interface CommentService {
    Comment findById(String id);

    List<Comment> findByBookId(String bookId);

    Comment create(CommentCreateDto commentCreateDto);

    Comment update(CommentUpdateDto commentUpdateDto);

    void deleteById(String id);
}