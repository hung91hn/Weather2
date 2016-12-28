package com.hung91hn.weather.services;

import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.IBinder;
import android.support.v4.app.NotificationManagerCompat;
import android.support.v7.app.NotificationCompat;
import android.util.Log;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.error.VolleyError;
import com.hung91hn.weather.MainActivity;
import com.hung91hn.weather.R;
import com.hung91hn.weather.model.WeatherDaily;
import com.hung91hn.weather.model.WeatherHourly;
import com.hung91hn.weather.utils.CustomRequest;
import com.hung91hn.weather.utils.JsonUtil;
import com.hung91hn.weather.utils.MySingleton;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

import io.realm.Realm;
import io.realm.RealmConfiguration;

import static com.hung91hn.weather.utils.JsonUtil.getInt;
import static com.hung91hn.weather.utils.Share.API_KEY;
import static com.hung91hn.weather.utils.Share.API_WEATHER_DAILY;
import static com.hung91hn.weather.utils.Share.API_WEATHER_HOURLY;
import static com.hung91hn.weather.utils.Share.FAKE_INT;
import static com.hung91hn.weather.utils.Share.LOCATION_KEY;
import static com.hung91hn.weather.utils.Share.PREFERENCE_KEY;
import static com.hung91hn.weather.utils.Share.TIME_UPDATE;
import static com.hung91hn.weather.utils.Share.WEATHER_DAILY_CHANGED;
import static com.hung91hn.weather.utils.Share.WEATHER_HOURLY_CHANGED;

public class AutoUpdateService extends Service {
    private int timeUpdate;
    private SharedPreferences preferences;
    private String currentLocationKey;

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    //******** hàm này chỉ  chạy nếu Service==null *********
    // không viết các biến có thể thay đổi vào đây
    @Override
    public void onCreate() {
        super.onCreate();
        preferences = getSharedPreferences(PREFERENCE_KEY, MODE_PRIVATE);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        currentLocationKey = preferences.getString(LOCATION_KEY, null);
        timeUpdate = preferences.getInt(TIME_UPDATE, FAKE_INT);

        Timer timer = new Timer();
        TimerTask hourlyTask = new TimerTask() {
            @Override
            public void run() {

                getWeather(currentLocationKey);
            }

        };
        timer.schedule(hourlyTask, 0, 1000 * 60 * 60 * timeUpdate);
        return START_STICKY;
    }

    private void getWeather(String locationKey) {

        Map<String, String> mapApiParam = new HashMap<>();
        mapApiParam.put("apikey", API_KEY);
        CustomRequest customRequestHourly = new CustomRequest(Request.Method.GET, API_WEATHER_HOURLY + locationKey, mapApiParam, responseListenerWeatherHourly, errorListener);

        mapApiParam = new HashMap<>();
        mapApiParam.put("apikey", API_KEY);
        CustomRequest customRequestDaily = new CustomRequest(Request.Method.GET, API_WEATHER_DAILY + locationKey, mapApiParam, responseListenerWeatherDaily, errorListener);

        MySingleton.getInstance(this).addToRequestQueue(customRequestHourly, true);
        MySingleton.getInstance(this).addToRequestQueue(customRequestDaily, true);
    }

    private Response.Listener<String> responseListenerWeatherHourly = new Response.Listener<String>() {
        @Override
        public void onResponse(String response) {
            JSONArray responseArr = JsonUtil.createJSONArray(response);
            badWeatherNotification(responseArr, 4);
            addHourlyWeatherToRealm(currentLocationKey, responseArr);
        }
    };

    private Response.Listener<String> responseListenerWeatherDaily = new Response.Listener<String>() {
        @Override
        public void onResponse(String response) {
            JSONObject jsonObject = JsonUtil.createJSONObject(response);
            JSONArray jsonArray = JsonUtil.getJSONArray(jsonObject, "DailyForecasts");
            addDailyWeatherToRealm(currentLocationKey, jsonArray);

        }
    };

    private void badWeatherNotification(JSONArray responseArr, int hours) {
        int[] weathers = new int[hours];

        for (int i = 0; i < hours; i++) {
            JSONObject jsonObjectNow = JsonUtil.getJSONObject(responseArr, i);
            weathers[i] = getInt(jsonObjectNow, "WeatherIcon", FAKE_INT);
        }

        int[] badWeathers = {12, 13, 14, 15, 16, 17, 18, 22, 24, 25, 26, 29, 39, 40, 41, 42, 43, 44};
        boolean hasBadWeather = false;
        for (int badWeather : badWeathers) {
            for (int weather : weathers)
                if (weather == badWeather) {
                    hasBadWeather = true;
                    break;
                }
            if (hasBadWeather) break;
        }
        if (hasBadWeather) {
            Intent intent = new Intent(this, MainActivity.class);
            PendingIntent pi = PendingIntent.getActivity(this, 0, intent, 0);
            Notification notification = new NotificationCompat.Builder(this)
                    .setSmallIcon(R.mipmap.weather)
                    .setContentTitle(getResources().getString(R.string.app_name))
                    .setContentText(getResources().getString(R.string.bad_weather))
                    .setContentIntent(pi)
                    .build();
            NotificationManagerCompat.from(this).notify(0, notification);
        }
    }

    private void addHourlyWeatherToRealm(String locaKey, JSONArray responseArr) {
        RealmConfiguration configuration = new RealmConfiguration.Builder()
                .name(locaKey).build();
        Realm realm = Realm.getInstance(configuration);
        realm.beginTransaction();
        realm.where(WeatherHourly.class).findAll().deleteAllFromRealm();

        int leng = responseArr.length();
        for (int i = 0; i < leng; i++) {
            JSONObject jsonObject = JsonUtil.getJSONObject(responseArr, i);
            WeatherHourly weatherHourly = new WeatherHourly();
            weatherHourly.setDateTime(JsonUtil.getString(jsonObject, "DateTime", null));
            weatherHourly.setWeatherIcon(getInt(jsonObject, "WeatherIcon", FAKE_INT));
            weatherHourly.setIconPhrase(JsonUtil.getString(jsonObject, "IconPhrase", null));
            weatherHourly.setDaylight(JsonUtil.getBoolean(jsonObject, "IsDaylight", false));
            weatherHourly.setMobileLink(JsonUtil.getString(jsonObject, "MobileLink", null));

            JSONObject jsonObjectTemp = JsonUtil.getJSONObject(jsonObject, "Temperature");
            weatherHourly.setTemperatureValue(JsonUtil.getInt(jsonObjectTemp, "Value", FAKE_INT));
            weatherHourly.setTemperatureUnit(JsonUtil.getString(jsonObjectTemp, "Unit", null));
            weatherHourly.setPrecipitationProbability(JsonUtil.getInt(jsonObject, "PrecipitationProbability", FAKE_INT));

            realm.insertOrUpdate(realm.copyToRealmOrUpdate(weatherHourly));
        }
        realm.commitTransaction();


        // phat song
        Intent intent = new Intent();
        intent.setAction(WEATHER_HOURLY_CHANGED);
        sendBroadcast(intent);
    }

    private void addDailyWeatherToRealm(String locaKey, JSONArray jsonArray) {
        RealmConfiguration configuration = new RealmConfiguration.Builder()
                .name(locaKey).build();
        Realm realm = Realm.getInstance(configuration);
        realm.beginTransaction();
        realm.where(WeatherDaily.class).findAll().deleteAllFromRealm();

        int lenght = jsonArray.length();
        for (int i = 0; i < lenght; i++) {
            WeatherDaily weatherDaily = new WeatherDaily();

            JSONObject jsonObject = JsonUtil.getJSONObject(jsonArray, i);

            weatherDaily.setDateTime(JsonUtil.getString(jsonObject, "Date", null));
            weatherDaily.setMobileLink(JsonUtil.getString(jsonObject, "MobileLink", null));

            JSONObject jsonObjectDay = JsonUtil.getJSONObject(jsonObject, "Day");
            weatherDaily.setIcon(JsonUtil.getInt(jsonObjectDay, "Icon", FAKE_INT));

            JSONObject jsonObjectTemp = JsonUtil.getJSONObject(jsonObject, "Temperature");
            JSONObject jsonObjectMin = JsonUtil.getJSONObject(jsonObjectTemp, "Minimum");
            JSONObject jsonObjectMax = JsonUtil.getJSONObject(jsonObjectTemp, "Maximum");
            weatherDaily.setTempValueMin(JsonUtil.getInt(jsonObjectMin, "Value", FAKE_INT));
            weatherDaily.setTempValueMAX(JsonUtil.getInt(jsonObjectMax, "Value", FAKE_INT));
            weatherDaily.setTempUnit(JsonUtil.getString(jsonObjectMax, "Unit", null));

            realm.insertOrUpdate(realm.copyToRealmOrUpdate(weatherDaily));
        }
        realm.commitTransaction();

        // phat song
        Intent intent = new Intent();
        intent.setAction(WEATHER_DAILY_CHANGED);
        sendBroadcast(intent);
    }

    private Response.ErrorListener errorListener = new Response.ErrorListener() {
        @Override
        public void onErrorResponse(VolleyError error) {
            Log.e("hung91hn", error.getMessage());
        }
    };

}
