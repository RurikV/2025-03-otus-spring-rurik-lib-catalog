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

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@WebAppConfiguration
@Import(TestMongoConfig.class)
class RoleBasedAuthorizationTest {

    @Autowired
    private WebApplicationContext webApplicationContext;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext)
                .apply(springSecurity())
                .build();
    }

    // Role-based authorization tests for book management

    @Test
    @WithMockUser(roles = "USER")
    void shouldDenyAccessToBookCreationForUser() throws Exception {
        mockMvc.perform(get("/books/new"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void shouldAllowAccessToBookCreationForAdmin() throws Exception {
        mockMvc.perform(get("/books/new"))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = "USER")
    void shouldDenyBookCreationPostForUser() throws Exception {
        mockMvc.perform(post("/books").with(csrf())
                .param("title", "Test Book")
                .param("authorId", "1"))
                .andExpect(status().isForbidden());
    }

    // Role-based authorization tests for authors management

    @Test
    @WithMockUser(roles = "USER")
    void shouldDenyAccessToAuthorsForUser() throws Exception {
        mockMvc.perform(get("/authors"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void shouldAllowAccessToAuthorsForAdmin() throws Exception {
        mockMvc.perform(get("/authors"))
                .andExpect(status().isOk());
    }

    // Role-based authorization tests for genres management

    @Test
    @WithMockUser(roles = "USER")
    void shouldDenyAccessToGenresForUser() throws Exception {
        mockMvc.perform(get("/genres"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void shouldAllowAccessToGenresForAdmin() throws Exception {
        mockMvc.perform(get("/genres"))
                .andExpect(status().isOk());
    }

    // Role-based authorization tests for book viewing (both roles should have access)

    @Test
    @WithMockUser(roles = "USER")
    void shouldAllowAccessToBooksListForUser() throws Exception {
        mockMvc.perform(get("/books"))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void shouldAllowAccessToBooksListForAdmin() throws Exception {
        mockMvc.perform(get("/books"))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = "USER")
    void shouldAllowAccessToHomePageForUser() throws Exception {
        mockMvc.perform(get("/"))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void shouldAllowAccessToHomePageForAdmin() throws Exception {
        mockMvc.perform(get("/"))
                .andExpect(status().isOk());
    }
}