package com.attendance.userservice.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.security.SecureRandom;
import java.util.List;

@Component
@RequiredArgsConstructor
public class PublicIdGeneratorImpl {

    private final JdbcTemplate jdbc;
    private final SecureRandom random = new SecureRandom();

    public String generateUnique() {
        return generateUnique8Digits();
    }

    public String generateUnique8Digits() {
        for (int attempt = 0; attempt < 50; attempt++) {

            int[] digits = {0,1,2,3,4,5,6,7,8,9};

            for (int i = digits.length - 1; i > 0; i--) {
                int j = random.nextInt(i + 1);
                int tmp = digits[i];
                digits[i] = digits[j];
                digits[j] = tmp;
            }

            if (digits[0] == 0) {
                for (int k = 1; k < 8; k++) {
                    if (digits[k] != 0) {
                        int tmp = digits[0];
                        digits[0] = digits[k];
                        digits[k] = tmp;
                        break;
                    }
                }
            }

            StringBuilder sb = new StringBuilder(8);
            for (int i = 0; i < 8; i++) sb.append(digits[i]);

            String id = sb.toString();

            List<Integer> rows = jdbc.queryForList("""
                SELECT 1
                FROM users
                WHERE public_id = ?
                  AND deleted_at IS NULL
                LIMIT 1
            """, Integer.class, id);

            if (rows.isEmpty()) {
                return id;
            }
        }

        throw new IllegalStateException("Cannot generate unique publicId after 50 attempts");
    }
}
