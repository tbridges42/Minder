package us.bridgeses.Minder.controllers;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.Fragment;
import android.app.LoaderManager;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.List;

import us.bridgeses.Minder.Reminder;
import us.bridgeses.Minder.receivers.ReminderReceiver;
import us.bridgeses.Minder.views.interfaces.ReminderListView;

import static android.content.Context.NOTIFICATION_SERVICE;

/**
 * Created by tbrid on 2/12/2017.
 */

public class DataController extends Fragment implements LoaderManager.LoaderCallbacks<Cursor> {

    public interface ActivityCallback {
        ReminderListView getListView();

        TrackingController getTracker();

        void createEditor(long id);
    }

    private Context applicationContext;
    private ActivityCallback callback;

    public static DataController getInstance() {
        DataController dataController = new DataController();
        return dataController;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
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
        try {
            Activity activity = getActivity();
            applicationContext = activity.getApplicationContext();
            callback = (ActivityCallback) activity;
        }
        catch (ClassCastException e) {
            throw new ClassCastException("The caller of DataController must implement ViewCallback");
        }
        catch (NullPointerException e) {
            throw new NullPointerException("Activity was not ready");
        }
        loadAll();
    }

    public void skipNext(int id) {
        // TODO: Break this godforsaken mess apart
        trackEvent("Skip Reminder","User Action");
        Reminder reminder = Reminder.get(applicationContext, id);
        Intent intentAlarm = new Intent(applicationContext, ReminderReceiver.class);      //Create alarm intent
        AlarmManager alarmManager = (AlarmManager) applicationContext.getSystemService(Context.ALARM_SERVICE);
        alarmManager.cancel(PendingIntent.getBroadcast(applicationContext, reminder.getId(), intentAlarm,
                PendingIntent.FLAG_UPDATE_CURRENT));
        reminder = Reminder.nextRepeat(reminder).save(applicationContext);
        if ((reminder.getActive()) && (reminder.getId() != -1)) {
            int alarmType;
            if (reminder.getWakeUp()){
                alarmType = AlarmManager.RTC_WAKEUP;
            }
            else {
                alarmType = AlarmManager.RTC;
            }
            intentAlarm = new Intent(applicationContext, ReminderReceiver.class);//Create alarm intent
            intentAlarm.putExtra("Id", id);           //Associate intent with specific Reminder
            intentAlarm.putExtra("Snooze", 0);                       //This alarm has not been snoozed
            alarmManager = (AlarmManager) applicationContext.getSystemService(Context.ALARM_SERVICE);
            alarmManager.set(alarmType, reminder.getDate().getTimeInMillis(),
                    PendingIntent.getBroadcast(applicationContext, id, intentAlarm, PendingIntent.FLAG_UPDATE_CURRENT));

        }
        // Gets an instance of the NotificationManager service
        NotificationManager mNotifyMgr =
                (NotificationManager) applicationContext.getSystemService(NOTIFICATION_SERVICE);
        // Builds the notification and issues it.
        mNotifyMgr.cancel(reminder.getId());
        // TODO: refresh the list
        callback.getListView();
    }

    public Reminder load(long id) {
        return null;
    }

    private List<Reminder> loadAll() {
        // TODO: Get all reminders as an arraylist and store in this class, pass that list
        // to view when necessary
        // Use a cursorloader; when there are changes to the cursor, compare against arraylist,
        // make additions, deletions and updates to view as necessary
        // !!! Look into SortedList
        // With SortedList handing comparisons and updates in the adapter, we don't need
        // to keep a redundant arraylist here
        // Without that heavy state, I don't think this needs to be a fragment.
        // But we can't retain the view across activity destructions, because configurations might
        // change. The list still needs to be cached here for retention purposes
        return null;
    }

    public void save(Reminder reminder) {
        trackEvent("Save Reminder", "User Action");
        // add Reminder to view and to internally tracked arraylist
        reminder.save(applicationContext);
    }

    public void delete(long id) {
        trackEvent("Delete Reminder", "User Action");
        // delete
        // remove reminder from view and internally tracked arraylist
    }

    private void trackEvent(String event, String category) {
        if (callback != null) {
            TrackingController controller = callback.getTracker();
            if (controller != null) {
                controller.sendEvent(category, event, null, 1L);
            }
        }
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        // TODO: 2/16/2017 Implement contentprovider so we can use cursorloader
        //CursorLoader loader = new CursorLoader(getActivity(), )
        return null;
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {

    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {

    }
}
