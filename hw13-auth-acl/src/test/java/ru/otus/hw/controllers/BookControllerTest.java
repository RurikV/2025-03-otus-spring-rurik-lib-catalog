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
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import ru.otus.hw.dto.BookCreateDto;
import ru.otus.hw.dto.BookUpdateDto;
import ru.otus.hw.exceptions.EntityNotFoundException;
import ru.otus.hw.models.Author;
import ru.otus.hw.models.Book;
import ru.otus.hw.models.Comment;
import ru.otus.hw.models.Genre;
import ru.otus.hw.services.AuthorService;
import ru.otus.hw.services.BookService;
import ru.otus.hw.services.CommentService;
import ru.otus.hw.services.GenreService;

import java.util.List;
import java.util.Set;

import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(BookController.class)
@Import(BookControllerTest.TestConfig.class)
@WithMockUser
@DisplayName("BookController should")
class BookControllerTest {

    @Autowired
    private MockMvc mvc;

    @MockBean
    private BookService bookService;

    @MockBean
    private AuthorService authorService;

    @MockBean
    private GenreService genreService;

    @MockBean
    private CommentService commentService;

    @Test
    @DisplayName("return books list page")
    void shouldReturnBooksListPage() throws Exception {
        var author = new Author("1", "Author Name");
        var genre = new Genre("1", "Genre Name");
        var book = new Book("1", "Book Title", author, List.of(genre));
        
        given(bookService.findAll()).willReturn(List.of(book));

        mvc.perform(get("/"))
                .andExpect(status().isOk())
                .andExpect(view().name("book/list"))
                .andExpect(model().attributeExists("books"));
    }

    @Test
    @DisplayName("return books list page for /books endpoint")
    void shouldReturnBooksListPageForBooksEndpoint() throws Exception {
        given(bookService.findAll()).willReturn(List.of());

        mvc.perform(get("/books"))
                .andExpect(status().isOk())
                .andExpect(view().name("book/list"));
    }

    @Test
    @DisplayName("return book view page")
    void shouldReturnBookViewPage() throws Exception {
        var author = new Author("1", "Author Name");
        var genre = new Genre("1", "Genre Name");
        var book = new Book("1", "Book Title", author, List.of(genre));
        var comment = new Comment("1", "Comment text", book);
        
        given(bookService.findById("1")).willReturn(book);
        given(commentService.findByBookId("1")).willReturn(List.of(comment));

        mvc.perform(get("/books/1"))
                .andExpect(status().isOk())
                .andExpect(view().name("book/view"))
                .andExpect(model().attributeExists("book"))
                .andExpect(model().attributeExists("comments"));
    }

    @Test
    @DisplayName("return 404 when book not found for view")
    void shouldReturn404WhenBookNotFoundForView() throws Exception {
        given(bookService.findById("1")).willThrow(new EntityNotFoundException("Book with id 1 not found"));

        mvc.perform(get("/books/1"))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("return new book form")
    void shouldReturnNewBookForm() throws Exception {
        var author = new Author("1", "Author Name");
        var genre = new Genre("1", "Genre Name");
        
        given(authorService.findAll()).willReturn(List.of(author));
        given(genreService.findAll()).willReturn(List.of(genre));

        mvc.perform(get("/books/new"))
                .andExpect(status().isOk())
                .andExpect(view().name("book/form"))
                .andExpect(model().attributeExists("book"))
                .andExpect(model().attributeExists("authors"))
                .andExpect(model().attributeExists("genres"));
    }

    @Test
    @DisplayName("return edit book form")
    void shouldReturnEditBookForm() throws Exception {
        var author = new Author("1", "Author Name");
        var genre = new Genre("1", "Genre Name");
        var book = new Book("1", "Book Title", author, List.of(genre));
        
        given(bookService.findById("1")).willReturn(book);
        given(authorService.findAll()).willReturn(List.of(author));
        given(genreService.findAll()).willReturn(List.of(genre));

        mvc.perform(get("/books/1/edit"))
                .andExpect(status().isOk())
                .andExpect(view().name("book/form"))
                .andExpect(model().attributeExists("book"))
                .andExpect(model().attributeExists("authors"))
                .andExpect(model().attributeExists("genres"));
    }

    @Test
    @DisplayName("return 404 when book not found for edit")
    void shouldReturn404WhenBookNotFoundForEdit() throws Exception {
        given(bookService.findById("1")).willThrow(new EntityNotFoundException("Book with id 1 not found"));

        mvc.perform(get("/books/1/edit"))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("create new book and redirect to home")
    void shouldCreateNewBookAndRedirectToHome() throws Exception {
        var author = new Author("1", "Author Name");
        var genre = new Genre("1", "Genre Name");
        var book = new Book("1", "Book Title", author, List.of(genre));
        var createDto = new BookCreateDto("Book Title", "1", Set.of("1"));
        
        given(bookService.create(createDto)).willReturn(book);

        mvc.perform(post("/books")
                .param("title", "Book Title")
                .param("authorId", "1")
                .param("genreIds", "1")
                .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/"));

        verify(bookService).create(createDto);
    }

    @Test
    @DisplayName("create new book without genres and redirect to home")
    void shouldCreateNewBookWithoutGenresAndRedirectToHome() throws Exception {
        var author = new Author("1", "Author Name");
        var book = new Book("1", "Book Title", author, List.of());
        var createDto = new BookCreateDto("Book Title", "1", Set.of());
        
        given(bookService.create(createDto)).willReturn(book);

        mvc.perform(post("/books")
                .param("title", "Book Title")
                .param("authorId", "1")
                .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/"));

        verify(bookService).create(createDto);
    }

    @Test
    @DisplayName("update book and redirect to home")
    void shouldUpdateBookAndRedirectToHome() throws Exception {
        var author = new Author("1", "Author Name");
        var genre = new Genre("1", "Genre Name");
        var book = new Book("1", "Updated Title", author, List.of(genre));
        var updateDto = new BookUpdateDto("1", "Updated Title", "1", Set.of("1"));
        
        given(bookService.update(updateDto)).willReturn(book);

        mvc.perform(post("/books/1")
                .param("title", "Updated Title")
                .param("authorId", "1")
                .param("genreIds", "1")
                .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/"));

        verify(bookService).update(updateDto);
    }

    @Test
    @DisplayName("return delete confirmation page")
    void shouldReturnDeleteConfirmationPage() throws Exception {
        var author = new Author("1", "Author Name");
        var genre = new Genre("1", "Genre Name");
        var book = new Book("1", "Book Title", author, List.of(genre));
        
        given(bookService.findById("1")).willReturn(book);

        mvc.perform(get("/books/1/delete"))
                .andExpect(status().isOk())
                .andExpect(view().name("book/delete"))
                .andExpect(model().attributeExists("book"));
    }

    @Test
    @DisplayName("return 404 when book not found for delete confirmation")
    void shouldReturn404WhenBookNotFoundForDeleteConfirmation() throws Exception {
        given(bookService.findById("1")).willThrow(new EntityNotFoundException("Book with id 1 not found"));

        mvc.perform(get("/books/1/delete"))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("delete book and redirect to home")
    void shouldDeleteBookAndRedirectToHome() throws Exception {
        mvc.perform(post("/books/1/delete")
                .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/"));

        verify(bookService).deleteById("1");
    }

    @Test
    @DisplayName("return 404 when author not found during book creation")
    void shouldReturn404WhenAuthorNotFoundDuringBookCreation() throws Exception {
        var createDto = new BookCreateDto("Book Title", "nonexistent-author", Set.of("1"));
        
        given(bookService.create(createDto)).willThrow(new EntityNotFoundException("Author with id nonexistent-author not found"));

        mvc.perform(post("/books")
                .param("title", "Book Title")
                .param("authorId", "nonexistent-author")
                .param("genreIds", "1")
                .with(csrf()))
                .andExpect(status().isNotFound());

        verify(bookService).create(createDto);
    }

    @Test
    @DisplayName("return 404 when genre not found during book creation")
    void shouldReturn404WhenGenreNotFoundDuringBookCreation() throws Exception {
        var createDto = new BookCreateDto("Book Title", "1", Set.of("nonexistent-genre"));
        
        given(bookService.create(createDto)).willThrow(new EntityNotFoundException("One or all genres with ids [nonexistent-genre] not found"));

        mvc.perform(post("/books")
                .param("title", "Book Title")
                .param("authorId", "1")
                .param("genreIds", "nonexistent-genre")
                .with(csrf()))
                .andExpect(status().isNotFound());

        verify(bookService).create(createDto);
    }

    @Test
    @DisplayName("return 404 when author not found during book update")
    void shouldReturn404WhenAuthorNotFoundDuringBookUpdate() throws Exception {
        var updateDto = new BookUpdateDto("1", "Updated Title", "nonexistent-author", Set.of("1"));
        
        given(bookService.update(updateDto)).willThrow(new EntityNotFoundException("Author with id nonexistent-author not found"));

        mvc.perform(post("/books/1")
                .param("title", "Updated Title")
                .param("authorId", "nonexistent-author")
                .param("genreIds", "1")
                .with(csrf()))
                .andExpect(status().isNotFound());

        verify(bookService).update(updateDto);
    }

    @Test
    @DisplayName("return 404 when genre not found during book update")
    void shouldReturn404WhenGenreNotFoundDuringBookUpdate() throws Exception {
        var updateDto = new BookUpdateDto("1", "Updated Title", "1", Set.of("nonexistent-genre"));
        
        given(bookService.update(updateDto)).willThrow(new EntityNotFoundException("One or all genres with ids [nonexistent-genre] not found"));

        mvc.perform(post("/books/1")
                .param("title", "Updated Title")
                .param("authorId", "1")
                .param("genreIds", "nonexistent-genre")
                .with(csrf()))
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