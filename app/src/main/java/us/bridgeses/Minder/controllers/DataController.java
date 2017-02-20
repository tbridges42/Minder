package us.bridgeses.Minder.controllers;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.Fragment;
import android.app.FragmentManager;
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
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import us.bridgeses.Minder.AboutFragment;
import us.bridgeses.Minder.R;
import us.bridgeses.Minder.Reminder;
import us.bridgeses.Minder.exporter.ExportActivity;
import us.bridgeses.Minder.exporter.ImportActivity;
import us.bridgeses.Minder.persistence.dao.DaoFactory;
import us.bridgeses.Minder.persistence.dao.ReminderDAO;
import us.bridgeses.Minder.receivers.ReminderReceiver;
import us.bridgeses.Minder.views.interfaces.ReminderListView;

import static android.content.Context.NOTIFICATION_SERVICE;
import static us.bridgeses.Minder.persistence.RemindersContract.Reminder.DISPLAY_PROJECTION;
import static us.bridgeses.Minder.persistence.RemindersContract.Reminder.REMINDER_URI;

/**
 * Created by tbrid on 2/12/2017.
 */

public class DataController extends Fragment implements LoaderManager.LoaderCallbacks<Cursor> {

    private static final String TAG = "DataController";

    private static final int LOADER_ID = 1;

    public interface ActivityCallback {
        ReminderListView getListView();

        TrackingController getTracker();

        void createEditor(long id);
    }

    private Context applicationContext;
    private ActivityCallback callback;
    private List<Reminder> cachedReminders;

    public static DataController getInstance() {
        DataController dataController = new DataController();
        return dataController;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
        setHasOptionsMenu(true);
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
        // TODO: 2/17/2017 Handle case where DataController is ready before ListView
        Log.d(TAG, "onActivityCreated:");
        loadAll();
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.data_menu, menu);
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            /*case R.id.defaults:
                // TODO: 2/20/2017 Fix defaults
                callback.createEditor(-1L);
                return true;*/
            case R.id.action_export:
                exportReminders();
                return true;
            case R.id.action_import:
                importReminders();
                return true;

            case R.id.action_new:
                createNew();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }



    private void importReminders() {
        // Should be handled by dataController
        new ImportActivity(getActivity()).importBackup();
        // CursorLoader should handle refreshing. Test this.
    }

    private void exportReminders() {
        // Should be handled by dataController
        new ExportActivity(getActivity()).export();
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

    public void load(long id) {
        callback.createEditor(id);
    }

    private void loadAll() {
        if (cachedReminders != null) {
            callback.getListView().setReminders(cachedReminders);
        }
        LoaderManager manager = getLoaderManager();
        Loader loader = manager.getLoader(LOADER_ID);
        if (loader == null || !loader.isStarted()) {
            Log.d(TAG, "loadAll: creating loader");
            loader = manager.initLoader(LOADER_ID, null, this);
            loader.startLoading();
        }
        Log.d(TAG, "loadAll: ");
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
        callback.getListView().displayProgress();
        Log.d(TAG, "onCreateLoader: ");
        return new CursorLoader(getActivity(), REMINDER_URI, DISPLAY_PROJECTION,
                null, null, null);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        callback.getListView().removeProgress();
        if (data != null) {
            DaoFactory daoFactory = DaoFactory.getInstance();
            ReminderDAO reminderDAO =daoFactory.getDao(getActivity());
            cachedReminders = new ArrayList<>();
            Reminder[] reminders = reminderDAO.getReminders();
            Collections.addAll(cachedReminders, reminders);
            callback.getListView().setReminders(cachedReminders);
            Log.d(TAG, "onLoadFinished: " + data.getCount());
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {

    }

    public void createNew() {
        callback.createEditor(-1L);
    }

    public void onReminderSelected(long id) {
        Log.d(TAG, "onReminderSelected: " + id);
        callback.createEditor(id);
    }
}
