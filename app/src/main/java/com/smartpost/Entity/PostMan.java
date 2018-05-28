package com.smartpost.Entity;

import java.util.List;

public class PostMan {

    public String token;

    public Double latitude;
    public Double longitude;

    private String email;

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }


    public Double getLatitude() {
        return latitude;
    }

    public void setLatitude(Double latitude) {
        this.latitude = latitude;
    }

    public Double getLongitude() {
        return longitude;
    }


    @Override
    public String toString() {
        return "PostMan{" +
                "token='" + token + '\'' +
                ", latitude=" + latitude +
                ", longitude=" + longitude +
                ", email='" + email + '\'' +
                '}';
    }

    public void setLongitude(Double longitude) {
        this.longitude = longitude;
    }
}
