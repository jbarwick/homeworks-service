package com.jvj28.homeworks.api.contract;

import com.jvj28.homeworks.model.db.entity.UsageByDayEntity;
import com.jvj28.homeworks.model.db.entity.UsageByMinuteEntity;
import lombok.Data;
import lombok.NonNull;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

@Data
public class UsageByMinuteResponse implements Serializable {

    private static final long serialVersionUID = 3986650959594827493L;

    @NonNull
    private Date startDate;

    @NonNull
    private Date endDate;

    private List<UsageByMinuteEntity> usageByMinuteList;

}
