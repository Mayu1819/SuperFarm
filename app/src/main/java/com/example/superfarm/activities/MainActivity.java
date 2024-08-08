package com.example.superfarm.activities;

import android.content.Intent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import com.example.superfarm.R;
import com.example.superfarm.helpers.SensorParser;
import com.example.superfarm.models.ENUM_Days;
import com.example.superfarm.models.Sensor;
import com.example.superfarm.models.UDPClient;

import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

public class MainActivity extends AppCompatActivity {

    private List<Sensor> sensorList;
    private UDPClient client;
    private ExecutorService executorService = Executors.newSingleThreadExecutor();
    private ArrayAdapter<String> adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ListView sensorListView = findViewById(R.id._dynamic);

        executorService.submit(() -> {
            try {
                client = new UDPClient();
                String responseList = client.sendGetCommand("farm2000_sensor_list");
                //String responseData = client.sendGetCommand("farm2000_Tuesday");
                runOnUiThread(() -> {
                    sensorList = new SensorParser().parseSensorList(responseList);
                    adapter = new ArrayAdapter<>(
                            this,
                            R.layout.support_simple_spinner_dropdown_item,
                            sensorList.stream().map(Sensor::getSensorType).collect(Collectors.toList()));

                    sensorListView.setAdapter(adapter);
                    sensorListView.setOnItemClickListener(this::onItemClick);

                    //Populate the sensor data for each day of the week
                    populateSensorData();
                });
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                if (client != null) {
                    client.close();
                }
            }
        });
    }

    /**
     * Populate the sensor data for each day of the week
     */
    private void populateSensorData() {
        executorService.submit(() -> {
            for (ENUM_Days day : ENUM_Days.values()) {
                try {
                    client = new UDPClient();
                    String dayString = day.toString();
                    String responseData = client.sendGetCommand("farm2000_" + dayString);

                    if(responseData.contains("No")) {
                        continue;
                    }

                    //Map de sensor --> Map de Heure --> Valeur
                    Map<Sensor, Map<String, Float>> sensorData = new SensorParser().parseSensorData(responseData, sensorList);

                    for(Sensor sensor : sensorList) {
                        Map<String, Float> sensorDataMap = sensorData.get(sensor);
                        sensor.mapDataWithSensors(day, sensorDataMap);
                    }
                }
                catch (Exception ignored) {
                } finally {
                    if (client != null) {
                        client.close();
                    }
                }
            }
        });
    }


    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        Sensor sensor = sensorList.get(position);
        Intent intent = new Intent(this, SensorActivity.class);
        intent.putExtra("sensor",sensor);
        startActivity(intent);
    }

    public void openSetUpScreen(View view){
        Intent intent = new Intent(this, SetUpActivity.class);
        Sensor.SensorList sensorList = new Sensor.SensorList(this.sensorList);
        intent.putExtra("sensorList", sensorList);
        startActivity(intent);
    }




}