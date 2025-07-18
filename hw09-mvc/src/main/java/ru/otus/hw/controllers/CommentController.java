package ru.otus.hw.controllers;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import ru.otus.hw.services.BookService;
import ru.otus.hw.services.CommentService;

@Controller
@RequiredArgsConstructor
public class CommentController {

    private final CommentService commentService;

    private final BookService bookService;

    @GetMapping("/books/{bookId}/comments/new")
    public String newCommentForm(@PathVariable String bookId, Model model) {
        try {
            var book = bookService.findById(bookId);
            model.addAttribute("book", book);
            return "comment/form";
        } catch (Exception e) {
            return "redirect:/";
        }
    }

    @PostMapping("/books/{bookId}/comments")
    public String saveComment(@PathVariable String bookId,
                             @RequestParam String text) {
        commentService.insert(text, bookId);
        return "redirect:/books/" + bookId;
    }

    @GetMapping("/comments/{id}/edit")
    public String editCommentForm(@PathVariable String id, Model model) {
        var comment = commentService.findById(id);
        if (comment.isEmpty()) {
            return "redirect:/";
        }
        model.addAttribute("comment", comment.get());
        return "comment/edit";
    }

    @PostMapping("/comments/{id}")
    public String updateComment(@PathVariable String id,
                               @RequestParam String text) {
        var comment = commentService.findById(id);
        if (comment.isEmpty()) {
            return "redirect:/";
        }
        commentService.update(id, text);
        return "redirect:/books/" + comment.get().getBook().getId();
    }

    @GetMapping("/comments/{id}/delete")
    public String deleteCommentConfirm(@PathVariable String id, Model model) {
        var comment = commentService.findById(id);
        if (comment.isEmpty()) {
            return "redirect:/";
        }
        model.addAttribute("comment", comment.get());
        return "comment/delete";
    }

    @PostMapping("/comments/{id}/delete")
    public String deleteComment(@PathVariable String id) {
        var comment = commentService.findById(id);
        if (comment.isPresent()) {
            String bookId = comment.get().getBook().getId();
            commentService.deleteById(id);
            return "redirect:/books/" + bookId;
        }
        return "redirect:/";
    }
}