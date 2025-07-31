package ru.otus.hw.controllers;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;
import ru.otus.hw.services.BookService;
import ru.otus.hw.services.CommentService;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(CommentPageController.class)
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
        mvc.perform(get("/books/1/comments/new"))
                .andExpect(status().isOk())
                .andExpect(view().name("comment/form"));
    }

    @Test
    @DisplayName("return new comment form regardless of book existence")
    void shouldReturnNewCommentFormRegardlessOfBookExistence() throws Exception {
        // Page controller doesn't validate book existence - returns view for AJAX loading
        mvc.perform(get("/books/999/comments/new"))
                .andExpect(status().isOk())
                .andExpect(view().name("comment/form"));
    }

    // POST endpoints are handled by CommentController (REST API), not CommentPageController
    // This test is removed as it doesn't apply to the page controller

    @Test
    @DisplayName("return edit comment form")
    void shouldReturnEditCommentForm() throws Exception {
        // Page controller just returns view name - data is loaded via AJAX
        mvc.perform(get("/comments/1/edit"))
                .andExpect(status().isOk())
                .andExpect(view().name("comment/edit"));
    }

    @Test
    @DisplayName("return edit comment form regardless of comment existence")
    void shouldReturnEditCommentFormRegardlessOfCommentExistence() throws Exception {
        // Page controller doesn't validate comment existence - returns view for AJAX loading
        mvc.perform(get("/comments/999/edit"))
                .andExpect(status().isOk())
                .andExpect(view().name("comment/edit"));
    }

    // Update comment POST endpoint is handled by CommentController (REST API), not CommentPageController
    // This test is removed as it doesn't apply to the page controller

    // This test is removed as it tests POST endpoint that doesn't exist in page controller

    @Test
    @DisplayName("return delete comment confirmation page")
    void shouldReturnDeleteCommentConfirmationPage() throws Exception {
        // Page controller just returns view name - data is loaded via AJAX
        mvc.perform(get("/comments/1/delete"))
                .andExpect(status().isOk())
                .andExpect(view().name("comment/delete"));
    }

    @Test
    @DisplayName("return delete comment confirmation page regardless of comment existence")
    void shouldReturnDeleteCommentConfirmationPageRegardlessOfCommentExistence() throws Exception {
        // Page controller doesn't validate comment existence - returns view for AJAX loading
        mvc.perform(get("/comments/999/delete"))
                .andExpect(status().isOk())
                .andExpect(view().name("comment/delete"));
    }

    // DELETE comment POST endpoints are handled by CommentController (REST API), not CommentPageController
    // These tests are removed as they don't apply to the page controller
}