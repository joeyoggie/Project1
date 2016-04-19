package com.example.android.project1;

/**
 * Created by Joey on 4/2/2016.
 */
public class ServiceProvider {

    private String phoneNumber;
    private String userName;
    private String serviceCategory;
    private int rating;
    private Double longitude;
    private Double latitude;
    private String address;

    public ServiceProvider()
    {
        this.phoneNumber = null;
        this.userName = null;
        this.serviceCategory = null;
        this.longitude = null;
        this.latitude = null;
    }

    public ServiceProvider(String phoneNumber, String userName, Double longitude, Double latitude, String serviceCategory) {
        this.phoneNumber = phoneNumber;
        this.userName = userName;
        this.longitude = longitude;
        this.latitude = latitude;
        this.serviceCategory = serviceCategory;
    }


    public String getPhoneNumber()
    {
        return this.phoneNumber;
    }
    public String getUserName()
    {
        return this.userName;
    }
    public Double getLongitude()
    {
        return this.longitude;
    }
    public Double getLatitude()
    {
        return this.latitude;
    }
    public String getServiceCategory()
    {
        return this.serviceCategory;
    }

    public int getRating()
    {
        return this.rating;
    }
    public String getAddress()
    {
        return this.address;
    }

    public void setPhoneNumber(String phoneNumber)
    {
        this.phoneNumber = phoneNumber;
    }
    public void setUserName(String userName)
    {
        this.userName = userName;
    }
    public void setLongitude(Double longitude)
    {
        this.longitude = longitude;
    }
    public void setLatitude(Double latitude)
    {
        this.latitude = latitude;
    }
    public void setServiceCategory(String serviceCategory) {
        this.serviceCategory = serviceCategory;
    }
    public void setRating(int rating)
    {
        this.rating = rating;
    }
    public void setAddress(String address)
    {
        this.address = address;
    }

}
