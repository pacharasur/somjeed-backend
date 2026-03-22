package com.pachara.somjeed.somjeed_chatbot.model.domain;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class UserContext {

    private String userId;
    private LocalDate dueDate;
    private BigDecimal outstandingBalance;
    private boolean hasPaymentToday;
    private List<Transaction> recentTransactions;
}
