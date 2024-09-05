package com.example.superfarm.activities;

import android.util.Pair;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import androidx.constraintlayout.widget.ConstraintLayout;
import com.example.superfarm.R;
import com.example.superfarm.models.ConfigFileModel;
import com.example.superfarm.models.Sensor;
import com.example.superfarm.models.UDPClient;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.io.NumberInput;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.Serializable;
import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

public class SetUpActivity extends AppCompatActivity {

    private Gson gsonInstance = new GsonBuilder().create();
    private UDPClient client;
    private ExecutorService executorService = Executors.newSingleThreadExecutor();
    private ConfigFileModel configFileModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_set_up);

        //List<Sensor> sensorList = getIntent().getExtras().getSerializable("sensorList", Sensor.SensorList.class).getSensorList();

        ConstraintLayout constraintLayout = findViewById(R.id.set_up_layout);
        LinearLayout linearLayout = new LinearLayout(this);
        linearLayout.setOrientation(LinearLayout.VERTICAL);
        linearLayout.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));

        ScrollView scrollView = new ScrollView(this);
        scrollView.setLayoutParams(new ScrollView.LayoutParams(ScrollView.LayoutParams.MATCH_PARENT, ScrollView.LayoutParams.MATCH_PARENT));
        constraintLayout.addView(scrollView);
        scrollView.addView(linearLayout);

        //get the parameters from the server
        try {
            configFileModel = getConfigValues();
        } catch (ExecutionException | InterruptedException e) {
            e.printStackTrace();
        }

        //for(Sensor sensor : sensorList)
        for(String parameter : configFileModel.getValues().keySet())
        {
            int i = 1;
            Pair<Integer, Integer> values = configFileModel.getValues().get(parameter);

            LinearLayout sensorLayout = new LinearLayout(this);
            sensorLayout.setOrientation(LinearLayout.HORIZONTAL);
            sensorLayout.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));
            linearLayout.addView(sensorLayout);

            TextView sensorName = new TextView(this);
            sensorName.setText(parameter);
            sensorName.setPadding(50, 50, 50, 50);
            sensorName.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT));
            // set the text alignment in the center of the view
            sensorName.setGravity(View.TEXT_ALIGNMENT_CENTER);
            sensorName.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
            sensorLayout.addView(sensorName);

            NumberPicker sensorMinValue = new NumberPicker(this);
            sensorMinValue.setId(i);
            sensorMinValue.setMinValue(0);
            sensorMinValue.setMaxValue(100);
            sensorMinValue.setValue(values.first);
            sensorLayout.addView(sensorMinValue);
            i++;

            NumberPicker sensorMaxValue = new NumberPicker(this);
            sensorMaxValue.setId(i);
            sensorMaxValue.setMinValue(0);
            sensorMaxValue.setMaxValue(100);
            sensorMaxValue.setValue(values.second);
            sensorLayout.addView(sensorMaxValue);
            i++;
        }
        Button saveButton = new Button(this);
        saveButton.setId(View.generateViewId());
        saveButton.setText("Save");
        saveButton.setOnClickListener(v -> saveButtonOnClick(linearLayout));
        linearLayout.addView(saveButton);
    }

    private ConfigFileModel getConfigValues() throws ExecutionException, InterruptedException {
        return executorService.submit(() -> {
            ConfigFileModel result = null;
            try {
                client = new UDPClient();
                String response = client.sendGetCommand("farm2000_configs");
                // convert JSON string to object
                result = gsonInstance.fromJson(response, ConfigFileModel.class);
            } catch (Exception e) {
                e.printStackTrace();
            }
            return result;
        }).get();
    }

    private void saveButtonOnClick(View view)
    {
        ArrayList<Number> minValues = new ArrayList<>();
        ArrayList<Number> maxValues = new ArrayList<>();

        ViewGroup viewGroup = (ViewGroup) view;
        Map<String, Pair<Integer, Integer>> values = new HashMap<>();

        for(int i = 0; i < viewGroup.getChildCount(); i++)
        {
            if(viewGroup.getChildAt(i) instanceof LinearLayout)
            {
                LinearLayout sensorLayout = (LinearLayout) viewGroup.getChildAt(i);

                String key = ((TextView) sensorLayout.getChildAt(0)).getText().toString();

                for(int j = 0; j < sensorLayout.getChildCount(); j++)
                {
                    if(sensorLayout.getChildAt(j) instanceof NumberPicker)
                    {
                        NumberPicker numberPicker = (NumberPicker) sensorLayout.getChildAt(j);
                        if(Math.floorMod(numberPicker.getId(), 2) == 0)
                        {
                            maxValues.add(numberPicker.getValue());
                        }
                        else
                        {
                            minValues.add(numberPicker.getValue());
                        }
                    }
                }
                values.put(key, new Pair<>((int)minValues.get(i), (int)maxValues.get(i)));
            }
        }
        configFileModel.setValues(values);
        String jsonResult = gsonInstance.toJson(configFileModel);
        //Toast.makeText(this, "json result : " + jsonResult, Toast.LENGTH_LONG).show();
        System.out.println("json result : " + jsonResult);
        try {
            client = new UDPClient();
            client.sendSetCommand("farm2000_configs", jsonResult);
            client.close();
        }
        catch (Exception e) {
            e.printStackTrace();
        }

        //Toast.makeText(this, "Saved  - dictionnaire : " + configFileModel.getValues(), Toast.LENGTH_LONG).show();
    }
}