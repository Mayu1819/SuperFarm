package com.example.superfarm.models;

import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Sensor implements Serializable {
    private String type;
    //Map of day --> hour --> value
    private Map<ENUM_Days,Map<String, Float>> data = new HashMap<>();

    public Sensor(String type) {
        this.type = type;
    }

    public void mapDataWithSensors(ENUM_Days day, Map<String, Float> sensorData) {
        data.put(day, sensorData);
    }

    public String getSensorType() {
        return type;
    }

    public Map<ENUM_Days, Map<String, Float>> getData() {
        return data;
    }

    public static class SensorList implements Serializable {
        private List<Sensor> sensorList;

        public SensorList(List<Sensor> sensorList) {
            this.sensorList = sensorList;
        }

        public List<Sensor> getSensorList() {
            return sensorList;
        }

    }
}
