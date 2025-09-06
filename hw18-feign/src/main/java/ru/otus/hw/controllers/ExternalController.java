package ru.otus.hw.controllers;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;
import ru.otus.hw.services.ExternalHttpService;

@RestController
@RequiredArgsConstructor
public class ExternalController {

    private final ExternalHttpService externalHttpService;

    @GetMapping("/external/status/{code}")
    public String status(@PathVariable("code") int code) {
        return externalHttpService.getStatus(code);
    }

    @GetMapping("/external/delay/{seconds}")
    public String delay(@PathVariable("seconds") int seconds) {
        return externalHttpService.getDelayed(seconds);
    }
}
