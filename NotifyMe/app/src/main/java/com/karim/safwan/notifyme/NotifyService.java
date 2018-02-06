package com.karim.safwan.notifyme;

import android.app.AlarmManager;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.media.RingtoneManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Parcelable;
import android.support.v4.app.NotificationCompat;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
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


        if(intent.getAction() != null && (
                intent.getAction().equals(Intent.ACTION_BOOT_COMPLETED) ||
                intent.getAction().equals(Intent.ACTION_TIMEZONE_CHANGED) ||
                intent.getAction().equals(Intent.ACTION_TIME_CHANGED)
        )) {
            if(intent.getAction().equals(Intent.ACTION_TIMEZONE_CHANGED))
                System.out.println("TIMEZONE changed " + context);
            else if(intent.getAction().equals(Intent.ACTION_TIME_CHANGED))
                System.out.println("TIME set " + context);
            else if(intent.getAction().equals(Intent.ACTION_BOOT_COMPLETED)){
                loadData(context);
            }

            if(context != null){
                resetAllAlarms(context);
            }

            return;
        }

        // get alarm object
        byte[] bytes = intent.getByteArrayExtra("alarm");
        Alarm alarmObj = ParcelableUtil.unmarshall(bytes, Alarm.CREATOR);


        if(alarmObj == null)
            return;

        //print for sanity check
//        System.out.printf("Alarm notification for id: %d at time %s\n" +
//                "title: %s, description: %s, location: %s\n", id, alarmObj.getTimeString(), alarmObj.getTitle(), alarmObj.getDescription(), alarmObj.getLocation());

        // check time
        long alarmTime = intent.getLongExtra("timeInMillis", 101010);
        if(Math.abs( alarmTime - System.currentTimeMillis() ) <= 4500) {
            // build notification title
            String notificationTitle = "";
            if(alarmObj.getTitle().equals(""))
                notificationTitle += "Notifying you";
            else
                notificationTitle += alarmObj.getTitle();

            notificationTitle += " - " + alarmObj.getTimeString();

            String notifiactionContent = alarmObj.getDescription();


            // send notification
            if (Build.VERSION.SDK_INT >= 26)
                notifyAndroidO(context, alarmObj.getAlarmId() ,notificationTitle,notifiactionContent);
            else {
                notifyAndroidN(context, alarmObj.getAlarmId(), notificationTitle,notifiactionContent);
            }
        }

        // reshceduleAlarms
        if(alarmObj.isRepeat())
            reshceduleAlarms(context, alarmObj);
    }



    private void reshceduleAlarms(final Context context, final Alarm alarmObj) {
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                Bundle b= new Bundle();
                b.putParcelable("alarmObj", alarmObj);
                setAlarm(context, alarmObj.getAlarmId(), b, true);
            }
        }, 2000);
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
        Calendar alarmCalendar = Calendar.getInstance();

        long shortestInterval = 0;
        // if not on repeat, set one alarm.
        if( !alarmObj.isRepeat() ) {
            alarmCalendar.setTimeInMillis(alarmObj.getTimeMillis());
            if((alarmCalendar.getTimeInMillis() < System.currentTimeMillis())) {
                return -1;
            }

            notifyIntent.putExtra("timeInMillis", alarmCalendar.getTimeInMillis());
            PendingIntent pendingIntent = PendingIntent.getBroadcast (context, id, notifyIntent, PendingIntent.FLAG_UPDATE_CURRENT);

            alarmManager.setExact(AlarmManager.RTC_WAKEUP,  alarmCalendar.getTimeInMillis(), pendingIntent);

            shortestInterval = alarmCalendar.getTimeInMillis(); // set return value
        }else {


            for(int i = 0; i < alarmObj.repeatingAlarmDaysList.length; i++) {
                Calendar tempCaldedar = Calendar.getInstance();
                tempCaldedar.setTimeInMillis(alarmObj.getTimeMillis());

                alarmCalendar = Calendar.getInstance();
                alarmCalendar.setTimeInMillis(System.currentTimeMillis());


                if(alarmObj.repeatingAlarmDaysList[i] == 1) {
                    alarmCalendar.set(Calendar.SECOND, 0);
                    alarmCalendar.set(Calendar.MINUTE, tempCaldedar.get(Calendar.MINUTE));
                    alarmCalendar.set(Calendar.HOUR_OF_DAY, tempCaldedar.get(Calendar.HOUR_OF_DAY));

                    // adds 7 days if addedMillis < 0
                    int days = i+1 + (7 - alarmCalendar.get(Calendar.DAY_OF_WEEK)); // how many days until alarm day
                    days %= 7;  // ensure 7,8,9 == 0, 1, 2 days added

                    if(alarmCalendar.getTimeInMillis() - System.currentTimeMillis() < 0 && days == 0) {
                        alarmCalendar.add(Calendar.DATE, 7);
                    } else{
                        alarmCalendar.add(Calendar.DATE, days); // days can be from 0-6
                    }

                    int repeatId = id+alarmCalendar.get(Calendar.DAY_OF_WEEK); // must be called after calendar day of week is set
                    notifyIntent.putExtra("timeInMillis", alarmCalendar.getTimeInMillis());
                    PendingIntent pendingIntent = PendingIntent.getBroadcast (context, repeatId , notifyIntent, PendingIntent.FLAG_UPDATE_CURRENT);
                    alarmManager.setExact(AlarmManager.RTC_WAKEUP,  alarmCalendar.getTimeInMillis(), pendingIntent);

                    System.out.println("norm "+ new java.text.SimpleDateFormat("h:mm a E, dd-MM-yyyy").format(alarmCalendar.getTime()));
                    System.out.println(alarmCalendar.getTimeInMillis());

                    if(alarmCalendar.getTimeInMillis() < shortestInterval || shortestInterval == 0){
                        shortestInterval = alarmCalendar.getTimeInMillis();
                    }
                }
            }
        }

        return shortestInterval;
    }




    // maybe needed later for android Oreo.
    private void notifyAndroidO(Context context, int id, String notificationTitle, String notifiactionContent) {
        if (Build.VERSION.SDK_INT < 26)
            return;

        NotificationManager mNotificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        // The id of the channel.
        String channelId = "my_channel_01";
        // The user-visible name of the channel.
        CharSequence name = "Notify Me Channel";
        // The user-visible description of the channel.
        String description = "Channel through which all your notifications from this app will come.";
        int importance = NotificationManager.IMPORTANCE_HIGH;

        NotificationChannel mChannel = new NotificationChannel(channelId , name, importance);
        // Configure the notification channel.
        mChannel.setDescription(description);
        mChannel.enableLights(true);
        // Sets the notification light color for notifications posted to this
        // channel, if the device supports this feature.
        mChannel.setLightColor(Color.RED);
        mChannel.enableVibration(true);
        mChannel.setShowBadge(true);
        mNotificationManager.createNotificationChannel(mChannel);

        CharSequence des = notifiactionContent;
        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(context, channelId )
                .setSmallIcon(R.drawable.notification_icon)
                .setContentTitle(notificationTitle)
                .setContentText(notifiactionContent)
                .setColor((int) R.color.myColorAccent)
                .setStyle(new NotificationCompat.BigTextStyle().bigText(des));


        mNotificationManager.notify(id, mBuilder.build());
    }


    // android api < 26
    private void notifyAndroidN(Context context, int id, String notificationTitle, String notifiactionContent) {
        CharSequence des = notifiactionContent;
        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(context)
                .setSmallIcon(R.drawable.notification_icon)
                .setContentTitle(notificationTitle)
                .setContentText(notifiactionContent)
                .setColor((int) R.color.myColorAccent)
                .setStyle(new NotificationCompat.BigTextStyle().bigText(des))
                .setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION))
                .setPriority(1)
                .setAutoCancel(true);


        NotificationManager mNotificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        mNotificationManager.notify(id, mBuilder.build());

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
//                notifyAndroidN(context, a.getAlarmId()+1234," alarms", a.getTimeString());

                System.out.println(a.getTimeString());
                Bundle bundle = new Bundle();
                bundle.putParcelable("alarmObj", a);
                //            Toast.makeText(context, "Rebooted....", Toast.LENGTH_LONG).show();
                setAlarm(context, a.getAlarmId(), bundle, NotifyService.SYSTEM_TIME_UPDATE);
            }
        }
    }

    /**
     * Function: Load alarm objects from alarm object list using Json data.
     *           If data not there then set up empty array of n fixed sze.
     */
    private void loadData(Context context) {
        SharedPreferences sharedPref = context.getSharedPreferences("alarmSharedPreferences ", Context.MODE_PRIVATE);
        Gson gson = new Gson();
        String json = sharedPref.getString("alarmObjList", null);
        Type type = new TypeToken<Alarm[]>(){}.getType();
        notifyServiceAlarms = gson.fromJson(json, type);

        if(notifyServiceAlarms == null) {
            notifyServiceAlarms = new Alarm[Alarm.INSTANCE_LIMIT];
        }
    }

}
