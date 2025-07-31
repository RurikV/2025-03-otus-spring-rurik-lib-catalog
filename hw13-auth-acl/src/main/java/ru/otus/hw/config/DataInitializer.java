package ru.otus.hw.config;

import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import ru.otus.hw.models.User;
import ru.otus.hw.repositories.UserRepository;

@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final UserRepository userRepository;

    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {
        if (userRepository.count() == 0) {
            // Create admin user
            User admin = new User("admin", passwordEncoder.encode("admin"), "ADMIN", true);
            userRepository.save(admin);

            // Create regular user
            User user = new User("user", passwordEncoder.encode("user"), "USER", true);
            userRepository.save(user);

            System.out.println("Demo users created:");
            System.out.println("Admin: admin/admin");
            System.out.println("User: user/user");
        }
    }
}