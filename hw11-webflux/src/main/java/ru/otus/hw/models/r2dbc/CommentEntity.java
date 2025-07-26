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
@Table("comments")
public class CommentEntity {
    @Id
    private Long id;

    private String text;

    @Column("book_id")
    private Long bookId;
}