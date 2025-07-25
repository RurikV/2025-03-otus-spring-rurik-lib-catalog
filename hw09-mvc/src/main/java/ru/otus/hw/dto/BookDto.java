package ru.otus.hw.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import jakarta.validation.constraints.NotBlank;

import java.util.Set;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class BookDto {
    @NotBlank(message = "Title is required and cannot be empty")
    private String title;
    
    @NotBlank(message = "Author is required")
    private String authorId;
    
    private Set<String> genreIds;
}