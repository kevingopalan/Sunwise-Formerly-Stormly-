package com.venomdevelopment.sunwise;

import android.content.Context;
import android.util.Log;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class WeatherService {
    private static final String TAG = WeatherService.class.getSimpleName();
    private static final String BASE_URL = "https://api.weather.gov/";

    private final Context context;
    private RequestQueue requestQueue;

    public WeatherService(Context context) {
        this.context = context;
        requestQueue = Volley.newRequestQueue(context);
    }

    public interface WeatherResponseListener {
        void onResponse(CurrentWeather currentWeather, List<HourlyForecast> hourlyForecasts, List<DailyForecast> dailyForecasts);
        void onError(String message);
    }

    public void getWeather(double latitude, double longitude, final WeatherResponseListener listener) {
        String url = BASE_URL + "gridpoints/MTR/" + latitude + "," + longitude + "/forecast";

        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.GET, url, null,
                response -> {
                    try {
                        JSONObject properties = response.getJSONObject("properties");

                        // Parse current weather
                        JSONArray periods = properties.getJSONArray("periods");
                        JSONObject current = periods.getJSONObject(0);
                        CurrentWeather currentWeather = parseCurrentWeather(current);

                        // Parse hourly forecast
                        List<HourlyForecast> hourlyForecasts = parseHourlyForecast(periods);

                        // Parse daily forecast
                        List<DailyForecast> dailyForecasts = parseDailyForecast(periods);

                        listener.onResponse(currentWeather, hourlyForecasts, dailyForecasts);
                    } catch (JSONException e) {
                        listener.onError("Failed to parse weather data: " + e.getMessage());
                    }
                }, error -> listener.onError("Failed to retrieve weather data: " + error.getMessage())) {

            // Override getHeaders() to set custom User-Agent header
            @Override
            public Map<String, String> getHeaders() {
                Map<String, String> headers = new HashMap<>();
                headers.put("User-Agent", "Mozilla/5.0");
                return headers;
            }
        };

        requestQueue.add(jsonObjectRequest);
    }


    private CurrentWeather parseCurrentWeather(JSONObject current) throws JSONException {
        String temperature = current.getString("temperature");
        String minTemperature = current.getString("temperature");
        String maxTemperature = current.getString("temperature");
        String forecast = current.getString("detailedForecast");

        return new CurrentWeather(temperature, minTemperature, maxTemperature, forecast);
    }

    private List<HourlyForecast> parseHourlyForecast(JSONArray periods) throws JSONException {
        List<HourlyForecast> hourlyForecasts = new ArrayList<>();

        for (int i = 0; i < periods.length(); i++) {
            JSONObject period = periods.getJSONObject(i);
            String time = period.getString("startTime");
            String temperature = period.getString("temperature");
            String forecast = period.getString("detailedForecast");

            HourlyForecast forecastItem = new HourlyForecast(time, temperature, forecast);
            hourlyForecasts.add(forecastItem);
        }

        return hourlyForecasts;
    }

    private List<DailyForecast> parseDailyForecast(JSONArray periods) throws JSONException {
        List<DailyForecast> dailyForecasts = new ArrayList<>();

        for (int i = 0; i < periods.length(); i++) {
            JSONObject period = periods.getJSONObject(i);
            String date = period.getString("startTime");
            String minTemperature = period.getString("temperature");
            String maxTemperature = period.getString("temperature");
            String forecast = period.getString("detailedForecast");

            DailyForecast forecastItem = new DailyForecast(date, minTemperature, maxTemperature, forecast);
            dailyForecasts.add(forecastItem);
        }

        return dailyForecasts;
    }
}
