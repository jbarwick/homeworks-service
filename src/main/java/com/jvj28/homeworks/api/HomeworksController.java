package com.jvj28.homeworks.api;

import com.jvj28.homeworks.data.db.UsageByDayRepository;
import com.jvj28.homeworks.data.db.UsageByHourRepository;
import com.jvj28.homeworks.data.db.UsageByMinuteRepository;
import com.jvj28.homeworks.data.model.*;
import com.jvj28.homeworks.data.Model;
import org.apache.logging.log4j.util.Strings;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import com.jvj28.homeworks.data.model.StatusData;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.*;
import java.util.Date;

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
    public HelpResponse getHelp() {
        return new HelpResponse();
    }

    @GetMapping("/status")
    public StatusData getStatus() {
        StatusData result = model.get(StatusData.class);
        if (result == null)
            throw new NotFoundException(new StatusData());
        return result;
    }

    @GetMapping("/netstat")
    public NetstatData getNetstat() {
        NetstatData result = model.get(NetstatData.class);
        if (result == null)
            throw new NotFoundException(new NetstatData());
        return result;
    }

    @GetMapping("/linkstatus")
    public LinkStatusData getLinkStatus() {
        LinkStatusData result = model.get(LinkStatusData.class);
        if (result == null)
            throw new NotFoundException(new LinkStatusData());
        return result;
    }

    @GetMapping("/usagebyday")
    public List<UsageByDayEntity> getUsageByDay(
            @RequestParam(name = "start", required = false, defaultValue = "-7d") String start,
            @RequestParam(name = "end", required = false, defaultValue = "0d") String end) {
        Date startDate = convertToDate(start);
        Date endDate = convertToDate(end);
        return usageByDay.findUsageBetweenDate(startDate, endDate);
    }
    
    @GetMapping("/circuits")
    public List<CircuitEntity> getCircuits(@RequestParam(name = "address", required = false) String address) {
        if (Strings.isBlank(address)) {
            List<CircuitEntity> data = model.getCircuits();
            if (data == null)
                throw new NotFoundException(new ArrayList<CircuitEntity>());
            List<CircuitRankEntity> ranks = model.findRanksByUserId(UUID.fromString("aad7b0bf-b210-4fbb-8a1b-b01622df52df"));
            ArrayList<CircuitEntity> zones = new ArrayList<>();
            ranks.forEach(rank -> data.stream().filter(c -> c.getAddress().equals(rank.getAddress())).findFirst()
                    .ifPresent(k -> {
                        k.setRank(rank.getRank());
                        zones.add(k);
                    }));
            return zones.stream().toList();
        } else {
            CircuitEntity zone = model.findCircuitByAddress(address);
            if (zone == null)
                throw new NotFoundException(List.of(new CircuitEntity()), String.format("Circuit [%s] not found", address));
            return List.of(zone);
        }
    }

    @GetMapping("/usagebyhour")
    public List<UsageByHourEntity> getUsageByHour(
            @RequestParam(name = "start", required = false, defaultValue = "-24h") String start,
            @RequestParam(name = "end", required = false, defaultValue = "0h") String end) {
        Date startDate = convertToDate(start);
        Date endDate = convertToDate(end);
        return usageByHour.findUsageBetweenDate(startDate, endDate);
    }

    @GetMapping("/usagebyminute")
    public List<UsageByMinuteEntity> getUsageByMinute(
            @RequestParam(name = "start", required = false, defaultValue = "-1h") String start,
            @RequestParam(name = "end", required = false, defaultValue = "0h") String end) {
        Date startDate = convertToDate(start);
        Date endDate = convertToDate(end);
        return usageByMinute.findUsageBetweenDate(startDate, endDate);
    }

    @GetMapping("/usage")
    public TotalUsage getUsage() {
        TotalUsage usage = new TotalUsage();
        usage.setWatts(model.getCurrentUsage());
        return usage;
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
