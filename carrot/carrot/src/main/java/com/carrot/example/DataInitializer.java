package com.carrot.example;

import com.carrot.example.user.*;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Set;

@Configuration
public class DataInitializer {

    @Bean
    CommandLineRunner initUsers(UserAccountRepository repo, PasswordEncoder encoder) {
        return args -> {
            if (!repo.existsByEmail("admin@example.com")) {
                repo.save(UserAccount.builder()
                        .email("admin@example.com")
                        .password(encoder.encode("admin1234"))
                        .roles(Set.of(Role.ROLE_ADMIN))
                        .build());
            }
            if (!repo.existsByEmail("user@example.com")) {
                repo.save(UserAccount.builder()
                        .email("user@example.com")
                        .password(encoder.encode("user1234"))
                        .roles(Set.of(Role.ROLE_USER))
                        .build());
            }
        };
    }
}
