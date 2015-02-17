package us.bridgeses.Minder.receivers;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import us.bridgeses.Minder.Reminder;
import us.bridgeses.Minder.ReminderDBHelper;

/**
 * Created by Tony on 8/25/2014.
 */
public class BootReceiver extends BroadcastReceiver{

	AlarmManager alarmManager;

	public void setAlarm(Context context, Reminder reminder){
		Intent intentAlarm = new Intent(context, ReminderReceiver.class);//Create alarm intent
		int id = reminder.getId();
		intentAlarm.putExtra("Id", id);           //Associate intent with specific reminder
		intentAlarm.putExtra("Snooze",0);                       //This alarm has not been snoozed
		if (alarmManager != null) {                             //Check against null
			alarmManager.set(AlarmManager.RTC_WAKEUP, reminder.getDate().getTimeInMillis(),
					PendingIntent.getBroadcast(context, id, intentAlarm, PendingIntent.FLAG_UPDATE_CURRENT));
		}
		Log.v("us.bridgeses.minder","Alarm "+id+" set");
	}

    @Override
    public void onReceive(Context context, Intent intent) {
        final PendingResult result = goAsync();

	    ReminderDBHelper dbHelper = ReminderDBHelper.getInstance(context);
	    SQLiteDatabase database = dbHelper.openDatabase();                      //Open database

	    Reminder[] reminders = Reminder.readReminders(database);                //Get all reminders
		Log.v("Minder","Reminders received");
	    dbHelper.closeDatabase();                                               //Close database
	    if (reminders.length != 0) {                                            //If there are reminders

		    alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);

		    for (Reminder reminder : reminders) {                               //For each reminder
			    Log.v("Minder", Integer.toString(reminders.length));
			    Log.v("Minder", Integer.toString(reminder.getId()));
			    if (reminder.getActive()) {                                     //If reminder is active
				    setAlarm(context, reminder);                                //Set alarm
			    }
		    }
	    }
        result.finish();
    }
}
