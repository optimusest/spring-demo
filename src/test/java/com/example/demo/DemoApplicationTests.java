package com.example.demo;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(properties = {
        "spring.datasource.url=jdbc:h2:mem:history_tests;MODE=PostgreSQL",
        "spring.datasource.username=sa",
        "spring.datasource.password="
})
@AutoConfigureMockMvc
@Transactional
class DemoApplicationTests {

    @Autowired
    private MockMvc mvc;

    @Test
    void contextLoads() {
    }

    @Test
    void recordsBothOperationsAndPreservesExactDecimalText() throws Exception {
        mvc.perform(post("/api/sum").param("first", "9007199254740993.25").param("second", "-0.125"))
                .andExpect(status().isOk())
                .andExpect(content().string("Sum: 9007199254740993.125"));

        mvc.perform(post("/api/subtract").param("first", "2").param("second", "4.5"))
                .andExpect(status().isOk())
                .andExpect(content().string("Difference: -2.5"));

        mvc.perform(get("/api/history"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].operation").value("Subtract"))
                .andExpect(jsonPath("$[0].result").value("-2.5"))
                .andExpect(jsonPath("$[0].createdAt").isNotEmpty())
                .andExpect(jsonPath("$[1].operation").value("Add"))
                .andExpect(jsonPath("$[1].firstOperand").value("9007199254740993.25"))
                .andExpect(jsonPath("$[1].secondOperand").value("-0.125"))
                .andExpect(jsonPath("$[1].result").value("9007199254740993.125"));
    }

    @Test
    void rejectsInvalidInputWithoutRecordingIt() throws Exception {
        mvc.perform(post("/api/sum").param("first", "1"))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("Second operand is required."));

        mvc.perform(post("/api/subtract").param("first", "two").param("second", "1"))
                .andExpect(status().isBadRequest());

        mvc.perform(get("/api/history"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));
    }

    @Test
    void returnsTheNewestTwentyCalculationsInReverseOrder() throws Exception {
        for (int i = 1; i <= 21; i++) {
            mvc.perform(post("/api/sum").param("first", Integer.toString(i)).param("second", "0"))
                    .andExpect(status().isOk());
        }

        mvc.perform(get("/api/history"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(20)))
                .andExpect(jsonPath("$[0].result").value("21"))
                .andExpect(jsonPath("$[19].result").value("2"));
    }

}
