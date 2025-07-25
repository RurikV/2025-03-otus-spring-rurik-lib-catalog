package ru.otus.hw.controllers;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import ru.otus.hw.dto.BookCreateDto;
import ru.otus.hw.dto.BookDto;
import ru.otus.hw.dto.BookUpdateDto;
import ru.otus.hw.exceptions.EntityNotFoundException;
import ru.otus.hw.services.AuthorService;
import ru.otus.hw.services.BookService;
import ru.otus.hw.services.GenreService;

import java.util.Set;

@Controller
@RequiredArgsConstructor
public class BookPageController {

    private final AuthorService authorService;
    
    private final GenreService genreService;
    
    private final BookService bookService;

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
        model.addAttribute("book", bookService.findById(id));
        model.addAttribute("bookId", id);
        return "book/view";
    }

    @GetMapping("/books/new")
    public String newBookForm(Model model) {
        model.addAttribute("book", new BookDto());
        model.addAttribute("authors", authorService.findAll());
        model.addAttribute("genres", genreService.findAll());
        return "book/form";
    }

    @GetMapping("/books/{id}/edit")
    public String editBookForm(@PathVariable String id, Model model) {
        model.addAttribute("book", bookService.findById(id));
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

    @PostMapping("/books")
    public String createBook(@RequestParam String title,
                           @RequestParam String authorId,
                           @RequestParam(required = false) Set<String> genreIds,
                           RedirectAttributes redirectAttributes,
                           Model model) {
        try {
            var createDto = new BookCreateDto(title, authorId, genreIds);
            bookService.create(createDto);
            return "redirect:/";
        } catch (EntityNotFoundException | IllegalArgumentException e) {
            model.addAttribute("error", e.getMessage());
            model.addAttribute("book", new BookDto(null, title, null, null));
            model.addAttribute("authors", authorService.findAll());
            model.addAttribute("genres", genreService.findAll());
            return "book/form";
        }
    }

    @PostMapping("/books/{id}")
    public String updateBook(@PathVariable String id,
                           @RequestParam String title,
                           @RequestParam String authorId,
                           @RequestParam(required = false) Set<String> genreIds,
                           RedirectAttributes redirectAttributes,
                           Model model) {
        try {
            var updateDto = new BookUpdateDto(id, title, authorId, genreIds);
            bookService.update(updateDto);
            return "redirect:/books/" + id;
        } catch (EntityNotFoundException | IllegalArgumentException e) {
            model.addAttribute("error", e.getMessage());
            model.addAttribute("book", bookService.findById(id));
            model.addAttribute("authors", authorService.findAll());
            model.addAttribute("genres", genreService.findAll());
            model.addAttribute("bookId", id);
            return "book/form";
        }
    }
}