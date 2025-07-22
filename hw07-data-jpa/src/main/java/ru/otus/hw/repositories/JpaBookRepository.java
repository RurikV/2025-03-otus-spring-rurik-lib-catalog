package ru.otus.hw.repositories;

import jakarta.persistence.EntityGraph;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.stereotype.Repository;
import ru.otus.hw.models.Book;

import java.util.List;
import java.util.Optional;

@Repository
public class JpaBookRepository implements BookRepository {

    @PersistenceContext
    private EntityManager em;

    @Override
    public Optional<Book> findById(long id) {
        EntityGraph<Book> entityGraph = em.createEntityGraph(Book.class);
        entityGraph.addAttributeNodes("author", "genres");

        return em.createQuery("select b from Book b where b.id = :id", Book.class)
                .setParameter("id", id)
                .setHint("jakarta.persistence.fetchgraph", entityGraph)
                .getResultList()
                .stream().findFirst();
    }

    @Override
    public List<Book> findAll() {
        EntityGraph<Book> entityGraph = em.createEntityGraph(Book.class);
        entityGraph.addAttributeNodes("author");

        return em.createQuery("select b from Book b", Book.class)
                .setHint("jakarta.persistence.fetchgraph", entityGraph)
                .getResultList();
    }

    @Override
    public Book save(Book book) {
        if (book.getId() == 0) {
            em.persist(book);
            return book;
        }
        return em.merge(book);
    }

    @Override
    public void deleteById(long id) {
        Book book = em.find(Book.class, id);
        if (book != null) {
            em.remove(book);
        }
    }
}
