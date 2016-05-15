package com.example.android.project1;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.List;

/**
 * Created by fady on 3/31/2016.
 */
public class JobsAdapter extends ArrayAdapter {
    List <JobContent> jobContentList;
    Activity activity;
    TextView job_name_text_view;

    public JobsAdapter (Activity activity,List jobContentList){
        super(activity,R.layout.job_items, jobContentList);
        this.jobContentList = jobContentList;
        this.activity=activity;
    }

    @Override
    public Object getItem(int position) {
        return null;
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View rowView = convertView;
        if(rowView==null){
            LayoutInflater inflater = activity.getLayoutInflater();
            rowView = inflater.inflate(R.layout.job_items, null);
            job_name_text_view= (TextView) rowView.findViewById(R.id.job_name);
        }
        job_name_text_view.setText(jobContentList.get(position).getJobName().toString());
        return rowView;
    }

}


