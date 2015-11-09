package com.example.android.project1;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.List;

/**
 * Created by Joey on 11/9/2015.
 */
public class MessageAdapter extends ArrayAdapter {
    private final Activity activity;
    private final List messages;

    public MessageAdapter(Activity activity, List objs) {
        super(activity, R.layout.message_list , objs);
        this.activity = activity;
        this.messages = objs;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View rowView = convertView;
        MessageView msgView = null;
        if(rowView == null)
        {
            // Get a new instance of the row layout view
            LayoutInflater inflater = activity.getLayoutInflater();
            rowView = inflater.inflate(R.layout.message_list, null);

            // Hold the view objects in an object,
            // so they don't need to be re-fetched
            msgView = new MessageView();
            msgView.msg = (TextView) rowView.findViewById(R.id.message_text);

            // Cache the view objects in the tag,
            // so they can be re-accessed later
            rowView.setTag(msgView);
        } else {
            msgView = (MessageView) rowView.getTag();
        }

        // Transfer the stock data from the data object
        // to the view objects
        MessageData currentMsg =  (MessageData)messages.get(position);
        msgView.msg.setText(currentMsg.getMessage());

        return rowView;
    }

    protected static class MessageView {
        protected TextView msg;
    }
}

