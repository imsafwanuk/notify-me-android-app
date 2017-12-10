package com.example.safwan.onetimealarm;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.ArrayList;
import java.util.List;

/**
 * A DEMO CLASS THAT WILL GET OVERWRITTEN BY PROPER OOP DESIGN PATTERN
 * Created by safwan on 6/12/2017.
 *
 */


public class AlarmDemo implements Parcelable {

    public boolean alarmSet;
    public int hr, min, am_pm;    //0 = am, 1 = pm
    public boolean changeWithDayLightSavings;
    public String title, description, location;


    public enum days{
        SUN, MON, TUE, WED, THU, FRI, SAT;
    }

    List<days> repeatingDays = new ArrayList<days>();

    AlarmDemo() {
        super();
        alarmSet = true;
        hr = 0;
        min = 0;
        changeWithDayLightSavings = false;
        title = "";
        description = "";
        location = "";
        am_pm = 0;
    }


    /**Parcelable stuff**/

    AlarmDemo(Parcel parcel) {
        alarmSet = (parcel.readInt() == 1 ? true : false);
        hr = parcel.readInt();
        min = parcel.readInt();
        am_pm = (parcel.readInt() == 1 ? 1 : 0);
        changeWithDayLightSavings = (parcel.readInt() == 1 ? true : false);
        title = parcel.readString();
        description = parcel.readString();
        location = parcel.readString();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {

        parcel.writeInt(alarmSet ? 1 : 0);
        parcel.writeInt(hr);
        parcel.writeInt(min);
        parcel.writeInt(am_pm);
        parcel.writeInt(changeWithDayLightSavings ? 1 : 0);
        parcel.writeString(title);
        parcel.writeString(description);
        parcel.writeString(location);
    }


    public static final Creator<AlarmDemo> CREATOR = new Creator<AlarmDemo>()
    {
        @Override
        public AlarmDemo createFromParcel(Parcel in)
        {
            return new AlarmDemo(in);
        }

        @Override
        public AlarmDemo[] newArray(int size)
        {
            return new AlarmDemo[size];
        }
    };


    public boolean isAlarmSet() {
        return alarmSet;
    }

    public void setAlarmSet(boolean alarmSet) {
        this.alarmSet = alarmSet;
    }

    public boolean isChangeWithDayLightSavings() {
        return changeWithDayLightSavings;
    }

    public void setChangeWithDayLightSavings(boolean changeWithDayLightSavings) {
        this.changeWithDayLightSavings = changeWithDayLightSavings;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public int getHr() {
        return hr;
    }

    public void setHr(int hr) {
        this.hr = hr;
    }

    public int getMin() {
        return min;
    }

    public void setMin(int min) {
        this.min = min;
    }

    public int getAm_pm() {
        return am_pm;
    }

    public void setAm_pm(int am_pm) {
        this.am_pm = am_pm;
    }



}
