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
        Object idObject = event.getDocument().get("_id");
        if (idObject != null) {
            String bookId = idObject.toString();
            log.info("Deleting comments for book with id: {}", bookId);
            commentRepository.deleteByBookId(bookId);
            log.info("Comments deleted for book with id: {}", bookId);
        }
    }
}