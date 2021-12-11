package com.jvj28.homeworks;

import com.jvj28.homeworks.command.Netstat;
import com.jvj28.homeworks.command.RequestZoneLevel;
import com.jvj28.homeworks.model.Model;
import com.jvj28.homeworks.model.db.entity.CircuitEntity;
import com.jvj28.homeworks.processor.HomeworksConfiguration;
import com.jvj28.homeworks.processor.HomeworksProcessor;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
class TestDataModel {

    @Autowired
    private HomeworksConfiguration service;

    @Autowired
    private HomeworksProcessor processor;

    @Autowired
    private Model model;

    @Test
    void TestData() {

        String zone_name = "01:05:05:05";

        try {

            processor.sendCommand(Netstat.class).onComplete(System.out::println).get();

            CircuitEntity circuit = model.findCircuitByAddress(zone_name);
            assertNotNull(circuit);
            System.out.println(circuit);
            processor.sendCommand(new RequestZoneLevel(circuit.getAddress())).onComplete(System.out::println).get();

            circuit.setLevel(23);
            model.saveCircuit(circuit);

            circuit = model.findCircuitByAddress("01:05:05:05");
            assertNotNull(circuit);
            System.out.println(circuit);
            assertEquals(23, circuit.getLevel());

            List<CircuitEntity> circuits = model.getCircuits();
            circuit = circuits.stream().filter(c -> c.getId()==20).findFirst().orElse(null);
            assertNotNull(circuit);
            assertEquals(20, circuit.getId());
            System.out.println(circuit);
            assertEquals(23, circuit.getLevel());


        } catch (Exception e) {
            fail(e.getMessage());
        }
    }
}
