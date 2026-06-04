package com.blooddonation.init;

import com.blooddonation.enums.Role;
import com.blooddonation.model.User;
import com.blooddonation.repository.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class DataInitializer implements CommandLineRunner {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public DataInitializer(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) throws Exception {
        if (userRepository.findByEmail("admin@bloodconnect.com").isEmpty()) {
            log.info("No admin user found. Seeding a default admin user...");
            User admin = User.builder()
                    .name("System Admin")
                    .email("admin@bloodconnect.com")
                    .password(passwordEncoder.encode("adminpassword"))
                    .phone("1234567890")
                    .role(Role.ADMIN)
                    .city("System")
                    .state("System")
                    .isAvailable(false)
                    .build();
            userRepository.save(admin);
            log.info("Default admin user registered with email: admin@bloodconnect.com and password: adminpassword");
        } else {
            log.info("Admin user already exists in the database. Skipping seeding.");
        }
    }
}
