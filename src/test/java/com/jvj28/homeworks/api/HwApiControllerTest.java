package com.jvj28.homeworks.api;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jvj28.homeworks.api.contract.HelpResponse;
import com.jvj28.homeworks.api.contract.UsageByDayResponse;
import com.jvj28.homeworks.model.Model;
import com.jvj28.homeworks.model.data.LinkStatusData;
import com.jvj28.homeworks.model.data.NetstatData;
import com.jvj28.homeworks.model.data.StatusData;
import com.jvj28.homeworks.model.db.entity.UsageByDayEntity;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Date;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;
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

            // we are not required to login to get help
            MockHttpServletResponse response = mockMvc.perform(get("/api/help")).andExpect(status().isOk()).andReturn().getResponse();
            HelpResponse help = new ObjectMapper().readValue(response.getContentAsString(), HelpResponse.class);
            assertNotNull(help);

        } catch (Exception e) {
            fail(e.getMessage());
        }
    }

    @Test
    void getStatus() {

        try {

            StatusData testData = new StatusData();
            testData.setLoggedIn(true);
            testData.setProcessorId("Id");
            testData.setBootRevision("Rev");
            testData.setMode("Mode");
            testData.setModel("Model");
            testData.setOsRevision("OsRev1");
            testData.setProcessorAddress("11:22:33:44");
            testData.setProcessorInfo("Not Running");

            when(model.get(StatusData.class)).thenReturn(testData);
            MockHttpServletResponse response = mockMvc.perform(get("/api/status")).andExpect(status().isOk()).andReturn().getResponse();
            StatusData status = new ObjectMapper().readValue(response.getContentAsString(), StatusData.class);
            assertEquals(testData, status);

        } catch (Exception e) {
            fail(e.getMessage());
        }
    }

    @Test
    void getNetstat() {
        try {

            NetstatData testData = new NetstatData();
            testData.setBufferHWM("HWM");
            testData.setErrorRx(3234234);
            testData.setErrorTx(234234);
            testData.setGateway("Gw");
            testData.setFtpPort(21);
            testData.setHttpPort(80);
            testData.setIpAddress("1.2.3.4");
            testData.setMacAddress("1:2:3:4:5");
            testData.setPingResponse(true);
            testData.setSubnetMask("255.255.250.0");
            testData.setSuccessfulRx(2342342);
            testData.setSuccessfulTx(99393923);
            testData.setTelnetPort(23);
            when(model.get(NetstatData.class)).thenReturn(testData);
            MockHttpServletResponse response = mockMvc.perform(get("/api/netstat")).andExpect(status().isOk()).andReturn().getResponse();
            NetstatData status = new ObjectMapper().readValue(response.getContentAsString(), NetstatData.class);
            assertEquals(testData, status);

        } catch (Exception e) {
            fail(e.getMessage());
        }
    }

    @Test
    void getLinkStatus() {
        try {
            LinkStatusData linkStatusData = new LinkStatusData();
            linkStatusData.setLinkStatus("OK");
            linkStatusData.setProcessorId("01");
            when(model.get(LinkStatusData.class)).thenReturn(linkStatusData);
            MockHttpServletResponse response = mockMvc.perform(get("/api/linkstatus")).andExpect(status().isOk()).andReturn().getResponse();
            LinkStatusData status = new ObjectMapper().readValue(response.getContentAsString(), LinkStatusData.class);
            assertEquals(linkStatusData, status);
        } catch (Exception e) {
            fail(e.getMessage());
        }
    }

    @Test
    void getUsageByDay() {
        try {
            UsageByDayEntity usageByDay = new UsageByDayEntity();
            Date startDate = new Date();
            Date endDate = new Date();
            when(model.getUsageByDayBetweenDate(startDate, endDate)).thenReturn(List.of(usageByDay));
            MockHttpServletResponse response = mockMvc.perform(get("/api/usagebyday")).andExpect(status().isOk()).andReturn().getResponse();
            UsageByDayResponse byday = new ObjectMapper().readValue(response.getContentAsString(), UsageByDayResponse.class);
            List<UsageByDayEntity> item = byday.getUsageByDayList();
            assertEquals(1, item.size());
            assertEquals(usageByDay, item.get(0));
        } catch (Exception e) {
            fail(e.getMessage());
        }
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