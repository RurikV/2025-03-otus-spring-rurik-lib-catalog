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
import ru.otus.hw.services.BookService;
import ru.otus.hw.services.CommentService;

import java.util.List;
import java.util.Optional;

import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(CommentController.class)
@DisplayName("CommentController should")
class CommentControllerTest {

    @Autowired
    private MockMvc mvc;

    @MockBean
    private CommentService commentService;

    @MockBean
    private BookService bookService;

    @Test
    @DisplayName("return new comment form")
    void shouldReturnNewCommentForm() throws Exception {
        var author = new Author("1", "Author Name");
        var genre = new Genre("1", "Genre Name");
        var book = new Book("1", "Book Title", author, List.of(genre));
        
        given(bookService.findById("1")).willReturn(Optional.of(book));

        mvc.perform(get("/books/1/comments/new"))
                .andExpect(status().isOk())
                .andExpect(view().name("comment/form"))
                .andExpect(model().attributeExists("book"))
                .andExpect(model().attribute("book", book));
    }

    @Test
    @DisplayName("redirect to home when book not found for new comment form")
    void shouldRedirectToHomeWhenBookNotFoundForNewCommentForm() throws Exception {
        given(bookService.findById("1")).willReturn(Optional.empty());

        mvc.perform(get("/books/1/comments/new"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/"));
    }

    @Test
    @DisplayName("save comment and redirect to book view")
    void shouldSaveCommentAndRedirectToBookView() throws Exception {
        var author = new Author("1", "Author Name");
        var genre = new Genre("1", "Genre Name");
        var book = new Book("1", "Book Title", author, List.of(genre));
        var comment = new Comment("1", "Comment text", book);
        
        given(commentService.insert("Comment text", "1")).willReturn(comment);

        mvc.perform(post("/books/1/comments")
                .param("text", "Comment text"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/books/1"));

        verify(commentService).insert("Comment text", "1");
    }

    @Test
    @DisplayName("return edit comment form")
    void shouldReturnEditCommentForm() throws Exception {
        var author = new Author("1", "Author Name");
        var genre = new Genre("1", "Genre Name");
        var book = new Book("1", "Book Title", author, List.of(genre));
        var comment = new Comment("1", "Comment text", book);
        
        given(commentService.findById("1")).willReturn(Optional.of(comment));

        mvc.perform(get("/comments/1/edit"))
                .andExpect(status().isOk())
                .andExpect(view().name("comment/edit"))
                .andExpect(model().attributeExists("comment"))
                .andExpect(model().attribute("comment", comment));
    }

    @Test
    @DisplayName("redirect to home when comment not found for edit form")
    void shouldRedirectToHomeWhenCommentNotFoundForEditForm() throws Exception {
        given(commentService.findById("1")).willReturn(Optional.empty());

        mvc.perform(get("/comments/1/edit"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/"));
    }

    @Test
    @DisplayName("update comment and redirect to book view")
    void shouldUpdateCommentAndRedirectToBookView() throws Exception {
        var author = new Author("1", "Author Name");
        var genre = new Genre("1", "Genre Name");
        var book = new Book("1", "Book Title", author, List.of(genre));
        var comment = new Comment("1", "Updated comment text", book);
        
        given(commentService.findById("1")).willReturn(Optional.of(comment));
        given(commentService.update("1", "Updated comment text")).willReturn(comment);

        mvc.perform(post("/comments/1")
                .param("text", "Updated comment text"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/books/1"));

        verify(commentService).update("1", "Updated comment text");
    }

    @Test
    @DisplayName("redirect to home when comment not found for update")
    void shouldRedirectToHomeWhenCommentNotFoundForUpdate() throws Exception {
        given(commentService.findById("1")).willReturn(Optional.empty());

        mvc.perform(post("/comments/1")
                .param("text", "Updated comment text"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/"));
    }

    @Test
    @DisplayName("return delete comment confirmation page")
    void shouldReturnDeleteCommentConfirmationPage() throws Exception {
        var author = new Author("1", "Author Name");
        var genre = new Genre("1", "Genre Name");
        var book = new Book("1", "Book Title", author, List.of(genre));
        var comment = new Comment("1", "Comment text", book);
        
        given(commentService.findById("1")).willReturn(Optional.of(comment));

        mvc.perform(get("/comments/1/delete"))
                .andExpect(status().isOk())
                .andExpect(view().name("comment/delete"))
                .andExpect(model().attributeExists("comment"))
                .andExpect(model().attribute("comment", comment));
    }

    @Test
    @DisplayName("redirect to home when comment not found for delete confirmation")
    void shouldRedirectToHomeWhenCommentNotFoundForDeleteConfirmation() throws Exception {
        given(commentService.findById("1")).willReturn(Optional.empty());

        mvc.perform(get("/comments/1/delete"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/"));
    }

    @Test
    @DisplayName("delete comment and redirect to book view")
    void shouldDeleteCommentAndRedirectToBookView() throws Exception {
        var author = new Author("1", "Author Name");
        var genre = new Genre("1", "Genre Name");
        var book = new Book("1", "Book Title", author, List.of(genre));
        var comment = new Comment("1", "Comment text", book);
        
        given(commentService.findById("1")).willReturn(Optional.of(comment));

        mvc.perform(post("/comments/1/delete"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/books/1"));

        verify(commentService).deleteById("1");
    }

    @Test
    @DisplayName("redirect to home when comment not found for delete")
    void shouldRedirectToHomeWhenCommentNotFoundForDelete() throws Exception {
        given(commentService.findById("1")).willReturn(Optional.empty());

        mvc.perform(post("/comments/1/delete"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/"));
    }
}