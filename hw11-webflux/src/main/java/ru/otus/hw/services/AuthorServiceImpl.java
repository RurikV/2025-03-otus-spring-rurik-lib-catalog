package ru.otus.hw.services;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import ru.otus.hw.dto.AuthorDto;
import ru.otus.hw.models.Author;
import ru.otus.hw.repositories.AuthorRepository;

@RequiredArgsConstructor
@Service
public class AuthorServiceImpl implements AuthorService {
    private final AuthorRepository authorRepository;

    @Override
    public Flux<AuthorDto> findAll() {
        return authorRepository.findAll()
                .map(this::toAuthorDto);
    }

    private AuthorDto toAuthorDto(Author author) {
        return new AuthorDto(author.getId(), author.getFullName());
    }
}
