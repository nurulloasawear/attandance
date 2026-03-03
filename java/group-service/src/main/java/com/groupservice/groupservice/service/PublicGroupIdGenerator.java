package com.groupservice.groupservice.service;

import com.groupservice.groupservice.repository.GroupRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.security.SecureRandom;

@Component
@RequiredArgsConstructor
public class PublicGroupIdGenerator {

    private static final String ALPHABET = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz";
    private static final int DEFAULT_LEN = 12;

    private final GroupRepository groupRepository;
    private final SecureRandom random = new SecureRandom();

    public String generate() {
        return generate(DEFAULT_LEN);
    }

    public String generate(int length) {
        String id;
        do {
            id = randomString(length);
        } while (groupRepository.existsByPublicId(id));
        return id;
    }

    private String randomString(int length) {
        char[] chars = new char[length];
        for (int i = 0; i < length; i++) {
            chars[i] = ALPHABET.charAt(random.nextInt(ALPHABET.length()));
        }
        return new String(chars);
    }
}