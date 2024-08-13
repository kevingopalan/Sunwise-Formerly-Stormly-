package com.venomdevelopment.sunwise;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.RecyclerView;
import com.android.volley.VolleyError;
import java.util.List;
import java.util.Objects;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = MainActivity.class.getSimpleName();
    private WeatherService weatherService;

    private TextView tempText, tempText2, temp1text, descText, humidityText;
    private Button search;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        // Initialize UI components
        tempText = findViewById(R.id.text_home);  // Replace with your actual TextView IDs
        descText = findViewById(R.id.text_desc);
        humidityText = findViewById(R.id.text_humidity);
        search = findViewById(R.id.search);

        // Initialize WeatherService
        weatherService = new WeatherService(this);

        // Example latitude and longitude (replace with actual coordinates)
        double latitude = 85;
        double longitude = 105;

        search.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Display toast
                Toast.makeText(MainActivity.this, "Fetching weather data...", Toast.LENGTH_SHORT).show();
                // Call method to fetch weather data
                fetchWeatherData(latitude, longitude);
            }
        });

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }

    private void fetchWeatherData(double latitude, double longitude) {
        weatherService.getWeather(latitude, longitude, new WeatherService.WeatherResponseListener() {
            @Override
            public void onResponse(CurrentWeather currentWeather, List<HourlyForecast> hourlyForecasts, List<DailyForecast> dailyForecasts) {
                // Update UI with weather data
                Log.d(TAG, "Current Temperature: " + currentWeather.getTemperature());
                Log.d(TAG, "Min Temperature Today: " + currentWeather.getMinTemperature());
                Log.d(TAG, "Max Temperature Today: " + currentWeather.getMaxTemperature());
                Log.d(TAG, "Current Forecast: " + currentWeather.getForecast());

                // Example: Update TextViews with weather data
                tempText.setText(currentWeather.getTemperature());
                descText.setText(currentWeather.getForecast());

                // Example: Handle hourly forecasts (if needed)
                for (HourlyForecast hourlyForecast : hourlyForecasts) {
                    Log.d(TAG, "Hour: " + hourlyForecast.getTime() + ", Temperature: " + hourlyForecast.getTemperature() + ", Forecast: " + hourlyForecast.getForecast());
                    // Handle displaying hourly forecast data
                }

                // Example: Handle daily forecasts (if needed)
                for (DailyForecast dailyForecast : dailyForecasts) {
                    Log.d(TAG, "Date: " + dailyForecast.getDate() + ", Min Temperature: " + dailyForecast.getMinTemperature() + ", Max Temperature: " + dailyForecast.getMaxTemperature() + ", Forecast: " + dailyForecast.getForecast());
                    // Handle displaying daily forecast data
                }
            }

            @Override
            public void onError(String message) {
                Log.e(TAG, "Weather API Error: " + message);
                Toast.makeText(MainActivity.this, "Failed to fetch weather data", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
