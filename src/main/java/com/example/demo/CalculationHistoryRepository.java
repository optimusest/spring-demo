package com.example.demo;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;

@Repository
public class CalculationHistoryRepository {

    private final JdbcClient jdbc;

    public CalculationHistoryRepository(JdbcClient jdbc) {
        this.jdbc = jdbc;
    }

    public void save(String operation, BigDecimal first, BigDecimal second, BigDecimal result) {
        int inserted = jdbc.sql("""
                INSERT INTO calculation_history (operation, first_operand, second_operand, result)
                VALUES (:operation, :first, :second, :result)
                """)
                .param("operation", operation)
                .param("first", first)
                .param("second", second)
                .param("result", result)
                .update();
        if (inserted != 1) {
            throw new DataIntegrityViolationException("Expected to save one calculation.");
        }
    }

    public List<CalculationHistoryEntry> latest() {
        return jdbc.sql("""
                SELECT operation, first_operand, second_operand, result, created_at
                FROM calculation_history
                ORDER BY created_at DESC, id DESC
                LIMIT 20
                """)
                .query((row, rowNumber) -> new CalculationHistoryEntry(
                        row.getString("operation"),
                        row.getBigDecimal("first_operand").toPlainString(),
                        row.getBigDecimal("second_operand").toPlainString(),
                        row.getBigDecimal("result").toPlainString(),
                        row.getObject("created_at", OffsetDateTime.class).toInstant()))
                .list();
    }
}
