package us.bridgeses.Minder.receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import us.bridgeses.Minder.AlarmScreen;


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
	    Intent newIntent = new Intent(context, AlarmScreen.class);      //on the reminder in the triggering
	    newIntent.putExtra("Id",id);                                    //intent
	    newIntent.putExtra("Snooze",snooze);                            //
	    newIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);              //

        result.finish();                                                //Return to UI thread
        context.startActivity(newIntent);                               //Start alarm screen
    }
}
