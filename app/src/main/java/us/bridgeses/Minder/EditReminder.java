package us.bridgeses.Minder;

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
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.google.android.gms.maps.model.LatLng;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;

import us.bridgeses.Minder.receivers.ReminderReceiver;


public class EditReminder extends Activity implements DeleteDialogFragment.NoticeDialogListener {

    Reminder reminder;
    ReminderPreferenceFragment mFragment;
    private static final String TAG_TASK_FRAGMENT = "task_fragment";

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

    private void setDaysOfWeek(SharedPreferences sharedPreferences, Reminder reminder) {
        byte daysOfWeek = 0;
        if (sharedPreferences.getBoolean("temp_sunday",false)) {
            daysOfWeek += Reminder.SUNDAY;
        }
        if (sharedPreferences.getBoolean("temp_monday",false)) {
            daysOfWeek += Reminder.MONDAY;
        }
        if (sharedPreferences.getBoolean("temp_tuesday",false)) {
            daysOfWeek += Reminder.TUESDAY;
        }
        if (sharedPreferences.getBoolean("temp_wednesday",false)) {
            daysOfWeek += Reminder.WEDNESDAY;
        }
        if (sharedPreferences.getBoolean("temp_thursday",false)) {
            daysOfWeek += Reminder.THURSDAY;
        }
        if (sharedPreferences.getBoolean("temp_friday",false)) {
            daysOfWeek += Reminder.FRIDAY;
        }
        if (sharedPreferences.getBoolean("temp_saturday",false)) {
            daysOfWeek += Reminder.SATURDAY;
        }
        reminder.setDaysOfWeek(daysOfWeek);
    }

	private void setRepeat(SharedPreferences sharedPreferences, Reminder reminder) {
		reminder.setRepeatType(Integer.parseInt(sharedPreferences.getString("temp_repeat_type", "0")));
		switch (reminder.getRepeatType()) {
			case 1: {
				reminder.setRepeatLength(Integer.parseInt(sharedPreferences.getString("temp_days", "1")));
				break;
			}
			case 2: {
				reminder.setRepeatLength(Integer.parseInt(sharedPreferences.getString("temp_weeks", "1")));
				setDaysOfWeek(sharedPreferences,reminder);
				break;
			}
			case 3: {
				reminder.setRepeatLength(Integer.parseInt(sharedPreferences.getString("temp_months", "1")));
				reminder.setMonthType((byte) Integer.parseInt(sharedPreferences.getString("temp_monthly_type","0")));
				break;
			}
			case 4: {
				reminder.setRepeatLength(Integer.parseInt(sharedPreferences.getString("temp_years", "1")));
				break;
			}
		}
	}

	private void setDate(SharedPreferences sharedPreferences, Reminder reminder) {
		Calendar date = Calendar.getInstance();
		SimpleDateFormat timeFormat = new SimpleDateFormat(getResources().getString(R.string.full_date_time_code));
		try {
			String newDate = sharedPreferences.getString("temp_time", "") + " " + sharedPreferences.getString("temp_date", "");
			date.setTime(timeFormat.parse(newDate));
		}
		catch (ParseException e){
			Log.e("Minder","Parse Error");
		}
		if ((!Reminder.checkDayOfWeek(reminder.getDaysOfWeek(),          //If initial day is not in
				date.get(Calendar.DAY_OF_WEEK)))&&(reminder.getRepeatType()==2)){                       //repeat pattern, skip
			ReminderDBHelper dbHelper = ReminderDBHelper.getInstance(this);
			SQLiteDatabase database = dbHelper.openDatabase();
			Reminder.nextRepeat(database,reminder);
			dbHelper.closeDatabase();
		}
		long time = date.getTimeInMillis();                             //Drop seconds
		time = time/60000;                                              //
		time = time*60000;                                              //
		date.setTimeInMillis(time);                                     //
		reminder.setDate(date);                                         //Store reminder date + time
		//reminder.setActive(date.after(Calendar.getInstance()));
	}

	private void setLocation(SharedPreferences sharedPreferences, Reminder reminder){
		int locationType = Integer.parseInt(sharedPreferences.getString("location_type", "0"));
		switch (locationType){
			case 0: {
				reminder.setOnlyAtLocation(false);
				reminder.setUntilLocation(false);
				break;
			}
			case 1: {
				reminder.setOnlyAtLocation(true);
				reminder.setUntilLocation(false);
				break;
			}
			case 2: {
				reminder.setOnlyAtLocation(false);
				reminder.setUntilLocation(true);
				break;
			}
		}

		LatLng location = new LatLng(sharedPreferences.getFloat("Latitude",0),sharedPreferences.getFloat("Longitude",0));
		reminder.setLocation(location);
	}

	private void setAlarm(int id, Reminder reminder){
		Intent intentAlarm = new Intent(this, ReminderReceiver.class);      //Create alarm intent
		intentAlarm.putExtra("Id", reminder.getId());                    //Associate intent with specific reminder
		AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
		alarmManager.set(AlarmManager.RTC_WAKEUP, reminder.getDate().getTimeInMillis(),
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
        reminder.setName(sharedPreferences.getString("temp_name",""));
        reminder.setDescription(sharedPreferences.getString("temp_description", ""));
        setDate(sharedPreferences,reminder);
        setRepeat(sharedPreferences,reminder);
	    setLocation(sharedPreferences,reminder);
	    reminder.setRadius(Integer.valueOf(sharedPreferences.getString("radius",Integer.toString(Reminder.RADIUSDEFAULT))));
        reminder.setVibrate(sharedPreferences.getBoolean("temp_vibrate", true));
        reminder.setRingtone(sharedPreferences.getString("temp_ringtone", ""));
	    reminder.setActive(reminder.getDate().after(Calendar.getInstance()));
	    reminder.setQr(sharedPreferences.getString("temp_code","0"));
		reminder.setNeedQr(sharedPreferences.getBoolean("code_type",false));
        reminder.setVolumeOverride(sharedPreferences.getBoolean("out_loud",false));
        reminder.setDisplayScreen(sharedPreferences.getBoolean("display_screen",false));
        reminder.setWakeUp(sharedPreferences.getBoolean("wake_up",false));

        ReminderDBHelper dbHelper = ReminderDBHelper.getInstance(this);
        SQLiteDatabase database = dbHelper.openDatabase();

        int id = (int) Reminder.saveReminder(database,reminder);    //Save reminder to database
		reminder.setId(id);

		dbHelper.closeDatabase();

        if ((id != -1) && (reminder.getActive())) {
            setAlarm(id,reminder);
        }

        NavUtils.navigateUpFromSameTask(this);
    }

    public void cancel(View view){
	    FragmentManager fragmentManager = getFragmentManager();
	    mFragment = (ReminderPreferenceFragment) fragmentManager.findFragmentByTag(TAG_TASK_FRAGMENT);
	    if (mFragment != null){
		    fragmentManager.beginTransaction().remove(mFragment).commit();
	    }
        NavUtils.navigateUpFromSameTask(this);
    }

    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        Log.v("Minder","Activity Configuration Changed");
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_preference_test);

        Intent intent = getIntent();
        Bundle incoming = intent.getExtras();
        if (incoming != null) {
            reminder = incoming.getParcelable("Reminder");
            if (reminder.getId()==-1){
                reminder = new Reminder();
            }
        }
        else
            reminder = new Reminder();

        ReminderPreferenceFragment fragment = ReminderPreferenceFragment.newInstance(reminder);
        FragmentManager fragmentManager = getFragmentManager();
        mFragment = (ReminderPreferenceFragment) fragmentManager.findFragmentByTag(TAG_TASK_FRAGMENT);

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
