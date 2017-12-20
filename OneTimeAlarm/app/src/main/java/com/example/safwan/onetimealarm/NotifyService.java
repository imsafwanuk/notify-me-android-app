package com.example.safwan.onetimealarm;

import android.annotation.TargetApi;
import android.app.AlarmManager;
import android.app.NotificationChannel;
import android.app.NotificationChannelGroup;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.TaskStackBuilder;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.icu.text.SimpleDateFormat;
import android.os.Build;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.v4.app.NotificationCompat;
import android.widget.Toast;

import java.util.Calendar;
import java.util.TimeZone;


public class NotifyService extends BroadcastReceiver {


/** Final Variables**/

/** Static Variables**/

/** Plain Old Variables**/



    public static final boolean SYSTEM_TIME_UPDATE = true;
        public static final boolean APP_TIME_UPDATE = false;

        private final static int ID_INCREMENT_VAL = 1000;
        Context mainAlarmContext;
        private static Alarm[] notifyServiceAlarms = new Alarm[100];

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

//            System.out.println(intent);
            // get unique alarm id
            int id = intent.getIntExtra("id", -1);
            System.out.printf("Alarm notification for id: %d\n", id);

            // get alarm object
            Alarm alarmObj = intent.getExtras().getParcelable("alarmObj");
            //print for sanity check
            System.out.printf("Alarm notification for id: %d at time %s\n" +
                    "title: %s, description: %s, location: %s\n", id, alarmObj.getTimeString(), alarmObj.getTitle(), alarmObj.getDescription(), alarmObj.getLocation());


            // build notification
            String CHANNEL_ID = "my_channel_01";
            NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(context, CHANNEL_ID)
                            .setSmallIcon(R.drawable.notification_icon)
                            .setContentTitle(alarmObj.getTitle())
                            .setContentText(alarmObj.getDescription());


            // launch notification
            NotificationManager mNotificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
            mNotificationManager.notify(id, mBuilder.build());

        }


    public void initChannels(Context context) {
        if (Build.VERSION.SDK_INT < 26)
            return;

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

    }



    public void setAlarm(Context context, int id, Bundle bundle, boolean isFromSystem)
    {
        Alarm alarmObj = bundle.getParcelable("alarmObj");

        /** demo starts **/
        if( !isFromSystem ) {
            Parcelable[] p =  bundle.getParcelableArray("alarm-array");
            for (int i = 0; i < p.length; i++ ) {
                notifyServiceAlarms[i] = (Alarm) p[i];
            }
        }

        /** demo ends **/

        // set main alarm context for future use in DST calculations
        mainAlarmContext = context;

        // clear all existing alarm for this alarm object
        deleteAllAlarmFor(context, id);

        id*=ID_INCREMENT_VAL;


        // create new fresh, alarms
        Intent notifyIntent = new Intent(context,NotifyService.class);
        notifyIntent.putExtra("id", id);
        notifyIntent.putExtras(bundle);


        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        Calendar ca = Calendar.getInstance();
        ca.set(Calendar.HOUR_OF_DAY, alarmObj.getHrOfDay());
        ca.set(Calendar.MINUTE, alarmObj.getMin());
        ca.set(Calendar.DAY_OF_WEEK, alarmObj.getDayOfWeek());



        // if not on repeat, set one alarm.
        if( !alarmObj.isOnRepeat() ) {
            // if given time is less than current time then add
            if(ca.getTimeInMillis() < System.currentTimeMillis()) {
                ca.setTimeInMillis( ca.getTimeInMillis() + 86400000L);
            }

            long addedMillis = ca.getTimeInMillis() - System.currentTimeMillis();
            System.out.printf("Once Alarm being set for id: %d at time %s, interval: %d\n", id , alarmObj.getTimeString(), addedMillis);
            System.out.println( new java.text.SimpleDateFormat("h:mm a E,  dd-MM-yyyy").format(ca.getTime()));

            PendingIntent pendingIntent = PendingIntent.getBroadcast (context, id, notifyIntent, PendingIntent.FLAG_UPDATE_CURRENT);
            alarmManager.set(AlarmManager.RTC_WAKEUP,  System.currentTimeMillis() + addedMillis, pendingIntent);

        }else {

            for(int i =0; i < alarmObj.repeatingAlarmDaysList.length; i++) {
                if(alarmObj.repeatingAlarmDaysList[i] == 1) {
                    ca.set(Calendar.DAY_OF_WEEK, i+1);

                    long addedMillis = ca.getTimeInMillis() - System.currentTimeMillis();
                    if(addedMillis < 0)
                        addedMillis += 604800000L;



                    int repeatId = id+ca.get(Calendar.DAY_OF_WEEK); // must be called after calendar day of week is set
                    System.out.printf("Rep Alarm being set for id: %d at time %s, interval: %d\n", repeatId , alarmObj.getTimeString(), addedMillis );
                    PendingIntent pendingIntent = PendingIntent.getBroadcast (context, repeatId , notifyIntent, PendingIntent.FLAG_UPDATE_CURRENT);
                    alarmManager.setRepeating(AlarmManager.RTC_WAKEUP,  System.currentTimeMillis() + addedMillis , AlarmManager.INTERVAL_DAY * 7 , pendingIntent);
                }
            }
        }
    }


    // id doesn't  need to be * with ID_INCREMENT_VAL before being passed in method
    public void deleteAllAlarmFor(Context context, int id) {
        id*= ID_INCREMENT_VAL;
        // delete from 0-7 days
        for(int i = 0; i <= Calendar.SATURDAY; i++) {
            int repeatId = id+i;
//            System.out.println("cancel alarm for " + repeatId);

            Intent intent = new Intent(context, NotifyService.class);
            PendingIntent sender = PendingIntent.getBroadcast(context, repeatId, intent, 0);
            AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
            alarmManager.cancel(sender);
        }
    }





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
