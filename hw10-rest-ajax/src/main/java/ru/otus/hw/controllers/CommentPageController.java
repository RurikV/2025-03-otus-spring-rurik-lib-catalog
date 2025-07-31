package ru.otus.hw.controllers;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@Controller
public class CommentPageController {

    @GetMapping("/books/{bookId}/comments/new")
    public String newCommentForm(@PathVariable String bookId) {
        return "comment/form";
    }

    @GetMapping("/comments/{id}/edit")
    public String editCommentForm(@PathVariable String id) {
        return "comment/edit";
    }

    @GetMapping("/comments/{id}/delete")
    public String deleteCommentConfirm(@PathVariable String id) {
        return "comment/delete";
    }

}