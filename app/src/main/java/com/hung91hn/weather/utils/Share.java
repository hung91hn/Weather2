package com.hung91hn.weather.utils;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;

import com.hung91hn.weather.model.WeatherLocation;

import org.json.JSONObject;

import io.realm.Realm;

/**
 * Created by hung91hn on 12/13/16.
 */

public class Share {
    public static final String PREFERENCE_KEY = "WEATHER";
    public static final String TEMP_UNIT = "donvi";
    public static final String TIME_UPDATE = "updateTime";
    public static final String LOCATION_KEY = "location";
    public static final String API_KEY = "JVZgNJ8lEM4EpcoHyuQvEePb3HjPS6A4";
    public static final String API_LOCATION = "http://dataservice.accuweather.com/locations/v1/cities/geoposition/search";
    public static final String API_WEATHER_HOURLY = "http://dataservice.accuweather.com/forecasts/v1/hourly/12hour/";
    public static final String API_WEATHER_DAILY = "http://dataservice.accuweather.com/forecasts/v1/daily/5day/";
    public static final String URL_WEATHER_ICON = "http://developer.accuweather.com/sites/default/files/";
    public static final String URL_WEATHER_ICON_TYPE = "-s.png";
    public static final String WEATHER_HOURLY_CHANGED = "hourCHANGE";
    public static final String WEATHER_DAILY_CHANGED = "dailyChage";

    public static final int FAKE_INT = 10000;
    public static final int MAX_LOCATION_NUMBER = 5;

    public static boolean LISTLOCA_CHANGED = false;
    public static boolean UNIT_CHANGED = false;
    public static boolean UPDATE_CHANGED = false;

    public static int toCelsius(int fahrenheit) {
        return (fahrenheit - 32) * 5 / 9;
    }


    public static void openWeb(Activity activity, String url) {
        Intent i = new Intent(Intent.ACTION_VIEW);
        i.setData(Uri.parse(url));
        activity.startActivity(i);
    }

    public static void addLocationToRealm(WeatherLocation location) {
        Realm realm = Realm.getDefaultInstance();
        if (realm.where(WeatherLocation.class).findAll().size() < MAX_LOCATION_NUMBER) {
            realm.beginTransaction();
            WeatherLocation weatherLocation = realm.copyToRealmOrUpdate(location);
            realm.insertOrUpdate(weatherLocation);
            realm.commitTransaction();
        }
    }


    public static WeatherLocation getWeatherLocation(JSONObject jsonObject) {
        WeatherLocation weatherLocation = new WeatherLocation();
        weatherLocation.setLocationKey(JsonUtil.getString(jsonObject, "Key", null));
        String locaName = JsonUtil.getString(jsonObject, "LocalizedName", null);
        weatherLocation.setLocalizedName(locaName);
        JSONObject jsonObject2 = JsonUtil.getJSONObject(jsonObject, "AdministrativeArea");
        weatherLocation.setArea(JsonUtil.getString(jsonObject2, "LocalizedName", null));
        jsonObject2 = JsonUtil.getJSONObject(jsonObject, "Country");
        weatherLocation.setCountry(JsonUtil.getString(jsonObject2, "LocalizedName", null));
        jsonObject2 = JsonUtil.getJSONObject(jsonObject, "Region");
        weatherLocation.setRegion(JsonUtil.getString(jsonObject2, "LocalizedName", null));
        jsonObject2 = JsonUtil.getJSONObject(jsonObject, "TimeZone");
        weatherLocation.setTimeZone(JsonUtil.getInt(jsonObject2, "GmtOffset", 0));
        return weatherLocation;
    }
}
