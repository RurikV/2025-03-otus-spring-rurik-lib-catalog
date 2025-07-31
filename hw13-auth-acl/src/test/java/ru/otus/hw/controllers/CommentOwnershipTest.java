package ru.otus.hw.controllers;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import ru.otus.hw.config.TestMongoConfig;
import ru.otus.hw.models.Author;
import ru.otus.hw.models.Book;
import ru.otus.hw.models.Comment;
import ru.otus.hw.models.Genre;
import ru.otus.hw.models.User;
import ru.otus.hw.repositories.AuthorRepository;
import ru.otus.hw.repositories.BookRepository;
import ru.otus.hw.repositories.CommentRepository;
import ru.otus.hw.repositories.GenreRepository;
import ru.otus.hw.repositories.UserRepository;

import java.util.List;

import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@WebAppConfiguration
@Import(TestMongoConfig.class)
class CommentOwnershipTest {

    @Autowired
    private WebApplicationContext webApplicationContext;

    @Autowired
    private CommentRepository commentRepository;

    @Autowired
    private BookRepository bookRepository;

    @Autowired
    private AuthorRepository authorRepository;

    @Autowired
    private GenreRepository genreRepository;

    @Autowired
    private UserRepository userRepository;

    private MockMvc mockMvc;
    private String commentId;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext)
                .apply(springSecurity())
                .build();

        // Clean up existing data
        commentRepository.deleteAll();
        bookRepository.deleteAll();
        authorRepository.deleteAll();
        genreRepository.deleteAll();
        userRepository.deleteAll();

        // Create test data
        User user1 = userRepository.save(new User("user1", "password", "USER", true));
        User user2 = userRepository.save(new User("user2", "password", "USER", true));
        
        Author author = authorRepository.save(new Author(null, "Test Author"));
        Genre genre = genreRepository.save(new Genre(null, "Test Genre"));
        Book book = bookRepository.save(new Book(null, "Test Book", author, List.of(genre)));
        
        // Create a comment owned by user1
        Comment comment = commentRepository.save(new Comment("Test comment", book, user1));
        commentId = comment.getId();
    }

    @Test
    @WithMockUser(username = "user2", roles = "USER")
    void shouldDenyAccessToEditCommentFormForNonOwner() throws Exception {
        mockMvc.perform(get("/comments/" + commentId + "/edit"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(username = "user2", roles = "USER")
    void shouldDenyAccessToDeleteCommentFormForNonOwner() throws Exception {
        mockMvc.perform(get("/comments/" + commentId + "/delete"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(username = "user1", roles = "USER")
    void shouldAllowAccessToEditCommentFormForOwner() throws Exception {
        mockMvc.perform(get("/comments/" + commentId + "/edit"))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(username = "user1", roles = "USER")
    void shouldAllowAccessToDeleteCommentFormForOwner() throws Exception {
        mockMvc.perform(get("/comments/" + commentId + "/delete"))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(username = "admin", roles = "ADMIN")
    void shouldAllowAccessToEditCommentFormForAdmin() throws Exception {
        mockMvc.perform(get("/comments/" + commentId + "/edit"))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(username = "admin", roles = "ADMIN")
    void shouldAllowAccessToDeleteCommentFormForAdmin() throws Exception {
        mockMvc.perform(get("/comments/" + commentId + "/delete"))
                .andExpect(status().isOk());
    }
}