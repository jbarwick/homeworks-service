package com.jvj28.homeworks.api;

import com.jvj28.homeworks.api.contract.*;
import com.jvj28.homeworks.auth.UUIDGrantedAuthority;
import com.jvj28.homeworks.model.Model;
import com.jvj28.homeworks.model.data.LinkStatusData;
import com.jvj28.homeworks.model.data.NetstatData;
import com.jvj28.homeworks.model.data.StatusData;
import com.jvj28.homeworks.model.db.entity.*;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.*;

/**
 * Translate the data in the model to the CONTRACT between the API and the client.
 *
 */
@Service
public class HwApiControllerService {

    private final Model model;

    public HwApiControllerService(Model model) {
        this.model = model;
    }

    public int getCurrentUsage() {
        return model.getCurrentUsage();
    }

    public RanksResponse getRanksForUser() {
        UUID id = getUUIDFromSecurityContext();
        RanksResponse response = new RanksResponse(id);
        response.setRanksList(model.findRanksByUserId(id));
        return response;
    }

    public CircuitsResponse getCircuits() {

        UUID userId = getUUIDFromSecurityContext();

        CircuitsResponse response = new CircuitsResponse(userId);
        List<CircuitEntity> data = model.getCircuits();
        List<CircuitRankEntity> ranks = model.findRanksByUserId(userId);
        if (ranks.isEmpty()) {
            response.setCircuitList(data);
        }
        else {
            final List<CircuitEntity> zones = new ArrayList<>();
            ranks.forEach(rank -> data.stream().filter(c -> c.getAddress().equals(rank.getAddress())).findFirst()
                    .ifPresent(k -> {
                        k.setRank(rank.getRank());
                        zones.add(k);
                    }));
            response.setCircuitList(zones);
        }
        return response;
    }

    private UUID getUUIDFromSecurityContext() {
        SecurityContext securityContext = SecurityContextHolder.getContext();
        Collection<? extends GrantedAuthority> authorities = securityContext.getAuthentication().getAuthorities();
        for (GrantedAuthority ga: authorities) {
            if (ga instanceof UUIDGrantedAuthority) {
                return ((UUIDGrantedAuthority) ga).getUserId();
            }
        }
        return UUID.fromString("aad7b0bf-b210-4fbb-8a1b-b01622df52df");
    }

    public CircuitEntity getCircuitByAddress(String address) {
        return model.findCircuitByAddress(address);
    }

    public LinkStatusData getLinkStatus() {
        return model.get(LinkStatusData.class);
    }

    public NetstatData getNetstatData() {
        return model.get(NetstatData.class);
    }

    public StatusData getStatusData() {
        return model.get(StatusData.class);
    }

    public UsageByDayResponse getUsageByDayBetweenDate(Date startDate, Date endDate) {
        UsageByDayResponse response = new UsageByDayResponse(startDate, endDate);
        response.setUsageByDayList(model.getUsageByDayBetweenDate(startDate, endDate));
        return response;
    }

    public UsageByHourResponse getUsageByHourBetweenDate(Date startDate, Date endDate) {
        UsageByHourResponse response = new UsageByHourResponse(startDate, endDate);
        response.setUsageByHourList(model.getUsageByHourBetweenDate(startDate, endDate));
        return response;
    }

    public UsageByMinuteResponse getUsageByMinuteBetweenDate(Date startDate, Date endDate) {
        UsageByMinuteResponse response = new UsageByMinuteResponse(startDate, endDate);
        response.setUsageByMinuteList(model.getUsageByMinuteBetweenDate(startDate, endDate));
        return response;
    }

}
