package com.hung91hn.weather.adapter;

import android.app.Activity;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.bumptech.glide.Glide;
import com.hung91hn.weather.R;
import com.hung91hn.weather.model.WeatherDaily;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import static com.hung91hn.weather.utils.Share.URL_WEATHER_ICON;
import static com.hung91hn.weather.utils.Share.URL_WEATHER_ICON_TYPE;
import static com.hung91hn.weather.utils.Share.openWeb;
import static com.hung91hn.weather.utils.Share.toCelsius;

/**
 * Created by hung91hn on 12/1/16.
 */

public class DailyWeatherAdapter extends RecyclerView.Adapter<WeatherHolder> {
    private Activity activity;
    private List<WeatherDaily> allWeatherDailies;
    private SimpleDateFormat sdf;
    private boolean unitIsC;

    public DailyWeatherAdapter(Activity activity, List<WeatherDaily> allWeatherHourlies, boolean unitIsC) {
        this.activity = activity;
        this.allWeatherDailies = allWeatherHourlies;
        this.unitIsC = unitIsC;

        sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
    }

    @Override
    public WeatherHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View itemview = inflater.inflate(R.layout.adapter_weather_hourly, parent, false);
        return new WeatherHolder(itemview);
    }

    @Override
    public void onBindViewHolder(WeatherHolder holder, int position) {
        final WeatherDaily weatherDaily = allWeatherDailies.get(position);
        try {
            Date dateTime = sdf.parse(weatherDaily.getDateTime());
            String thisDay = activity.getResources().getStringArray(R.array.day_in_week)[dateTime.getDay()];
            holder.tvTime.setText(thisDay);

        } catch (ParseException e) {
            e.printStackTrace();
        }

        final String iconInx = String.format("%02d", weatherDaily.getIcon());
        String imageUrl = URL_WEATHER_ICON + iconInx + URL_WEATHER_ICON_TYPE;
        Glide.with(activity).load(imageUrl).into(holder.ivIcon);

        holder.ivIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                openWeb(activity,weatherDaily.getMobileLink());
            }
        });

        int valueMin = weatherDaily.getTempValueMin();
        int valueMax = weatherDaily.getTempValueMAX();
        String unit = weatherDaily.getTempUnit();
        if (unitIsC) {
            valueMin = toCelsius(valueMin);
            valueMax = toCelsius(valueMax);
            unit = "C";
        }
        holder.tvTemp.setText(valueMin + "-" + valueMax + "°" + unit);
    }

    @Override
    public int getItemCount() {
        return allWeatherDailies.size();
    }
}
