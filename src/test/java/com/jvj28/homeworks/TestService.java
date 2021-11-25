package com.jvj28.homeworks;

import com.jvj28.homeworks.command.*;
import com.jvj28.homeworks.data.model.Circuit;
import com.jvj28.homeworks.data.model.Keypad;
import com.jvj28.homeworks.service.HomeworksConfiguration;
import com.jvj28.homeworks.data.model.Status;
import com.jvj28.homeworks.data.Model;
import com.jvj28.homeworks.service.HomeworksProcessor;
import com.jvj28.homeworks.util.Promise;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(webEnvironment = WebEnvironment.DEFINED_PORT)
public class TestService {

    @Autowired
    private HomeworksProcessor processor;

    @Autowired
    private Model model;

    @Test
    public void testTelnetConnection() {

        try {

            assertNotNull(processor);
            processor.sendCommand(Netstat.class).onComplete(System.out::println);
            processor.sendCommand(new RequestSystemTime()).onComplete(System.out::println);
            Promise<RequestSystemDate> rd = processor.sendCommand(new RequestSystemDate()).onComplete(System.out::println);

            while (!processor.isQueueEmpty()) Thread.sleep(1000);
            assertNotNull(rd.getCommand().getDate());
            System.out.println("Processor date = " + rd.getCommand().getDate());

            System.out.println("All command initialized.... retrieving status class");

            Status hw = model.get(Status.class);
            assertNotNull(hw);
            System.out.println(hw);
        } catch (Exception e) {
            fail(e.getMessage());
        }
    }

    @Test
    public void testReadSeedData() {

        try {
            System.out.println("Letting the system warm up");
            Thread.sleep(10000); // Let the system warm up

            System.out.println("Getting Zone Details for Test");
            Circuit zone = model.findCircuitByAddress("01:05:05:01");
            processor.sendCommand(new RequestZoneLevel(zone.getAddress())).onComplete(
                    p -> {
                        Circuit circuit = model.findCircuitByAddress(p.getAddress());
                        circuit.setLevel(p.getLevel());
                        model.saveCircuit(circuit);
                        System.out.printf("Circuit [%s] at %d%% %n", p.getAddress(), p.getLevel());
                    }
            ).get();
            System.out.println("Waiting for all commands to complete");
            while (!processor.isQueueEmpty()) Thread.sleep(1000);
            System.out.println("Done Waiting!");

            List<Circuit> alldata = model.getCircuits();
            alldata.forEach(System.out::println);

            System.out.println("Total Watts: " + model.getCurrentUsage());

            zone = model.findCircuitByAddress("01:05:05:01");
            assertNotNull(zone);
            model.saveCircuit(zone);
            System.out.println(zone);

            List<Keypad> kpData = model.getKeypadsSeedData();
            assertNotNull(kpData);
            assertTrue(kpData.size() > 0);
            model.saveKeypads(kpData);
            System.out.println(kpData);
            List<Keypad> allkeys = model.geKeypads();
            allkeys.forEach(System.out::println);

            Keypad kp = kpData.stream().filter(k -> k.getId() == 2).findFirst().orElse(null);
            assertNotNull(kp);
            assertEquals("1:6:6", kp.getAddress());

            Keypad keypad = model.findKeypadByAddress("1:6:6");
            assertNotNull(keypad);
            model.saveKeypad(keypad);
            System.out.println(keypad);

        } catch (Exception e) {
            fail(e.getMessage());
        }
    }
}
