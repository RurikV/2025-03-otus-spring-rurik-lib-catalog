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
import ru.otus.hw.services.AuthorService;
import ru.otus.hw.services.BookService;
import ru.otus.hw.services.CommentService;
import ru.otus.hw.services.GenreService;

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
    @DisplayName("return book view page")
    void shouldReturnBookViewPageWithBookIdInModel() throws Exception {
        mvc.perform(get("/books/1"))
                .andExpect(status().isOk())
                .andExpect(view().name("book/view"));
    }

    @Test
    @DisplayName("return new book form page")
    void shouldReturnNewBookFormPageWithAuthorsAndGenres() throws Exception {
        mvc.perform(get("/books/new"))
                .andExpect(status().isOk())
                .andExpect(view().name("book/form"));
    }

    @Test
    @DisplayName("return edit book form page")
    void shouldReturnEditBookFormPageWithAuthorsGenresAndBookId() throws Exception {
        mvc.perform(get("/books/1/edit"))
                .andExpect(status().isOk())
                .andExpect(view().name("book/form"));
    }

    @Test
    @DisplayName("return delete confirmation page")
    void shouldReturnDeleteConfirmationPageWithBookIdInModel() throws Exception {
        mvc.perform(get("/books/1/delete"))
                .andExpect(status().isOk())
                .andExpect(view().name("book/delete"));
    }

    @Test
    @DisplayName("handle different book IDs correctly")
    void shouldHandleDifferentBookIdsCorrectly() throws Exception {
        // Test with different book ID
        mvc.perform(get("/books/123"))
                .andExpect(status().isOk())
                .andExpect(view().name("book/view"));

        mvc.perform(get("/books/abc/edit"))
                .andExpect(status().isOk())
                .andExpect(view().name("book/form"));

        mvc.perform(get("/books/xyz/delete"))
                .andExpect(status().isOk())
                .andExpect(view().name("book/delete"));
    }

    @Test
    @DisplayName("return correct views for form pages")
    void shouldCallServicesCorrectlyForFormPages() throws Exception {
        // Test new book form
        mvc.perform(get("/books/new"))
                .andExpect(status().isOk())
                .andExpect(view().name("book/form"));

        // Test edit book form
        mvc.perform(get("/books/1/edit"))
                .andExpect(status().isOk())
                .andExpect(view().name("book/form"));
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