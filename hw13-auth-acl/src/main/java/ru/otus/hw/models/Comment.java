package ru.otus.hw.models;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.data.mongodb.core.mapping.Document;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Document(collection = "comments")
public class Comment {
    @Id
    private String id;

    private String text;

    @DBRef(lazy = true)
    private Book book;

    @DBRef(lazy = true)
    private User user;

    public Comment(String text, Book book, User user) {
        this.text = text;
        this.book = book;
        this.user = user;
    }

    // Backward-compatible constructor for tests
    public Comment(String id, String text, Book book) {
        this.id = id;
        this.text = text;
        this.book = book;
        this.user = null;
    }
}