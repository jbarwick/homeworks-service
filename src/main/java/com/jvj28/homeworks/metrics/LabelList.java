package com.jvj28.homeworks.metrics;

import java.util.HashMap;

public class LabelList extends HashMap<String, String> {

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        keySet().forEach(key -> {
            if (sb.length() > 0) sb.append(",");
            sb.append(String.format("%s=\"%s\"", key, get(key)));
        });
        return sb.toString();
    }
}
