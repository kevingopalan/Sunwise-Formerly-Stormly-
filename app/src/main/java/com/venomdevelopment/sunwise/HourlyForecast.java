package com.venomdevelopment.sunwise;

public class HourlyForecast {
    private String time;
    private String temperature;
    private String forecast;

    public HourlyForecast(String time, String temperature, String forecast) {
        this.time = time;
        this.temperature = temperature;
        this.forecast = forecast;
    }

    public String getTime() {
        return time;
    }

    public String getTemperature() {
        return temperature;
    }

    public String getForecast() {
        return forecast;
    }
}
