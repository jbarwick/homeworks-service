package com.jvj28.homeworks.api;

import com.jvj28.homeworks.api.contract.*;
import com.jvj28.homeworks.model.data.LinkStatusData;
import com.jvj28.homeworks.model.data.NetstatData;
import com.jvj28.homeworks.model.data.StatusData;
import com.jvj28.homeworks.model.data.TotalUsageData;
import com.jvj28.homeworks.model.db.entity.CircuitEntity;
import com.jvj28.homeworks.model.db.entity.UsersEntity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;

@RestController
@RequestMapping("/api")
public class HwApiController {

    private static final Logger log = LoggerFactory.getLogger(HwApiController.class);

    // Model is the "Service" for this "Controller" MVC.
    private final HwApiControllerService service;

    public HwApiController(HwApiControllerService service) {
        this.service = service;
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
        StatusData result = service.getStatusData();
        if (result == null)
            throw new NotFoundException(new StatusData());
        return result;
    }

    @GetMapping("/ranks")
    public RanksResponse getRanks() {
        Thread.currentThread().setName("/ranks");
        log.debug("Request Ranks for current user");
        RanksResponse response = service.getRanksForUser();
        log.debug("Ranks returned for user [{}]", response.getUserId());
        return response;
    }

    @PostMapping("/ranks")
    public void setRanks(@RequestBody RankItemRequest[] ranksList) {
        service.setRanksForUser(ranksList);
    }

    @GetMapping("/netstat")
    public NetstatData getNetstat() {
        Thread.currentThread().setName("/netstat");
        log.debug("Request Network Status");
        NetstatData result = service.getNetstatData();
        if (result == null)
            throw new NotFoundException(new NetstatData());
        return result;
    }

    @GetMapping("/linkstatus")
    public LinkStatusData getLinkStatus() {
        Thread.currentThread().setName("/linkstatus");
        log.debug("Request Link Status");
        LinkStatusData result = service.getLinkStatus();
        if (result == null)
            throw new NotFoundException(new LinkStatusData());
        return result;
    }

    @GetMapping("/usagebyday")
    public UsageByDayResponse getUsageByDay(
            @RequestParam(name = "start", required = false, defaultValue = "-7d") String start,
            @RequestParam(name = "end", required = false, defaultValue = "0d") String end) {
        Thread.currentThread().setName("/usagebyday");
        Date startDate = convertToDate(start);
        Date endDate = convertToDate(end);
        return service.getUsageByDayBetweenDate(startDate, endDate);
    }

    @PutMapping("/circuits/{address}/{level}")
    public CircuitEntity putCircuitLevelByAddress(@PathVariable String address, @PathVariable int level) {
        CircuitEntity entity = service.getCircuitByAddress(address);
        if (entity != null) {
            entity.setLevel(level);
        }
        return entity;
    }

    @GetMapping("/circuits/{address}")
    public CircuitEntity getCircuitByAddress(@PathVariable String address) {
        Thread.currentThread().setName("/circuits");
        log.debug("Request Circuit: {}", address);
        return service.getCircuitByAddress(address);
    }

    @GetMapping("/circuits")
    public CircuitsResponse getCircuits() {
        Thread.currentThread().setName("/circuits");
        log.debug("Request Circuits List");
        return service.getCircuits();
    }

    @GetMapping("/usagebyhour")
    public UsageByHourResponse getUsageByHour(
            @RequestParam(name = "start", required = false, defaultValue = "-24h") String start,
            @RequestParam(name = "end", required = false, defaultValue = "0h") String end) {
        Thread.currentThread().setName("/usagebyhour");
        log.debug("Request Usage by Hour");
        Date startDate = convertToDate(start);
        Date endDate = convertToDate(end);
        return service.getUsageByHourBetweenDate(startDate, endDate);
    }

    @GetMapping("/usagebyminute")
    public UsageByMinuteResponse getUsageByMinute(
            @RequestParam(name = "start", required = false, defaultValue = "-1h") String start,
            @RequestParam(name = "end", required = false, defaultValue = "0h") String end) {
        Thread.currentThread().setName("/usagebyminute");
        log.debug("Request Usage by Minute");
        Date startDate = convertToDate(start);
        Date endDate = convertToDate(end);
        return service.getUsageByMinuteBetweenDate(startDate, endDate);
    }

    @GetMapping("/usage")
    public TotalUsageData getUsage() {
        Thread.currentThread().setName("/usage");
        log.debug("Request total usage");
        TotalUsageData usage = new TotalUsageData();
        usage.setWatts(service.getCurrentUsage());
        return usage;
    }

    @GetMapping("/user")
    public UserResponse getUser() {
        Thread.currentThread().setName("/user");
        log.debug("Request user details");
        UsersEntity user = service.getCurrentUser();
        if (user == null)
            throw new BadRequestException("Not Authenticated");
        UserResponse response = new UserResponse();
        response.setFirstName(user.getFirstName());
        response.setLastName(user.getLastName());
        response.setInfo(user.getInfo());
        return response;
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
