package ru.otus.hw.controllers;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class GenrePageController {

    @GetMapping("/genres")
    public String listGenres() {
        return "genre/list";
    }
}