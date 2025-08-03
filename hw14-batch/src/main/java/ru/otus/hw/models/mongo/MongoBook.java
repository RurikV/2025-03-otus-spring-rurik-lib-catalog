package ru.otus.hw.models.mongo;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.List;

@Document(collection = "books")
public class MongoBook {
    @Id
    private String id;

    private String title;

    private MongoAuthor author;

    private List<MongoGenre> genres;

    public MongoBook() {
    }

    public MongoBook(String id, String title, MongoAuthor author, List<MongoGenre> genres) {
        this.id = id;
        this.title = title;
        this.author = author;
        this.genres = genres;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public MongoAuthor getAuthor() {
        return author;
    }

    public void setAuthor(MongoAuthor author) {
        this.author = author;
    }

    public List<MongoGenre> getGenres() {
        return genres;
    }

    public void setGenres(List<MongoGenre> genres) {
        this.genres = genres;
    }
}