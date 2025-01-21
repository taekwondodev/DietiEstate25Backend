package main.java.com.dietiestate25backend.dto;

public class MeteoResponse {
    private String date;
    private double temperature;
    private String weatherDescription;

    // Costruttore senza argomenti richiesto da Jackson, ovvero da Spring
    public MeteoResponse() {}

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public double getTemperature() {
        return temperature;
    }

    public void setTemperature(double temperature) {
        this.temperature = temperature;
    }

    public String getWeatherDescription() {
        return weatherDescription;
    }

    public void setWeatherDescription(String weatherDescription) {
        this.weatherDescription = weatherDescription;
    }
}
