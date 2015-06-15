package us.bridgeses.Minder;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.DialogFragment;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;
import com.orhanobut.logger.Logger;

import us.bridgeses.Minder.editor.EditReminder;
import us.bridgeses.Minder.receivers.ReminderReceiver;
import us.bridgeses.Minder.util.ConfirmDialogFragment;

/**
 * Created by Tony on 8/8/2014.
 */
public class MainListActivity extends Activity implements TaskCallbacks,
        ConfirmDialogFragment.NoticeDialogListener,ReminderListAdapter.ListClicksListener{

    private static final String TAG_ASYNC_FRAGMENT = "Async_fragment";
    private ReminderListFragment mReminderListFragment;
	private AboutFragment mAboutFragment;
	private Boolean firstRun;
    private FragmentManager fragmentManager;
    private ProgressDialog progressDialog;

    @Override
    public void SkipClick(int id){
        ConfirmDialogFragment df = ConfirmDialogFragment.newInstance("Skip Reminder",
                "Skip the next instance of this Reminder","Skip",getResources().getString(R.string.edit_cancel),id);
        df.show(fragmentManager, "SkipDialog");
    }

    @Override
    public void IconClick(int id){
        Logger.d("IconClick");
    }

	@Override
	public void onDialogNegativeClick(DialogFragment dialog) {

	}

	@Override
	public void onDialogPositiveClick(DialogFragment dialog, int id) {
		Logger.e(Integer.toString(id));
		Reminder reminder = Reminder.get(this, id);
        Intent intentAlarm = new Intent(this, ReminderReceiver.class);      //Create alarm intent
        AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        alarmManager.cancel(PendingIntent.getBroadcast(getApplicationContext(), reminder.getId(), intentAlarm,
                PendingIntent.FLAG_UPDATE_CURRENT));
		reminder = Reminder.nextRepeat(reminder).save(this);
        if ((reminder.getActive()) && (reminder.getId() != -1)) {
            int alarmType;
            if (reminder.getWakeUp()){
                alarmType = AlarmManager.RTC_WAKEUP;
            }
            else {
                alarmType = AlarmManager.RTC;
            }
            intentAlarm = new Intent(this, ReminderReceiver.class);//Create alarm intent
            intentAlarm.putExtra("Id", id);           //Associate intent with specific Reminder
            intentAlarm.putExtra("Snooze", 0);                       //This alarm has not been snoozed
            alarmManager = (AlarmManager) this.getSystemService(Context.ALARM_SERVICE);
            alarmManager.set(alarmType, reminder.getDate().getTimeInMillis(),
                    PendingIntent.getBroadcast(this, id, intentAlarm, PendingIntent.FLAG_UPDATE_CURRENT));

            Logger.v("Alarm " + id + " set");
        }
		// Gets an instance of the NotificationManager service
		NotificationManager mNotifyMgr =
				(NotificationManager) getSystemService(NOTIFICATION_SERVICE);
		// Builds the notification and issues it.
		mNotifyMgr.cancel(reminder.getId());
        mReminderListFragment = new ReminderListFragment();
        fragmentManager = getFragmentManager();
        fragmentManager.beginTransaction().replace(R.id.list, mReminderListFragment,TAG_ASYNC_FRAGMENT).commit();
	}

	private void setTracker(){
		// Get tracker.
		Tracker t = ((Minder) getApplication()).getTracker(
				Minder.TrackerName.APP_TRACKER);

		// Set screen name.
		t.setScreenName("Main Activity");
		t.enableExceptionReporting(true);

		// Send a screen view.
		t.send(new HitBuilders.AppViewBuilder().build());
	}

    //Called if the user creates a new reminder
    public void editReminder(View view) {
        Intent intent = new Intent(this, EditReminder.class);
        intent.putExtra("New",true);
        startActivity(intent);
    }

	@Override
	public void onBackPressed()
	{
		fragmentManager = getFragmentManager();
        Fragment fragment = fragmentManager.findFragmentByTag(TAG_ASYNC_FRAGMENT);
        if (!(fragment instanceof AboutFragment)){
            finish();
        }
		fragmentManager.popBackStack();
	}



	@Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mainlist);

        SharedPreferences defaultPreferences = getApplication().
                getSharedPreferences("Defaults", Context.MODE_PRIVATE);
        firstRun = defaultPreferences.getBoolean("first_run-0.7.3",true);

        if (firstRun){
            SharedPreferences.Editor editor = defaultPreferences.edit();
            Reminder reminder = new Reminder();
            Reminder.reminderToPreference(this,defaultPreferences, reminder);

            editor.putBoolean("first_run-0.7.1", false);
            editor.apply();
            editor =  PreferenceManager.getDefaultSharedPreferences(this).edit();
            editor.clear().apply();
        }

        fragmentManager = getFragmentManager();
	    Fragment fragment = fragmentManager.findFragmentByTag(TAG_ASYNC_FRAGMENT);
	    if (fragment instanceof ReminderListFragment){
		    mReminderListFragment = (ReminderListFragment) fragment;
	    }
	    else{
		    if (fragment instanceof AboutFragment){
			    mAboutFragment = (AboutFragment) fragment;
		    }
	    }
        // If the Fragment is non-null, then it is currently being
        // retained across a configuration change.
        if (fragment == null) {
	        // create new fragment
            setTracker();
            mReminderListFragment = new ReminderListFragment();
	        fragmentManager.beginTransaction().replace(R.id.list, mReminderListFragment,TAG_ASYNC_FRAGMENT).commit();
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        if (fragmentManager == null) {
            fragmentManager = getFragmentManager();
        }
	    Fragment fragment = fragmentManager.findFragmentByTag(TAG_ASYNC_FRAGMENT);
	    if (fragment instanceof ReminderListFragment){
		    mReminderListFragment = (ReminderListFragment) fragment;
	    }
	    else{
		    if (fragment instanceof AboutFragment){
			    mAboutFragment = (AboutFragment) fragment;
		    }
	    }
        // If the Fragment is non-null, then it is currently being
        // retained across a configuration change.
        if (mReminderListFragment == null) {
            mReminderListFragment = new ReminderListFragment();
            fragmentManager.beginTransaction().replace(R.id.list, mReminderListFragment,TAG_ASYNC_FRAGMENT).commit();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        switch (item.getItemId()) {
            case R.id.defaults:
                Intent intent = new Intent(this, EditReminder.class);
                intent.putExtra("default",true);
                startActivity(intent);
                return true;
	        case R.id.action_about:
				FragmentManager fm = getFragmentManager();
		        mAboutFragment = new AboutFragment();
		        fm.beginTransaction().replace(R.id.list,mAboutFragment,TAG_ASYNC_FRAGMENT).addToBackStack(null).commit();
		        return true;
            case R.id.action_new:
                editReminder(findViewById(android.R.id.content));
                return true;
            default:
                return super.onOptionsItemSelected(item);
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
        if (progressDialog != null) {
            Logger.d("Dismissing progress dialog");
            progressDialog.dismiss();
            progressDialog = null;
        }
    }

    @Override
    public void onProgressUpdate(int percent) {

    }
}
