package com.example.superfarm.models;

import android.util.Pair;

import java.util.HashMap;
import java.util.Map;

public class ConfigFileModel {
    private String type;
    private MoistureValues moisture;
    private LightValues light;
    private PumpSettings pump;

    public ConfigFileModel(String type, MoistureValues moisture, LightValues light, PumpSettings pump) {
        this.type = type;
        this.moisture = moisture;
        this.light = light;
        this.pump = pump;
    }

    public Map<String, Pair<Integer, Integer>> getValues() {
        Map<String, Pair<Integer, Integer>> values = new HashMap<>();
        values.put("moisture", new Pair<>(moisture.getMin(), moisture.getMax()));
        values.put("light", new Pair<>(light.getMin(), light.getMax()));
        values.put("pump", new Pair<>(pump.getOpen_time(), pump.getInterval()));
        return values;
    }

    public void setValues(Map<String, Pair<Integer, Integer>> values) {
        moisture = new MoistureValues(values.get("moisture").first, values.get("moisture").second);
        light = new LightValues(values.get("light").first, values.get("light").second);
        pump = new PumpSettings(values.get("pump").first, values.get("pump").second);
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }


    public class MoistureValues {
        private int min;
        private int max;

        public MoistureValues(int min, int max) {
            this.min = min;
            this.max = max;
        }

        public int getMin() {
            return min;
        }

        public int getMax() {
            return max;
        }
    }

    public class LightValues {
        private int min;
        private int max;

        public LightValues(int min, int max) {
            this.min = min;
            this.max = max;
        }

        public LightValues(int min) {
            this.min = min;
            this.max = 100;
        }

        public int getMin() {
            return min;
        }

        public int getMax() {
            return max;
        }
    }

    public class PumpSettings {
        private int open_time;
        private int interval;

        public PumpSettings(int open_time, int interval) {
            this.open_time = open_time;
            this.interval = interval;
        }

        public int getOpen_time() {
            return open_time;
        }

        public int getInterval() {
            return interval;
        }
    }
}
