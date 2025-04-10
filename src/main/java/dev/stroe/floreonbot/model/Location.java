package dev.stroe.floreonbot.model;

public class Location {
    private final double latitude;
    private final double longitude;
    private final String name;
    private final String country;

    public Location(double latitude, double longitude, String name, String country) {
        this.latitude = latitude;
        this.longitude = longitude;
        this.name = name;
        this.country = country;
    }

    public double getLatitude() {
        return latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public String getName() {
        return name;
    }

    public String getCountry() {
        return country;
    }

    public String getFormattedName() {
        return name + ", " + country;
    }
}