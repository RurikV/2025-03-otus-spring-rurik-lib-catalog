package ru.otus.hw.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.otus.hw.models.jpa.Comment;
import ru.otus.hw.models.jpa.Book;

@Repository
public interface CommentRepository extends JpaRepository<Comment, Long> {
    Comment findByTextAndBook(String text, Book book);
}