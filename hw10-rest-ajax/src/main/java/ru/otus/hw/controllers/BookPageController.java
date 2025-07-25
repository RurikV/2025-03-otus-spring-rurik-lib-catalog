package ru.otus.hw.controllers;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import ru.otus.hw.services.AuthorService;
import ru.otus.hw.services.GenreService;

@Controller
@RequiredArgsConstructor
public class BookPageController {

    private final AuthorService authorService;
    
    private final GenreService genreService;

    @GetMapping("/")
    public String listBooks() {
        return "book/list";
    }

    @GetMapping("/books")
    public String listBooksAlternative() {
        return "book/list";
    }

    @GetMapping("/books/{id}")
    public String viewBook(@PathVariable String id, Model model) {
        model.addAttribute("bookId", id);
        return "book/view";
    }

    @GetMapping("/books/new")
    public String newBookForm(Model model) {
        model.addAttribute("authors", authorService.findAll());
        model.addAttribute("genres", genreService.findAll());
        return "book/form";
    }

    @GetMapping("/books/{id}/edit")
    public String editBookForm(@PathVariable String id, Model model) {
        model.addAttribute("authors", authorService.findAll());
        model.addAttribute("genres", genreService.findAll());
        model.addAttribute("bookId", id);
        return "book/form";
    }

    @GetMapping("/books/{id}/delete")
    public String deleteBookConfirm(@PathVariable String id, Model model) {
        model.addAttribute("bookId", id);
        return "book/delete";
    }
}