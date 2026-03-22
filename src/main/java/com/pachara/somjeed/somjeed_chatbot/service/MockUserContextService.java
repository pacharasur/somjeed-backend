package com.pachara.somjeed.somjeed_chatbot.service;

import com.pachara.somjeed.somjeed_chatbot.model.domain.Transaction;
import com.pachara.somjeed.somjeed_chatbot.model.domain.UserContext;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class MockUserContextService implements UserContextService {

    private final Clock clock;

    @Override
    public UserContext getUserContext(String userId) {
        LocalDate today = LocalDate.now(clock);
        LocalDateTime now = LocalDateTime.now(clock);

        if ("user_001".equalsIgnoreCase(userId)) {
            return new UserContext(
                    "user_001",
                    today.minusDays(5),
                    new BigDecimal("120000"),
                    false,
                    Collections.emptyList()
            );
        }

        if ("user_002".equalsIgnoreCase(userId)) {
            return new UserContext(
                    "user_002",
                    today.plusDays(5),
                    new BigDecimal("80000"),
                    true,
                    Collections.emptyList()
            );
        }

        if ("user_003".equalsIgnoreCase(userId)) {
            return new UserContext(
                    "user_003",
                    today.plusDays(5),
                    new BigDecimal("45000"),
                    false,
                    List.of(
                            new Transaction(new BigDecimal("2500"), now.minusMinutes(3)),
                            new Transaction(new BigDecimal("2500"), now.minusMinutes(1)),
                            new Transaction(new BigDecimal("1200"), now.minusMinutes(20))
                    )
            );
        }

        return new UserContext(
                userId,
                today.plusDays(10),
                new BigDecimal("0"),
                false,
                Collections.emptyList()
        );
    }
}
