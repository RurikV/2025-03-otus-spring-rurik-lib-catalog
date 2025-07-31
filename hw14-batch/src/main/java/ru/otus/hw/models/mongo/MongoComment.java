package ru.otus.hw.models.mongo;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "comments")
public class MongoComment {
    @Id
    private String id;

    private String text;

    @DBRef(lazy = true)
    private MongoBook book;

    public MongoComment() {
    }

    public MongoComment(String id, String text, MongoBook book) {
        this.id = id;
        this.text = text;
        this.book = book;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public MongoBook getBook() {
        return book;
    }

    public void setBook(MongoBook book) {
        this.book = book;
    }
}