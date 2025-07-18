package ru.otus.hw.controllers;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import ru.otus.hw.models.Book;
import ru.otus.hw.services.AuthorService;
import ru.otus.hw.services.BookService;
import ru.otus.hw.services.CommentService;
import ru.otus.hw.services.GenreService;

import java.util.Set;

@Controller
@RequiredArgsConstructor
public class BookController {

    private final BookService bookService;

    private final AuthorService authorService;

    private final GenreService genreService;

    private final CommentService commentService;

    @GetMapping("/")
    public String listBooks(Model model) {
        model.addAttribute("books", bookService.findAll());
        return "book/list";
    }

    @GetMapping("/books")
    public String listBooksAlternative(Model model) {
        return listBooks(model);
    }

    @GetMapping("/books/{id}")
    public String viewBook(@PathVariable String id, Model model) {
        var book = bookService.findById(id);
        if (book.isEmpty()) {
            return "redirect:/";
        }
        model.addAttribute("book", book.get());
        model.addAttribute("comments", commentService.findByBookId(id));
        return "book/view";
    }

    @GetMapping("/books/new")
    public String newBookForm(Model model) {
        model.addAttribute("book", new Book());
        model.addAttribute("authors", authorService.findAll());
        model.addAttribute("genres", genreService.findAll());
        return "book/form";
    }

    @GetMapping("/books/{id}/edit")
    public String editBookForm(@PathVariable String id, Model model) {
        var book = bookService.findById(id);
        if (book.isEmpty()) {
            return "redirect:/";
        }
        model.addAttribute("book", book.get());
        model.addAttribute("authors", authorService.findAll());
        model.addAttribute("genres", genreService.findAll());
        return "book/form";
    }

    @PostMapping("/books")
    public String saveBook(@RequestParam String title,
                          @RequestParam String authorId,
                          @RequestParam(required = false) Set<String> genreIds,
                          RedirectAttributes redirectAttributes,
                          Model model) {
        // Validate title
        if (title == null || title.trim().isEmpty()) {
            model.addAttribute("book", new Book());
            model.addAttribute("authors", authorService.findAll());
            model.addAttribute("genres", genreService.findAll());
            model.addAttribute("error", "Title is required and cannot be empty");
            return "book/form";
        }
        
        // Validate authorId
        if (authorId == null || authorId.trim().isEmpty()) {
            model.addAttribute("book", new Book());
            model.addAttribute("authors", authorService.findAll());
            model.addAttribute("genres", genreService.findAll());
            model.addAttribute("error", "Author is required");
            return "book/form";
        }
        
        if (genreIds == null) {
            genreIds = Set.of();
        }
        bookService.insert(title, authorId, genreIds);
        return "redirect:/";
    }

    @PostMapping("/books/{id}")
    public String updateBook(@PathVariable String id,
                            @RequestParam String title,
                            @RequestParam String authorId,
                            @RequestParam(required = false) Set<String> genreIds,
                            Model model) {
        // Validate title
        if (title == null || title.trim().isEmpty()) {
            var book = bookService.findById(id);
            if (book.isEmpty()) {
                return "redirect:/";
            }
            model.addAttribute("book", book.get());
            model.addAttribute("authors", authorService.findAll());
            model.addAttribute("genres", genreService.findAll());
            model.addAttribute("error", "Title is required and cannot be empty");
            return "book/form";
        }
        
        // Validate authorId
        if (authorId == null || authorId.trim().isEmpty()) {
            var book = bookService.findById(id);
            if (book.isEmpty()) {
                return "redirect:/";
            }
            model.addAttribute("book", book.get());
            model.addAttribute("authors", authorService.findAll());
            model.addAttribute("genres", genreService.findAll());
            model.addAttribute("error", "Author is required");
            return "book/form";
        }
        
        if (genreIds == null) {
            genreIds = Set.of();
        }
        bookService.update(id, title, authorId, genreIds);
        return "redirect:/";
    }

    @GetMapping("/books/{id}/delete")
    public String deleteBookConfirm(@PathVariable String id, Model model) {
        var book = bookService.findById(id);
        if (book.isEmpty()) {
            return "redirect:/";
        }
        model.addAttribute("book", book.get());
        return "book/delete";
    }

    @PostMapping("/books/{id}/delete")
    public String deleteBook(@PathVariable String id) {
        bookService.deleteById(id);
        return "redirect:/";
    }
}