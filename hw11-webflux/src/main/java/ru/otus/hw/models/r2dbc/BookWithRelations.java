package ru.otus.hw.models.r2dbc;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class BookWithRelations {
    private Long id;

    private String title;

    private AuthorEntity author;

    private List<GenreEntity> genres;
}