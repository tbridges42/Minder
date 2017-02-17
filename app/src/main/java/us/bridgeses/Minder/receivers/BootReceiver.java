package us.bridgeses.Minder.receivers;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.orhanobut.logger.Logger;

import us.bridgeses.Minder.Reminder;

/**
 * Created by Tony on 8/25/2014.
 */
public class BootReceiver extends BroadcastReceiver{

	AlarmManager alarmManager;

	public void setAlarm(Context context, Reminder reminder){
		Intent intentAlarm = new Intent(context, ReminderReceiver.class);//Create alarm intent
		int id = reminder.getId();
		intentAlarm.putExtra("Id", id);           //Associate intent with specific reminder
		if (alarmManager != null) {                             //Check against null
			alarmManager.set(AlarmManager.RTC_WAKEUP, reminder.getDate().getTimeInMillis(),
					PendingIntent.getBroadcast(context, id, intentAlarm, PendingIntent.FLAG_UPDATE_CURRENT));
		}
		Logger.v("Alarm " + id + " set");
	}

    @Override
    public void onReceive(Context context, Intent intent) {
        final PendingResult result = goAsync();

	    Reminder[] reminders = Reminder.getAll(context);                //Get all reminders
		Logger.v("Reminders received");
	    if (reminders.length != 0) {                                            //If there are reminders

		    alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);

		    for (Reminder reminder : reminders) {                               //For each reminder
			    Logger.v(Integer.toString(reminders.length));
			    Logger.v(Integer.toString(reminder.getId()));
			    if (reminder.getActive()) {                                     //If reminder is active
				    setAlarm(context, reminder);                                //Set alarm
			    }
		    }
	    }
        result.finish();
    }
}
