package com.example.superfarm.activities;

import android.view.View;
import android.widget.LinearLayout;
import android.widget.NumberPicker;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import com.example.superfarm.R;
import com.example.superfarm.models.Sensor;
import com.fasterxml.jackson.core.io.NumberInput;

import java.io.Serializable;
import java.util.List;

public class SetUpActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_set_up);

        List<Sensor> sensorList = getIntent().getExtras().getSerializable("sensorList", Sensor.SensorList.class).getSensorList();

        LinearLayout linearLayout = new LinearLayout(this);
        linearLayout.setOrientation(LinearLayout.VERTICAL);

        for(Sensor sensor : sensorList)
        {
            LinearLayout sensorLayout = new LinearLayout(this);
            sensorLayout.setOrientation(LinearLayout.HORIZONTAL);
            sensorLayout.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));
            linearLayout.addView(sensorLayout);

            TextView sensorName = new TextView(this);
            sensorName.setText(sensor.getSensorType());
            sensorName.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT));
            sensorName.setVisibility(View.VISIBLE);
            sensorLayout.addView(sensorName);

            NumberPicker sensorMinValue = new NumberPicker(this);
            sensorMinValue.setMinValue(0);
            sensorMinValue.setMaxValue(100);
            sensorLayout.addView(sensorMinValue);

            NumberPicker sensorMaxValue = new NumberPicker(this);
            sensorMaxValue.setMinValue(0);
            sensorMaxValue.setMaxValue(100);
            sensorLayout.addView(sensorMaxValue);
        }
    }
}