package com.pachara.somjeed.somjeed_chatbot.model.domain;

import com.pachara.somjeed.somjeed_chatbot.enums.PredictionTypeEnum;
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
public class ChatContext {

    private LocalDate dueDate;
    private BigDecimal outstandingBalance;
    private Integer rewardPoints;
    private LocalDate lastPaymentDate;
    private List<Transaction> transactions;
    private PredictionTypeEnum lastPredictionType;
    private boolean awaitingConfirmation;
    private boolean awaitingCancellation;
}
