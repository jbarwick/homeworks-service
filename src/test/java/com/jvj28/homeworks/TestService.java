package com.jvj28.homeworks;

import com.jvj28.homeworks.command.Netstat;
import com.jvj28.homeworks.command.RequestSystemDate;
import com.jvj28.homeworks.command.RequestSystemTime;
import com.jvj28.homeworks.command.RequestZoneLevel;
import com.jvj28.homeworks.model.Model;
import com.jvj28.homeworks.model.db.entity.CircuitEntity;
import com.jvj28.homeworks.model.db.entity.KeypadEntity;
import com.jvj28.homeworks.model.data.StatusData;
import com.jvj28.homeworks.service.HomeworksProcessor;
import com.jvj28.homeworks.util.Promise;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;

import java.util.List;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(webEnvironment = WebEnvironment.DEFINED_PORT)
class TestService {

    @Autowired
    private HomeworksProcessor processor;

    @Autowired
    private Model model;

    @Test
    void testTelnetConnection() {

        try {

            assertNotNull(processor);
            processor.sendCommand(Netstat.class).onComplete(System.out::println);
            processor.sendCommand(new RequestSystemTime()).onComplete(System.out::println);
            Promise<RequestSystemDate> rd = processor.sendCommand(new RequestSystemDate()).onComplete(System.out::println);

            while (processor.queueIsNotEmpty())
                TimeUnit.SECONDS.sleep(1);
            assertNotNull(rd.getCommand().getDate());
            System.out.println("Processor date = " + rd.getCommand().getDate());

            System.out.println("All command initialized.... retrieving status class");

            StatusData hw = model.get(StatusData.class);
            assertNotNull(hw);
            System.out.println(hw);
        } catch (Exception e) {
            fail(e.getMessage());
        }
    }

    @Test
    void testReadSeedData() {

        try {
            System.out.println("Letting the system warm up");
            TimeUnit.SECONDS.sleep(10); // Let the system warm up

            System.out.println("Getting Zone Details for Test");
            CircuitEntity zone = model.findCircuitByAddress("01:05:05:01");
            processor.sendCommand(new RequestZoneLevel(zone.getAddress())).onComplete(
                    p -> {
                        CircuitEntity circuit = model.findCircuitByAddress(p.getAddress());
                        circuit.setLevel(p.getLevel());
                        model.saveCircuit(circuit);
                        System.out.printf("Circuit [%s] at %d%% %n", p.getAddress(), p.getLevel());
                    }
            ).get();
            System.out.println("Waiting for all commands to complete");
            while (processor.queueIsNotEmpty())
                TimeUnit.SECONDS.sleep(1);
            System.out.println("Done Waiting!");

            List<CircuitEntity> alldata = model.getCircuits();
            alldata.forEach(System.out::println);

            System.out.println("Total Watts: " + model.getCurrentUsage());

            zone = model.findCircuitByAddress("01:05:05:01");
            assertNotNull(zone);
            model.saveCircuit(zone);
            System.out.println(zone);

            List<KeypadEntity> kpData = model.getKeypadsSeedData();
            assertNotNull(kpData);
            assertTrue(kpData.size() > 0);
            model.saveKeypads(kpData);
            System.out.println(kpData);
            List<KeypadEntity> allkeys = model.geKeypads();
            allkeys.forEach(System.out::println);

            KeypadEntity kp = kpData.stream().filter(k -> k.getId() == 2).findFirst().orElse(null);
            assertNotNull(kp);
            assertEquals("1:6:6", kp.getAddress());

            KeypadEntity keypad = model.findKeypadByAddress("1:6:6");
            assertNotNull(keypad);
            model.saveKeypad(keypad);
            System.out.println(keypad);

        } catch (Exception e) {
            fail(e.getMessage());
        }
    }
}
