package com.venomdevelopment.sunwise;

import static com.venomdevelopment.sunwise.GraphViewUtils.setLabelTypeface;
import static com.venomdevelopment.sunwise.GraphViewUtils.setTitleTypeface;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Typeface;
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

import androidx.core.content.res.ResourcesCompat;
import androidx.datastore.core.*;
import androidx.datastore.preferences.core.Preferences;
import androidx.datastore.preferences.rxjava3.RxPreferenceDataStoreBuilder;
import androidx.datastore.rxjava3.RxDataStore;
import androidx.fragment.app.Fragment;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.airbnb.lottie.LottieAnimationView;
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

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.GridLabelRenderer;
import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.LineGraphSeries;

public class HomeFragment extends Fragment {
    private static final String TAG = MainActivity.class.getSimpleName();
    private static final String BASE_URL_POINTS = "https://api.weather.gov/points/";
    private static final String NOMINATIM_URL = "https://nominatim.openstreetmap.org/search?q=";
    private static final String USER_AGENT = "Mozilla/5.0";

    private RequestQueue requestQueue;
    private TextView tempText, descText, humidityText, wind, precipitation;
    private EditText search;
    private LottieAnimationView animationView;
    private Button searchButton;
    GraphView graphView;
    GraphView dayGraphView;

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
        View v = inflater.inflate(R.layout.fragment_home, container, false);
        tempText = v.findViewById(R.id.text_home);
        descText = v.findViewById(R.id.text_desc);
        search = v.findViewById(R.id.text_search);
        animationView = v.findViewById(R.id.animation_view);
        searchButton = v.findViewById(R.id.search);
        humidityText = v.findViewById(R.id.humidity);
        wind = v.findViewById(R.id.wind);
        precipitation = v.findViewById(R.id.precipitation);
        // Initialize Volley RequestQueue
        requestQueue = Volley.newRequestQueue(getContext());
        searchButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
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
        if(!search.getText().toString().isEmpty()) {
            searchButton.performClick();
        }
        graphView = v.findViewById(R.id.hrGraphContent);
        dayGraphView = v.findViewById(R.id.dayGraphContent);
        dayGraphView.getGridLabelRenderer().setGridStyle(GridLabelRenderer.GridStyle.NONE);
        graphView.getGridLabelRenderer().setGridStyle(GridLabelRenderer.GridStyle.NONE);
        graphView.getViewport().setXAxisBoundsManual(true);
        dayGraphView.getViewport().setXAxisBoundsManual(true);
        graphView.getViewport().setMinX(0);
        graphView.getViewport().setMaxX(23);
        dayGraphView.getViewport().setMinX(0);
        dayGraphView.getViewport().setMaxX(6);
        setLabelTypeface(getContext(), graphView, R.font.montsemibold);
        setLabelTypeface(getContext(), dayGraphView, R.font.montsemibold);
        setTitleTypeface(getContext(), graphView, R.font.montsemibold);
        setTitleTypeface(getContext(), dayGraphView, R.font.montsemibold);
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
                                                boolean daytime = current.getBoolean("isDaytime");
                                                String temperature = current.optString("temperature");
                                                String description = current.optString("shortForecast");
                                                double precipChance = current.optDouble("probabilityOfPrecipitation", 0);
                                                double humidity = current.optDouble("humidity", 0);
                                                Log.d("precip", String.valueOf(precipChance));
                                                Log.d("humidity", String.valueOf(humidity));
                                                WeatherData weatherData = new WeatherData(temperature, description);
                                                LineGraphSeries<DataPoint> series = new LineGraphSeries<>();
                                                LineGraphSeries<DataPoint> series2 = new LineGraphSeries<>();
                                                if (daytime) {
                                                    for (int i = 0; i < 7; i++) {
                                                        series.appendData(new DataPoint(i, Double.parseDouble(properties.getJSONArray("periods").getJSONObject(2 * i).getString("temperature"))), false, 7);
                                                    }
                                                    for (int i = 0; i < 7; i++) {
                                                        series2.appendData(new DataPoint(i, Double.parseDouble(properties.getJSONArray("periods").getJSONObject(2*i+1).getString("temperature"))), false, 7);
                                                    }
                                                } else {
                                                    for (int i = 0; i < 7; i++) {
                                                        series.appendData(new DataPoint(i, Double.parseDouble(properties.getJSONArray("periods").getJSONObject(2 * i).getString("temperature"))), false, 7);
                                                    }
                                                    for (int i = 0; i < 7; i++) {
                                                        series2.appendData(new DataPoint(i + 1, Double.parseDouble(properties.getJSONArray("periods").getJSONObject(2*i+1).getString("temperature"))), false, 7);
                                                    }
                                                }

                                                // after adding data to our line graph series.
                                                // on below line we are setting
                                                // title for our graph view.
                                                dayGraphView.setTitle("Daily");

                                                // on below line we are setting
                                                // text color to our graph view.
                                                dayGraphView.setTitleColor(Color.parseColor("#FFFFFF"));
                                                if (daytime) {
                                                    series.setColor(Color.parseColor("#FF5555"));
                                                    series2.setColor(Color.parseColor("#0000FF"));
                                                } else {
                                                    series.setColor(Color.parseColor("#0000FF"));
                                                    series2.setColor(Color.parseColor("#FF5555"));
                                                }

                                                // on below line we are setting
                                                // our title text size.
                                                dayGraphView.setTitleTextSize(50);
                                                dayGraphView.getGridLabelRenderer().setVerticalLabelsAlign(Paint.Align.LEFT);

                                                // on below line we are adding
                                                // data series to our graph view.
                                                dayGraphView.addSeries(series);
                                                dayGraphView.addSeries(series2);
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
                                                Boolean daytime = current.getBoolean("isDaytime");
                                                String icon;
                                                String lottieAnim;
                                                int humidity = current.getJSONObject("relativeHumidity").getInt("value");
                                                int precipitationProbability = current.getJSONObject("probabilityOfPrecipitation").getInt("value");
                                                Log.d("precip", String.valueOf(precipitationProbability));
                                                Log.d("humidity", String.valueOf(humidity));
                                                humidityText.setText(humidity + "%");
                                                precipitation.setText(precipitationProbability + "%");
                                                new WeatherData(temperature, description);
                                                WeatherData weatherHourlyData;
                                                LineGraphSeries<DataPoint> series = new LineGraphSeries<>();
                                                for (int i = 0; i < 24; i++) {
                                                    series.appendData(new DataPoint(i, Double.parseDouble(properties.getJSONArray("periods").getJSONObject(i).getString("temperature"))), false, 24);
                                                }

                                                // after adding data to our line graph series.
                                                // on below line we are setting
                                                // title for our graph view.
                                                graphView.setTitle("Hourly");

                                                // on below line we are setting
                                                // text color to our graph view.
                                                graphView.setTitleColor(Color.parseColor("#FFFFFF"));
                                                series.setColor(Color.parseColor("#FFFFFF"));

                                                // on below line we are setting
                                                // our title text size.
                                                graphView.setTitleTextSize(50);
                                                graphView.getGridLabelRenderer().setVerticalLabelsAlign(Paint.Align.LEFT);
                                                String prefix;
                                                // on below line we are adding
                                                // data series to our graph view.
                                                graphView.addSeries(series);
                                                for (int i = 0; i < 144; i++) {
                                                    current = properties.getJSONArray("periods").getJSONObject(i);
                                                    temperature = current.getString("temperature");
                                                    description = current.getString("shortForecast");
                                                    if (daytime) {
                                                        prefix = "_day";
                                                    } else {
                                                        prefix = "_night";
                                                    }
                                                    if (description.toLowerCase().contains("snow")) {
                                                        icon = "snow";
                                                        lottieAnim = "snow";
                                                    }
                                                    else if (description.toLowerCase().contains("rain") || description.toLowerCase().contains("showers")) {
                                                        icon = "lrain";
                                                        lottieAnim = "rain";
                                                    }
                                                    else if (description.toLowerCase().contains("partly")) {
                                                        icon = "pcloudy";
                                                        lottieAnim = "partly_cloudy" + prefix;
                                                    }
                                                    else if (description.toLowerCase().contains("sun")) {
                                                        icon = "sun";
                                                        lottieAnim = "clear" + prefix;
                                                    }
                                                    else if (description.toLowerCase().contains("clear")) {
                                                        icon = "clear";
                                                        lottieAnim = "clear" + prefix;
                                                    }
                                                    else if (description.toLowerCase().contains("storm")) {
                                                        icon = "tstorm";
                                                        lottieAnim = "thunderstorms" + prefix;
                                                    }
                                                    else if (description.toLowerCase().contains("wind") || description.toLowerCase().contains("gale") || description.toLowerCase().contains("dust") || description.toLowerCase().contains("blow")) {
                                                        icon = "wind";
                                                        lottieAnim = "wind";
                                                    } else if (description.toLowerCase().contains("fog")) {
                                                        icon = "clouds";
                                                        lottieAnim = "fog";
                                                    } else if (description.toLowerCase().contains("haze")) {
                                                        icon = "clouds";
                                                        lottieAnim = "haze";
                                                    }
                                                    else {
                                                        icon = "clouds";
                                                        lottieAnim = "cloudy";
                                                    }
                                                    weatherHourlyData = new WeatherData(temperature, description);
                                                    if (i == 0) {
                                                        updateUI(weatherHourlyData);
                                                        Log.d("icon", icon);
                                                        Log.d("lottie", "Lottie icon: " + lottieAnim);
                                                        animationView.setAnimation(getResources().getIdentifier(lottieAnim, "raw", getContext().getPackageName()));
                                                        animationView.loop(true);
                                                        animationView.playAnimation();
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