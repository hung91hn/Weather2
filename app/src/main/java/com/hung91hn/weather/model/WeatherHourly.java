package com.hung91hn.weather.model;

import android.os.Parcel;
import android.os.Parcelable;

import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

/**
 * Created by hung91hn on 12/1/16.
 */

public class WeatherHourly extends RealmObject implements Parcelable {
    @PrimaryKey
    private String dateTime;
    private String  iconPhrase, temperatureUnit, mobileLink;
    private int weatherIcon, temperatureValue, precipitationProbability;
    private boolean daylight;

    public String getDateTime() {
        return dateTime;
    }

    public void setDateTime(String dateTime) {
        this.dateTime = dateTime;
    }

    public String getIconPhrase() {
        return iconPhrase;
    }

    public void setIconPhrase(String iconPhrase) {
        this.iconPhrase = iconPhrase;
    }

    public String getTemperatureUnit() {
        return temperatureUnit;
    }

    public void setTemperatureUnit(String temperatureUnit) {
        this.temperatureUnit = temperatureUnit;
    }

    public String getMobileLink() {
        return mobileLink;
    }

    public void setMobileLink(String mobileLink) {
        this.mobileLink = mobileLink;
    }

    public int getWeatherIcon() {
        return weatherIcon;
    }

    public void setWeatherIcon(int weatherIcon) {
        this.weatherIcon = weatherIcon;
    }

    public int getTemperatureValue() {
        return temperatureValue;
    }

    public void setTemperatureValue(int temperatureValue) {
        this.temperatureValue = temperatureValue;
    }

    public int getPrecipitationProbability() {
        return precipitationProbability;
    }

    public void setPrecipitationProbability(int precipitationProbability) {
        this.precipitationProbability = precipitationProbability;
    }

    public boolean isDaylight() {
        return daylight;
    }

    public void setDaylight(boolean daylight) {
        this.daylight = daylight;
    }


    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.dateTime);
        dest.writeString(this.iconPhrase);
        dest.writeString(this.temperatureUnit);
        dest.writeString(this.mobileLink);
        dest.writeInt(this.weatherIcon);
        dest.writeInt(this.temperatureValue);
        dest.writeInt(this.precipitationProbability);
        dest.writeByte(this.daylight ? (byte) 1 : (byte) 0);
    }

    public WeatherHourly() {
    }

    protected WeatherHourly(Parcel in) {
        this.dateTime = in.readString();
        this.iconPhrase = in.readString();
        this.temperatureUnit = in.readString();
        this.mobileLink = in.readString();
        this.weatherIcon = in.readInt();
        this.temperatureValue = in.readInt();
        this.precipitationProbability = in.readInt();
        this.daylight = in.readByte() != 0;
    }

    public static final Parcelable.Creator<WeatherHourly> CREATOR = new Parcelable.Creator<WeatherHourly>() {
        @Override
        public WeatherHourly createFromParcel(Parcel source) {
            return new WeatherHourly(source);
        }

        @Override
        public WeatherHourly[] newArray(int size) {
            return new WeatherHourly[size];
        }
    };
}
