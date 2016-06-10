package com.example.android.project1;

import android.app.Activity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by fady on 3/31/2016.
 */
public class JobsAdapter extends ArrayAdapter implements Filterable {
    List<JobContent> jobsList;
    ArrayList<JobContent> jobsBackupList = new ArrayList<>();
    Activity activity;
    //TextView job_name_text_view;

    public JobsAdapter(Activity activity,List jobContentList){
        super(activity,R.layout.job_item, jobContentList);
        this.jobsList = jobContentList;
        jobsBackupList.addAll(jobsList);
        this.activity=activity;
    }

    Filter myFilter = new Filter() {
        @Override
        protected FilterResults performFiltering(CharSequence constraint) {
            FilterResults filterResults = new FilterResults();
            ArrayList<JobContent> tempList = new ArrayList<JobContent>();
            //constraint is the result from text you want to filter against.
            //jobContentList is your data set you will filter from
            if(constraint != null && jobsList != null) {
                if(jobsList.isEmpty()) {
                    jobsList.addAll(jobsBackupList);
                }
                if(constraint.length() >= 1){
                    for (JobContent element : jobsList) {
                        if(element.getJobName().toLowerCase().contains(constraint)) {
                            tempList.add(element);
                        }
                    }
                    //following two lines is very important
                    //as publish result can only take FilterResults objects
                    filterResults.values = tempList;
                    filterResults.count = tempList.size();
                }
                else {
                    filterResults.values = jobsBackupList;
                    filterResults.count = jobsBackupList.size();
                }
            }
            return filterResults;
        }

        @SuppressWarnings("unchecked")
        @Override
        protected void publishResults(CharSequence constraint, FilterResults results) {
            jobsList = (ArrayList<JobContent>) results.values;
            if (results.count > 0) {
                notifyDataSetChanged();
            } else {
                notifyDataSetInvalidated();
            }
        }
    };

    public void notifyDataSetChanged() {
        super.notifyDataSetChanged();
    }

    @Override
    public Filter getFilter() {
        return myFilter;
    }

    @Override
    public Object getItem(int position) {
        return jobsList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public int getCount() {
        return jobsList.size();
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View rowView = convertView;
        ViewHolder holder = null;
        if(rowView == null){
            rowView = activity.getLayoutInflater().inflate(R.layout.job_item, null);
            holder = new ViewHolder();
            holder.text_view = (TextView) rowView.findViewById(R.id.job_name);
            //job_name_text_view = (TextView) rowView.findViewById(R.id.job_name);
            rowView.setTag(holder);
        }
        else{
            holder = (ViewHolder)rowView.getTag();
        }

        //job_name_text_view.setText(jobsList.get(position).getJobName());
        holder.text_view.setText(jobsList.get(position).getJobName());

        return rowView;
    }

    static class ViewHolder {
        TextView text_view;
        int position;
    }

}


