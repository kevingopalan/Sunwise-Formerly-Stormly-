package com.venomdevelopment.sunwise;

public class CurrentWeather {
    private String temperature;
    private String minTemperature;
    private String maxTemperature;
    private String forecast;

    public CurrentWeather(String temperature, String minTemperature, String maxTemperature, String forecast) {
        this.temperature = temperature;
        this.minTemperature = minTemperature;
        this.maxTemperature = maxTemperature;
        this.forecast = forecast;
    }

    public String getTemperature() {
        return temperature;
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
