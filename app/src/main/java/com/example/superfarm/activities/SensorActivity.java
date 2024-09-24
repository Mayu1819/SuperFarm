package com.example.superfarm.activities;

import android.view.View;
import android.widget.*;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import com.example.superfarm.R;
import com.example.superfarm.helpers.SensorParser;
import com.example.superfarm.models.ENUM_Days;
import com.example.superfarm.models.Sensor;
import com.example.superfarm.models.UDPClient;
import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.series.BarGraphSeries;
import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.LineGraphSeries;

import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

public class SensorActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sensor);

        GraphView graph = findViewById(R.id.graph);
        Sensor currentSensor = getIntent().getExtras().getSerializable("sensor", Sensor.class);

        Spinner spinner = findViewById(R.id.spinner);

        spinner.setAdapter(new ArrayAdapter<>(
                this,
                R.layout.support_simple_spinner_dropdown_item,
                ENUM_Days.values()));

        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String selectedDay = parent.getItemAtPosition(position).toString();
                for(ENUM_Days day : ENUM_Days.values())
                {
                    if(day.toString().equals(selectedDay))
                    {
                        makeGraphForTheDay(day, currentSensor, graph);
                    }
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });

        try {
            int dayOfToday = Calendar.getInstance().get(Calendar.DAY_OF_WEEK) - 1;
            ENUM_Days day = ENUM_Days.values()[dayOfToday];

            spinner.setSelection(day.ordinal());

            this.makeGraphForTheDay(day,currentSensor,graph);

            //set units for the x and y axis
            graph.getGridLabelRenderer().setHorizontalAxisTitle("Hour");
            graph.getGridLabelRenderer().setVerticalLabelsVisible(true);

            graph.getViewport().setXAxisBoundsManual(true);
            graph.getViewport().setYAxisBoundsManual(true);
            graph.getViewport().setScrollable(true);
            //graph.getViewport().setScalable(true);
            graph.getViewport().setScalableY(true);
            graph.getViewport().setMinX(0);
            graph.getViewport().setMaxX(24);

            switch (currentSensor.getSensorType()) {
                case "Temperature":
                    graph.getViewport().setMinY(0);
                    graph.getViewport().setMaxY(50);
                    graph.getGridLabelRenderer().setVerticalAxisTitle("Temperature (°C)");
                    break;
                case "Humidity":
                case "Light":
                case "Moisture":
                    graph.getViewport().setMinY(0);
                    graph.getViewport().setMaxY(100);
                    graph.getGridLabelRenderer().setVerticalAxisTitle("%");
                    break;
            }
        }
        catch (Exception e) {
            e.printStackTrace();
        }

    }

    private void makeGraphForTheDay(ENUM_Days day, Sensor currentSensor, GraphView graph) {
        Map<String, Float> data = currentSensor.getData().get(day);

        if (data == null) {
            return;
        }

        //populate the graph with the data for the selected day
        ArrayList<DataPoint> dataPoints = new ArrayList<>();
        data.forEach((hour, value) -> {
            dataPoints.add(new DataPoint(Integer.parseInt(hour), value));
        });

        // order the data points by the x value
        dataPoints.sort(Comparator.comparingDouble(DataPoint::getX));

        BarGraphSeries<DataPoint> series = new BarGraphSeries<>(dataPoints.toArray(new DataPoint[0]));
        graph.removeAllSeries();
        graph.addSeries(series);
        graph.setTitle("Sensor Data for " + currentSensor.getSensorType() + " on " + day);
    }

}