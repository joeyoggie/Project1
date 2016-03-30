package com.example.android.project1;

/**
 * Created by Joey on 3/29/2016.
 */
public class Contact {
    private String phoneNumber;
    private String userName;

    public Contact()
    {
        this.phoneNumber = null;
        this.userName = null;
    }
    public Contact(String phoneNumber, String userName)
    {
        this.phoneNumber = phoneNumber;
        this.userName = userName;
    }

    public void setPhoneNumber(String phoneNumber)
    {
        this.phoneNumber = phoneNumber;
    }
    public void setUserName(String userName)
    {
        this.userName = userName;
    }

    public String getPhoneNumber()
    {
        return this.phoneNumber;
    }
    public String getUserName()
    {
        return this.userName;
    }

}
