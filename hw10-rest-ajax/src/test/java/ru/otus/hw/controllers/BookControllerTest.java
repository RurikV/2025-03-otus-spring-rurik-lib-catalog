package ru.otus.hw.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.MessageSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.context.support.ResourceBundleMessageSource;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import ru.otus.hw.dto.BookCreateDto;
import ru.otus.hw.dto.BookDto;
import ru.otus.hw.dto.BookUpdateDto;
import ru.otus.hw.exceptions.EntityNotFoundException;
import ru.otus.hw.models.Author;
import ru.otus.hw.models.Genre;
import ru.otus.hw.services.AuthorService;
import ru.otus.hw.services.BookService;
import ru.otus.hw.services.CommentService;
import ru.otus.hw.services.GenreService;

import java.util.List;
import java.util.Set;

import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(BookController.class)
@Import(BookControllerTest.TestConfig.class)
@DisplayName("BookController should")
class BookControllerTest {

    @Autowired
    private MockMvc mvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private BookService bookService;

    @MockBean
    private AuthorService authorService;

    @MockBean
    private GenreService genreService;

    @MockBean
    private CommentService commentService;

    @Test
    @DisplayName("return all books as JSON")
    void shouldReturnAllBooksAsJson() throws Exception {
        var author = new Author("1", "Author Name");
        var genre = new Genre("1", "Genre Name");
        var bookDto = new BookDto("1", "Book Title", author, List.of(genre));
        
        given(bookService.findAll()).willReturn(List.of(bookDto));

        mvc.perform(get("/api/books")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].id").value("1"))
                .andExpect(jsonPath("$[0].title").value("Book Title"));
    }

    @Test
    @DisplayName("return book by id as JSON")
    void shouldReturnBookByIdAsJson() throws Exception {
        var author = new Author("1", "Author Name");
        var genre = new Genre("1", "Genre Name");
        var bookDto = new BookDto("1", "Book Title", author, List.of(genre));
        
        given(bookService.findById("1")).willReturn(bookDto);

        mvc.perform(get("/api/books/1")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value("1"))
                .andExpect(jsonPath("$.title").value("Book Title"))
                .andExpect(jsonPath("$.author.name").value("Author Name"));
    }

    @Test
    @DisplayName("return 404 when book not found")
    void shouldReturn404WhenBookNotFound() throws Exception {
        given(bookService.findById("1")).willThrow(new EntityNotFoundException("Book with id 1 not found"));

        mvc.perform(get("/api/books/1")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("create new book via REST API")
    void shouldCreateNewBookViaRestApi() throws Exception {
        var author = new Author("1", "Author Name");
        var genre = new Genre("1", "Genre Name");
        var bookDto = new BookDto("1", "Book Title", author, List.of(genre));
        var createDto = new BookCreateDto("Book Title", "1", Set.of("1"));
        
        given(bookService.create(createDto)).willReturn(bookDto);

        mvc.perform(post("/api/books")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(createDto)))
                .andExpect(status().isCreated())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value("1"))
                .andExpect(jsonPath("$.title").value("Book Title"))
                .andExpect(jsonPath("$.author.name").value("Author Name"));

        verify(bookService).create(createDto);
    }

    @Test
    @DisplayName("create new book without genres via REST API")
    void shouldCreateNewBookWithoutGenresViaRestApi() throws Exception {
        var author = new Author("1", "Author Name");
        var bookDto = new BookDto("1", "Book Title", author, List.of());
        var createDto = new BookCreateDto("Book Title", "1", Set.of());
        
        given(bookService.create(createDto)).willReturn(bookDto);

        mvc.perform(post("/api/books")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(createDto)))
                .andExpect(status().isCreated())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value("1"))
                .andExpect(jsonPath("$.title").value("Book Title"));

        verify(bookService).create(createDto);
    }

    @Test
    @DisplayName("update book via REST API")
    void shouldUpdateBookViaRestApi() throws Exception {
        var author = new Author("1", "Author Name");
        var genre = new Genre("1", "Genre Name");
        var bookDto = new BookDto("1", "Updated Title", author, List.of(genre));
        var updateDto = new BookUpdateDto("1", "Updated Title", "1", Set.of("1"));
        
        given(bookService.update(updateDto)).willReturn(bookDto);

        mvc.perform(put("/api/books/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateDto)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value("1"))
                .andExpect(jsonPath("$.title").value("Updated Title"))
                .andExpect(jsonPath("$.author.name").value("Author Name"));

        verify(bookService).update(updateDto);
    }

    @Test
    @DisplayName("delete book via REST API")
    void shouldDeleteBookViaRestApi() throws Exception {
        mvc.perform(delete("/api/books/1")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNoContent());

        verify(bookService).deleteById("1");
    }

    @Test
    @DisplayName("return 404 when author not found during book creation")
    void shouldReturn404WhenAuthorNotFoundDuringBookCreation() throws Exception {
        var createDto = new BookCreateDto("Book Title", "nonexistent-author", Set.of("1"));
        
        given(bookService.create(createDto)).willThrow(new EntityNotFoundException("Author with id nonexistent-author not found"));

        mvc.perform(post("/api/books")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(createDto)))
                .andExpect(status().isNotFound());

        verify(bookService).create(createDto);
    }

    @Test
    @DisplayName("return 404 when genre not found during book creation")
    void shouldReturn404WhenGenreNotFoundDuringBookCreation() throws Exception {
        var createDto = new BookCreateDto("Book Title", "1", Set.of("nonexistent-genre"));
        
        given(bookService.create(createDto)).willThrow(new EntityNotFoundException("One or all genres with ids [nonexistent-genre] not found"));

        mvc.perform(post("/api/books")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(createDto)))
                .andExpect(status().isNotFound());

        verify(bookService).create(createDto);
    }

    @Test
    @DisplayName("return 404 when author not found during book update")
    void shouldReturn404WhenAuthorNotFoundDuringBookUpdate() throws Exception {
        var updateDto = new BookUpdateDto("1", "Updated Title", "nonexistent-author", Set.of("1"));
        
        given(bookService.update(updateDto)).willThrow(new EntityNotFoundException("Author with id nonexistent-author not found"));

        mvc.perform(put("/api/books/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateDto)))
                .andExpect(status().isNotFound());

        verify(bookService).update(updateDto);
    }

    @Test
    @DisplayName("return 404 when genre not found during book update")
    void shouldReturn404WhenGenreNotFoundDuringBookUpdate() throws Exception {
        var updateDto = new BookUpdateDto("1", "Updated Title", "1", Set.of("nonexistent-genre"));
        
        given(bookService.update(updateDto)).willThrow(new EntityNotFoundException("One or all genres with ids [nonexistent-genre] not found"));

        mvc.perform(put("/api/books/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateDto)))
                .andExpect(status().isNotFound());

        verify(bookService).update(updateDto);
    }

    @TestConfiguration
    static class TestConfig {
        @Bean
        public MessageSource messageSource() {
            ResourceBundleMessageSource messageSource = new ResourceBundleMessageSource();
            messageSource.setBasename("messages");
            messageSource.setDefaultEncoding("UTF-8");
            return messageSource;
        }
    }
}