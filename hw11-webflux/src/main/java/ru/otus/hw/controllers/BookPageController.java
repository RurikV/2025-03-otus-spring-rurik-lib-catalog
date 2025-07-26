package ru.otus.hw.controllers;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import reactor.core.publisher.Mono;
import ru.otus.hw.dto.BookCreateDto;
import ru.otus.hw.dto.BookDto;
import ru.otus.hw.dto.BookFormDto;
import ru.otus.hw.dto.BookUpdateDto;
import ru.otus.hw.exceptions.EntityNotFoundException;
import ru.otus.hw.services.AuthorService;
import ru.otus.hw.services.BookService;
import ru.otus.hw.services.CommentService;
import ru.otus.hw.services.GenreService;

@Controller
@RequiredArgsConstructor
public class BookPageController {

    private final AuthorService authorService;
    
    private final GenreService genreService;
    
    private final BookService bookService;
    
    private final CommentService commentService;

    @GetMapping("/")
    public String listBooks() {
        return "book/list";
    }

    @GetMapping("/books")
    public String listBooksAlternative() {
        return "book/list";
    }

    @GetMapping("/books/{id}")
    public Mono<String> viewBook(@PathVariable String id, Model model) {
        model.addAttribute("bookId", id);
        return Mono.zip(bookService.findById(id), commentService.findByBookId(id).collectList())
                .map(tuple -> {
                    model.addAttribute("book", tuple.getT1());
                    model.addAttribute("comments", tuple.getT2());
                    return "book/view";
                });
    }

    @GetMapping("/books/new")
    public Mono<String> newBookForm(Model model) {
        model.addAttribute("book", new BookDto());
        return Mono.zip(authorService.findAll().collectList(), genreService.findAll().collectList())
                .map(tuple -> {
                    model.addAttribute("authors", tuple.getT1());
                    model.addAttribute("genres", tuple.getT2());
                    return "book/form";
                });
    }

    @GetMapping("/books/{id}/edit")
    public Mono<String> editBookForm(@PathVariable String id, Model model) {
        model.addAttribute("bookId", id);
        return Mono.zip(
                bookService.findById(id),
                authorService.findAll().collectList(),
                genreService.findAll().collectList()
        ).map(tuple -> {
            model.addAttribute("book", tuple.getT1());
            model.addAttribute("authors", tuple.getT2());
            model.addAttribute("genres", tuple.getT3());
            return "book/form";
        });
    }

    @GetMapping("/books/{id}/delete")
    public String deleteBookConfirm(@PathVariable String id, Model model) {
        model.addAttribute("bookId", id);
        return "book/delete";
    }

    @PostMapping("/books")
    public Mono<String> createBook(@ModelAttribute BookFormDto formDto,
                                 Model model) {
        var createDto = new BookCreateDto(formDto.getTitle(), formDto.getAuthorId(), formDto.getGenreIds());
        return bookService.create(createDto)
                .map(createdBook -> "redirect:/")
                .onErrorResume(EntityNotFoundException.class, e -> {
                    model.addAttribute("error", e.getMessage());
                    model.addAttribute("book", new BookDto(null, formDto.getTitle(), null, null));
                    return Mono.zip(authorService.findAll().collectList(), genreService.findAll().collectList())
                            .map(tuple -> {
                                model.addAttribute("authors", tuple.getT1());
                                model.addAttribute("genres", tuple.getT2());
                                return "book/form";
                            });
                })
                .onErrorResume(IllegalArgumentException.class, e -> {
                    model.addAttribute("error", e.getMessage());
                    model.addAttribute("book", new BookDto(null, formDto.getTitle(), null, null));
                    return Mono.zip(authorService.findAll().collectList(), genreService.findAll().collectList())
                            .map(tuple -> {
                                model.addAttribute("authors", tuple.getT1());
                                model.addAttribute("genres", tuple.getT2());
                                return "book/form";
                            });
                });
    }

    @PostMapping("/books/{id}")
    public Mono<String> updateBook(@PathVariable String id,
                                 @ModelAttribute BookFormDto formDto,
                                 Model model) {
        var updateDto = new BookUpdateDto(id, formDto.getTitle(), formDto.getAuthorId(), formDto.getGenreIds());
        return bookService.update(updateDto)
                .map(updatedBook -> "redirect:/books/" + id)
                .onErrorResume(EntityNotFoundException.class, e -> {
                    model.addAttribute("error", e.getMessage());
                    model.addAttribute("bookId", id);
                    return Mono.zip(
                            bookService.findById(id),
                            authorService.findAll().collectList(),
                            genreService.findAll().collectList()
                    ).map(tuple -> {
                        model.addAttribute("book", tuple.getT1());
                        model.addAttribute("authors", tuple.getT2());
                        model.addAttribute("genres", tuple.getT3());
                        return "book/form";
                    });
                })
                .onErrorResume(IllegalArgumentException.class, e -> {
                    model.addAttribute("error", e.getMessage());
                    model.addAttribute("bookId", id);
                    return Mono.zip(
                            bookService.findById(id),
                            authorService.findAll().collectList(),
                            genreService.findAll().collectList()
                    ).map(tuple -> {
                        model.addAttribute("book", tuple.getT1());
                        model.addAttribute("authors", tuple.getT2());
                        model.addAttribute("genres", tuple.getT3());
                        return "book/form";
                    });
                });
    }
}