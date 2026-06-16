package com.junmo.certificatesystem.service.user;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.junmo.certificatesystem.common.enums.RoleType;
import com.junmo.certificatesystem.entity.User;
import com.junmo.certificatesystem.repository.UserRepository;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class CsvUserImportService implements CommandLineRunner {

    private final UserRepository userRepository;

    @Value("classpath:data/users.csv")
    private Resource usersCsv;

    @Override
    @Transactional
    public void run(String... args) throws Exception {
        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(usersCsv.getInputStream(), StandardCharsets.UTF_8))) {
            String line = reader.readLine();
            while ((line = reader.readLine()) != null) {
                if (line.isBlank()) {
                    continue;
                }
                importLine(line);
            }
        }
    }

    private void importLine(String line) {
        String[] columns = line.split(",", -1);
        if (columns.length < 5) {
            return;
        }

        String userId = columns[0].trim();
        if (userRepository.existsByUserId(userId)) {
            return;
        }

        User user = new User();
        user.setUserId(userId);
        user.setPassword(columns[1].trim());
        user.setName(columns[2].trim());
        user.setHireDate(LocalDate.parse(columns[3].trim()));
        user.setRole("Y".equalsIgnoreCase(columns[4].trim()) ? RoleType.ADMIN : RoleType.USER);
        userRepository.save(user);
    }
}
