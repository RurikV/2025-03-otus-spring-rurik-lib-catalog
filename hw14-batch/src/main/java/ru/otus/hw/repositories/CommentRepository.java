package ru.otus.hw.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.otus.hw.models.jpa.Comment;

@Repository
public interface CommentRepository extends JpaRepository<Comment, Long> {
}