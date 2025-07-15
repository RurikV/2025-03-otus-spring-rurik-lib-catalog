package ru.otus.hw.listeners;

import lombok.RequiredArgsConstructor;
import org.springframework.data.mongodb.core.mapping.event.AbstractMongoEventListener;
import org.springframework.data.mongodb.core.mapping.event.BeforeDeleteEvent;
import org.springframework.stereotype.Component;
import ru.otus.hw.models.Book;
import ru.otus.hw.repositories.CommentRepository;

@Component
@RequiredArgsConstructor
public class BookCascadeDeleteListener extends AbstractMongoEventListener<Book> {

    private final CommentRepository commentRepository;

    @Override
    public void onBeforeDelete(BeforeDeleteEvent<Book> event) {
        if (event.getDocument() != null) {
            Object bookIdObj = event.getDocument().get("_id");
            if (bookIdObj != null) {
                String bookId = bookIdObj.toString();
                commentRepository.deleteByBookId(bookId);
            }
        }
    }
}
