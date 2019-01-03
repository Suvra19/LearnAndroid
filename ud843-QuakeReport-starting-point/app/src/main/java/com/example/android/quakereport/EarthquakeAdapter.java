package com.example.android.quakereport;

import android.content.Context;
import android.graphics.drawable.GradientDrawable;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

public class EarthquakeAdapter extends ArrayAdapter {

    private static final String LOG_TAG = EarthquakeAdapter.class.getSimpleName();
    private static final String LOCATION_SEPARATOR = " of ";

    public EarthquakeAdapter(@NonNull Context context, @NonNull List<Earthquake> earthquakes) {
        super(context, 0, earthquakes);
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        View listItemView = convertView;

        if (listItemView == null) {
            listItemView = LayoutInflater.from(getContext()).inflate(R.layout.list_item, parent, false);
        }

        Earthquake earthquake = (Earthquake) getItem(position);

        TextView magnitudeTextView = (TextView) listItemView.findViewById(R.id.magnitude);
        double magnitude = earthquake.getmMagnitude();

        GradientDrawable magnitudeCircle = (GradientDrawable) magnitudeTextView.getBackground();
        int magnitudeColor = getMagnitudeColor(magnitude);
        magnitudeCircle.setColor(magnitudeColor);

        DecimalFormat decimalFormat = new DecimalFormat("0.0");
        magnitudeTextView.setText(decimalFormat.format(magnitude));


        String near, place;
        String location = earthquake.getmLocation();
        if (location.contains(LOCATION_SEPARATOR)) {
            String[] parts = location.split(LOCATION_SEPARATOR);
            near = parts[0] + LOCATION_SEPARATOR;
            place = parts[1];
        } else {
            near = getContext().getString(R.string.near_the);
            place = location;
        }

        TextView cityTextView = (TextView) listItemView.findViewById(R.id.city);
        cityTextView.setText(place);

        TextView nearTextView = (TextView) listItemView.findViewById(R.id.near);
        nearTextView.setText(near);

        TextView dateTextView = (TextView) listItemView.findViewById(R.id.date);
        Date date = new Date(earthquake.getmDate());
        dateTextView.setText(formatDate(date));

        TextView timeView = (TextView) listItemView.findViewById(R.id.time);
        String formattedTime = formatTime(date);
        timeView.setText(formattedTime);

        return listItemView;
    }

    private int getMagnitudeColor(double magnitude) {
        int resourceId;
        switch ((int) Math.floor(magnitude)) {
            case 1:
                resourceId = R.color.magnitude1;
                break;
            case 2:
                resourceId = R.color.magnitude2;
                break;
            case 3:
                resourceId = R.color.magnitude3;
                break;
            case 4:
                resourceId = R.color.magnitude4;
                break;
            case 5:
                resourceId = R.color.magnitude5;
                break;
            case 6:
                resourceId = R.color.magnitude6;
                break;
            case 7:
                resourceId = R.color.magnitude7;
                break;
            case 8:
                resourceId = R.color.magnitude8;
                break;
            case 9:
                resourceId = R.color.magnitude9;
                break;
            default:
                resourceId = R.color.magnitude10plus;
        }
        return ContextCompat.getColor(getContext(), resourceId);
    }

    /**
     * Return the formatted date string (i.e. "Mar 3, 1984") from a Date object.
     */
    private String formatDate(Date dateObject) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("LLL dd, yyyy");
        return dateFormat.format(dateObject);
    }

    /**
     * Return the formatted date string (i.e. "4:30 PM") from a Date object.
     */
    private String formatTime(Date dateObject) {
        SimpleDateFormat timeFormat = new SimpleDateFormat("h:mm a");
        return timeFormat.format(dateObject);
    }
}
