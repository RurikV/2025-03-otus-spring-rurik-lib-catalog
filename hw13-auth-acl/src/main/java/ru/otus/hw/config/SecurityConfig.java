package ru.otus.hw.config;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import ru.otus.hw.security.CustomUserDetailsService;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true)
@RequiredArgsConstructor
public class SecurityConfig {

    private final CustomUserDetailsService userDetailsService;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        return http
            .authorizeHttpRequests(this::configureAuthorization)
            .formLogin(this::configureFormLogin)
            .logout(this::configureLogout)
            .userDetailsService(userDetailsService)
            .build();
    }

    private void configureAuthorization(
            org.springframework.security.config.annotation.web.configurers.AuthorizeHttpRequestsConfigurer<
                    HttpSecurity>.AuthorizationManagerRequestMatcherRegistry authz) {
        authz
            .requestMatchers("/login", "/css/**", "/js/**", "/images/**").permitAll()
            // Write access for books - only ADMIN can create, edit, delete books (must come before general /books rules)
            .requestMatchers("/books/new", "/books/{id}/edit", "/books/{id}/delete").hasRole("ADMIN")
            .requestMatchers(org.springframework.http.HttpMethod.POST, "/books").hasRole("ADMIN")
            .requestMatchers(org.springframework.http.HttpMethod.POST, "/books/{id}").hasRole("ADMIN")
            // Write access for comments - both ADMIN and USER can create comments
            .requestMatchers("/books/{id}/comments/new").hasAnyRole("ADMIN", "USER")
            // Edit/delete comments - handled by method-level security
            .requestMatchers("/comments/{id}/edit", "/comments/{id}/delete").hasAnyRole("ADMIN", "USER")
            // Admin-only areas
            .requestMatchers("/authors/**", "/genres/**").hasRole("ADMIN")
            // Read access - both ADMIN and USER can view books and comments (must come after specific rules)
            .requestMatchers("/", "/books", "/books/{id}").hasAnyRole("ADMIN", "USER")
            .anyRequest().authenticated();
    }

    private void configureFormLogin(
            org.springframework.security.config.annotation.web.configurers.FormLoginConfigurer<HttpSecurity> form) {
        form
            .loginPage("/login")
            .defaultSuccessUrl("/", true)
            .permitAll();
    }

    private void configureLogout(
            org.springframework.security.config.annotation.web.configurers.LogoutConfigurer<HttpSecurity> logout) {
        logout
            .logoutSuccessUrl("/login?logout")
            .permitAll();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}