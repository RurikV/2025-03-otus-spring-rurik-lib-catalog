package ru.otus.hw.repositories;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.stereotype.Repository;
import ru.otus.hw.models.Comment;

import java.util.List;
import java.util.Optional;

@Repository
public class JpaCommentRepository implements CommentRepository {

    @PersistenceContext
    private EntityManager em;

    @Override
    public Optional<Comment> findById(long id) {
        return Optional.ofNullable(
                em.createQuery(
                        "select c from Comment c " +
                        "left join fetch c.book " +
                        "where c.id = :id", Comment.class)
                .setParameter("id", id)
                .getResultList()
                .stream().findFirst().orElse(null)
        );
    }

    @Override
    public List<Comment> findAllByBookId(long bookId) {
        return em.createQuery(
                "select c from Comment c " +
                "where c.book.id = :bookId", Comment.class)
                .setParameter("bookId", bookId)
                .getResultList();
    }

    @Override
    public Comment save(Comment comment) {
        if (comment.getId() == 0) {
            em.persist(comment);
            return comment;
        }
        return em.merge(comment);
    }

    @Override
    public void deleteById(long id) {
        em.createQuery("delete from Comment c where c.id = :id")
                .setParameter("id", id)
                .executeUpdate();
    }
}