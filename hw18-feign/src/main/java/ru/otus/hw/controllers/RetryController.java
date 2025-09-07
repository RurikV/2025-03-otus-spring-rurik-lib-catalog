package ru.otus.hw.controllers;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.otus.hw.services.SpringRetryService;

@RestController
@RequestMapping("/api/retry")
@RequiredArgsConstructor
public class RetryController {

    private final SpringRetryService springRetryService;

    @GetMapping("/status/{code}")
    public String getStatusWithRetry(@PathVariable int code) {
        return springRetryService.getStatusWithRetry(code);
    }

    @GetMapping("/delay/{seconds}")
    public String getDelayedWithRetry(@PathVariable int seconds) {
        return springRetryService.getDelayedWithRetry(seconds);
    }

    @GetMapping("/simulate/{input}")
    public String simulateFailingOperation(@PathVariable String input) {
        return springRetryService.simulateFailingOperation(input);
    }
}