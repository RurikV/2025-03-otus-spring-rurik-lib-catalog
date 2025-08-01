package ru.otus.hw.controllers;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class AuthorPageController {

    @GetMapping("/authors")
    public String listAuthors() {
        return "author/list";
    }
}