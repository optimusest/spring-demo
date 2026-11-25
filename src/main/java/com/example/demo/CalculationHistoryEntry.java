package com.example.demo;

import java.time.Instant;

public record CalculationHistoryEntry(
        String operation,
        String firstOperand,
        String secondOperand,
        String result,
        Instant createdAt) {
}
