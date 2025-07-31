package ru.otus.hw.controllers;

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

import java.util.List;

import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(BookPageController.class)
@Import(LocalizationIntegrationTest.TestConfig.class)
class LocalizationIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private BookService bookService;

    @MockBean
    private AuthorService authorService;

    @MockBean
    private GenreService genreService;

    @MockBean
    private CommentService commentService;

    @Test
    void shouldDisplayRussianTextCorrectly() throws Exception {
        given(bookService.findAll()).willReturn(List.of());

        mockMvc.perform(get("/").param("lang", "ru"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith("text/html"))
                .andExpect(content().encoding("UTF-8"))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("Каталог библиотеки")))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("Книги")))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("Авторы")))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("Жанры")))
                .andExpect(content().string(org.hamcrest.Matchers.not(org.hamcrest.Matchers.containsString("????"))));
    }

    @Test
    void shouldDisplayEnglishTextCorrectly() throws Exception {
        given(bookService.findAll()).willReturn(List.of());

        mockMvc.perform(get("/").param("lang", "en"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith("text/html"))
                .andExpect(content().encoding("UTF-8"))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("Library Catalog")))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("Books")))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("Authors")))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("Genres")));
    }

    @Test
    void shouldSwitchBetweenLocales() throws Exception {
        given(bookService.findAll()).willReturn(List.of());

        // Test Russian locale
        mockMvc.perform(get("/").param("lang", "ru"))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("Каталог библиотеки")));

        // Test English locale
        mockMvc.perform(get("/").param("lang", "en"))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("Library Catalog")));
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