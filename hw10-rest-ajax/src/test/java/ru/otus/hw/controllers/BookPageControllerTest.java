package ru.otus.hw.controllers;

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
import org.springframework.test.web.servlet.MockMvc;
import ru.otus.hw.dto.BookDto;
import ru.otus.hw.models.Author;
import ru.otus.hw.models.Genre;
import ru.otus.hw.services.AuthorService;
import ru.otus.hw.services.BookService;
import ru.otus.hw.services.CommentService;
import ru.otus.hw.services.GenreService;

import java.util.List;

import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(BookPageController.class)
@Import(BookPageControllerTest.TestConfig.class)
@DisplayName("BookPageController should")
class BookPageControllerTest {

    @Autowired
    private MockMvc mvc;

    @MockBean
    private AuthorService authorService;

    @MockBean
    private GenreService genreService;

    @MockBean
    private BookService bookService;

    @MockBean
    private CommentService commentService;

    @Test
    @DisplayName("return books list page for root endpoint")
    void shouldReturnBooksListPageForRootEndpoint() throws Exception {
        mvc.perform(get("/"))
                .andExpect(status().isOk())
                .andExpect(view().name("book/list"));
    }

    @Test
    @DisplayName("return books list page for /books endpoint")
    void shouldReturnBooksListPageForBooksEndpoint() throws Exception {
        mvc.perform(get("/books"))
                .andExpect(status().isOk())
                .andExpect(view().name("book/list"));
    }

    @Test
    @DisplayName("return book view page with bookId in model")
    void shouldReturnBookViewPageWithBookIdInModel() throws Exception {
        var author = new Author("1", "Author Name");
        var genre = new Genre("1", "Genre Name");
        var bookDto = new BookDto("1", "Book Title", author, List.of(genre));
        
        given(bookService.findById("1")).willReturn(bookDto);
        given(commentService.findByBookId("1")).willReturn(List.of());
        
        mvc.perform(get("/books/1"))
                .andExpect(status().isOk())
                .andExpect(view().name("book/view"))
                .andExpect(model().attribute("bookId", "1"));
    }

    @Test
    @DisplayName("return new book form page with authors and genres")
    void shouldReturnNewBookFormPageWithAuthorsAndGenres() throws Exception {
        var author = new Author("1", "Author Name");
        var genre = new Genre("1", "Genre Name");
        
        given(authorService.findAll()).willReturn(List.of(author));
        given(genreService.findAll()).willReturn(List.of(genre));

        mvc.perform(get("/books/new"))
                .andExpect(status().isOk())
                .andExpect(view().name("book/form"))
                .andExpect(model().attributeExists("authors"))
                .andExpect(model().attributeExists("genres"))
                .andExpect(model().attribute("authors", List.of(author)))
                .andExpect(model().attribute("genres", List.of(genre)));
    }

    @Test
    @DisplayName("return edit book form page with authors, genres and bookId")
    void shouldReturnEditBookFormPageWithAuthorsGenresAndBookId() throws Exception {
        var author = new Author("1", "Author Name");
        var genre = new Genre("1", "Genre Name");
        var bookDto = new BookDto("1", "Book Title", author, List.of(genre));
        
        given(authorService.findAll()).willReturn(List.of(author));
        given(genreService.findAll()).willReturn(List.of(genre));
        given(bookService.findById("1")).willReturn(bookDto);

        mvc.perform(get("/books/1/edit"))
                .andExpect(status().isOk())
                .andExpect(view().name("book/form"))
                .andExpect(model().attributeExists("authors"))
                .andExpect(model().attributeExists("genres"))
                .andExpect(model().attributeExists("bookId"))
                .andExpect(model().attribute("authors", List.of(author)))
                .andExpect(model().attribute("genres", List.of(genre)))
                .andExpect(model().attribute("bookId", "1"));
    }

    @Test
    @DisplayName("return delete confirmation page with bookId in model")
    void shouldReturnDeleteConfirmationPageWithBookIdInModel() throws Exception {
        mvc.perform(get("/books/1/delete"))
                .andExpect(status().isOk())
                .andExpect(view().name("book/delete"))
                .andExpect(model().attribute("bookId", "1"));
    }

    @Test
    @DisplayName("handle different book IDs correctly")
    void shouldHandleDifferentBookIdsCorrectly() throws Exception {
        var author = new Author("1", "Author Name");
        var genre = new Genre("1", "Genre Name");
        var bookDto123 = new BookDto("123", "Book Title 123", author, List.of(genre));
        var bookDtoAbc = new BookDto("abc", "Book Title abc", author, List.of(genre));
        
        given(bookService.findById("123")).willReturn(bookDto123);
        given(bookService.findById("abc")).willReturn(bookDtoAbc);
        given(commentService.findByBookId("123")).willReturn(List.of());
        given(commentService.findByBookId("abc")).willReturn(List.of());
        given(authorService.findAll()).willReturn(List.of(author));
        given(genreService.findAll()).willReturn(List.of(genre));
        
        // Test with different book ID
        mvc.perform(get("/books/123"))
                .andExpect(status().isOk())
                .andExpect(view().name("book/view"))
                .andExpect(model().attribute("bookId", "123"));

        mvc.perform(get("/books/abc/edit"))
                .andExpect(status().isOk())
                .andExpect(view().name("book/form"))
                .andExpect(model().attribute("bookId", "abc"));

        mvc.perform(get("/books/xyz/delete"))
                .andExpect(status().isOk())
                .andExpect(view().name("book/delete"))
                .andExpect(model().attribute("bookId", "xyz"));
    }

    @Test
    @DisplayName("call services correctly for form pages")
    void shouldCallServicesCorrectlyForFormPages() throws Exception {
        var authors = List.of(new Author("1", "Author 1"), new Author("2", "Author 2"));
        var genres = List.of(new Genre("1", "Genre 1"), new Genre("2", "Genre 2"));
        var bookDto = new BookDto("1", "Book Title", authors.get(0), genres);
        
        given(authorService.findAll()).willReturn(authors);
        given(genreService.findAll()).willReturn(genres);
        given(bookService.findById("1")).willReturn(bookDto);

        // Test new book form
        mvc.perform(get("/books/new"))
                .andExpect(status().isOk())
                .andExpect(model().attribute("authors", authors))
                .andExpect(model().attribute("genres", genres));

        // Test edit book form
        mvc.perform(get("/books/1/edit"))
                .andExpect(status().isOk())
                .andExpect(model().attribute("authors", authors))
                .andExpect(model().attribute("genres", genres));
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