package com.mariospatsis.unipismartalert;

import android.content.Context;
import android.text.format.DateFormat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.Calendar;
import java.util.List;
import java.util.Locale;


/*
    Το καθε item της λιστας στο Statistics Activity
*  */
public class ItemAdapter extends BaseAdapter {
    List<EventModel> events;
    Context context;
    private static LayoutInflater inflater = null;

    public ItemAdapter (Context context, List<EventModel> events){
        this.events = events;
        this.context = context;
        inflater = (LayoutInflater) context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }
    @Override
    public int getCount() {
        return events.size();
    }

    @Override
    public Object getItem(int i) {
        return i;
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {
        EventModel event = events.get(i);
        View vi = view;
        if (vi == null)
            vi = inflater.inflate(R.layout.item, null);
        TextView time = (TextView) vi.findViewById(R.id.i_time);
        TextView lat = (TextView) vi.findViewById(R.id.i_lat);
        TextView lon = (TextView) vi.findViewById(R.id.i_lon);

        Calendar cal = Calendar.getInstance(Locale.ENGLISH);
        cal.setTimeInMillis(event.timestamp);
        String date = DateFormat.format("dd-MM-yyyy HH:mm", cal).toString();

        time.setText(context.getResources().getString(R.string.i_time,date));
        lat.setText(context.getResources().getString(R.string.i_lat,Double.toString(event.lat)));
        lon.setText(context.getResources().getString(R.string.i_lon,Double.toString(event.lon)));
        return vi;
    }
}
