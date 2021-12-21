package com.jvj28.homeworks;

import com.jvj28.homeworks.processor.Processor;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.fail;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;

@AutoConfigureMockMvc
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
class TestMetrics {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private Processor processor;

    @Test
    void TestServiceMetrics() {
        try {
            assertNotNull(mockMvc);

            System.out.println("Sleeping...");
            TimeUnit.SECONDS.sleep(10);
            while (processor.queueIsNotEmpty())
                TimeUnit.SECONDS.sleep(1);
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
