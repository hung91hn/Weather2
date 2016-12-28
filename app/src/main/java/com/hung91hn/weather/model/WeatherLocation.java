package com.hung91hn.weather.model;

import android.os.Parcel;
import android.os.Parcelable;

import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

/**
 * Created by hung91hn on 12/5/16.
 */

public class WeatherLocation extends RealmObject implements Parcelable {
    @PrimaryKey
    private String locationKey;
    private String localizedName, area, country, region;
    private int timeZone;

    public String getLocationKey() {
        return locationKey;
    }

    public void setLocationKey(String locationKey) {
        this.locationKey = locationKey;
    }

    public String getLocalizedName() {
        return localizedName;
    }

    public void setLocalizedName(String localizedName) {
        this.localizedName = localizedName;
    }

    public String getArea() {
        return area;
    }

    public void setArea(String area) {
        this.area = area;
    }

    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    public String getRegion() {
        return region;
    }

    public void setRegion(String region) {
        this.region = region;
    }

    public int getTimeZone() {
        return timeZone;
    }

    public void setTimeZone(int timeZone) {
        this.timeZone = timeZone;
    }

    @Override
    public String toString() {
        return localizedName + " - " + area + " - " + country;
    }


    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.locationKey);
        dest.writeString(this.localizedName);
        dest.writeString(this.area);
        dest.writeString(this.country);
        dest.writeString(this.region);
        dest.writeInt(this.timeZone);
    }

    public WeatherLocation() {
    }

    protected WeatherLocation(Parcel in) {
        this.locationKey = in.readString();
        this.localizedName = in.readString();
        this.area = in.readString();
        this.country = in.readString();
        this.region = in.readString();
        this.timeZone = in.readInt();
    }

    public static final Creator<WeatherLocation> CREATOR = new Creator<WeatherLocation>() {
        @Override
        public WeatherLocation createFromParcel(Parcel source) {
            return new WeatherLocation(source);
        }

        @Override
        public WeatherLocation[] newArray(int size) {
            return new WeatherLocation[size];
        }
    };
}
