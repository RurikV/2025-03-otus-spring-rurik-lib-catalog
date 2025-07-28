package ru.otus.hw.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CommentUpdateDto {
    @NotEmpty(message = "ID is required")
    private String id;
    
    @NotBlank(message = "Comment text is required and cannot be empty")
    private String text;
}