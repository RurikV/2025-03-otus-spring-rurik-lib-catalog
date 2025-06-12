package ru.otus.hw.repositories;

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
        return Optional.ofNullable(
                em.createQuery(
                        "select b from Book b " +
                        "left join fetch b.author " +
                        "left join fetch b.genres " +
                        "where b.id = :id", Book.class)
                .setParameter("id", id)
                .getResultList()
                .stream().findFirst().orElse(null)
        );
    }

    @Override
    public List<Book> findAll() {
        return em.createQuery(
                "select distinct b from Book b " +
                "left join fetch b.author " +
                "left join fetch b.genres", Book.class)
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
        em.createQuery("delete from Book b where b.id = :id")
                .setParameter("id", id)
                .executeUpdate();
    }
}