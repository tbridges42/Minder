package us.bridgeses.Minder.editor;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.DialogFragment;
import android.app.FragmentManager;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.NavUtils;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.orhanobut.logger.Logger;

import java.text.SimpleDateFormat;
import java.util.Calendar;

import us.bridgeses.Minder.R;
import us.bridgeses.Minder.Reminder;
import us.bridgeses.Minder.ReminderDBHelper;
import us.bridgeses.Minder.receivers.ReminderReceiver;


public class EditReminder extends Activity implements DeleteDialogFragment.NoticeDialogListener {

    Reminder reminder;
    EditReminderFragment mFragment;
    private static final String TAG_TASK_FRAGMENT = "task_fragment";
    private Boolean defaults = false;

    @Override
    public void onDialogNegativeClick(DialogFragment dialog) {

    }

    @Override
    public void onDialogPositiveClick(DialogFragment dialog) {
        Intent intentAlarm = new Intent(this, ReminderReceiver.class);      //Create alarm intent
        AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        alarmManager.cancel(PendingIntent.getBroadcast(getApplicationContext(), reminder.getId(), intentAlarm,
                PendingIntent.FLAG_UPDATE_CURRENT));

        ReminderDBHelper dbHelper = ReminderDBHelper.getInstance(this);
        SQLiteDatabase database = dbHelper.openDatabase();
        Reminder.deleteReminder(database,reminder.getId());
	    dbHelper.closeDatabase();
	    // Gets an instance of the NotificationManager service
	    NotificationManager mNotifyMgr =
			    (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
	    // Builds the notification and issues it.
	    mNotifyMgr.cancel(reminder.getId());
        String toastText = getResources().getString(R.string.reminder_deleted);
        int duration = Toast.LENGTH_SHORT;
        Toast toast = Toast.makeText(getApplicationContext(), toastText, duration);
        toast.show();
        NavUtils.navigateUpFromSameTask(this);
    }

    public void delete(View view) {
        DeleteDialogFragment df = new DeleteDialogFragment();
        df.show(getFragmentManager(),"DeleteDialogFragment");
    }

	private void setAlarm(int id, Reminder reminder){
		Intent intentAlarm = new Intent(this, ReminderReceiver.class);      //Create alarm intent
		intentAlarm.putExtra("Id", reminder.getId());                    //Associate intent with specific reminder
		AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        int alarmType;
        if (reminder.getWakeUp()){
            alarmType = AlarmManager.RTC_WAKEUP;
        }
        else {
            alarmType = AlarmManager.RTC;
        }
		alarmManager.set(alarmType, reminder.getDate().getTimeInMillis(),
				PendingIntent.getBroadcast(getApplicationContext(), id, intentAlarm, PendingIntent.FLAG_UPDATE_CURRENT));
		SimpleDateFormat timeFormat = new SimpleDateFormat(getResources().getString(R.string.time_code));
		String time = timeFormat.format(reminder.getDate().getTime());
		String toastText = "Reminder saved for "+time;
		int duration = Toast.LENGTH_SHORT;
		Toast toast = Toast.makeText(this, toastText, duration);
		toast.show();                                               //Let the user know everything went fine
	}

    public void save(View view){

        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        reminder = Reminder.reminderFactory(sharedPreferences, this);

        if (defaults) {
	        Logger.d("Saving defaults with name: "+reminder.getName());
            SharedPreferences defaultPreferences = getSharedPreferences("Minder.Defaults", Context.MODE_PRIVATE);
            Reminder.reminderToPreference(defaultPreferences, reminder);
        }
        else {

            reminder.setActive(reminder.getDate().after(Calendar.getInstance()));

            ReminderDBHelper dbHelper = ReminderDBHelper.getInstance(this);
            SQLiteDatabase database = dbHelper.openDatabase();
			Logger.d("Old ID: "+reminder.getId());
            int id = (int) Reminder.saveReminder(database, reminder);    //Save reminder to database
            reminder.setId(id);
			Logger.d("New ID: "+id);
            dbHelper.closeDatabase();


            if ((id != -1) && (reminder.getActive())) {
                setAlarm(id, reminder);
            }
        }
        NavUtils.navigateUpFromSameTask(this);
    }

    public void cancel(View view){
	    FragmentManager fragmentManager = getFragmentManager();
	    mFragment = (EditReminderFragment) fragmentManager.findFragmentByTag(TAG_TASK_FRAGMENT);
	    if (mFragment != null){
		    fragmentManager.beginTransaction().remove(mFragment).commit();
	    }
        NavUtils.navigateUpFromSameTask(this);
    }

    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        Logger.v("Activity Configuration Changed");
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_preference_test);

        Intent intent = getIntent();
        Bundle incoming = intent.getExtras();
	    SharedPreferences defaultPreferences = getSharedPreferences("Minder.Defaults", Context.MODE_PRIVATE);
	    Boolean isNew;
        if (incoming != null) {
            isNew = incoming.getBoolean("New");
            defaults = incoming.getBoolean("default",false);
        }
        else
            isNew = true;

	    Logger.d("isNew: " + isNew);
	    Logger.d("defaults: " + defaults);
	    if (isNew||defaults){
		    reminder = Reminder.reminderFactory(defaultPreferences,getApplicationContext());
		    reminder.setDate(Calendar.getInstance());
		    Logger.d("Loading Defaults with name: "+reminder.getName());
		    Button deleteButton = (Button)findViewById(R.id.delete_button);
		    deleteButton.setEnabled(false);
	    }
	    else{
		    reminder = incoming.getParcelable("Reminder");
	    }
	    Reminder.reminderToPreference(PreferenceManager.getDefaultSharedPreferences(this),reminder);

        EditReminderFragment fragment = EditReminderFragment.newInstance(reminder);
        FragmentManager fragmentManager = getFragmentManager();
        mFragment = (EditReminderFragment) fragmentManager.findFragmentByTag(TAG_TASK_FRAGMENT);

        if (mFragment == null) {
            fragmentManager.beginTransaction().replace(R.id.reminder_frame, fragment,TAG_TASK_FRAGMENT).commit();
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        /*if (id == R.id.action_settings) {
            return true;
        }*/
        return super.onOptionsItemSelected(item);
    }
}
