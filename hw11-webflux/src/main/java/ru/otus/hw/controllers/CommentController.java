package ru.otus.hw.controllers;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import reactor.core.publisher.Mono;
import ru.otus.hw.dto.CommentCreateDto;
import ru.otus.hw.dto.CommentUpdateDto;
import ru.otus.hw.services.BookService;
import ru.otus.hw.services.CommentService;

@Controller
@RequiredArgsConstructor
public class CommentController {

    private final CommentService commentService;

    private final BookService bookService;

    @GetMapping("/books/{bookId}/comments/new")
    public Mono<String> newCommentForm(@PathVariable String bookId, Model model) {
        return bookService.findById(bookId)
                .map(book -> {
                    model.addAttribute("book", book);
                    return "comment/form";
                })
                .onErrorResume(Exception.class, e -> Mono.just("redirect:/"));
    }

    @PostMapping("/books/{bookId}/comments")
    public Mono<String> saveComment(@PathVariable String bookId,
                                   @RequestParam String text) {
        var createDto = new CommentCreateDto(text, bookId);
        return commentService.create(createDto)
                .map(createdComment -> "redirect:/books/" + bookId)
                .onErrorResume(Exception.class, e -> Mono.just("redirect:/"));
    }

    @GetMapping("/comments/{id}/edit")
    public Mono<String> editCommentForm(@PathVariable String id, Model model) {
        return commentService.findById(id)
                .map(comment -> {
                    model.addAttribute("comment", comment);
                    return "comment/edit";
                })
                .onErrorResume(Exception.class, e -> Mono.just("redirect:/"));
    }

    @PostMapping("/comments/{id}")
    public Mono<String> updateComment(@PathVariable String id,
                                     @RequestParam String text) {
        return commentService.findById(id)
                .flatMap(comment -> {
                    var updateDto = new CommentUpdateDto(id, text);
                    return commentService.update(updateDto)
                            .map(updatedComment -> "redirect:/books/" + comment.getBookId());
                })
                .onErrorResume(Exception.class, e -> Mono.just("redirect:/"));
    }

    @GetMapping("/comments/{id}/delete")
    public Mono<String> deleteCommentConfirm(@PathVariable String id, Model model) {
        return commentService.findById(id)
                .map(comment -> {
                    model.addAttribute("comment", comment);
                    return "comment/delete";
                })
                .onErrorResume(Exception.class, e -> Mono.just("redirect:/"));
    }

    @PostMapping("/comments/{id}/delete")
    public Mono<String> deleteComment(@PathVariable String id) {
        return commentService.findById(id)
                .flatMap(comment -> {
                    String bookId = comment.getBookId();
                    return commentService.deleteById(id)
                            .then(Mono.just("redirect:/books/" + bookId));
                })
                .onErrorResume(Exception.class, e -> Mono.just("redirect:/"));
    }
}