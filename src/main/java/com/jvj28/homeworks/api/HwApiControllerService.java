package com.jvj28.homeworks.api;

import com.jvj28.homeworks.model.Model;
import com.jvj28.homeworks.model.data.LinkStatusData;
import com.jvj28.homeworks.model.data.NetstatData;
import com.jvj28.homeworks.model.data.StatusData;
import com.jvj28.homeworks.model.db.entity.*;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;
import java.util.UUID;

@Service
public class HwApiControllerService {

    private final Model model;

    public HwApiControllerService(Model model) {
        this.model = model;
    }

    public List<UsageByHourEntity> getUsageByHourBetweenDate(Date startDate, Date endDate) {
        return model.getUsageByHourBetweenDate(startDate, endDate);
    }

    public List<UsageByMinuteEntity> getUsageByMinuteBetweenDate(Date startDate, Date endDate) {
        return model.getUsageByMinuteBetweenDate(startDate, endDate);
    }

    public int getCurrentUsage() {
        return model.getCurrentUsage();
    }

    public List<CircuitRankEntity> getRanksByUserId(UUID id) {
        return model.findRanksByUserId(id);
    }

    public List<CircuitEntity> getCircuits() {
        return model.getCircuits();
    }

    public CircuitEntity getCircuitByAddress(String address) {
        return model.findCircuitByAddress(address);
    }

    public List<UsageByDayEntity> getUsageByDayBetweenDate(Date startDate, Date endDate) {
        return model.getUsageByDayBetweenDate(startDate, endDate);
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
}
