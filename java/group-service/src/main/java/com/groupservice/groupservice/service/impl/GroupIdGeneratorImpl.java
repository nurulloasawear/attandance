package com.groupservice.groupservice.service.impl;

import com.groupservice.groupservice.repository.GroupRepository;
import com.groupservice.groupservice.service.GroupIdGenerator;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Component
@RequiredArgsConstructor
public class GroupIdGeneratorImpl implements GroupIdGenerator {

    private final GroupRepository groupRepository;
    private final SecureRandom rnd = new SecureRandom();

    @Override
    public String nextGroupPublicId() {
        for (int attempt = 0; attempt < 50; attempt++) {
            String id = "GRP-" + uniqueDigits(6);
            if (!groupRepository.existsByGroupPublicId(id)) return id;
        }
        for (int attempt = 0; attempt < 50; attempt++) {
            String id = "GRP-" + uniqueDigits(7);
            if (!groupRepository.existsByGroupPublicId(id)) return id;
        }
        throw new IllegalStateException("Cannot generate unique group id");
    }

    private String uniqueDigits(int n) {
        List<Integer> digits = new ArrayList<>();
        for (int i = 0; i <= 9; i++) digits.add(i);
        Collections.shuffle(digits, rnd);
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < n; i++) sb.append(digits.get(i));
        return sb.toString();
    }
}