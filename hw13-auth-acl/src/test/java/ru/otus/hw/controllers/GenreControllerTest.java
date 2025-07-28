package ru.otus.hw.controllers;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import ru.otus.hw.models.Genre;
import ru.otus.hw.services.GenreService;

import java.util.List;

import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(GenreController.class)
@DisplayName("GenreController should")
class GenreControllerTest {

    @Autowired
    private MockMvc mvc;

    @MockBean
    private GenreService genreService;

    @Test
    @DisplayName("return genres list page")
    @WithMockUser
    void shouldReturnGenresListPage() throws Exception {
        var genre1 = new Genre("1", "Fantasy");
        var genre2 = new Genre("2", "Science Fiction");
        
        given(genreService.findAll()).willReturn(List.of(genre1, genre2));

        mvc.perform(get("/genres"))
                .andExpect(status().isOk())
                .andExpect(view().name("genre/list"))
                .andExpect(model().attributeExists("genres"))
                .andExpect(model().attribute("genres", List.of(genre1, genre2)));
    }

    @Test
    @DisplayName("return genres list page with empty list")
    @WithMockUser
    void shouldReturnGenresListPageWithEmptyList() throws Exception {
        given(genreService.findAll()).willReturn(List.of());

        mvc.perform(get("/genres"))
                .andExpect(status().isOk())
                .andExpect(view().name("genre/list"))
                .andExpect(model().attributeExists("genres"))
                .andExpect(model().attribute("genres", List.of()));
    }
}