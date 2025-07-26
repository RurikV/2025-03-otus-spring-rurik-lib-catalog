package ru.otus.hw.listeners;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.mongodb.core.mapping.event.AbstractMongoEventListener;
import org.springframework.data.mongodb.core.mapping.event.BeforeDeleteEvent;
import org.springframework.stereotype.Component;
import ru.otus.hw.models.Book;
import ru.otus.hw.repositories.CommentRepository;

@Slf4j
@Component
@RequiredArgsConstructor
public class BookDeleteListener extends AbstractMongoEventListener<Book> {

    private final CommentRepository commentRepository;

    @Override
    public void onBeforeDelete(BeforeDeleteEvent<Book> event) {
        if (event.getDocument() != null) {
            Object idObject = event.getDocument().get("_id");
            if (idObject != null) {
                String bookId = idObject.toString();
                log.info("Deleting comments for book with id: {}", bookId);
                try {
                    commentRepository.deleteByBookId(bookId)
                            .doOnSuccess(unused -> log.info("Comments deleted for book with id: {}", bookId))
                            .doOnError(error -> log.error("Failed to delete comments for book with id: {}", 
                                    bookId, error))
                            .block();
                } catch (Exception e) {
                    log.error("Failed to delete comments for book with id: {}", bookId, e);
                }
            }
        }
    }
}