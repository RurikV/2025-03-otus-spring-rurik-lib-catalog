package ru.otus.hw.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import ru.otus.hw.validation.NotBlankElements;

import java.util.Set;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class BookUpdateDto {
    @NotEmpty(message = "ID is required")
    private String id;
    
    @NotBlank(message = "Title is required and cannot be empty")
    private String title;
    
    @NotBlank(message = "Author is required")
    private String authorId;
    
    @NotBlankElements(message = "Genre IDs cannot be blank")
    private Set<String> genreIds;
}