package ru.otus.hw.controllers;

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

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@WebAppConfiguration
@Import(TestMongoConfig.class)
class SecurityIntegrationTest {

    @Autowired
    private WebApplicationContext webApplicationContext;

    private MockMvc mockMvc;

    @org.junit.jupiter.api.BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext)
                .apply(springSecurity())
                .build();
    }

    @Test
    void shouldRedirectToLoginWhenAccessingProtectedResourceWithoutAuthentication() throws Exception {
        mockMvc.perform(get("/"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrlPattern("**/login"));
    }

    @Test
    void shouldRedirectToLoginWhenAccessingBooksWithoutAuthentication() throws Exception {
        mockMvc.perform(get("/books"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrlPattern("**/login"));
    }

    @Test
    void shouldRedirectToLoginWhenAccessingAuthorsWithoutAuthentication() throws Exception {
        mockMvc.perform(get("/authors"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrlPattern("**/login"));
    }

    @Test
    void shouldRedirectToLoginWhenAccessingGenresWithoutAuthentication() throws Exception {
        mockMvc.perform(get("/genres"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrlPattern("**/login"));
    }

    @Test
    void shouldAllowAccessToLoginPage() throws Exception {
        mockMvc.perform(get("/login"))
                .andExpect(status().isOk())
                .andExpect(view().name("login"));
    }

    @Test
    @WithMockUser
    void shouldAllowAccessToHomePageWhenAuthenticated() throws Exception {
        mockMvc.perform(get("/"))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser
    void shouldAllowAccessToBooksWhenAuthenticated() throws Exception {
        mockMvc.perform(get("/books"))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser
    void shouldAllowAccessToAuthorsWhenAuthenticated() throws Exception {
        mockMvc.perform(get("/authors"))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser
    void shouldAllowAccessToGenresWhenAuthenticated() throws Exception {
        mockMvc.perform(get("/genres"))
                .andExpect(status().isOk());
    }

    @Test
    void shouldRedirectToLoginWhenAccessingBookFormWithoutAuthentication() throws Exception {
        mockMvc.perform(get("/books/new"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrlPattern("**/login"));
    }

    @Test
    @WithMockUser
    void shouldAllowAccessToBookFormWhenAuthenticated() throws Exception {
        mockMvc.perform(get("/books/new"))
                .andExpect(status().isOk());
    }

    @Test
    void shouldRedirectToLoginWhenPostingBookWithoutAuthentication() throws Exception {
        mockMvc.perform(post("/books").with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrlPattern("**/login"));
    }
}