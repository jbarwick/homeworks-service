package com.jvj28.homeworks.api;

import com.jvj28.homeworks.api.contract.*;
import com.jvj28.homeworks.model.Model;
import com.jvj28.homeworks.model.data.LinkStatusData;
import com.jvj28.homeworks.model.data.NetstatData;
import com.jvj28.homeworks.model.data.StatusData;
import com.jvj28.homeworks.model.db.entity.*;
import org.springframework.security.core.Authentication;
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
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        Object principal = authentication.getPrincipal();
        if (principal instanceof UsersEntity) {
            return ((UsersEntity) principal).getId();
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

    public void setRanksForUser(RankItemRequest[] ranksList) {

        UUID uuid = getUUIDFromSecurityContext();

        // Get the existing ranks for this user and we'll update them
        final List<CircuitRankEntity> ranks = model.findRanksByUserId(uuid);
        for (RankItemRequest r : ranksList) {
            final String address = r.getAddress();
            CircuitRankEntity rankEntity = ranks.stream().filter(i -> i.getAddress().equals(address)).findFirst().orElse(null);
            if (rankEntity == null) {
                rankEntity = getNewRankFromCircuit(address);
                ranks.add(rankEntity);
            }
            rankEntity.setUid(uuid);
            rankEntity.setRank(r.getRank());
        }
        model.saveRanks(ranks);
    }

    private CircuitRankEntity getNewRankFromCircuit(String address) {

        CircuitRankEntity newRank = new CircuitRankEntity();

        // Find the circuit in the list From REDIS to get information for the new rank
        CircuitEntity circuit = model.getCircuits().stream().filter(r ->
                r.getAddress().equals(address)).findFirst().orElse(null);
        if (circuit != null) {
            newRank.setCircuitId(circuit.getId());
            newRank.setAddress(circuit.getAddress());
            newRank.setLights(circuit.getLights());
            newRank.setName(circuit.getName());
            newRank.setRoom(circuit.getRoom());
            newRank.setType(circuit.getType());
            newRank.setWatts(circuit.getWatts());
        } else {
            newRank.setAddress(address);
        }

        return newRank;
    }

}
