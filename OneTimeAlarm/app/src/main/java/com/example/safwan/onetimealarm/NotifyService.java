package com.example.safwan.onetimealarm;

import android.annotation.TargetApi;
import android.app.AlarmManager;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.media.RingtoneManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.v4.app.NotificationCompat;

import java.util.Calendar;


public class NotifyService extends BroadcastReceiver {

/** Final Variables**/
    public static final boolean SYSTEM_TIME_UPDATE = true;
    public static final boolean APP_TIME_UPDATE = false;
    private final static int ID_INCREMENT_VAL = 1000;

/** Static Variables**/
    private static Alarm[] notifyServiceAlarms = new Alarm[100];

/** Plain Old Variables**/
    private Context mainAlarmContext;


    /**
     * Function: Can receive 2 different broadcast,
     *           1. Pending intents, issued by android alarm manager.
     *           2. ACTIONs when timezone, time manually or automatically changed.
     *
     * Assumption: For any of the code in this method to work, user must set at least 1 alarm on.
     *             This will allow this class to save the CONTEXT.
     */
    @Override
    public void onReceive(Context context, Intent intent) {

        if(intent.getAction() != null && ( intent.getAction().equals(Intent.ACTION_TIMEZONE_CHANGED) || intent.getAction().equals(Intent.ACTION_TIME_CHANGED) )) {
            if(intent.getAction().equals(Intent.ACTION_TIMEZONE_CHANGED))
                System.out.println("TIMEZONE changed " + context);
            else if(intent.getAction().equals(Intent.ACTION_TIME_CHANGED))
                System.out.println("TIME set " + context);

            if(context != null)
                resetAllAlarms(context);
            return;
        }

        // get unique alarm id
        int id = intent.getIntExtra("id", -1);
        System.out.printf("Alarm notification for id: %d\n", id);

        // get alarm object
        byte[] bytes = intent.getByteArrayExtra("alarm");
        Alarm alarmObj = ParcelableUtil.unmarshall(bytes, Alarm.CREATOR);

        System.out.printf("Alarm notification for %s\n", alarmObj);

        if(alarmObj == null)
            return;

        //print for sanity check
        System.out.printf("Alarm notification for id: %d at time %s\n" +
                "title: %s, description: %s, location: %s\n", id, alarmObj.getTimeString(), alarmObj.getTitle(), alarmObj.getDescription(), alarmObj.getLocation());

        // send notification
        if (Build.VERSION.SDK_INT >= 26)
            notifyAndroidO(context, alarmObj);
        else {
            notifyAndroidN(context, alarmObj);
        }

    }

    /**
     * Function: This basically initiates pending alarms and schedules them via alarm manager.
     *           If called from main alarm fragment then it should also have an alarm list in bundle. We save that.
     *           So, when this method is called due to system time change, we can go over all the saved alarm instances and update their pending time.
     *
     *           This method also takes care of DST and time zone changes.
     *           Also, before setting any alarm, it deletes all the possible alarms, even if not present, for the given alarm ID.

     *           Beware, lot of things are happening below.
     *
     * Stimuli: Either from main alarm fragment, with isFromSystem == APP_TIME_UPDATE == false
     *          Or, from this class, with isFromSystem == SYSTEM_TIME_UPDATE == true
     *
     * Return: long: shortestInterval, this gives the time when the next alarm is due.
     */
    @TargetApi(24)
    public long setAlarm(Context context, int id, Bundle bundle, boolean isFromSystem)
    {
        Alarm alarmObj = bundle.getParcelable("alarmObj");

        if( !isFromSystem ) {
            Parcelable[] p =  bundle.getParcelableArray("alarm-array");
            for (int i = 0; i < p.length; i++ ) {
                notifyServiceAlarms[i] = (Alarm) p[i];
            }
        }

        // set main alarm context for future use in DST calculations
        mainAlarmContext = context;

        // clear all existing alarm for this alarm object
        deleteAllAlarmFor(context, id);

        id*=ID_INCREMENT_VAL;


        // create new fresh, alarms
        Intent notifyIntent = new Intent(context,NotifyService.class);
        notifyIntent.putExtra("id", id);
        byte[] bytes = ParcelableUtil.marshall(alarmObj);
        notifyIntent.putExtra("alarm", bytes);


        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        Calendar ca = Calendar.getInstance();
        ca.set(Calendar.HOUR_OF_DAY, alarmObj.getHrOfDay());
        ca.set(Calendar.MINUTE, alarmObj.getMin());
        ca.set(Calendar.DAY_OF_WEEK, alarmObj.getDayOfWeek());


        long currentTimeMillis = System.currentTimeMillis();
        long addedMillis = 0;
        long shortestInterval = 0;
        // if not on repeat, set one alarm.
        if( !alarmObj.isOnRepeat() ) {
            // if given time is less than current time then add
            if(ca.getTimeInMillis() < System.currentTimeMillis()) {
                ca.setTimeInMillis( ca.getTimeInMillis() + 86400000L);
            }

            addedMillis = ca.getTimeInMillis() - System.currentTimeMillis() - 30000;
            System.out.printf("Once Alarm being set for id: %d at time %s, interval: %d\n", id , alarmObj.getTimeString(), addedMillis);
            System.out.println( new java.text.SimpleDateFormat("h:mm a E,  dd-MM-yyyy").format(ca.getTime()));

            PendingIntent pendingIntent = PendingIntent.getBroadcast (context, id, notifyIntent, PendingIntent.FLAG_UPDATE_CURRENT);
            alarmManager.setExact(AlarmManager.RTC_WAKEUP,  currentTimeMillis + addedMillis, pendingIntent);

            shortestInterval = currentTimeMillis + addedMillis; // set return value

        }else {

            for(int i =0; i < alarmObj.repeatingAlarmDaysList.length; i++) {
                if(alarmObj.repeatingAlarmDaysList[i] == 1) {
                    ca.set(Calendar.DAY_OF_WEEK, i+1);

                    addedMillis = ca.getTimeInMillis() - System.currentTimeMillis() - 30000;
                    if(addedMillis < 0)
                        addedMillis += 604800000L;

                    int repeatId = id+ca.get(Calendar.DAY_OF_WEEK); // must be called after calendar day of week is set
                    System.out.printf("Rep Alarm being set for id: %d at time %s, interval: %d\n", repeatId , alarmObj.getTimeString(), addedMillis );
                    PendingIntent pendingIntent = PendingIntent.getBroadcast (context, repeatId , notifyIntent, PendingIntent.FLAG_UPDATE_CURRENT);
                    alarmManager.setInexactRepeating(AlarmManager.RTC_WAKEUP,  currentTimeMillis + addedMillis, AlarmManager.INTERVAL_DAY * 7 , pendingIntent);
                    if(currentTimeMillis+addedMillis < shortestInterval || shortestInterval == 0)
                        shortestInterval = currentTimeMillis+addedMillis;
                }
            }
        }

        return shortestInterval;
    }




    // maybe needed later for android Oreo.
    private void notifyAndroidO(Context context, Alarm alarmObj) {
        if (Build.VERSION.SDK_INT < 26)
            return;
        System.out.printf("In these channels\n");
        System.out.printf("Out these channels\n");
        NotificationManager mNotificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        // The id of the channel.
        String id = "my_channel_01";
        // The user-visible name of the channel.
        CharSequence name = "my channel";
        // The user-visible description of the channel.
        String description = "My description";
        int importance = NotificationManager.IMPORTANCE_HIGH;

        NotificationChannel mChannel = new NotificationChannel(id, name, importance);
        // Configure the notification channel.
        mChannel.setDescription(description);
        mChannel.enableLights(true);
        // Sets the notification light color for notifications posted to this
        // channel, if the device supports this feature.
        mChannel.setLightColor(Color.RED);
        mChannel.enableVibration(true);
        mChannel.setVibrationPattern(new long[]{100, 200, 300, 400, 500, 400, 300, 200, 400});
        mNotificationManager.createNotificationChannel(mChannel);

        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(context, id)
                .setSmallIcon(R.drawable.notification_icon)
                .setContentTitle(alarmObj.getTitle())
                .setContentText(alarmObj.getDescription());

        mNotificationManager.notify(alarmObj.getAlarmId(), mBuilder.build());
    }


    // android api < 26
    private void notifyAndroidN(Context context, Alarm alarmObj) {
        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(context)
                .setSmallIcon(R.drawable.notification_icon)
                .setContentTitle(alarmObj.getTitle())
                .setContentText(alarmObj.getDescription())
                .setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION))
                .setAutoCancel(true);

        NotificationManager mNotificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        mNotificationManager.notify(alarmObj.getAlarmId(), mBuilder.build());

    }


    /**
     * Function: Deletes all alarms for a given alarm id.
     * Assumption: ID should be a value from 0 -> Alarm.INSTANCE_LIMIT, and not to be * with ID_INCREMENT_VAL before being passed in method
     * Stimuli: Called everytime setAlarm sets an alarm for an ID.
     */
    public void deleteAllAlarmFor(Context context, int id) {
        id*= ID_INCREMENT_VAL;
        // delete from 0-7 days
        for(int i = 0; i <= Calendar.SATURDAY; i++) {
            int repeatId = id+i;
            Intent intent = new Intent(context, NotifyService.class);
            PendingIntent sender = PendingIntent.getBroadcast(context, repeatId, intent, 0);
            AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
            alarmManager.cancel(sender);
        }
    }



    /**
     * Function: If alarms are saved in this class, it will ensure they are scheduled again, to allow proper time.
     * Stimuli: Called when system intent actions are fired to onReceive method.
     * Assumption: A valid context MUST be passed.
     */
    private void resetAllAlarms(Context context) {
        for( Alarm a : notifyServiceAlarms ) {
            if( a != null) {
                System.out.println(a.getTimeString());
                Bundle bundle = new Bundle();
                bundle.putParcelable("alarmObj", a);
                setAlarm(context, a.getAlarmId(), bundle, NotifyService.SYSTEM_TIME_UPDATE);
            }
        }
    }

}
