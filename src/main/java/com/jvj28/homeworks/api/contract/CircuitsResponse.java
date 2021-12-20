package com.jvj28.homeworks.api.contract;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.jvj28.homeworks.model.db.entity.CircuitEntity;
import lombok.Data;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Data
public class CircuitsResponse implements Serializable {

    private static final long serialVersionUID = -3296567478403283989L;

    private UUID userId;

    @JsonProperty("circuits")
    private List<CircuitEntity> circuitList;

    public CircuitsResponse(UUID userId) {
        this.userId = userId;
    }

    public void add(CircuitEntity circuit) {
        if (circuitList == null)
            setCircuitList(new ArrayList<>());
        circuitList.add(circuit);
    }

    public boolean isEmpty() {
        return circuitList == null || circuitList.isEmpty();
    }
}
