package ru.otus.hw.services;

import ru.otus.hw.models.Comment;

import java.util.List;

public interface CommentService {
    Comment findById(String id);

    List<Comment> findByBookId(String bookId);

    Comment insert(String text, String bookId);

    Comment update(String id, String text);

    void deleteById(String id);
}