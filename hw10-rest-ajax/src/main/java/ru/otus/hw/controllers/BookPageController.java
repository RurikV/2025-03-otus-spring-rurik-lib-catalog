package ru.otus.hw.controllers;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@Controller
public class BookPageController {

    @GetMapping("/")
    public String listBooks() {
        return "book/list";
    }

    @GetMapping("/books")
    public String listBooksAlternative() {
        return "book/list";
    }

    @GetMapping("/books/{id}")
    public String viewBook(@PathVariable String id) {
        return "book/view";
    }

    @GetMapping("/books/new")
    public String newBookForm() {
        return "book/form";
    }

    @GetMapping("/books/{id}/edit")
    public String editBookForm(@PathVariable String id) {
        return "book/form";
    }

    @GetMapping("/books/{id}/delete")
    public String deleteBookConfirm(@PathVariable String id) {
        return "book/delete";
    }
}