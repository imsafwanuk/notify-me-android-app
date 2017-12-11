package com.example.safwan.onetimealarm;

import android.os.Parcel;
import android.os.Parcelable;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.TimeZone;

/**
 * A DEMO CLASS THAT WILL GET OVERWRITTEN BY PROPER OOP DESIGN PATTERN
 * Created by safwan on 6/12/2017.
 *
 */


public class Alarm implements Parcelable, Cloneable {

    private boolean alarmSet;
    private int hr, min, am_pm;    //0 = am, 1 = pm
    private boolean changeWithDayLightSavings;
    private String title, description, location;
    private Calendar alarmTime;

    public enum days{
        SUN, MON, TUE, WED, THU, FRI, SAT;
    }
    public enum AM_PM{
        AM,PM;
    }

    List<days> repeatingDays = new ArrayList<days>();

    Alarm() {
        super();
        alarmSet = true;
        hr = 0;
        min = 0;
        changeWithDayLightSavings = false;
        title = "";
        description = "";
        location = "";
        am_pm = 0;
        alarmTime = new GregorianCalendar();
    }


    /**Parcelable stuff**/

    Alarm(Parcel parcel) {
        alarmSet = (parcel.readInt() == 1 ? true : false);
        hr = parcel.readInt();
        min = parcel.readInt();
        am_pm = (parcel.readInt() == 1 ? 1 : 0);
        changeWithDayLightSavings = (parcel.readInt() == 1 ? true : false);
        title = parcel.readString();
        description = parcel.readString();
        location = parcel.readString();
        // Calendar alarm time obj
        long milisecs = parcel.readLong();
        alarmTime = new GregorianCalendar();
//        String timeZoneId = parcel.readString();
//        alarmTime = new GregorianCalendar(TimeZone.getTimeZone(timeZoneId));
        alarmTime.setTimeInMillis(milisecs);
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
        parcel.writeLong(alarmTime.getTimeInMillis());
//        parcel.writeString(alarmTime.getTimeZone().getID());
    }


    public static final Creator<Alarm> CREATOR = new Creator<Alarm>()
    {
        @Override
        public Alarm createFromParcel(Parcel in)
        {
            return new Alarm(in);
        }

        @Override
        public Alarm[] newArray(int size)
        {
            return new Alarm[size];
        }
    };


    /** Cloneable interface stuff **/
    @Override
    protected Alarm clone() throws CloneNotSupportedException {
        Alarm newAlarm = new Alarm();
        newAlarm.setAlarmTime(this.alarmTime.get(Calendar.HOUR_OF_DAY),this.getMin());
        return newAlarm;
    }

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
        return alarmTime.get(Calendar.HOUR);
    }

    public int getMin() {
        return alarmTime.get(Calendar.MINUTE);
    }

    public int getAm_pm() {
        return alarmTime.get(Calendar.AM_PM);
    }


    public void setAlarmTime(int nHr, int nMin) {
        this.alarmTime.set(Calendar.HOUR_OF_DAY, nHr);
        this.alarmTime.set(Calendar.MINUTE, nMin);
//        this.alarmTime.setTimeZone(nTimeZone);
    }

    public String getTimeString() {
        return new SimpleDateFormat("h:mm a").format(alarmTime.getTime());
    }

}
