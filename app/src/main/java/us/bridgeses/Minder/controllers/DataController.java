package us.bridgeses.Minder.controllers;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.Fragment;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.orhanobut.logger.Logger;

import us.bridgeses.Minder.R;
import us.bridgeses.Minder.Reminder;
import us.bridgeses.Minder.ReminderListFragment;
import us.bridgeses.Minder.receivers.ReminderReceiver;

import static android.content.Context.NOTIFICATION_SERVICE;

/**
 * Created by tbrid on 2/12/2017.
 */

public class DataController extends Fragment{

    public interface ViewCallback {
        void refreshList(/* List?*/);
    }

    private Activity activity;
    private ViewCallback callback;

    public static DataController getInstance() {
        DataController dataController = new DataController();
        return dataController;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // This fragment has no UI. Do not attempt to inflate it
        return null;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        activity = getActivity();
        try {
            callback = (ViewCallback) activity;
        }
        catch (ClassCastException e) {
            throw new ClassCastException("The caller of DataController must implement ViewCallback");
        }
    }

    public void skipNext(int id) {
        // TODO: Break this godforsaken mess apart
        Logger.e(Integer.toString(id));
        Reminder reminder = Reminder.get(activity, id);
        Intent intentAlarm = new Intent(activity, ReminderReceiver.class);      //Create alarm intent
        AlarmManager alarmManager = (AlarmManager) activity.getSystemService(Context.ALARM_SERVICE);
        alarmManager.cancel(PendingIntent.getBroadcast(activity.getApplicationContext(), reminder.getId(), intentAlarm,
                PendingIntent.FLAG_UPDATE_CURRENT));
        reminder = Reminder.nextRepeat(reminder).save(activity);
        if ((reminder.getActive()) && (reminder.getId() != -1)) {
            int alarmType;
            if (reminder.getWakeUp()){
                alarmType = AlarmManager.RTC_WAKEUP;
            }
            else {
                alarmType = AlarmManager.RTC;
            }
            intentAlarm = new Intent(activity, ReminderReceiver.class);//Create alarm intent
            intentAlarm.putExtra("Id", id);           //Associate intent with specific Reminder
            intentAlarm.putExtra("Snooze", 0);                       //This alarm has not been snoozed
            alarmManager = (AlarmManager) activity.getSystemService(Context.ALARM_SERVICE);
            alarmManager.set(alarmType, reminder.getDate().getTimeInMillis(),
                    PendingIntent.getBroadcast(activity, id, intentAlarm, PendingIntent.FLAG_UPDATE_CURRENT));

            Logger.v("Alarm " + id + " set");
        }
        // Gets an instance of the NotificationManager service
        NotificationManager mNotifyMgr =
                (NotificationManager) activity.getSystemService(NOTIFICATION_SERVICE);
        // Builds the notification and issues it.
        mNotifyMgr.cancel(reminder.getId());
        // TODO: refresh the list
    }
}
