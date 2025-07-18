package ru.otus.hw.controllers;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;
import ru.otus.hw.models.Author;
import ru.otus.hw.models.Book;
import ru.otus.hw.models.Comment;
import ru.otus.hw.models.Genre;
import ru.otus.hw.services.AuthorService;
import ru.otus.hw.services.BookService;
import ru.otus.hw.services.CommentService;
import ru.otus.hw.services.GenreService;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(BookController.class)
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
        
        given(bookService.findById("1")).willReturn(Optional.of(book));
        given(commentService.findByBookId("1")).willReturn(List.of(comment));

        mvc.perform(get("/books/1"))
                .andExpect(status().isOk())
                .andExpect(view().name("book/view"))
                .andExpect(model().attributeExists("book"))
                .andExpect(model().attributeExists("comments"));
    }

    @Test
    @DisplayName("redirect to home when book not found for view")
    void shouldRedirectToHomeWhenBookNotFoundForView() throws Exception {
        given(bookService.findById("1")).willReturn(Optional.empty());

        mvc.perform(get("/books/1"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/"));
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
        
        given(bookService.findById("1")).willReturn(Optional.of(book));
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
    @DisplayName("redirect to home when book not found for edit")
    void shouldRedirectToHomeWhenBookNotFoundForEdit() throws Exception {
        given(bookService.findById("1")).willReturn(Optional.empty());

        mvc.perform(get("/books/1/edit"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/"));
    }

    @Test
    @DisplayName("create new book and redirect to home")
    void shouldCreateNewBookAndRedirectToHome() throws Exception {
        var author = new Author("1", "Author Name");
        var genre = new Genre("1", "Genre Name");
        var book = new Book("1", "Book Title", author, List.of(genre));
        
        given(bookService.insert("Book Title", "1", Set.of("1"))).willReturn(book);

        mvc.perform(post("/books")
                .param("title", "Book Title")
                .param("authorId", "1")
                .param("genreIds", "1"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/"));

        verify(bookService).insert("Book Title", "1", Set.of("1"));
    }

    @Test
    @DisplayName("create new book without genres and redirect to home")
    void shouldCreateNewBookWithoutGenresAndRedirectToHome() throws Exception {
        var author = new Author("1", "Author Name");
        var book = new Book("1", "Book Title", author, List.of());
        
        given(bookService.insert("Book Title", "1", Set.of())).willReturn(book);

        mvc.perform(post("/books")
                .param("title", "Book Title")
                .param("authorId", "1"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/"));

        verify(bookService).insert("Book Title", "1", Set.of());
    }

    @Test
    @DisplayName("update book and redirect to home")
    void shouldUpdateBookAndRedirectToHome() throws Exception {
        var author = new Author("1", "Author Name");
        var genre = new Genre("1", "Genre Name");
        var book = new Book("1", "Updated Title", author, List.of(genre));
        
        given(bookService.update("1", "Updated Title", "1", Set.of("1"))).willReturn(book);

        mvc.perform(post("/books/1")
                .param("title", "Updated Title")
                .param("authorId", "1")
                .param("genreIds", "1"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/"));

        verify(bookService).update("1", "Updated Title", "1", Set.of("1"));
    }

    @Test
    @DisplayName("return delete confirmation page")
    void shouldReturnDeleteConfirmationPage() throws Exception {
        var author = new Author("1", "Author Name");
        var genre = new Genre("1", "Genre Name");
        var book = new Book("1", "Book Title", author, List.of(genre));
        
        given(bookService.findById("1")).willReturn(Optional.of(book));

        mvc.perform(get("/books/1/delete"))
                .andExpect(status().isOk())
                .andExpect(view().name("book/delete"))
                .andExpect(model().attributeExists("book"));
    }

    @Test
    @DisplayName("redirect to home when book not found for delete confirmation")
    void shouldRedirectToHomeWhenBookNotFoundForDeleteConfirmation() throws Exception {
        given(bookService.findById("1")).willReturn(Optional.empty());

        mvc.perform(get("/books/1/delete"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/"));
    }

    @Test
    @DisplayName("delete book and redirect to home")
    void shouldDeleteBookAndRedirectToHome() throws Exception {
        mvc.perform(post("/books/1/delete"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/"));

        verify(bookService).deleteById("1");
    }
}