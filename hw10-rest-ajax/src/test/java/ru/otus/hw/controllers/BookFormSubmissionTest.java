package ru.otus.hw.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureWebMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import ru.otus.hw.dto.BookCreateDto;
import ru.otus.hw.dto.BookUpdateDto;
import ru.otus.hw.services.AuthorService;
import ru.otus.hw.services.BookService;
import ru.otus.hw.services.GenreService;

import java.util.Set;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureWebMvc
@DisplayName("Book REST API Test should")
class BookFormSubmissionTest {

    @Autowired
    private WebApplicationContext webApplicationContext;

    @Autowired
    private BookService bookService;

    @Autowired
    private AuthorService authorService;

    @Autowired
    private GenreService genreService;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @DisplayName("handle POST request to create new book")
    void shouldHandlePostRequestToCreateNewBook() throws Exception {
        MockMvc mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();
        
        var authors = authorService.findAll();
        var genres = genreService.findAll();
        
        if (authors.isEmpty() || genres.isEmpty()) {
            throw new RuntimeException("No authors or genres found in database");
        }
        
        var createDto = new BookCreateDto("New Test Book", authors.get(0).getId(), Set.of(genres.get(0).getId()));
        
        mockMvc.perform(post("/api/books")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(createDto)))
                .andExpect(status().isCreated())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.title").value("New Test Book"));
    }

    @Test
    @DisplayName("handle PUT request to update existing book")
    void shouldHandlePostRequestToUpdateExistingBook() throws Exception {
        MockMvc mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();
        
        var books = bookService.findAll();
        var authors = authorService.findAll();
        var genres = genreService.findAll();
        
        if (books.isEmpty() || authors.isEmpty() || genres.isEmpty()) {
            throw new RuntimeException("No books, authors or genres found in database");
        }
        
        var existingBook = books.get(0);
        var updateDto = new BookUpdateDto(existingBook.getId(), "Updated Test Book", authors.get(0).getId(), Set.of(genres.get(0).getId()));
        
        mockMvc.perform(put("/api/books/" + existingBook.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateDto)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.title").value("Updated Test Book"));
    }

    @Test
    @DisplayName("return 405 Method Not Allowed for unsupported HTTP method")
    void shouldReturn405ForUnsupportedHttpMethod() throws Exception {
        MockMvc mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();
        
        // Try to PUT to an endpoint that doesn't support PUT (page controller endpoint)
        mockMvc.perform(put("/books/some-id")
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .param("title", "Test Book"))
                .andExpect(status().isMethodNotAllowed())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.status").value(405))
                .andExpect(jsonPath("$.error").value("Method Not Allowed"));
    }

    @Test
    @DisplayName("handle validation errors gracefully")
    void shouldHandleFormValidationErrorsGracefully() throws Exception {
        MockMvc mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();
        
        // Try to create book with invalid data
        var invalidCreateDto = new BookCreateDto("", "invalid-author-id", Set.of("invalid-genre-id"));
        
        mockMvc.perform(post("/api/books")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidCreateDto)))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.status").value(400));
    }

    @Test
    @DisplayName("handle update validation errors gracefully")
    void shouldHandleUpdateValidationErrorsGracefully() throws Exception {
        MockMvc mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();
        
        var books = bookService.findAll();
        if (books.isEmpty()) {
            throw new RuntimeException("No books found in database");
        }
        
        var existingBook = books.get(0);
        var invalidUpdateDto = new BookUpdateDto(existingBook.getId(), "", "invalid-author-id", Set.of("invalid-genre-id"));
        
        // Try to update book with invalid data
        mockMvc.perform(put("/api/books/" + existingBook.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidUpdateDto)))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.status").value(400));
    }
}