package ru.otus.hw.repositories;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Repository;
import ru.otus.hw.models.jpa.Book;

import java.util.List;

@Repository
public interface BookRepository extends JpaRepository<Book, Long> {
    
    @Override
    @EntityGraph(attributePaths = {"author", "genres"})
    @NonNull
    List<Book> findAll();
    
    Book findByTitle(String title);
}