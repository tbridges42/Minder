package us.bridgeses.Minder.receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.orhanobut.logger.Logger;

import us.bridgeses.Minder.AlarmClass;


public class ReminderReceiver extends BroadcastReceiver {

    public ReminderReceiver() {
    }

    @Override
    public void onReceive(Context context, Intent alarmIntent) {
        final PendingResult result = goAsync();                         //Move off UI thread

        int id = alarmIntent.getIntExtra("Id", -1);                     //These lines create an intent
        if (id == -1){
            return;
        }
	    int snooze = alarmIntent.getIntExtra("Snooze",0);               //for the alarm screen based
	    boolean dismiss = alarmIntent.getBooleanExtra("Dismiss",false);
	    Logger.d(Boolean.toString(dismiss));
        AlarmClass alarmClass = new AlarmClass(context,id,alarmIntent.getBooleanExtra("Dismiss",false));
        alarmClass.run();
        result.finish();
    }
}
