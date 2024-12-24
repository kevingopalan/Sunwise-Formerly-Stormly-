package com.venomdevelopment.sunwise;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import androidx.fragment.app.Fragment;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public class HomeFragment extends Fragment {
    private static final String TAG = MainActivity.class.getSimpleName();
    private static final String BASE_URL_POINTS = "https://api.weather.gov/points/";
    private static final String NOMINATIM_URL = "https://nominatim.openstreetmap.org/search?q=";
    private static final String USER_AGENT = "Mozilla/5.0";

    private RequestQueue requestQueue;
    private TextView tempText, descText, humidityText, uv, precipitation;
    private EditText search;
    private ImageView mainimg;
    private Button searchButton;
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_home, container, false);
        tempText = v.findViewById(R.id.text_home);
        descText = v.findViewById(R.id.text_desc);
        search = v.findViewById(R.id.text_search);
        searchButton = v.findViewById(R.id.search);
        humidityText = v.findViewById(R.id.humidity);
        uv = v.findViewById(R.id.uv);
        precipitation = v.findViewById(R.id.precipitation);
        mainimg = v.findViewById(R.id.imageView);
        // Initialize Volley RequestQueue
        requestQueue = Volley.newRequestQueue(getContext());

        searchButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String address = search.getText().toString().trim();
                if (!address.isEmpty()) {
                    fetchGeocodingData(address);
                } else {
                    Toast.makeText(getContext(), "Please enter an address", Toast.LENGTH_SHORT).show();
                }
            }
        });
        return v;
    }
    private void fetchGeocodingData(String address) {
        // Encode the address for the URL
        String encodedAddress = address.replaceAll(" ", "+");
        String geocodeUrl = NOMINATIM_URL + encodedAddress + "&format=json&addressdetails=1";

        JsonArrayRequest jsonArrayRequest = new JsonArrayRequest
                (Request.Method.GET, geocodeUrl, null, new Response.Listener<JSONArray>() {
                    @Override
                    public void onResponse(JSONArray response) {
                        try {
                            // Get the first result
                            JSONObject firstResult = response.getJSONObject(0);
                            String lat = firstResult.getString("lat");
                            String lon = firstResult.getString("lon");

                            // Build the points URL using the coordinates
                            String pointsUrl = BASE_URL_POINTS + lat + "," + lon;
                            fetchWeatherData(pointsUrl);
                        } catch (JSONException e) {
                            e.printStackTrace();
                            Toast.makeText(getContext(), "Error parsing geocoding data", Toast.LENGTH_SHORT).show();
                        }
                    }
                }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.e(TAG, "Error fetching geocoding data: " + error.getMessage());
                        Toast.makeText(getContext(), "Error fetching geocoding data", Toast.LENGTH_SHORT).show();
                    }
                }) {
            @Override
            public Map<String, String> getHeaders() {
                Map<String, String> headers = new HashMap<>();
                headers.put("User-Agent", USER_AGENT);
                return headers;
            }
        };

        requestQueue.add(jsonArrayRequest);
    }

    private void fetchWeatherData(String pointsUrl) {
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest
                (Request.Method.GET, pointsUrl, null, new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            // Extract the forecast URL from the response
                            JSONObject properties = response.getJSONObject("properties");
                            String forecastUrl = properties.getString("forecast");
                            String forecastHourlyUrl = properties.getString("forecastHourly");

                            // Fetch weather data from the forecast URL
                            JsonObjectRequest forecastRequest = new JsonObjectRequest
                                    (Request.Method.GET, forecastUrl, null, new Response.Listener<JSONObject>() {
                                        @Override
                                        public void onResponse(JSONObject response) {
                                            try {
                                                // Parse the weather data
                                                JSONObject properties = response.getJSONObject("properties");
                                                JSONObject current = properties.getJSONArray("periods").getJSONObject(0);
                                                String temperature = current.getString("temperature");
                                                String description = current.getString("shortForecast");
                                                double precipChance = current.optDouble("probabilityOfPrecipitation", 0);
                                                double humidity = current.optDouble("humidity", 0);
                                                Log.d("precip", String.valueOf(precipChance));
                                                Log.d("humidity", String.valueOf(humidity));
                                                WeatherData weatherData = new WeatherData(temperature, description);
                                                //updateUI(weatherData);
                                            } catch (JSONException e) {
                                                e.printStackTrace();
                                                Toast.makeText(getContext(), "Error parsing weather data", Toast.LENGTH_SHORT).show();
                                            }
                                        }
                                    }, new Response.ErrorListener() {
                                        @Override
                                        public void onErrorResponse(VolleyError error) {
                                            Log.e(TAG, "Error fetching weather data: " + error.getMessage());
                                            Toast.makeText(getContext(), "Error fetching weather data", Toast.LENGTH_SHORT).show();
                                        }
                                    }) {
                                @Override
                                public Map<String, String> getHeaders() {
                                    Map<String, String> headers = new HashMap<>();
                                    headers.put("User-Agent", USER_AGENT);
                                    return headers;
                                }
                            };
                            JsonObjectRequest forecastHourlyRequest = new JsonObjectRequest
                                    (Request.Method.GET, forecastHourlyUrl, null, new Response.Listener<JSONObject>() {
                                        @Override
                                        public void onResponse(JSONObject response) {
                                            try {
                                                // Parse the weather data
                                                JSONObject properties = response.getJSONObject("properties");
                                                JSONObject current = properties.getJSONArray("periods").getJSONObject(0);
                                                String temperature = current.getString("temperature");
                                                String description = current.getString("shortForecast");
                                                String icon;
                                                int humidity = current.getJSONObject("relativeHumidity").getInt("value");
                                                int precipitationProbability = current.getJSONObject("probabilityOfPrecipitation").getInt("value");
                                                Log.d("precip", String.valueOf(precipitationProbability));
                                                Log.d("humidity", String.valueOf(humidity));
                                                humidityText.setText(humidity + "%");
                                                precipitation.setText(precipitationProbability + "%");
                                                new WeatherData(temperature, description);
                                                WeatherData weatherHourlyData;
                                                for (int i = 0; i < 144; i++) {
                                                    current = properties.getJSONArray("periods").getJSONObject(i);
                                                    temperature = current.getString("temperature");
                                                    description = current.getString("shortForecast");
                                                    if (description.toLowerCase().contains("snow")) {
                                                        icon = "snow";
                                                    }
                                                    else if (description.toLowerCase().contains("rain") || description.toLowerCase().contains("showers")) {
                                                        icon = "lrain";
                                                    }
                                                    else if (description.toLowerCase().contains("partly")) {
                                                        icon = "pcloudy";
                                                    }
                                                    else if (description.toLowerCase().contains("sun")) {
                                                        icon = "sun";
                                                    }
                                                    else if (description.toLowerCase().contains("clear")) {
                                                        icon = "clear";
                                                    }
                                                    else if (description.toLowerCase().contains("storm")) {
                                                        icon = "tstorm";
                                                    }
                                                    else if (description.toLowerCase().contains("wind") || description.toLowerCase().contains("gale") || description.toLowerCase().contains("dust")) {
                                                        icon = "wind";
                                                    }
                                                    else {
                                                        icon = "clouds";
                                                    }
                                                    weatherHourlyData = new WeatherData(temperature, description);
                                                    if (i == 0) {
                                                        updateUI(weatherHourlyData);
                                                        Log.d("icon", icon);
                                                        mainimg.setImageResource(getResources().getIdentifier(icon, "drawable", getContext().getPackageName()));
                                                    }
                                                }
                                            } catch (JSONException e) {
                                                e.printStackTrace();
                                                Toast.makeText(getContext(), "Error parsing weather data", Toast.LENGTH_SHORT).show();
                                            }
                                        }
                                    }, new Response.ErrorListener() {
                                        @Override
                                        public void onErrorResponse(VolleyError error) {
                                            Log.e(TAG, "Error fetching weather data: " + error.getMessage());
                                            Toast.makeText(getContext(), "Error fetching weather data", Toast.LENGTH_SHORT).show();
                                        }
                                    }) {
                                @Override
                                public Map<String, String> getHeaders() {
                                    Map<String, String> headers = new HashMap<>();
                                    headers.put("User-Agent", USER_AGENT);
                                    return headers;
                                }
                            };

                            requestQueue.add(forecastRequest);
                            requestQueue.add(forecastHourlyRequest);
                        } catch (JSONException e) {
                            e.printStackTrace();
                            Toast.makeText(getContext(), "Error parsing points data", Toast.LENGTH_SHORT).show();
                        }
                    }
                }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.e(TAG, "Error fetching points data: " + error.getMessage());
                        Toast.makeText(getContext(), "Error fetching points data", Toast.LENGTH_SHORT).show();
                    }
                }) {
            @Override
            public Map<String, String> getHeaders() {
                Map<String, String> headers = new HashMap<>();
                headers.put("User-Agent", USER_AGENT);
                return headers;
            }
        };

        requestQueue.add(jsonObjectRequest);
    }

    private void updateUI(WeatherData weatherData) {
        tempText.setText(weatherData.getTemperature() + "°");
        descText.setText(weatherData.getDescription());
    }
}