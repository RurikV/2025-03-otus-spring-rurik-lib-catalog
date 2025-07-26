package ru.otus.hw.models.r2dbc;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Table("books")
public class BookEntity {
    @Id
    private Long id;

    private String title;

    @Column("author_id")
    private Long authorId;
}