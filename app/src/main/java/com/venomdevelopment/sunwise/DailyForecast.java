package com.venomdevelopment.sunwise;

public class DailyForecast {
    private String date;
    private String minTemperature;
    private String maxTemperature;
    private String forecast;

    public DailyForecast(String date, String minTemperature, String maxTemperature, String forecast) {
        this.date = date;
        this.minTemperature = minTemperature;
        this.maxTemperature = maxTemperature;
        this.forecast = forecast;
    }

    public String getDate() {
        return date;
    }

    public String getMinTemperature() {
        return minTemperature;
    }

    public String getMaxTemperature() {
        return maxTemperature;
    }

    public String getForecast() {
        return forecast;
    }
}
