package ru.otus.hw.models;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Document(collection = "comments")
public class Comment {
    @Id
    private String id;

    private String text;

    private String bookId;
    
    // Transient field for convenience - not stored in MongoDB
    @org.springframework.data.annotation.Transient
    private Book book;
    
    // Convenient constructor for tests and services
    public Comment(String id, String text, String bookId) {
        this.id = id;
        this.text = text;
        this.bookId = bookId;
        this.book = null;
    }
}