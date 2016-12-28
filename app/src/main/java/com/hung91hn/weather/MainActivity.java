package com.hung91hn.weather;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.location.Location;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.error.VolleyError;
import com.bumptech.glide.Glide;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.hung91hn.weather.adapter.DailyWeatherAdapter;
import com.hung91hn.weather.adapter.HourlyWeatherAdapter;
import com.hung91hn.weather.model.WeatherDaily;
import com.hung91hn.weather.model.WeatherHourly;
import com.hung91hn.weather.model.WeatherLocation;
import com.hung91hn.weather.services.AutoUpdateService;
import com.hung91hn.weather.utils.CustomRequest;
import com.hung91hn.weather.utils.JsonUtil;
import com.hung91hn.weather.utils.MySingleton;

import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import io.realm.Realm;
import io.realm.RealmConfiguration;
import io.realm.RealmResults;

import static android.view.View.GONE;
import static com.hung91hn.weather.utils.Share.API_KEY;
import static com.hung91hn.weather.utils.Share.API_LOCATION;
import static com.hung91hn.weather.utils.Share.FAKE_INT;
import static com.hung91hn.weather.utils.Share.LISTLOCA_CHANGED;
import static com.hung91hn.weather.utils.Share.LOCATION_KEY;
import static com.hung91hn.weather.utils.Share.MAX_LOCATION_NUMBER;
import static com.hung91hn.weather.utils.Share.PREFERENCE_KEY;
import static com.hung91hn.weather.utils.Share.TEMP_UNIT;
import static com.hung91hn.weather.utils.Share.UNIT_CHANGED;
import static com.hung91hn.weather.utils.Share.UPDATE_CHANGED;
import static com.hung91hn.weather.utils.Share.URL_WEATHER_ICON;
import static com.hung91hn.weather.utils.Share.URL_WEATHER_ICON_TYPE;
import static com.hung91hn.weather.utils.Share.WEATHER_DAILY_CHANGED;
import static com.hung91hn.weather.utils.Share.WEATHER_HOURLY_CHANGED;
import static com.hung91hn.weather.utils.Share.addLocationToRealm;
import static com.hung91hn.weather.utils.Share.getWeatherLocation;
import static com.hung91hn.weather.utils.Share.openWeb;
import static com.hung91hn.weather.utils.Share.toCelsius;

public class MainActivity extends AppCompatActivity {
    private Resources resources;
    private GoogleApiClient mClient;
    private SimpleDateFormat sdf;

    private TextView tvCity, tvCountry, tvTimeZone, tvWeatherPhrase, tvDayLight, tvTemperature, tvPrecipitationProbability, tvNavTemp, tvInsertArea;
    private Button btNavArea;
    private ImageButton ibNavGPS;
    private ImageView ivWeatherIcon, ivNav;
    private RecyclerView rvHourly, rvDaily;
    private ListView lvNavArea;
    private SwipeRefreshLayout srlMain;

    private Realm realm;
    private SharedPreferences preferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.app_name, R.string.app_name);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
        resources = getResources();
        preferences = getSharedPreferences(PREFERENCE_KEY, MODE_PRIVATE);
        realm = Realm.getDefaultInstance();
        mClient = new GoogleApiClient.Builder(this)
                .addApi(LocationServices.API).build();

        setView();

        WeatherLocation location = getCurrentLocation();

        //neu co dia diem hien tai
        if (location != null) {
            setViewCurrentLocation(location);

            //neu co du lieu thoi tiet 12h va 5 ngay
            setViewWeather(preferences.getString(LOCATION_KEY,null));
        }

        setListViewLocation();
    }


    @Override
    protected void onResume() {
        super.onResume();
        if (UNIT_CHANGED) {
            WeatherLocation location = getCurrentLocation();
            if (location != null) {
                setViewCurrentLocation(location);

                setViewWeather(preferences.getString(LOCATION_KEY,null));
            }
            UNIT_CHANGED = false;
        }

        if (UPDATE_CHANGED) {
            startService(new Intent(this, AutoUpdateService.class));

            UPDATE_CHANGED = false;
        }

        if (LISTLOCA_CHANGED) {
            setListViewLocation();
            WeatherLocation location = getCurrentLocation();
            setViewCurrentLocation(location);
            setViewWeather(preferences.getString(LOCATION_KEY,null));

            LISTLOCA_CHANGED = false;
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        mClient.connect();
        // update db lien tuc
        registerReceiver(receiver, new IntentFilter(WEATHER_HOURLY_CHANGED));
        registerReceiver(receiver, new IntentFilter(WEATHER_DAILY_CHANGED));
    }

    @Override
    public void onStop() {
        super.onStop();
        mClient.disconnect();
        unregisterReceiver(receiver);
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_setting:
                Intent intent = new Intent(this, SettingActivity.class);
                startActivity(intent);
                break;
        }

        return true;
    }


    private void setView() {
        srlMain = (SwipeRefreshLayout) findViewById(R.id.srl_main);
        srlMain.setOnRefreshListener(refreshListener);

        tvCity = (TextView) findViewById(R.id.tv_city);
        tvCountry = (TextView) findViewById(R.id.tv_country);
        tvTimeZone = (TextView) findViewById(R.id.tv_TimeZone);

        ivWeatherIcon = (ImageView) findViewById(R.id.iv_WeatherIcon);
        ivWeatherIcon.setOnClickListener(clickListener);
        ivNav = (ImageView) findViewById(R.id.iv_nav);
        tvWeatherPhrase = (TextView) findViewById(R.id.tv_weatherPhrase);
        tvDayLight = (TextView) findViewById(R.id.tv_dayLight);
        tvTemperature = (TextView) findViewById(R.id.tv_Temperature);
        tvPrecipitationProbability = (TextView) findViewById(R.id.tv_PrecipitationProbability);
        tvNavTemp = (TextView) findViewById(R.id.tv_nav_temp);
        tvInsertArea = (TextView) findViewById(R.id.tv_insertArea);
        ibNavGPS = (ImageButton) findViewById(R.id.bt_nav_gps);
        ibNavGPS.setOnClickListener(clickListener);
        btNavArea = (Button) findViewById(R.id.bt_nav_insertArea);
        btNavArea.setOnClickListener(clickListener);

        rvHourly = (RecyclerView) findViewById(R.id.rv_main_hourly);
        rvHourly.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        rvDaily = (RecyclerView) findViewById(R.id.rv_main_daily);
        rvDaily.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        lvNavArea = (ListView) findViewById(R.id.lv_area);
        lvNavArea.setOnItemClickListener(itemClickListener);
        lvNavArea.setOnItemLongClickListener(itemLongClickListener);
    }


    private View.OnClickListener clickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            switch (view.getId()) {
                case R.id.iv_WeatherIcon:
                    List<WeatherHourly> hourlies = getHourlyWeatherFromRealm(preferences.getString(LOCATION_KEY,null));
                    openWeb(MainActivity.this, hourlies.get(0).getMobileLink());
                    break;
                case R.id.bt_nav_gps:
                    setWeatherGPS();
                    break;
                case R.id.bt_nav_insertArea:
                    startActivity(new Intent(MainActivity.this, SearchAreaActivity.class));
                    break;
            }
        }
    };

    private AdapterView.OnItemClickListener itemClickListener = new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
            RealmResults<WeatherLocation> allLocations = realm.where(WeatherLocation.class).findAll();
            WeatherLocation location = allLocations.get(i);


            preferences.edit().putString(LOCATION_KEY, location.getLocationKey()).commit();
            setViewCurrentLocation(location);
            setViewWeather(preferences.getString(LOCATION_KEY,null));
        }
    };

    private AdapterView.OnItemLongClickListener itemLongClickListener = new AdapterView.OnItemLongClickListener() {
        @Override
        public boolean onItemLongClick(AdapterView<?> adapterView, View view, int i, long l) {

            realm.beginTransaction();
            RealmResults<WeatherLocation> allLocations = realm.where(WeatherLocation.class).findAll();
            allLocations.deleteFromRealm(i);
            realm.commitTransaction();

            setListViewLocation();

            preferences.edit().remove(LOCATION_KEY).commit();
            return true;
        }
    };


    private SwipeRefreshLayout.OnRefreshListener refreshListener = new SwipeRefreshLayout.OnRefreshListener() {
        @Override
        public void onRefresh() {
            if (preferences.getString(LOCATION_KEY,null) != null)
                startService(new Intent(MainActivity.this, AutoUpdateService.class));
            else srlMain.setRefreshing(false);
        }
    };

    private WeatherLocation getCurrentLocation() {
        String locaKey = preferences.getString(LOCATION_KEY, null);
        if (locaKey != null) {
            RealmResults<WeatherLocation> allLocations = realm.where(WeatherLocation.class).findAll();
            for (WeatherLocation location : allLocations) {
                if (location.getLocationKey().equals(locaKey))
                    return location;
            }
        }
        return null;
    }

    private List<WeatherHourly> getHourlyWeatherFromRealm(String locationKey) {

        RealmConfiguration configuration = new RealmConfiguration.Builder()
                .name(locationKey).build();
        Realm realm = Realm.getInstance(configuration);
        return realm.copyFromRealm(realm.where(WeatherHourly.class).findAll());
    }

    private List<WeatherDaily> getDailyWeatherFromRealm(String locationKey) {

        RealmConfiguration configuration = new RealmConfiguration.Builder()
                .name(locationKey).build();
        Realm realm = Realm.getInstance(configuration);
        return realm.copyFromRealm(realm.where(WeatherDaily.class).findAll());
    }

    private void setListViewLocation() {
        RealmResults<WeatherLocation> allLocations = realm.where(WeatherLocation.class).findAll();
        int size = allLocations.size();


        ArrayList<String> allLocaName = new ArrayList<>();
        for (int i = 0; i < size; i++)
            allLocaName.add(allLocations.get(i).toString());
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, allLocaName);
        lvNavArea.setAdapter(adapter);

        if (allLocations.size() < MAX_LOCATION_NUMBER) {
            tvInsertArea.setText(R.string.add_location);
            btNavArea.setVisibility(View.VISIBLE);
            ibNavGPS.setVisibility(View.VISIBLE);
        } else {
            tvInsertArea.setText(R.string.delete_location);
            btNavArea.setVisibility(GONE);
            ibNavGPS.setVisibility(GONE);
        }
    }


    private void setViewCurrentLocation(WeatherLocation weatherLocation) {
        setTitle(weatherLocation.getLocalizedName());
        tvCity.setText(resources.getString(R.string.area_name) + ": " + weatherLocation.getArea());
        tvCountry.setText(resources.getString(R.string.country_name) + ": " + weatherLocation.getCountry());
        tvTimeZone.setText(resources.getString(R.string.timezone) + ": " + weatherLocation.getTimeZone());

    }

    private void setViewWeatherNow(List<WeatherHourly> weatherHourlies) {

        WeatherHourly weatherNow = weatherHourlies.get(0);

        int weatherIcon = weatherNow.getWeatherIcon();
        if (weatherIcon != FAKE_INT) {
            final String iconInx = String.format("%02d", weatherIcon);
            String imageUrl = URL_WEATHER_ICON + iconInx + URL_WEATHER_ICON_TYPE;
            Glide.with(MainActivity.this).load(imageUrl).into(ivWeatherIcon);
            Glide.with(MainActivity.this).load(imageUrl).into(ivNav);
        }

        tvWeatherPhrase.setText(resources.getString(R.string.iconPhrase) + ": " + weatherNow.getIconPhrase());
        if (weatherNow.isDaylight())
            tvDayLight.setText(R.string.day);
        else tvDayLight.setText(R.string.night);

        int value = weatherNow.getTemperatureValue();
        boolean tempUnitC = preferences.getBoolean(TEMP_UNIT, false);
        String unit = weatherNow.getTemperatureUnit();
        if (tempUnitC && unit.equals("F")) {
            unit = "C";
            value = toCelsius(value);
        }
        tvTemperature.setText(resources.getString(R.string.temperature) + ": " + value + "°" + unit);
        tvNavTemp.setText(getCurrentLocation().getLocalizedName() + ": " + value + "°" + unit);

        tvPrecipitationProbability.setText(resources.getString(R.string.precipitationProbability) + ": " + weatherNow.getPrecipitationProbability() + "%");

    }

    private void setViewWeather(String locationKey) {
        List<WeatherHourly> hourlyList = getHourlyWeatherFromRealm(locationKey);
        List<WeatherDaily> dailyList = getDailyWeatherFromRealm(locationKey);
        if (hourlyList.size() != 0 || dailyList.size() != 0) {
            setViewWeatherNow(hourlyList);
            boolean unitIsC = preferences.getBoolean(TEMP_UNIT, false);
            rvHourly.setAdapter(new HourlyWeatherAdapter(MainActivity.this, hourlyList, unitIsC));
            rvDaily.setAdapter(new DailyWeatherAdapter(MainActivity.this, dailyList, unitIsC));
        } else{
            startService(new Intent(MainActivity.this, AutoUpdateService.class));}
    }

    private void setWeatherGPS() {

        LocationRequest request = LocationRequest.create();
        request.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        request.setNumUpdates(1);
        request.setInterval(0);

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        LocationServices.FusedLocationApi.requestLocationUpdates(mClient, request, new LocationListener() {
            @Override
            public void onLocationChanged(Location location) {
                Map<String, String> mapApiParam = new HashMap<>();
                mapApiParam.put("apikey", API_KEY);
                mapApiParam.put("q", location.getLatitude() + "," + location.getLongitude());

                CustomRequest customRequest = new CustomRequest(Request.Method.GET, API_LOCATION, mapApiParam, new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        JSONObject responseJson = JsonUtil.createJSONObject(response);
                        WeatherLocation weatherLocation = getWeatherLocation(responseJson);

                        addLocationToRealm(weatherLocation);
                        setListViewLocation();

                        preferences.edit().putString(LOCATION_KEY, weatherLocation.getLocationKey()).commit();
                        setViewCurrentLocation(weatherLocation);

                        setViewWeather(preferences.getString(LOCATION_KEY,null));

                    }
                }, errorListener);
                MySingleton.getInstance(MainActivity.this).addToRequestQueue(customRequest, false);
            }
        });
    }

    private Response.ErrorListener errorListener = new Response.ErrorListener() {
        @Override
        public void onErrorResponse(VolleyError error) {
            srlMain.setRefreshing(false);
            Log.e("hung91hn", error.getMessage());
        }
    };

    private BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            srlMain.setRefreshing(false);

            if (intent.getAction().equals(WEATHER_HOURLY_CHANGED)) {
                List<WeatherHourly> hourlyList = getHourlyWeatherFromRealm(getCurrentLocation().getLocationKey());
                setViewWeatherNow(hourlyList);
                rvHourly.setAdapter(new HourlyWeatherAdapter(MainActivity.this, hourlyList
                        , preferences.getBoolean(TEMP_UNIT, false)));
            } else if (intent.getAction().equals(WEATHER_DAILY_CHANGED)) {
                rvDaily.setAdapter(new DailyWeatherAdapter(MainActivity.this
                        , getDailyWeatherFromRealm(getCurrentLocation().getLocationKey()), preferences.getBoolean(TEMP_UNIT, false)));
            }
        }
    };
}
