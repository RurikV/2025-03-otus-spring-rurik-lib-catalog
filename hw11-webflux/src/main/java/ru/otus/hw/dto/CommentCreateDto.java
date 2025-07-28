package ru.otus.hw.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.validation.constraints.NotBlank;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CommentCreateDto {
    @NotBlank(message = "Comment text is required and cannot be empty")
    private String text;
    
    @NotBlank(message = "Book ID is required")
    private String bookId;
}