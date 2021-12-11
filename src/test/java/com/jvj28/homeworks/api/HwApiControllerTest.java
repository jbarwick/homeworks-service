package com.jvj28.homeworks.api;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jvj28.homeworks.api.contract.HelpResponse;
import com.jvj28.homeworks.model.Model;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.web.servlet.MockMvc;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@AutoConfigureMockMvc(addFilters = false)
@ContextConfiguration(classes = { HwApiController.class, HwApiControllerService.class })
@WebMvcTest
class HwApiControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private Model model;

    @Test
    void getHelp() {

        try {
            HelpResponse helpResponse = new HelpResponse();
            String json = new ObjectMapper().writeValueAsString(helpResponse);
            assertNotNull(json);

            MockHttpServletResponse response = mockMvc.perform(get("/api/help")).andExpect(status().isOk()).andReturn().getResponse();
            HelpResponse help = new ObjectMapper().readValue(response.getContentAsString(), HelpResponse.class);
            assertNotNull(help);

        } catch (Exception e) {
            fail(e.getMessage());
        }
    }

    @Test
    void getStatus() {
        assertTrue(true);
    }

    @Test
    void getNetstat() {
        assertTrue(true);
    }

    @Test
    void getLinkStatus() {
        assertTrue(true);
    }

    @Test
    void getUsageByDay() {
        assertTrue(true);
    }

    @Test
    void putCircuitLevelByAddress() {
        assertTrue(true);
    }

    @Test
    void getCircuitByAddress() {
        assertTrue(true);
    }

    @Test
    void getCircuits() {
        assertTrue(true);
    }

    @Test
    void getUsageByHour() {
        assertTrue(true);
    }

    @Test
    void getUsageByMinute() {
        assertTrue(true);
    }

    @Test
    void getUsage() {
        assertTrue(true);
    }

    @Test
    void convertToDate() {
        assertTrue(true);
    }
}