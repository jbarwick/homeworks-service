package com.jvj28.homeworks.api;

import com.jvj28.homeworks.model.Model;
import com.jvj28.homeworks.model.data.LinkStatusData;
import com.jvj28.homeworks.model.data.NetstatData;
import com.jvj28.homeworks.model.data.StatusData;
import com.jvj28.homeworks.model.db.UsageByDayRepository;
import com.jvj28.homeworks.model.db.UsageByHourRepository;
import com.jvj28.homeworks.model.db.UsageByMinuteRepository;
import com.jvj28.homeworks.model.db.entity.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api")
public class HomeworksController {

    private static final Logger log = LoggerFactory.getLogger(HomeworksController.class);

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
        Thread.currentThread().setName("/help");
        log.debug("Request Help");
        return new HelpResponse();
    }

    @GetMapping("/status")
    public StatusData getStatus() {
        Thread.currentThread().setName("/status");
        log.debug("Request System Status");
        StatusData result = model.get(StatusData.class);
        if (result == null)
            throw new NotFoundException(new StatusData());
        return result;
    }

    @GetMapping("/netstat")
    public NetstatData getNetstat() {
        Thread.currentThread().setName("/netstat");
        log.debug("Request Network Status");
        NetstatData result = model.get(NetstatData.class);
        if (result == null)
            throw new NotFoundException(new NetstatData());
        return result;
    }

    @GetMapping("/linkstatus")
    public LinkStatusData getLinkStatus() {
        Thread.currentThread().setName("/linkstatus");
        log.debug("Request Link Status");
        LinkStatusData result = model.get(LinkStatusData.class);
        if (result == null)
            throw new NotFoundException(new LinkStatusData());
        return result;
    }

    @GetMapping("/usagebyday")
    public List<UsageByDayEntity> getUsageByDay(
            @RequestParam(name = "start", required = false, defaultValue = "-7d") String start,
            @RequestParam(name = "end", required = false, defaultValue = "0d") String end) {
        Thread.currentThread().setName("/usagebyday");
        Date startDate = convertToDate(start);
        Date endDate = convertToDate(end);
        return usageByDay.findUsageBetweenDate(startDate, endDate);
    }

    @PutMapping("/circuits/{address}/{level}")
    public CircuitEntity putCircuitLevelByAddress(@PathVariable String address, @PathVariable int level) {
        CircuitEntity entity = model.findCircuitByAddress(address);
        if (entity != null) {
            entity.setLevel(level);
        }
        return entity;
    }

    @GetMapping("/circuits/{address}")
    public CircuitEntity getCircuitByAddress(@PathVariable String address) {
        Thread.currentThread().setName("/circuits");
        log.debug("Request Circuit: {}", address);
        return model.findCircuitByAddress(address);
    }

    @GetMapping("/circuits")
    public List<CircuitEntity> getCircuits() {
        Thread.currentThread().setName("/circuits");
        log.debug("Request Circuits List");
        List<CircuitEntity> data = model.getCircuits();
        if (data.isEmpty())
            throw new NotFoundException(new ArrayList<CircuitEntity>());
        List<CircuitRankEntity> ranks = model.findRanksByUserId(UUID.fromString("aad7b0bf-b210-4fbb-8a1b-b01622df52df"));
        ArrayList<CircuitEntity> zones = new ArrayList<>();
        ranks.forEach(rank -> data.stream().filter(c -> c.getAddress().equals(rank.getAddress())).findFirst()
                .ifPresent(k -> {
                    k.setRank(rank.getRank());
                    zones.add(k);
                }));
        return zones;
    }

    @GetMapping("/usagebyhour")
    public List<UsageByHourEntity> getUsageByHour(
            @RequestParam(name = "start", required = false, defaultValue = "-24h") String start,
            @RequestParam(name = "end", required = false, defaultValue = "0h") String end) {
        Thread.currentThread().setName("/usagebyhour");
        log.debug("Request Usage by Hour");
        Date startDate = convertToDate(start);
        Date endDate = convertToDate(end);
        return usageByHour.findUsageBetweenDate(startDate, endDate);
    }

    @GetMapping("/usagebyminute")
    public List<UsageByMinuteEntity> getUsageByMinute(
            @RequestParam(name = "start", required = false, defaultValue = "-1h") String start,
            @RequestParam(name = "end", required = false, defaultValue = "0h") String end) {
        Thread.currentThread().setName("/usagebyminute");
        log.debug("Request Usage by Minute");
        Date startDate = convertToDate(start);
        Date endDate = convertToDate(end);
        return usageByMinute.findUsageBetweenDate(startDate, endDate);
    }

    @GetMapping("/usage")
    public TotalUsage getUsage() {
        Thread.currentThread().setName("/usage");
        log.debug("Request total usage");
        TotalUsage usage = new TotalUsage();
        usage.setWatts(model.getCurrentUsage());
        return usage;
    }

    public static Date convertToDate(String relative) {
        char unit = relative.charAt(relative.length() - 1);
        int amount = Integer.parseInt(relative.substring(0, relative.length() - 1));
        LocalDateTime d;
        switch (unit) {
            case 'y':
                d = LocalDateTime.now().plusYears(amount);
                break;
            case 'm':
                d = LocalDateTime.now().plusMonths(amount);
                break;
            case 'h':
                d = LocalDateTime.now().plusHours(amount);
                break;
            default:
                d = LocalDateTime.now().plusDays(amount);
        }
        return Date.from(d.atZone(ZoneId.systemDefault()).toInstant());
    }

}
