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

        int id = alarmIntent.getIntExtra("Id", -1);
        if (id == -1){
            return;
        }
	    boolean dismiss = alarmIntent.getBooleanExtra("Dismiss",false);
	    boolean snooze = alarmIntent.getBooleanExtra("Snooze",false);
	    int snoozeNum = alarmIntent.getIntExtra("SnoozeNum",0);
	    Logger.d(Boolean.toString(dismiss));
        AlarmClass alarmClass = new AlarmClass(context,id,dismiss,snooze,snoozeNum);
        alarmClass.run();
        result.finish();
    }
}
