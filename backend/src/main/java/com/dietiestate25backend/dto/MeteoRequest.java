package main.java.com.dietiestate25backend.dto;

public class MeteoRequest {
    private String city;
    private String date;

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }
}