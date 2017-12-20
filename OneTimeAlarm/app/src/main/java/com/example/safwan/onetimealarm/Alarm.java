package com.example.safwan.onetimealarm;

import android.os.Parcel;
import android.os.Parcelable;

import java.sql.Time;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
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
    private static int instanceCount = 0;
    public static final int instanceLimit = 100;
    private static Queue<Integer> alarmIdQ = new LinkedList<Integer>();
    private int alarmId;
    private boolean onDstTime;  // will be true if time was saved while DST was on, otherwise false.

    int[] alarmDaysList;

    static{
        for( int i = 0; i < instanceLimit; i++)
            alarmIdQ.add(i);
    }


    public static Alarm getAlarmInstance() {
        if( instanceCount >= instanceLimit )
            return null;

        if( alarmIdQ.peek() != null ) {
            Alarm a = new Alarm(alarmIdQ.remove());
            return a;
        }

        // this line should never come unless coding error with limit, count or queue
        Alarm a = new Alarm(-10000);
        return null;
    }

    private Alarm(int id) {
        super();
        alarmId = id;
        alarmSet = true;
        hr = 0;
        min = 0;
        changeWithDayLightSavings = false;
        title = "";
        description = "";
        location = "";
        am_pm = 0;
        alarmTime = new GregorianCalendar();
        setOnDstTime();
        alarmDaysList = new int[7];
        instanceCount++;
    }

    public static void deconstruct(Alarm a) {
        if(a == null)
            return;

        alarmIdQ.add(a.getAlarmId());
        if(instanceCount <= 0)
            System.out.println("Error in ALARM!");
            instanceCount--;
        a = null;
    }

    /**Parcelable stuff**/

    Alarm(Parcel parcel) {

        alarmId = parcel.readInt();
        alarmSet = (parcel.readInt() == 1 ? true : false);
        hr = parcel.readInt();
        min = parcel.readInt();
        am_pm = (parcel.readInt() == 1 ? 1 : 0);
        changeWithDayLightSavings = (parcel.readInt() == 1 ? true : false);
        title = parcel.readString();
        description = parcel.readString();
        location = parcel.readString();
        alarmDaysList = parcel.createIntArray();
        // Calendar alarm time obj
        long milisecs = parcel.readLong();
        alarmTime = new GregorianCalendar();
        alarmTime.setTimeInMillis(milisecs);
        onDstTime = (parcel.readInt() == 1 ? true : false);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {

        parcel.writeInt(alarmId);
        parcel.writeInt(alarmSet ? 1 : 0);
        parcel.writeInt(hr);
        parcel.writeInt(min);
        parcel.writeInt(am_pm);
        parcel.writeInt(changeWithDayLightSavings ? 1 : 0);
        parcel.writeString(title);
        parcel.writeString(description);
        parcel.writeString(location);
        parcel.writeIntArray(alarmDaysList);
        parcel.writeLong(alarmTime.getTimeInMillis());
        parcel.writeInt(onDstTime ? 1 : 0);
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
    public Alarm clone() throws CloneNotSupportedException {
         Alarm a = (Alarm) super.clone();
         a.giveAlarmId();
        System.out.println("cloned id: " +a.getAlarmId());
        return a;
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

    public int getHrOfDay() {
        return alarmTime.get(Calendar.HOUR_OF_DAY);
    }

    public int getMin() {
        return alarmTime.get(Calendar.MINUTE);
    }

    public int getAm_pm() {
        return alarmTime.get(Calendar.AM_PM);
    }

    public int getDayOfWeek() {
        return alarmTime.get(Calendar.DAY_OF_WEEK);
    }


    public void setAlarmTime(int nHr, int nMin) {
        this.alarmTime.set(Calendar.HOUR_OF_DAY, nHr);
        this.alarmTime.set(Calendar.MINUTE, nMin);
//        this.alarmTime.setTimeZone(nTimeZone);
    }

    public String getTimeString() {
        return new SimpleDateFormat("h:mm a").format(alarmTime.getTime());
    }

    public long getTimeMillis() {
        return this.alarmTime.getTimeInMillis();
    }

    public boolean isOnRepeat() {
        int count = 0;
        for (int i : alarmDaysList) {
            if( i == 1)
                return true;
        }
        return false;

    }

    public int getAlarmId() {
        return alarmId;
    }

    public static int getInstanceCount() {
        return instanceCount;
    }

    public static void showCurrentQ() {
        for ( int i : Alarm.alarmIdQ ) {
            System.out.println("Queue has : " +i);
        }
    }

    public void removeIdFromQ() {
        System.out.println("Trying to find id : " +this.getAlarmId());
        if( alarmIdQ.contains(this.getAlarmId()) ) {
            alarmIdQ.remove(this.getAlarmId());
            instanceCount++;
        }
    }

    private void giveAlarmId() {
        if( alarmIdQ.peek() != null ) {
            this.alarmId = Alarm.alarmIdQ.remove();
            Alarm.instanceCount++;
        }
    }

    public boolean isOnDstTime() {
        return onDstTime;
    }

    public void setOnDstTime() {
        Calendar ca = Calendar.getInstance();
        TimeZone tz = ca.getTimeZone();
        this.onDstTime = tz.inDaylightTime(ca.getTime());
    }


    /**
     * Function: Checks all the listed alarms. If DST setting on, then:
     *           If alarm time is on DST and current time is standard, then convert alarm time to standard.
     *           If alarm time is not on DST and current time is on DST, then convert alarm time to DST
     * Stimuli: Called when an intent with 'Intent.ACTION_TIMEZONE_CHANGED' is received from NotifyService class.
     */
    public void resolveDstAlarmTime() {
        Calendar ca = Calendar.getInstance();
        TimeZone tz = ca.getTimeZone();
        boolean nowOnDstTime = tz.inDaylightTime(ca.getTime());
        long convertTimeInMillies = tz.getOffset(ca.getTimeInMillis()) - tz.getOffset(this.getTimeMillis());

        System.out.println("In Alarm DST, id: " + this.alarmId + " "+ this.getTimeString() + " DST: " + this.isOnDstTime() + " current DST: "+ nowOnDstTime+" Time in milli: "+convertTimeInMillies);
        System.out.println(tz.getOffset(this.getTimeMillis()));
        System.out.println(tz.getOffset(ca.getTimeInMillis()));

        if(this.isChangeWithDayLightSavings() && nowOnDstTime != this.isOnDstTime()) {
            // add convertTimeInMillies to current time
            if(nowOnDstTime) {
                this.alarmTime.setTimeInMillis(alarmTime.getTimeInMillis() + tz.getDSTSavings() );
                System.out.println("now DST "+ this.getTimeString());
            }
            else {
                // subtract convertTimeInMillies to current time
                this.alarmTime.setTimeInMillis(alarmTime.getTimeInMillis() - tz.getDSTSavings() );
                System.out.println("now DST` "+this.getTimeString());
            }

        }
        this.setOnDstTime();
//        this.alarmTime.setTimeZone(tz.getID());
    }


}
