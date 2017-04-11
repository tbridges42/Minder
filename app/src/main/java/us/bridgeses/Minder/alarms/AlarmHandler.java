package us.bridgeses.Minder.alarms;

import android.app.AlarmManager;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;

import us.bridgeses.Minder.model.Reminder;
import us.bridgeses.Minder.receivers.ReminderReceiver;

import static android.content.Context.NOTIFICATION_SERVICE;

/**
 * Created by tbrid on 2/27/2017.
 */

public class AlarmHandler {

    private Context applicationContext;
    private AlarmManager alarmManager;

    public AlarmHandler(Context context) {
        this.applicationContext = context.getApplicationContext();
        alarmManager =
                (AlarmManager) applicationContext.getSystemService(Context.ALARM_SERVICE);
    }

    public void setAlarm(Reminder reminder) {
        Intent intentAlarm = new Intent(applicationContext, ReminderReceiver.class);//Create alarm intent
        intentAlarm.putExtra("Id", reminder.getId());           //Associate intent with specific Reminder
        intentAlarm.putExtra("Snooze", 0);                       //This alarm has not been snoozed
        if ((reminder.getActive()) && (reminder.getId() != -1)) {
            int alarmType;
            if (reminder.getWakeUp()){
                alarmType = AlarmManager.RTC_WAKEUP;
            }
            else {
                alarmType = AlarmManager.RTC;
            }
            alarmManager = (AlarmManager) applicationContext.getSystemService(Context.ALARM_SERVICE);
            alarmManager.set(alarmType, reminder.getDate().getTimeInMillis(),
                    PendingIntent.getBroadcast(applicationContext, reminder.getId(),
                            intentAlarm, PendingIntent.FLAG_UPDATE_CURRENT));
        }
    }

    public void cancelAlarm(Reminder reminder) {
        Intent intentAlarm = new Intent(applicationContext, ReminderReceiver.class);
        alarmManager.cancel(PendingIntent.getBroadcast(applicationContext, reminder.getId(), intentAlarm,
                PendingIntent.FLAG_UPDATE_CURRENT));
        NotificationManager notifyMgr =
                (NotificationManager) applicationContext.getSystemService(NOTIFICATION_SERVICE);
        notifyMgr.cancel(reminder.getId());
    }
}
