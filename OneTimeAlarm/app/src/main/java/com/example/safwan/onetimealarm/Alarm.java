package com.example.safwan.onetimealarm;

import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.Nullable;

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

/** Final Variables**/
    public static final int INSTANCE_LIMIT = 100;    // can only have this many diff alarm instances.

/** Static Variables**/
    private static int instanceCount = 0;
    private static Queue<Integer> alarmIdQ = new LinkedList<Integer>(); // contains unique id.

/** Plain Old Variables**/
    private boolean alarmSet;   // alarm on or off ? true if on.
    private int hr, min, am_pm;    //0 = am, 1 = pm
    private boolean changeWithDayLightSavings;  // should time change with DST or should it stay fixed? true if time should change.
    private String title, description, location;
    private Calendar alarmTime;
    private int alarmId;
    private boolean onDstTime;  // will be true if time was saved while DST was on, otherwise false.
    int[] repeatingAlarmDaysList;    // contains int value of Caldendar.DAYS; says what days alarm is repeated.


    /**
     * Function: Put id (0-99) in queue at start of app life.
     * Assumption: When app starts and there are saved alarms in Main Alarm Fragment, its that class's responsibility to
     *             remove id from queue for which that class has alarm objects for.
     */
    static{
        for( int i = 0; i < INSTANCE_LIMIT; i++)
            alarmIdQ.add(i);
    }

    /**
     * Function: Provides a Object pooling pattern.
     * Stimuli: When a new alarm is needed, this method is to be called.
     * Return: if instance count <= 99, return an alarm instance.
     *         else, return null.
     */
    @Nullable
    public static Alarm getAlarmInstance() {
        if( instanceCount >= INSTANCE_LIMIT )
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
        repeatingAlarmDaysList = new int[7];
        instanceCount++;
    }


    /**
     * Function: Acts as a deconstructor. Nulls the provides alarm instance and recycles alarm ID.
     *           Also ensures instance count is decremented.
     * Stimuli: Should be called by client when an Alarm instance is no longer needed.
     */
    public static void deconstruct(Alarm a) {
        if(a == null)
            return;

        alarmIdQ.add(a.getAlarmId());
        if(instanceCount <= 0)
            System.out.println("Error in ALARM!");
            instanceCount--;
        a = null;
    }

    /**Start parcelable stuff**/

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
        repeatingAlarmDaysList = parcel.createIntArray();
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
        parcel.writeIntArray(repeatingAlarmDaysList);
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

    /** End parcelable stuff **/

    /** Start cloneable interface stuff **/
    @Override
    public Alarm clone() throws CloneNotSupportedException {
         Alarm a = (Alarm) super.clone();
         a.giveAlarmId();
        System.out.println("cloned id: " +a.getAlarmId());
        return a;
    }
    /** End cloneable interface stuff **/


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

    // returns hour in 12 hrs clock
    public int getHr() {
        return alarmTime.get(Calendar.HOUR);
    }

    // returns hour in 24 hrs clock
    public int getHrOfDay() {
        return alarmTime.get(Calendar.HOUR_OF_DAY);
    }

    public int getMin() {
        return alarmTime.get(Calendar.MINUTE);
    }

    // 0 if am, 1 if pm
    public int getAm_pm() {
        return alarmTime.get(Calendar.AM_PM);
    }

    // 1 = sun, 7 = sat
    public int getDayOfWeek() {
        return alarmTime.get(Calendar.DAY_OF_WEEK);
    }


    /**
     * Function: Sets alarm time in a Calender object.
     * Assumption: Hr, min is set through this method.
     *             Day, Month and Year are set to the time when this method is called.
     *             Also sets onDstTime to true if time was saved on DST time.
     * Stimuli: Called when alarm time is changed.
     */
    public void setAlarmTime(int nHr, int nMin) {
        Calendar ca = Calendar.getInstance();
        ca.set(Calendar.HOUR_OF_DAY, nHr);
        ca.set(Calendar.MINUTE, nMin);
        this.alarmTime.setTimeInMillis(ca.getTimeInMillis());
        this.setOnDstTime();
    }

    public String getTimeString() {
        return new SimpleDateFormat("h:mm a").format(alarmTime.getTime());
    }

    public long getTimeMillis() {
        return this.alarmTime.getTimeInMillis();
    }

    /**
     * Function: Checks if there's any day in the alarmDaysList
     * Return: If list contains >= 1 then return true,
     *         else false.
     */
    public boolean isOnRepeat() {
        for (int i : repeatingAlarmDaysList) {
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

    // sanity method to check existing ID in queue
    public static void showCurrentQ() {
        for ( int i : Alarm.alarmIdQ ) {
            System.out.println("Queue has : " +i);
        }
    }

    /**
     * Function: Removes specific ID from. This ensures new alarms won't get existing alarm IDs.
     *           Also increments instance count if ID found and removed from queue.
     * Assumption: ID must be given to alarm object before hand. Ie alarm was created and saved, loaded back again, but not destroyed.
     * Stimuli: Should be called by initAlarmTable method in mainAlarmFragment, when the fragment is loaded.
     */
    public void removeIdFromQ() {
        System.out.println("Trying to find id : " +this.getAlarmId());
        if( alarmIdQ.contains(this.getAlarmId()) ) {
            alarmIdQ.remove(this.getAlarmId());
            instanceCount++;
        }
    }

    /**
     * Function: Removes ID from queue by popping and assigns it to an alarm instance.
     * Stimuli: Called by getAlarmInstance method.
     */
    private void giveAlarmId() {
        if( alarmIdQ.peek() != null ) {
            this.alarmId = Alarm.alarmIdQ.remove();
            Alarm.instanceCount++;
        }
    }

    /**
     * Function: Check if alarm time was saved when time was in DST.
     * Return: True if alarm time was saved when time was in DST,
     *         else false.
     */
    public boolean isOnDstTime() {
        return onDstTime;
    }

    /**
     * Function: Sets onDstTime boolean var to true if
     * Stimuli: Called by getAlarmInstance method.
     */
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

//        System.out.println("In Alarm DST, id: " + this.alarmId + " "+ this.getTimeString() + " DST: " + this.isOnDstTime() + " current DST: "+ nowOnDstTime+" Time in milli: "+convertTimeInMillies);
//        System.out.println(tz.getOffset(this.getTimeMillis()));
//        System.out.println(tz.getOffset(ca.getTimeInMillis()));

        if(this.isChangeWithDayLightSavings() && nowOnDstTime != this.isOnDstTime()) {
            // add convertTimeInMillies to current time
            if(nowOnDstTime) {
                this.alarmTime.setTimeInMillis(alarmTime.getTimeInMillis() + tz.getDSTSavings() );
//                System.out.println("now DST "+ this.getTimeString());
            }
            else {
                // subtract convertTimeInMillies to current time
                this.alarmTime.setTimeInMillis(alarmTime.getTimeInMillis() - tz.getDSTSavings() );
//                System.out.println("now DST` "+this.getTimeString());
            }

        }
        this.setOnDstTime();
    }


    public int[] getRepeatingAlarmDays() {
        return this.repeatingAlarmDaysList;
    }
}
