package com.venomdevelopment.sunwise;

import android.content.SharedPreferences;
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

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Objects;

public class ForecastFragment extends Fragment {


    private static final String TAG = MainActivity.class.getSimpleName();
    private static final String BASE_URL_POINTS = "https://api.weather.gov/points/";
    private static final String NOMINATIM_URL = "https://nominatim.openstreetmap.org/search?q=";
    private static final String USER_AGENT = "Mozilla/5.0";
    public int formattime;
    public int day;
    public String dayshort;
    private RequestQueue requestQueue;
    private TextView tempText, descText, humidityText;
    private ImageView mainimg;
    private EditText search;
    private Button searchButton;
    private Switch hrSwitch;
    private String forecastType;
    private String dayset;
    private String[] days = {"Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun"};

    public int currentDay;
    MyRecyclerViewAdapter adapter;
    public static final String myPref = "addressPref";

    public String getPreferenceValue()
    {
        SharedPreferences sp = getActivity().getSharedPreferences(myPref,0);
        String str = sp.getString("address","");
        return str;
    }
    public void writeToPreference(String thePreference)
    {
        SharedPreferences.Editor editor = getActivity().getSharedPreferences(myPref,0).edit();
        editor.putString("address", thePreference);
        editor.commit();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_forecast, container, false);
        tempText = v.findViewById(R.id.text_home);
        descText = v.findViewById(R.id.text_desc);
        hrSwitch = v.findViewById(R.id.hrSwitch);
        mainimg = v.findViewById(R.id.imageView);
        search = v.findViewById(R.id.text_search);
        searchButton = v.findViewById(R.id.search);
        // Initialize Volley RequestQueue
        requestQueue = Volley.newRequestQueue(getContext());

        searchButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (hrSwitch.isChecked()) {
                    forecastType = "";
                }
                else {
                    forecastType = "Hourly";
                }
                String address = search.getText().toString().trim();
                if (!address.isEmpty()) {
                    fetchGeocodingData(address);
                    writeToPreference(address);
                } else {
                    Toast.makeText(getContext(), "Please enter an address", Toast.LENGTH_SHORT).show();
                }
            }
        });
        search.setText(getPreferenceValue(), TextView.BufferType.EDITABLE);
        searchButton.performClick();
        hrSwitch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (hrSwitch.isChecked()) {
                    forecastType = "";
                }
                else {
                    forecastType = "Hourly";
                }
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
                            String forecastUrl = properties.getString("forecast" + forecastType);

                            // Fetch weather data from the forecast URL
                            JsonObjectRequest forecastHourlyRequest = new JsonObjectRequest
                                    (Request.Method.GET, forecastUrl, null, new Response.Listener<JSONObject>() {
                                        @Override
                                        public void onResponse(JSONObject response) {
                                            try {
                                                // Parse the weather data
                                                JSONObject properties = response.getJSONObject("properties");
                                                JSONObject current = properties.getJSONArray("periods").getJSONObject(0);
                                                String temperature = current.getString("temperature");
                                                String description = current.getString("shortForecast");
                                                String humidity;
                                                if (!hrSwitch.isChecked()) {
                                                    humidity = current.getJSONObject("relativeHumidity").getInt("value") + "%";
                                                } else {
                                                    humidity = "?";
                                                }
                                                String precipitationProbability = current.getJSONObject("probabilityOfPrecipitation").getInt("value") + "";
                                                Log.d("precip", precipitationProbability);
                                                boolean daytime = current.getBoolean("isDaytime");
                                                String icon;
                                                new WeatherData(temperature, description);
                                                WeatherData weatherHourlyData;
                                                ArrayList<String> hourlyItems = new ArrayList<>();
                                                ArrayList<String> hourlyTime = new ArrayList<>();
                                                ArrayList<String> hourlyIcon = new ArrayList<>();
                                                ArrayList<String> hourlyDay = new ArrayList<>();
                                                ArrayList<String> hourlyPrecipitation = new ArrayList<>();
                                                ArrayList<String> hourlyHumidity = new ArrayList<>();
                                                formattime = java.time.LocalTime.now().getHour();
                                                dayshort = LocalDateTime.now().format(DateTimeFormatter.ofPattern("EEE"));
                                                day = 0;
                                                dayset = "no";
                                                Log.d("currentday", dayshort);
                                                Log.d("dayinarray", days[0]);
                                                for (int i = 0; i < 7; i++) {
                                                    if (day < 6) {
                                                        day++;
                                                    }
                                                    else {
                                                        day = 0;
                                                    }
                                                    if (Objects.equals(dayshort, days[day]) && !Objects.equals(dayset, "yes")) {
                                                        currentDay = day;
                                                        Log.d("firstdayset", days[currentDay]);
                                                        dayset = "yes";
                                                    }

                                                }
                                                for (int i = 0; i < 144; i++) {
                                                    current = properties.getJSONArray("periods").getJSONObject(i);
                                                    if (!hrSwitch.isChecked()) {
                                                        humidity = current.getJSONObject("relativeHumidity").getInt("value") + "%";
                                                    } else {
                                                        humidity = "?";
                                                    }
                                                    precipitationProbability = String.valueOf(current.getJSONObject("probabilityOfPrecipitation").getInt("value"));
                                                    if (precipitationProbability.isEmpty()) {
                                                        precipitationProbability = "?";
                                                    }
                                                    if (humidity.isEmpty()) {
                                                        humidity = "?";
                                                    }
                                                    if (forecastType.isEmpty()) {
                                                        current = properties.getJSONArray("periods").getJSONObject(i);
                                                    }
                                                    if(dayset.equals("yes")) {
                                                        Log.d("day", days[day]);
                                                    }
                                                    if (day < 6) {
                                                        day++;
                                                    }
                                                    else {
                                                        day = 0;
                                                    }
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
//                                                    if (i == 0) {
//                                                        updateUI(weatherHourlyData);
//                                                        Log.d("icon", icon);
//                                                        mainimg.setImageResource(getResources().getIdentifier(icon, "drawable", getContext().getPackageName()));
//                                                    }
                                                    hourlyItems.add(weatherHourlyData.getTemperature() + "°");
                                                    hourlyPrecipitation.add(precipitationProbability + "%");
                                                    hourlyHumidity.add(humidity);
                                                    formattime++;
                                                    if (formattime > 23) {
                                                        formattime = formattime - 24;
                                                    }
                                                    hourlyTime.add(formattime + ":00");
                                                    if (daytime) {
                                                        if (i == 0) {
                                                            hourlyDay.add("Today");
                                                            hourlyDay.add("Tonight");
                                                        } else {
                                                            hourlyDay.add(days[day]);
                                                            hourlyDay.add(days[day] + ". Night");
                                                        }

                                                    } else {
                                                        if (i == 0) {
                                                            hourlyDay.add("Today");
                                                        } else {
                                                            hourlyDay.add(days[day] + ". Night");
                                                        }
                                                        hourlyDay.add(days[day]);
                                                    }
                                                    hourlyIcon.add(icon);

                                                    // set up the RecyclerView
                                                    RecyclerView recyclerView = getView().findViewById(R.id.hourlyRecyclerView);
                                                    recyclerView.setNestedScrollingEnabled(false);
                                                    recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
                                                    if (hrSwitch.isChecked()) {
                                                        adapter = new MyRecyclerViewAdapter(getContext(), hourlyItems, hourlyDay, hourlyIcon, hourlyPrecipitation, hourlyHumidity);
                                                    } else {
                                                        adapter = new MyRecyclerViewAdapter(getContext(), hourlyItems, hourlyTime, hourlyIcon, hourlyPrecipitation, hourlyHumidity);
                                                    }
                                                    recyclerView.setAdapter(adapter);
                                                }
                                            } catch (JSONException e) {
                                                e.printStackTrace();
                                                Toast.makeText(getContext(), "Something went wrong (If it still works, we just ran out of data. That's all.)", Toast.LENGTH_SHORT).show();
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
                            forecastUrl = properties.getString("forecastHourly");
                            JsonObjectRequest forecastHourlyRequestTwo = new JsonObjectRequest
                                    (Request.Method.GET, forecastUrl, null, new Response.Listener<JSONObject>() {
                                        @Override
                                        public void onResponse(JSONObject response) {
                                            try {
                                                // Parse the weather data
                                                JSONObject properties = response.getJSONObject("properties");
                                                JSONObject current = properties.getJSONArray("periods").getJSONObject(0);
                                                String temperature = current.getString("temperature");
                                                String description = current.getString("shortForecast");
                                                String icon;
                                                new WeatherData(temperature, description);
                                                WeatherData weatherHourlyData;
                                                formattime = java.time.LocalTime.now().getHour();
                                                for (int i = 0; i < 144; i++) {
                                                    current = properties.getJSONArray("periods").getJSONObject(i);
                                                    if (forecastType.isEmpty()) {
                                                        current = properties.getJSONArray("periods").getJSONObject(i);
                                                    }
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
                                                Toast.makeText(getContext(), "Something went wrong (If it still works, we just ran out of data. That's all.)", Toast.LENGTH_SHORT).show();
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
                            requestQueue.add(forecastHourlyRequest);
                            requestQueue.add(forecastHourlyRequestTwo);
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