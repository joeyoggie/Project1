package com.example.android.project1;

/**
 * Created by Joey on 3/29/2016.
 */
public class Contact {
    private String phoneNumber;
    private String userName;
    private String name;

    public Contact()
    {
        this.phoneNumber = null;
        this.userName = null;
        this.name = null;
    }
    public Contact(String phoneNumber, String userName, String name)
    {
        this.phoneNumber = phoneNumber;
        this.userName = userName;
        this.name = name;
    }

    public void setPhoneNumber(String phoneNumber)
    {
        this.phoneNumber = phoneNumber;
    }
    public void setUserName(String userName)
    {
        this.userName = userName;
    }
    public void setName(String name){
        this.name = name;
    }

    public String getPhoneNumber()
    {
        return this.phoneNumber;
    }
    public String getUserName()
    {
        return this.userName;
    }
    public String getName(){
        return this.name;
    }

}
