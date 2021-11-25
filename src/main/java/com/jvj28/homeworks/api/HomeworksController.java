package com.jvj28.homeworks.api;

import com.jvj28.homeworks.command.Cmd;
import com.jvj28.homeworks.data.db.UsageByDayRepository;
import com.jvj28.homeworks.data.db.UsageByHourRepository;
import com.jvj28.homeworks.data.db.UsageByMinuteRepository;
import com.jvj28.homeworks.data.model.*;
import com.jvj28.homeworks.data.Model;
import org.apache.logging.log4j.util.Strings;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import com.jvj28.homeworks.data.model.Status;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.*;
import java.util.Date;
import java.util.concurrent.TimeoutException;

@RestController
@RequestMapping("/api")
public class HomeworksController {

    private final Model model;
    private final UsageByDayRepository usageByDay;
    private final UsageByHourRepository usageByHour;
    private final UsageByMinuteRepository usageByMinute;

    public HomeworksController(Model model, UsageByDayRepository usageByDay,
                               UsageByHourRepository usageByHour,
                               UsageByMinuteRepository usageByMinute) {
        this.model = model;
        this.usageByDay = usageByDay;
        this.usageByHour = usageByHour;
        this.usageByMinute = usageByMinute;
    }

    @GetMapping("/help")
    public ResponseEntity<?> getHelp() {
        HelpResponse response = new HelpResponse();
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @GetMapping("/status")
    public ResponseEntity<?> getStatus() {
        try {
            // Pull this and ensure it's cached for 24 hours
            Status result = model.get(Status.class, 86400);
            if (result != null) {
                return new ResponseEntity<>(result, HttpStatus.OK);
            }
            return new ResponseEntity<>("Status Unavailable", HttpStatus.BAD_REQUEST);
        } catch (TimeoutException e) {
            return new ResponseEntity<>( "Timeout", HttpStatus.REQUEST_TIMEOUT);
        } catch (InterruptedException e) {
            return new ResponseEntity<>( "Interrupted", HttpStatus.GONE);
        }
    }

    @GetMapping("/netstat")
    public ResponseEntity<?> getNetstat() {
        try {
            Netstat result = model.get(Netstat.class);
            if (result != null) {
                return new ResponseEntity<>(result, HttpStatus.OK);
            }
            return new ResponseEntity<>("Status Unavailable", HttpStatus.BAD_REQUEST);
        } catch (TimeoutException e) {
            return new ResponseEntity<>( "Timeout", HttpStatus.REQUEST_TIMEOUT);
        } catch (InterruptedException e) {
            return new ResponseEntity<>( "Interrupted", HttpStatus.GONE);
        }
    }

    @GetMapping("/linkstatus")
    public ResponseEntity<?> getLinkStatus() {
        try {
            LinkStatus result = model.get(LinkStatus.class);
            if (result != null) {
                return new ResponseEntity<>(result, HttpStatus.OK);
            }
            return new ResponseEntity<>("Status Unavailable", HttpStatus.BAD_REQUEST);
        } catch (TimeoutException e) {
            return new ResponseEntity<>( "Timeout", HttpStatus.REQUEST_TIMEOUT);
        } catch (InterruptedException e) {
            return new ResponseEntity<>( "Interrupted", HttpStatus.GONE);
        }
    }

    @GetMapping("/usagebyday")
    public ResponseEntity<?> getUsageByDay(
            @RequestParam(name = "start", required = false, defaultValue = "-7d") String start,
            @RequestParam(name = "end", required = false, defaultValue = "0d") String end) {
        Date startDate = convertToDate(start);
        Date endDate = convertToDate(end);
        List<UsageByDay> ubd = usageByDay.findUsageBetweenDate(startDate, endDate);
        return new ResponseEntity<>(ubd, HttpStatus.OK);
    }
    
    @GetMapping("/circuits")
    public ResponseEntity<?> getCircuits(@RequestParam(name = "address", required = false) String address) {
        if (Strings.isBlank(address)) {
            List<Circuit> data = model.getCircuits();
            if (data == null)
                return new ResponseEntity<>("No circuits found", HttpStatus.NOT_FOUND);
            List<CircuitRank> ranks = model.findRanksByUserId(UUID.fromString("aad7b0bf-b210-4fbb-8a1b-b01622df52df"));
            ArrayList<Circuit> zones = new ArrayList<>();
            ranks.forEach(rank -> {
                data.stream().filter(c -> c.getAddress().equals(rank.getAddress())).findFirst()
                        .ifPresent(k -> {
                            k.setRank(rank.getRank());
                            zones.add(k);
                        });
            });
            return new ResponseEntity<>(zones.toArray(), HttpStatus.OK);
        }
        Circuit zone = model.findCircuitByAddress(address);
        if (zone == null)
            return new ResponseEntity<>(String.format("Circuit [%s] not found", address), HttpStatus.NOT_FOUND);

        return new ResponseEntity<>(List.of(zone).toArray(),  HttpStatus.OK);
    }

    @GetMapping("/usagebyhour")
    public ResponseEntity<?> getUsageByHour(
            @RequestParam(name = "start", required = false, defaultValue = "-24h") String start,
            @RequestParam(name = "end", required = false, defaultValue = "0h") String end) {
        Date startDate = convertToDate(start);
        Date endDate = convertToDate(end);
        List<UsageByHour> ubh = usageByHour.findUsageBetweenDate(startDate, endDate);
        return new ResponseEntity<>(ubh, HttpStatus.OK);
    }

    @GetMapping("/usagebyminute")
    public ResponseEntity<?> getUsageByMinute(
            @RequestParam(name = "start", required = false, defaultValue = "-1h") String start,
            @RequestParam(name = "end", required = false, defaultValue = "0h") String end) {
        Date startDate = convertToDate(start);
        Date endDate = convertToDate(end);
        List<UsageByMinute> ubh = usageByMinute.findUsageBetweenDate(startDate, endDate);
        return new ResponseEntity<>(ubh, HttpStatus.OK);
    }

    @GetMapping("/usage")
    public ResponseEntity<?> getUsage() {
        int watts = model.getCurrentUsage();
        return new ResponseEntity<>(String.format("{\"watts\": %d}", watts), HttpStatus.OK);
    }

    public static Date convertToDate(String relative) {
        char unit = relative.charAt(relative.length()-1);
        int amount = Integer.parseInt(relative.substring(0, relative.length()-1));
        LocalDateTime d = switch (unit) {
            case 'y' -> LocalDateTime.now().plusYears(amount);
            case 'm' -> LocalDateTime.now().plusMonths(amount);
            case 'h' -> LocalDateTime.now().plusHours(amount);
            default -> LocalDateTime.now().plusDays(amount);
        };
        return Date.from(d.atZone(ZoneId.systemDefault()).toInstant());
    }

}
