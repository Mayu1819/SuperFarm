package com.example.superfarm.helpers;

import com.example.superfarm.models.Sensor;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.core.type.TypeReference;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class SensorParser {

    /**
     * Parse the list of sensors from the server
     * @param listSensor the list of sensors from the server in the format :
     *                   {"sensors": [{"name": "DHT11", "type": "TEMPERATURE"}, {"name": "DHT11", "type": "HUMIDITY"}, {"name": "LDR", "type": "LIGHT"}, {"name": "Soil Moisture Sensor", "type": "MOISTURE"}]}
     * @return the list of sensors
     */
    public List<Sensor> parseSensorList(String listSensor) {
        List<Sensor> sensors = new ArrayList<>();
        ObjectMapper mapper = new ObjectMapper();

        try {
            // Parse the JSON string into a List
            sensors = mapper.readValue(listSensor, new TypeReference<ArrayList<String>>(){})
                    .stream()
                    .map(s -> new Sensor(s))
                    .collect(Collectors.toList());
        } catch (Exception e) {
            e.printStackTrace();
        }

        return sensors;
    }

    //         Type,      Hour,     Value for one day
    public Map<Sensor, Map<String, Float>> parseSensorData(String sensorData, List<Sensor> sensors) {
        //ObjectMapper mapper = new ObjectMapper();
        JSONObject jsonObject = null;
        Map<Sensor, Map<String, Float>> sensorDataMap = new HashMap<>();

        try {
            jsonObject = new JSONObject(sensorData);
            for(Sensor sensor : sensors) {
                sensorDataMap.put(sensor, new HashMap<>());
            }

            for (int i = 0; i < 24; i++) {
                JSONObject sensorDataObject;
                String hourKey = String.valueOf(i);
                try {
                    sensorDataObject = jsonObject.getJSONObject(hourKey);
                } catch (JSONException ignored) {
                    continue;
                }

                sensorDataMap.forEach((key, value) -> {
                    try {
                        value.put(hourKey, (float) sensorDataObject.getDouble(key.getSensorType()));
                    } catch (JSONException ignored) {
                    }
                });
            }


        } catch (JSONException e) {
            e.printStackTrace();
        }

        return sensorDataMap;
    }
}