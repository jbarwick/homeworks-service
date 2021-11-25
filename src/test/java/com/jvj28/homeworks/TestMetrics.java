package com.jvj28.homeworks;

import com.jvj28.homeworks.data.Model;
import com.jvj28.homeworks.service.HomeworksProcessor;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.fail;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;

@AutoConfigureMockMvc
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
public class TestMetrics {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private HomeworksProcessor processor;

    @Test
    public void TestServiceMetrics() {
        try {
            assertNotNull(mockMvc);

            System.out.println("Sleeping...");
            Thread.sleep(10000); // Pause for startup to begin
            while (!processor.isQueueEmpty()) Thread.sleep(1000);
            System.out.println("Ready.");

            ResultActions result = this.mockMvc
                    .perform(get("/metrics"))
                    .andExpect(MockMvcResultMatchers.status().isOk());
            System.out.println(result.andReturn().getResponse().getContentAsString());

        } catch (Exception e) {
            fail(e.getMessage());
        }
    }
}
