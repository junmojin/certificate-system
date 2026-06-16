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
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class CsvUserImportService implements CommandLineRunner {

    private final UserRepository userRepository;

    @Value("classpath:data/users.csv")
    private Resource usersCsv;

    @Override
    @Transactional
    public void run(String... args) throws Exception {
        int imported = 0;
        int updated = 0;

        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(usersCsv.getInputStream(), StandardCharsets.UTF_8))) {
            reader.readLine();
            String line;
            while ((line = reader.readLine()) != null) {
                if (line.isBlank()) {
                    continue;
                }
                boolean isNew = upsertLine(line);
                if (isNew) {
                    imported++;
                } else {
                    updated++;
                }
            }
        }

        long total = userRepository.count();
        log.info("CSV 사용자 동기화 완료 - 신규: {}, 갱신: {}, 전체: {}", imported, updated, total);
    }

    private boolean upsertLine(String line) {
        String[] columns = line.split(",", -1);
        if (columns.length < 5) {
            return false;
        }

        String userId = columns[0].trim();
        String password = columns[1].trim();
        String name = columns[2].trim();
        LocalDate hireDate = LocalDate.parse(columns[3].trim());
        RoleType role = "Y".equalsIgnoreCase(columns[4].trim()) ? RoleType.ADMIN : RoleType.USER;

        return userRepository.findByUserId(userId)
                .map(user -> {
                    user.setPassword(password);
                    user.setName(name);
                    user.setHireDate(hireDate);
                    user.setRole(role);
                    user.setEnabled(true);
                    return false;
                })
                .orElseGet(() -> {
                    User user = new User();
                    user.setUserId(userId);
                    user.setPassword(password);
                    user.setName(name);
                    user.setHireDate(hireDate);
                    user.setRole(role);
                    userRepository.save(user);
                    return true;
                });
    }
}
