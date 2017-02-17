package us.bridgeses.Minder.editor;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.DialogFragment;
import android.app.FragmentManager;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.NavUtils;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.orhanobut.logger.Logger;

import java.text.SimpleDateFormat;
import java.util.Calendar;

import us.bridgeses.Minder.R;
import us.bridgeses.Minder.Reminder;
import us.bridgeses.Minder.TaskCallbacks;
import us.bridgeses.Minder.receivers.ReminderReceiver;
import us.bridgeses.Minder.util.ConfirmDialogFragment;

/**
 * This is the primary editor class. All essential reminder settings should be changed here.
 * Supplemental settings should be changed in sub-activities
 */
public class EditReminder extends Activity implements ConfirmDialogFragment.NoticeDialogListener,
                                    TaskCallbacks{

    TaskCallbacks callbacks;
    ProgressDialog progressDialog;
    Reminder reminder;
    EditReminderFragment mFragment;
    private static final String TAG_TASK_FRAGMENT = "task_fragment";
    private Boolean defaults = false;
    ConfirmDialogFragment df;
    Context context;

    /**
     * Called when a ConfirmDialogFragment's negative button is clicked
     * All we want to happen is for the dialog to be dismissed
     * @param dialog the dialog whose button was clicked
     */
    @Override
    public void onDialogNegativeClick(DialogFragment dialog) {
        // The only thing we want to happen on a negative click is to close the dialog
    }

    /**
     * Called when a ConfirmDialogFragment's negative button is clicked
     * We will remove any alarms associated with the reminder, and delete the reminder
     * @param dialog
     */
    @Override
    public void onDialogPositiveClick(DialogFragment dialog, int id) {

        // Cancel any alarms associated with this reminder
        Intent intentAlarm = new Intent(this, ReminderReceiver.class);
        AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        alarmManager.cancel(PendingIntent.getBroadcast(getApplicationContext(), reminder.getId(), intentAlarm,
                PendingIntent.FLAG_UPDATE_CURRENT));

        // Cancel any notification associated with this reminder
        NotificationManager mNotifyMgr =
                (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        mNotifyMgr.cancel(reminder.getId());

        //Delete the reminder
        reminder.delete(this);

        //Let the user know the reminder has been deleted
        String toastText = getResources().getString(R.string.reminder_deleted);
        int duration = Toast.LENGTH_SHORT;
        Toast toast = Toast.makeText(getApplicationContext(), toastText, duration);
        toast.show();

        //This reminder no longer exists; Navigate back to the parent activity
        NavUtils.navigateUpFromSameTask(this);
    }

    /**
     * The user has pressed the delete button
     * Get confirmation from the user that they really want to delete the reminder
     * @param view a reference to the button that was pressed
     */
    public void delete(View view) {
        Resources resources = getResources();
        df = ConfirmDialogFragment.newInstance(resources.getString(R.string.delete_title),
                resources.getString(R.string.delete_message),
                resources.getString(R.string.edit_delete),
                resources.getString(R.string.edit_cancel));
        df.show(getFragmentManager(),"ConfirmDialogFragment");
    }

	private void setAlarm(int id, Reminder reminder){
        // Create an intent and associate it with this reminder
		Intent intentAlarm = new Intent(this, ReminderReceiver.class);
		intentAlarm.putExtra("Id", reminder.getId());

        // Create the alarm
		AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        int alarmType;
        if (reminder.getWakeUp()){
            // This alarm type will wake the phone at the alarm type
            alarmType = AlarmManager.RTC_WAKEUP;
        }
        else {
            // This alarm type should wait until the phone wakes to fire the alarm
            alarmType = AlarmManager.RTC;
        }
		alarmManager.set(alarmType, reminder.getDate().getTimeInMillis(),
				PendingIntent.getBroadcast(getApplicationContext(), id, intentAlarm, PendingIntent.FLAG_UPDATE_CURRENT));

        // Let the user know the alarm was created
		SimpleDateFormat timeFormat = new SimpleDateFormat(getResources().getString(R.string.time_code));
		String time = timeFormat.format(reminder.getDate().getTime());
		String toastText = "Reminder saved for "+time;
		int duration = Toast.LENGTH_SHORT;
		Toast toast = Toast.makeText(this, toastText, duration);
		toast.show();
	}

    /**
     * The user has requested to save the reminder
     * @param view the button that was pressed to get here
     */
    public void save(View view){
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        reminder = Reminder.reminderFactory(sharedPreferences, this);

        if (defaults) {
            // If the user is trying to change defaults, persist to a special preference file
            SharedPreferences defaultPreferences = getSharedPreferences("Minder.Defaults", Context.MODE_PRIVATE);
            Reminder.reminderToPreference(this, defaultPreferences, reminder);
        }
        else {
            reminder.setActive(true);

            // If the time of the reminder is before the current time, attempt to repeat until it's not
            if (reminder.getDate().getTimeInMillis() <= Calendar.getInstance().getTimeInMillis()){
                reminder = Reminder.nextRepeat(reminder);
            }

            // Save the reminder
            reminder = reminder.save(this);
            int id = reminder.getId();

            // Create an alarm associated with the reminder
            if ((id != -1) && (reminder.getActive())) {
                setAlarm(id, reminder);
            }
        }

        //We're done here, navigate up to the parent activity
        NavUtils.navigateUpFromSameTask(this);
    }

    /**
     * The user has requested to cancel editing the reminder
     * @param view
     */
    public void cancel(View view){
        // We're done here, navigate up to the parent activity
        NavUtils.navigateUpFromSameTask(this);
    }

    @Override
    protected void onPause(){
        super.onPause();
        if ((df != null)&&(df.isVisible())){
            df.dismiss();
        }
    }

    private void loadFragment() {
        // Store the reminder in the preference file for easy manipulation


        // Check if there is already a fragment attached
        FragmentManager fragmentManager = getFragmentManager();
        mFragment = (EditReminderFragment) fragmentManager.findFragmentByTag(TAG_TASK_FRAGMENT);

        if (mFragment == null) {
            // If there is not a fragment, create one
            EditReminderFragment fragment = EditReminderFragment.newInstance(reminder);
            fragmentManager.beginTransaction().replace(R.id.reminder_frame, fragment,TAG_TASK_FRAGMENT).commit();
            if (progressDialog != null) {
                Logger.d("Dismissing progress dialog");
                progressDialog.dismiss();
                progressDialog = null;
            }
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_preference_test);
        context = this;
        callbacks = this;
        Intent incoming = getIntent();
	    Boolean isNew;
        if (incoming != null) {
            // If there is incoming data, get the data
            isNew = incoming.getBooleanExtra("New",false);
            defaults = incoming.getBooleanExtra("default",false);
        }
        else {
            // If there is no incoming data, this is a new reminder
            isNew = true;
        }

	    if (isNew||defaults){
            // Load the defaults into the reminder
            SharedPreferences defaultPreferences = getSharedPreferences("Minder.Defaults", Context.MODE_PRIVATE);
		    reminder = Reminder.reminderFactory(defaultPreferences,getApplicationContext());
		    reminder.setDate(Calendar.getInstance());

            // The user should not be able to delete a reminder that is not saved
		    Button deleteButton = (Button)findViewById(R.id.delete_button);
		    deleteButton.setEnabled(false);
            Reminder.reminderToPreference(this, PreferenceManager.getDefaultSharedPreferences(this), reminder);
            loadFragment();
	    }
	    else{
            // if there is an incoming reminder, store it in our reminder
            int id = incoming.getIntExtra("id",-1);
            if (id != -1) {
                LoadReminderTask task = new LoadReminderTask();
                task.execute(id);
            }
	    }
    }

    @Override
    public void onPreExecute() {
        progressDialog = ProgressDialog.show(this, "", "Loading. . .", true, true);
        Logger.d("New ProgressDialog");
    }

    @Override
    public void onCancelled() {
        if (progressDialog != null) {
            progressDialog.dismiss();
            progressDialog = null;
        }
    }

    @Override
    public void onPostExecute() {

    }

    @Override
    public void onProgressUpdate(int percent) {

    }

    /**
     * This task pulls a single reminder from the DAO and passes it to the editor
     */
    private class LoadReminderTask extends AsyncTask<Integer, Integer, Void> {

        @Override
        protected void onPreExecute() {
            if (callbacks != null) {
                callbacks.onPreExecute();
            }
        }

        @Override
        protected void onProgressUpdate(Integer... percent) {
            if (callbacks != null) {
                callbacks.onProgressUpdate(percent[0]);
            }
        }

        @Override
        protected void onCancelled() {
            if (callbacks != null) {
                callbacks.onCancelled();
            }
        }

        /**
         * Note that we do NOT call the callback object's methods
         * directly from the background thread, as this could result
         * in a race condition.
         */
        @Override
        protected Void doInBackground(Integer... id) {
            Logger.d("Loading reminder:" + id[0]);
            reminder = Reminder.get(context,id[0]);
            Reminder.reminderToPreference(context, PreferenceManager.getDefaultSharedPreferences(context),reminder);
            return null;
        }

        @Override
        protected void onPostExecute(Void ignore) {
            Logger.d("Calling loadfragment");
            if (callbacks != null) {
                callbacks.onPostExecute();
            }
            loadFragment();
        }
    }
}
