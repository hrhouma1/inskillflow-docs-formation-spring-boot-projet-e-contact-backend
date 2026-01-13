package com.example.contact.config;

import com.example.contact.model.User;
import com.example.contact.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class DataInitializer implements CommandLineRunner {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {
        // Créer un admin par défaut si aucun n'existe
        if (userRepository.count() == 0) {
            User admin = User.builder()
                    .email("admin@example.com")
                    .password(passwordEncoder.encode("admin123"))
                    .firstName("Admin")
                    .lastName("System")
                    .role(User.Role.ADMIN)
                    .build();

            userRepository.save(admin);
            log.info("Admin par défaut créé: admin@example.com / admin123");
        }
    }
}

