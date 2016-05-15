package com.example.android.project1;

/**
 * Created by fady on 3/31/2016.
 */
public class JobContent {

    String jobName;
    public JobContent (String job){
        this.jobName=job;
    }
    public void setJobName(String job){
        this.jobName=job;
    }
    public String getJobName(){
        return this.jobName;
    }
}
