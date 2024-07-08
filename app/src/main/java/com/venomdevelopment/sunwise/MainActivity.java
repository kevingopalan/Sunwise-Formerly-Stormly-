package com.venomdevelopment.sunwise;

import static androidx.core.content.ContentProviderCompat.requireContext;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.android.volley.VolleyError;
import com.venomdevelopment.sunwise.Adapters.HourlyAdapters;
import com.venomdevelopment.sunwise.Domains.Hourly;
import com.venomdevelopment.sunwise.WeatherService;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Objects;

public class MainActivity extends AppCompatActivity {
    private RecyclerView.Adapter adapterHourly;
    private RecyclerView recyclerView;
    private WeatherService weatherService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        Objects.requireNonNull(getSupportActionBar()).hide();
        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        initRecyclerView();
        TextView tempText, tempText2, temp1text, descText , humidityText;
        Button search;
        search = findViewById(R.id.search);
        CharSequence toasttext = "I am rewriting the forecast code, just wait a bit lol";
        int duration = Toast.LENGTH_SHORT;
        Toast wiptoast = Toast.makeText(this, toasttext, duration);
        search.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // calling api
                wiptoast.show();
            }
        });

    }

    private void initRecyclerView() {
        weatherService = new WeatherService(this);
        weatherService.getForecast("Vestal", "imperial", new WeatherService.ForecastCallback() {
            @Override
            public void onSuccess(JSONObject forecast) {
                try {
                    JSONArray list = forecast.getJSONArray("list");
                    ArrayList<Hourly> items = new ArrayList<>();
                    Date date = new Date();
                    Calendar calendar = GregorianCalendar.getInstance();
                    // Loop through each forecast item
                    for (int i = 0; i < list.length()/8; i++) {
                        String onehr = String.valueOf(calendar.get(Calendar.HOUR) + i + 1);
                        JSONObject forecastItem = list.getJSONObject(i*8);
                        JSONObject main = forecastItem.getJSONObject("main");

                        // Get temperature information
                        int temp = main.getInt("temp");
                        int tempMin = main.getInt("temp_min");
                        int tempMax = main.getInt("temp_max");

                        // Do something with the temperature data (e.g., display, store, etc.)
                        Log.d("Forecast", "Temperature: " + temp + "°F");
                        Log.d("Forecast", "Min Temperature: " + tempMin + "°F");
                        Log.d("Forecast", "Max Temperature: " + tempMax + "°F");
                        items.add(new Hourly(onehr + ":00", temp, "clouds"));

                        // You can access other weather details like humidity, pressure, etc., if needed
                        // Example: int humidity = main.getInt("humidity");
                        // Example: int pressure = main.getInt("pressure");
                    }

                    recyclerView = findViewById(R.id.view1);
                    recyclerView.setLayoutManager(new LinearLayoutManager(MainActivity.this, LinearLayoutManager.VERTICAL, false));
                    adapterHourly = new HourlyAdapters(items);
                    recyclerView.setAdapter(adapterHourly);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onError(VolleyError error) {
                Log.d("DATA", "Error occured, VolleyError was triggered");
            }


        });
    }
}