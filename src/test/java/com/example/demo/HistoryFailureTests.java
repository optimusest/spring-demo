package com.example.demo;

import org.junit.jupiter.api.Test;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class HistoryFailureTests {

    @Test
    void returnsServiceUnavailableWhenTheHistoryTableIsMissing() throws Exception {
        DriverManagerDataSource database = new DriverManagerDataSource("jdbc:h2:mem:", "sa", "");
        CalculationHistoryRepository history = new CalculationHistoryRepository(JdbcClient.create(database));
        MockMvc mvc = MockMvcBuilders.standaloneSetup(new SumController(history)).build();

        mvc.perform(post("/api/sum").param("first", "1").param("second", "2"))
                .andExpect(status().isServiceUnavailable())
                .andExpect(content().string("Could not save the calculation to history."));

        mvc.perform(get("/api/history"))
                .andExpect(status().isServiceUnavailable())
                .andExpect(content().string("Could not load calculation history."));
    }
}
