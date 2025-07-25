package ru.otus.hw.controllers;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.validation.BindingResult;
import jakarta.validation.Valid;
import ru.otus.hw.dto.BookCreateDto;
import ru.otus.hw.dto.BookDto;
import ru.otus.hw.dto.BookUpdateDto;
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
        model.addAttribute("book", book);
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
        model.addAttribute("book", book);
        model.addAttribute("authors", authorService.findAll());
        model.addAttribute("genres", genreService.findAll());
        return "book/form";
    }

    @PostMapping("/books")
    public String saveBook(@Valid @ModelAttribute BookDto bookDto,
                          BindingResult bindingResult,
                          RedirectAttributes redirectAttributes,
                          Model model) {
        if (bindingResult.hasErrors()) {
            setupFormModel(model, new Book(), null);
            return "book/form";
        }
        
        Set<String> genreIds = bookDto.getGenreIds();
        if (genreIds == null) {
            genreIds = Set.of();
        }
        BookCreateDto createDto = new BookCreateDto(bookDto.getTitle(), bookDto.getAuthorId(), genreIds);
        bookService.create(createDto);
        return "redirect:/";
    }

    @PostMapping("/books/{id}")
    public String updateBook(@PathVariable String id,
                            @Valid @ModelAttribute BookUpdateDto bookUpdateDto,
                            BindingResult bindingResult,
                            Model model) {
        // Set the id from path variable to ensure consistency
        bookUpdateDto.setId(id);
        
        if (bindingResult.hasErrors()) {
            setupFormModelWithBookId(model, id, null);
            return "book/form";
        }
        
        Set<String> genreIds = bookUpdateDto.getGenreIds();
        if (genreIds == null) {
            genreIds = Set.of();
            bookUpdateDto.setGenreIds(genreIds);
        }
        bookService.update(bookUpdateDto);
        return "redirect:/";
    }

    @GetMapping("/books/{id}/delete")
    public String deleteBookConfirm(@PathVariable String id, Model model) {
        var book = bookService.findById(id);
        model.addAttribute("book", book);
        return "book/delete";
    }

    @PostMapping("/books/{id}/delete")
    public String deleteBook(@PathVariable String id) {
        bookService.deleteById(id);
        return "redirect:/";
    }


    private void setupFormModel(Model model, Book book, String error) {
        model.addAttribute("book", book);
        model.addAttribute("authors", authorService.findAll());
        model.addAttribute("genres", genreService.findAll());
        model.addAttribute("error", error);
    }

    private void setupFormModelWithBookId(Model model, String id, String error) {
        var book = bookService.findById(id);
        setupFormModel(model, book, error);
    }
}