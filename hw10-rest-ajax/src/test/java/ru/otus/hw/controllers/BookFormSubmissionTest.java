package ru.otus.hw.controllers;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureWebMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import ru.otus.hw.services.AuthorService;
import ru.otus.hw.services.BookService;
import ru.otus.hw.services.GenreService;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureWebMvc
@DisplayName("Book Form Submission Test should")
class BookFormSubmissionTest {

    @Autowired
    private WebApplicationContext webApplicationContext;

    @Autowired
    private BookService bookService;

    @Autowired
    private AuthorService authorService;

    @Autowired
    private GenreService genreService;

    @Test
    @DisplayName("handle POST request to create new book")
    void shouldHandlePostRequestToCreateNewBook() throws Exception {
        MockMvc mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();
        
        var authors = authorService.findAll();
        var genres = genreService.findAll();
        
        if (authors.isEmpty() || genres.isEmpty()) {
            throw new RuntimeException("No authors or genres found in database");
        }
        
        mockMvc.perform(post("/books")
                .param("title", "New Test Book")
                .param("authorId", authors.get(0).getId())
                .param("genreIds", genres.get(0).getId()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/"));
    }

    @Test
    @DisplayName("handle POST request to update existing book")
    void shouldHandlePostRequestToUpdateExistingBook() throws Exception {
        MockMvc mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();
        
        var books = bookService.findAll();
        var authors = authorService.findAll();
        var genres = genreService.findAll();
        
        if (books.isEmpty() || authors.isEmpty() || genres.isEmpty()) {
            throw new RuntimeException("No books, authors or genres found in database");
        }
        
        var existingBook = books.get(0);
        
        mockMvc.perform(post("/books/" + existingBook.getId())
                .param("title", "Updated Test Book")
                .param("authorId", authors.get(0).getId())
                .param("genreIds", genres.get(0).getId()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/books/" + existingBook.getId()));
    }

    @Test
    @DisplayName("return 405 Method Not Allowed for unsupported HTTP method")
    void shouldReturn405ForUnsupportedHttpMethod() throws Exception {
        MockMvc mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();
        
        // Try to PUT to an endpoint that doesn't support PUT (non-API endpoint)
        mockMvc.perform(put("/books/some-id")
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .param("title", "Test Book"))
                .andExpect(status().isMethodNotAllowed())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.status").value(405))
                .andExpect(jsonPath("$.error").value("Method Not Allowed"));
    }

    @Test
    @DisplayName("handle form validation errors gracefully")
    void shouldHandleFormValidationErrorsGracefully() throws Exception {
        MockMvc mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();
        
        // Try to create book with missing required fields
        mockMvc.perform(post("/books")
                .param("title", "")  // Empty title
                .param("authorId", "invalid-author-id")
                .param("genreIds", "invalid-genre-id"))
                .andExpect(status().isOk())  // Should return to form with error
                .andExpect(view().name("book/form"))
                .andExpect(model().attributeExists("error"));
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
        
        // Try to update book with invalid data
        mockMvc.perform(post("/books/" + existingBook.getId())
                .param("title", "")  // Empty title
                .param("authorId", "invalid-author-id")
                .param("genreIds", "invalid-genre-id"))
                .andExpect(status().isOk())  // Should return to form with error
                .andExpect(view().name("book/form"))
                .andExpect(model().attributeExists("error"));
    }
}