package com.example.android.project1;

/**
 * Created by fady on 5/10/2016.
 */
public class User {String phoneNumber;
    String userName;
    String name;
    String status;
    //profile picture kaman
    //online/offline flag


    public User() {

    }

    public String getPhoneNumber(){
        return this.phoneNumber;
    }

    public String getUserName(){
        return this.userName;
    }

    public String getName(){
        return this.name;
    }

    public String getStatus(){
        return this.status;
    }

    public void setPhoneNumber(String phoneNumber){
        this.phoneNumber = phoneNumber;
    }

    public void setUserName(String userName){
        this.userName = userName;
    }

    public void setName(String name){
        this.name = name;
    }

    public void setStatus(String status){
        this.status = status;
    }
}
