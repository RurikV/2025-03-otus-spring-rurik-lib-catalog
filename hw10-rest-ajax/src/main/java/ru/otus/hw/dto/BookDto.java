package ru.otus.hw.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import ru.otus.hw.models.Author;
import ru.otus.hw.models.Genre;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class BookDto {
    private String id;
    
    private String title;
    
    private Author author;
    
    private List<Genre> genres;
}