package com.venomdevelopment.sunwise;

import android.content.Context;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class WeatherService {
    private static final String API_KEY = "a7b6b9afe7bd471b10175c9743ddb5b3";
    private static final String BASE_URL = "https://api.openweathermap.org/data/2.5/forecast";
    private RequestQueue queue;

    public WeatherService(Context context) {
        queue = Volley.newRequestQueue(context);
    }

    public interface ForecastCallback {
        void onSuccess(JSONObject forecast);
        void onError(VolleyError error);
    }

    public void getForecast(String cityName, String units, final ForecastCallback callback) {
        String url = String.format("%s?q=%s&appid=%s&units=%s", BASE_URL, cityName, API_KEY, units);

        JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET, url, null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        callback.onSuccess(response);
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        callback.onError(error);
                    }
                });

        queue.add(request);
    }
}
