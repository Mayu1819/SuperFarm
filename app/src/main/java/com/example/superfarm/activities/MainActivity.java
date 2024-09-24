package com.example.superfarm.activities;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.StrictMode;
import android.util.Base64;
import android.view.*;
import android.widget.*;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import androidx.cardview.widget.CardView;
import com.example.superfarm.R;
import com.example.superfarm.helpers.SensorParser;
import com.example.superfarm.models.ENUM_Days;
import com.example.superfarm.models.Sensor;
import com.example.superfarm.models.UDPClient;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import org.json.JSONObject;

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
    private Gson gson = new GsonBuilder().create();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);

        setContentView(R.layout.activity_main);

        ImageView camIcon = findViewById(R.id.imageViewCam);
        camIcon.setOnClickListener(v -> {
            //display the image in a popup
            LayoutInflater inflater = (LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE);
            View popupView = inflater.inflate(R.layout.popup_window, (ViewGroup) v.getParent(), false);

            int width = LinearLayout.LayoutParams.WRAP_CONTENT;
            int height = LinearLayout.LayoutParams.WRAP_CONTENT;
            final PopupWindow popupWindow = new PopupWindow(popupView, width, height, true);

            popupWindow.showAtLocation(v, Gravity.CENTER, 0, 0);

            //get the image and inflate it in the image view in the popup ?
            this.populatePopUpImageView(popupWindow);

            // dismiss the popup window when touched
            popupView.setOnTouchListener((v1, event) -> {
                popupWindow.dismiss();
                return false;
            });
        });

        //static
        CardView humidityCard = findViewById(R.id.cardViewHumidity);
        humidityCard.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, SensorActivity.class);
            intent.putExtra("sensor", sensorList.stream().filter(sensor -> sensor.getSensorType().equals("HUMIDITY")).collect(Collectors.toList()).get(0));
            startActivity(intent);
        });
        CardView temperatureCard = findViewById(R.id.cardViewTemperature);
        temperatureCard.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, SensorActivity.class);
            intent.putExtra("sensor", sensorList.stream().filter(sensor -> sensor.getSensorType().equals("TEMPERATURE")).collect(Collectors.toList()).get(0));
            startActivity(intent);
        });
        CardView lightCard = findViewById(R.id.cardViewLight);
        lightCard.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, SensorActivity.class);
            intent.putExtra("sensor", sensorList.stream().filter(sensor -> sensor.getSensorType().equals("LIGHT")).collect(Collectors.toList()).get(0));
            startActivity(intent);
        });
        CardView moistureCard = findViewById(R.id.cardViewMoisture);
        moistureCard.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, SensorActivity.class);
            intent.putExtra("sensor", sensorList.stream().filter(sensor -> sensor.getSensorType().equals("MOISTURE")).collect(Collectors.toList()).get(0));
            startActivity(intent);
        });

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

                    //sensorListView.setAdapter(adapter);
                    //sensorListView.setOnItemClickListener(this::onItemClick);

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

    private void populatePopUpImageView(PopupWindow popup) {
        //get data from the server
        executorService.submit(() -> {
            try {
                client = new UDPClient();
                String response = client.sendGetCommand("farm2000_camera");
                System.out.println("réponse pure : " + response);

                runOnUiThread(() -> {
                    byte[] decodedString = Base64.decode(response, Base64.DEFAULT);
                    Bitmap decodedBitmap = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);
                    ImageView imageView = popup.getContentView().findViewById(R.id.popUpImageView);
                    imageView.setImageBitmap(decodedBitmap);
                });
            }
            catch (Exception e) {
                e.printStackTrace();
            }
        });
    }

    /**
     * Populate the sensor data for each day of the week
     */
    private void populateSensorData() {
        executorService.submit(() -> {
            try {
            client = new UDPClient();
            for (ENUM_Days day : ENUM_Days.values()) {

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
            } catch (Exception ignored) {
            } finally {
                if (client != null) {
                    client.close();
                }
            }
        });
    }

    /*
    private void createLayout() {
        GridLayout gridLayout = new GridLayout(this);
    }
     */

    //for the list view
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