package com.smartpost.entities;

public abstract class User {

    public String token;

    public Long lat;
    public Long lng;

    public User(){

    }

    public User(String token){
        this.token = token;
    }

    public abstract void openPostLoginActivity();

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public Long getLat() {
        return lat;
    }

    public void setLat(Long lat) {
        this.lat = lat;
    }
}
