package com.hung91hn.weather.model;

import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

/**
 * Created by hung91hn on 12/1/16.
 */

public class WeatherDaily extends RealmObject {
    @PrimaryKey
    private String dateTime;
    private String tempUnit, iconPhrase, mobileLink;
    private int icon, tempValueMin, tempValueMAX;

    public String getDateTime() {
        return dateTime;
    }

    public void setDateTime(String dateTime) {
        this.dateTime = dateTime;
    }

    public String getTempUnit() {
        return tempUnit;
    }

    public void setTempUnit(String tempUnit) {
        this.tempUnit = tempUnit;
    }

    public String getIconPhrase() {
        return iconPhrase;
    }

    public void setIconPhrase(String iconPhrase) {
        this.iconPhrase = iconPhrase;
    }

    public String getMobileLink() {
        return mobileLink;
    }

    public void setMobileLink(String mobileLink) {
        this.mobileLink = mobileLink;
    }

    public int getTempValueMin() {
        return tempValueMin;
    }

    public void setTempValueMin(int tempValueMin) {
        this.tempValueMin = tempValueMin;
    }

    public int getTempValueMAX() {
        return tempValueMAX;
    }

    public void setTempValueMAX(int tempValueMAX) {
        this.tempValueMAX = tempValueMAX;
    }

    public int getIcon() {
        return icon;
    }

    public void setIcon(int icon) {
        this.icon = icon;
    }
}
