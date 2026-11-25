package com.example.demo;

import java.math.BigDecimal;
import java.util.function.BinaryOperator;

import org.springframework.dao.DataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class SumController {

    private final CalculationHistoryRepository historyRepository;

    public SumController(CalculationHistoryRepository historyRepository) {
        this.historyRepository = historyRepository;
    }

    @PostMapping(value = "/api/sum", produces = "text/plain")
    public ResponseEntity<String> sum(
            @RequestParam(name = "first", defaultValue = "") String first,
            @RequestParam(name = "second", defaultValue = "") String second) {
        return calculate(first, second, "Add", "Sum", BigDecimal::add);
    }

    @PostMapping(value = "/api/subtract", produces = "text/plain")
    public ResponseEntity<String> subtract(
            @RequestParam(name = "first", defaultValue = "") String first,
            @RequestParam(name = "second", defaultValue = "") String second) {
        return calculate(first, second, "Subtract", "Difference", BigDecimal::subtract);
    }

    @GetMapping("/api/history")
    public ResponseEntity<?> history() {
        try {
            return ResponseEntity.ok(historyRepository.latest());
        } catch (DataAccessException e) {
            return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                    .contentType(MediaType.TEXT_PLAIN).body("Could not load calculation history.");
        }
    }

    private ResponseEntity<String> calculate(String first, String second, String operationName, String label,
            BinaryOperator<BigDecimal> operation) {
        try {
            BigDecimal a = parseNumber(first, "First operand");
            BigDecimal b = parseNumber(second, "Second operand");
            BigDecimal result = operation.apply(a, b);
            historyRepository.save(operationName, a, b, result);

            return ResponseEntity.ok(label + ": " + result.toPlainString());
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (DataAccessException e) {
            return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                    .body("Could not save the calculation to history.");
        }
    }

    private BigDecimal parseNumber(String input, String label) {
        String value = input.trim();

        if (value.isEmpty()) {
            throw new IllegalArgumentException(label + " is required.");
        }

        // Accept ordinary decimal notation, with an optional sign.
        if (!value.matches("[+-]?([0-9]+(\\.[0-9]*)?|\\.[0-9]+)")) {
            throw new IllegalArgumentException(
                    label + " must be a number, for example -3 or 2.5. Use a decimal point.");
        }

        return new BigDecimal(value);
    }
}
