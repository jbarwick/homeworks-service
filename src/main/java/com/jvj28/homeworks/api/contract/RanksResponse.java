package com.jvj28.homeworks.api.contract;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.jvj28.homeworks.model.db.entity.CircuitRankEntity;
import lombok.Data;
import lombok.NonNull;

import java.io.Serializable;
import java.util.List;
import java.util.UUID;

@Data
public class RanksResponse implements Serializable {

    private static final long serialVersionUID = -392795903481911916L;

    @NonNull
    private UUID userId;

    @JsonProperty("ranks")
    private List<CircuitRankEntity> ranksList;
}
