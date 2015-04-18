package us.bridgeses.Minder.editor;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.EditTextPreference;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.preference.PreferenceScreen;
import android.preference.RingtonePreference;
import android.widget.BaseAdapter;

import com.google.android.gms.maps.model.LatLng;

import java.text.SimpleDateFormat;

import us.bridgeses.Minder.R;
import us.bridgeses.Minder.Reminder;
import us.bridgeses.Minder.util.DatePreference;
import us.bridgeses.Minder.util.TimePreference;

/**
 * Created by Tony on 8/27/2014.
 */

public class EditReminderFragment extends PreferenceFragment implements SharedPreferences.OnSharedPreferenceChangeListener, Preference.OnPreferenceClickListener {

    private Reminder reminder;
    private SimpleDateFormat dateFormat = new SimpleDateFormat("EEEE, MMMM d, yyyy");
    private SimpleDateFormat timeFormat = new SimpleDateFormat("h:mm aa");

    private SharedPreferences sharedPreferences;

    private PreferenceScreen preferenceScreen;
    private EditTextPreference namePreference;
    private EditTextPreference descriptionPreference;
    private TimePreference timePreference;
    private DatePreference datePreference;
    private PreferenceScreen repeatScreenPreference;
    private CheckBoxPreference vibratePreference;
    private RingtonePreference ringtonePreference;
    private PreferenceScreen conditionsPreference;
	private PreferenceScreen stylePreference;
	private PreferenceScreen persistencePreference;
	private ProgressDialog progressDialog;

    @Override
    public boolean onPreferenceClick(Preference preference) {
        String key = preference.getKey();
        if (key.equals("button_repeat_menu_key")) {
            Intent intent = new Intent(getActivity(), EditRepeat.class);
            //Bundle bundle = new Bundle();
            //bundle.putParcelable("Reminder", reminder);
            //intent.putExtras(bundle);
            startActivityForResult(intent, 1);
        }
        if (key.equals("button_conditions")) {
            Intent intent = new Intent(getActivity(), EditConditions.class);
            /*Bundle bundle = new Bundle();
            bundle.putParcelable("Reminder", reminder);
            intent.putExtras(bundle);*/
            startActivityForResult(intent, 2);
        }
	    if (key.equals("button_style")) {
		    Intent intent = new Intent(getActivity(), EditStyle.class);
		    startActivityForResult(intent, 4);
	    }
	    if (key.equals("button_persistence")) {
		    Intent intent = new Intent(getActivity(), EditPersistence.class);
		    startActivityForResult(intent, 3);
	    }
        return false;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode,resultCode,data);
	    if (resultCode == Activity.RESULT_OK){
		    switch (requestCode) {
			    case 1: {
				    setRepeatSummary();
				    break;
			    }
		    }
	    }
    }

    public static EditReminderFragment newInstance(Reminder reminder){
        EditReminderFragment fragment = new EditReminderFragment();
        Bundle args = new Bundle();
        args.putParcelable("Reminder", reminder);
        fragment.setArguments(args);
        return fragment;
    }

    private void initWeekly() {
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getActivity());
        SharedPreferences.Editor editor = sharedPreferences.edit();
        byte daysOfWeek = reminder.getDaysOfWeek();
        if (Reminder.checkDayOfWeek(daysOfWeek, 1)) {
            editor.putBoolean("temp_sunday",true);
        }
        if (Reminder.checkDayOfWeek(daysOfWeek, 2)) {
            editor.putBoolean("temp_monday",true);
        }
        if (Reminder.checkDayOfWeek(daysOfWeek, 3)) {
            editor.putBoolean("temp_tuesday",true);
        }
        if (Reminder.checkDayOfWeek(daysOfWeek, 4)) {
            editor.putBoolean("temp_wednesday",true);
        }
        if (Reminder.checkDayOfWeek(daysOfWeek, 5)) {
            editor.putBoolean("temp_thursday",true);
        }
        if (Reminder.checkDayOfWeek(daysOfWeek, 6)) {
            editor.putBoolean("temp_friday",true);
        }
        if (Reminder.checkDayOfWeek(daysOfWeek, 7)) {
            editor.putBoolean("temp_saturday",true);
        }
        editor.apply();
    }

	private SharedPreferences.Editor initRepeatValues(SharedPreferences.Editor editor) {
		int repeatTypeIndex = reminder.getRepeatType();
		editor.putString("temp_repeat_type", Integer.toString(repeatTypeIndex));
		editor.putString("temp_days", Integer.toString(reminder.getRepeatLength()));
		editor.putString("temp_weeks", Integer.toString(reminder.getRepeatLength()));
		initWeekly();
		editor.putString("temp_months", Integer.toString(reminder.getRepeatLength()));
		editor.putString("temp_monthly_type", Integer.toString(reminder.getMonthType()));
		editor.putString("temp_years", Integer.toString(reminder.getRepeatLength()));
		return editor;
	}

	private SharedPreferences.Editor initConditionsValues(SharedPreferences.Editor editor) {
		if (reminder.getOnlyAtLocation()){
			editor.putString("location_type", "1");
		}
		else {
			if (reminder.getUntilLocation()) {
				editor.putString("location_type", "2");
			}
			else {
				editor.putString("location_type","0");
			}
		}
		LatLng location = reminder.getLocation();
		editor.putFloat("Latitude",(float) location.latitude);
		editor.putFloat("Longitude",(float) location.longitude);
		editor.putInt("radius",reminder.getRadius());
        editor.putBoolean("wifi",reminder.getNeedWifi());
        editor.putString("ssid",reminder.getSSID());
		return editor;
	}

    private SharedPreferences.Editor initStyleValues(SharedPreferences.Editor editor){
        editor.putString("snooze_duration",Integer.toString(reminder.getSnoozeDuration()));
        editor.putBoolean("led",reminder.getLed());
        editor.putString("led_pattern",Integer.toString(reminder.getLedPattern()));
        editor.putInt("led_color",reminder.getLedColor());
	    editor.putBoolean("fade",reminder.getFadeVolume());
        return editor;
    }

	private SharedPreferences.Editor initPersistenceValues(SharedPreferences.Editor editor) {
		editor.putString("temp_code",reminder.getQr());
		editor.putBoolean("code_type",reminder.getNeedQr());

        editor.putBoolean("out_loud",reminder.getVolumeOverride());
        editor.putBoolean("display_screen",reminder.getDisplayScreen());
        editor.putBoolean("wake_up",reminder.getWakeUp());

		editor.putBoolean("dismiss_check",reminder.getConfirmDismiss());
		return editor;
	}

    private void initValues(){
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getActivity());
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.clear().commit();
	    editor.putInt("temp_id",reminder.getId());
        editor.putString("temp_name",reminder.getName());
        editor.putString("temp_description",reminder.getDescription());
        editor.putString("temp_time",timeFormat.format(reminder.getDate().getTime()));
        editor.putString("temp_date",dateFormat.format(reminder.getDate().getTime()));
        editor.putBoolean("temp_vibrate",reminder.getVibrate());
        editor.putString("temp_ringtone",reminder.getRingtone());
	    editor = initConditionsValues(editor);

	    editor = initRepeatValues(editor);

        editor = initStyleValues(editor);

	    editor = initPersistenceValues(editor);

        editor.apply();
    }

	private void initPreferences(){
		preferenceScreen = (PreferenceScreen) super.findPreference("preference_screen");
		namePreference = (EditTextPreference) super.findPreference("temp_name");
		descriptionPreference = (EditTextPreference) super.findPreference("temp_description");
		timePreference = (TimePreference) super.findPreference("temp_time");
		datePreference = (DatePreference) super.findPreference("temp_date");
		repeatScreenPreference = (PreferenceScreen) super.findPreference("button_repeat_menu_key");
		repeatScreenPreference.setOnPreferenceClickListener(this);
		vibratePreference = (CheckBoxPreference) super.findPreference("temp_vibrate");
		ringtonePreference = (RingtonePreference) super.findPreference("temp_ringtone");
		conditionsPreference = (PreferenceScreen) super.findPreference("button_conditions");
		conditionsPreference.setOnPreferenceClickListener(this);
		stylePreference = (PreferenceScreen) super.findPreference("button_style");
		stylePreference.setOnPreferenceClickListener(this);
		persistencePreference = (PreferenceScreen) super.findPreference("button_persistence");
		persistencePreference.setOnPreferenceClickListener(this);
	}

    private void initSummaries(){
	    initPreferences();
        namePreference.setSummary(reminder.getName());
	    namePreference.setText(reminder.getName());
        descriptionPreference.setSummary(reminder.getDescription());
	    descriptionPreference.setText(reminder.getDescription());
	    String time = timeFormat.format(reminder.getDate().getTime());
	    String date = dateFormat.format(reminder.getDate().getTime());
	    timePreference.setSummary(time);
	    timePreference.setTime(time);
        datePreference.setSummary(date);
	    datePreference.setDate(date);
        setRepeatSummary();
        if (reminder.getRingtone().equals("")){
            ringtonePreference.setSummary(getResources().getString(R.string.silent));
        }
        else {
            Ringtone ringtone = RingtoneManager.getRingtone(getActivity(), Uri.parse(reminder.getRingtone()));
            String ringtoneTitle = ringtone.getTitle(getActivity());
            ringtonePreference.setSummary(ringtoneTitle);
        }
	    vibratePreference.setChecked(reminder.getVibrate());
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
	    Bundle args = getArguments();
	    reminder = args.getParcelable("Reminder");
	    progressDialog = new ProgressDialog(getActivity());
	    progressDialog.setIndeterminate(true);
	    progressDialog.setTitle("");
	    progressDialog.setMessage(getResources().getString(R.string.loading));
	    progressDialog.show();


	    addPreferencesFromResource(R.xml.reminder_preference);

        LoadReminderTask mTask = new LoadReminderTask();
	    mTask.execute();

    }

    @Override
    public void onResume() {
        super.onResume();
        getPreferenceScreen().getSharedPreferences()
                .registerOnSharedPreferenceChangeListener(this);
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    private void setRepeatSummary(){
        int repeatType = Integer.parseInt(sharedPreferences.getString("temp_repeat_type",
                Integer.toString(Reminder.REPEATTYPEDEFAULT)));
        switch (repeatType) {
            case 0: {
                repeatScreenPreference.setSummary("Do not repeat");
                ((BaseAdapter)getPreferenceScreen().getRootAdapter()).notifyDataSetChanged();
                break;
            }
            case 1: {
                if (sharedPreferences.getString("temp_days",Integer.toString(Reminder.REPEATLENGTHDEFAULT)).equals("1")) {
                    repeatScreenPreference.setSummary("Repeat every day");
                }
                else
                    repeatScreenPreference.setSummary("Repeat every " +
                            sharedPreferences.getString("temp_days", Integer.toString(Reminder.REPEATLENGTHDEFAULT)) + " days");
                ((BaseAdapter)getPreferenceScreen().getRootAdapter()).notifyDataSetChanged();
                break;
            }
            case 2: {
                if (sharedPreferences.getString("temp_weeks",Integer.toString(Reminder.REPEATLENGTHDEFAULT)).equals("1")) {
                    repeatScreenPreference.setSummary("Repeat every week");
                }
                else
                    repeatScreenPreference.setSummary("Repeat every " +
                            sharedPreferences.getString("temp_weeks", Integer.toString(Reminder.REPEATLENGTHDEFAULT)) + " weeks");
                ((BaseAdapter)getPreferenceScreen().getRootAdapter()).notifyDataSetChanged();
                break;
            }
            case 3: {
                if (sharedPreferences.getString("temp_months",Integer.toString(Reminder.REPEATLENGTHDEFAULT)).equals("1")) {
                    repeatScreenPreference.setSummary("Repeat every month");
                }
                else
                    repeatScreenPreference.setSummary("Repeat every " +
                            sharedPreferences.getString("temp_months", Integer.toString(Reminder.REPEATLENGTHDEFAULT)) + " months");
                ((BaseAdapter)getPreferenceScreen().getRootAdapter()).notifyDataSetChanged();
                break;
            }
            case 4: {
                if (sharedPreferences.getString("temp_years",Integer.toString(Reminder.REPEATLENGTHDEFAULT)).equals("1")) {
                    repeatScreenPreference.setSummary("Repeat every year");
                }
                else
                    repeatScreenPreference.setSummary("Repeat every " +
                            sharedPreferences.getString("temp_years", Integer.toString(Reminder.REPEATLENGTHDEFAULT)) + " years");
                ((BaseAdapter)getPreferenceScreen().getRootAdapter()).notifyDataSetChanged();
                break;
            }
        }
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences preference, String key) {
        Preference mPreference = findPreference(key);
        if (mPreference instanceof  EditTextPreference){
            EditTextPreference textPreference = (EditTextPreference) mPreference;
            textPreference.setSummary(textPreference.getText());

        }
	    if (mPreference instanceof DatePreference){

	    }
        if (mPreference instanceof RingtonePreference) {

            String strRingtonePreference = preference.getString(key,"");


            if (strRingtonePreference.equals("")){
                mPreference.setSummary("Silent");
            }
            else {
                Uri ringtoneUri = Uri.parse(strRingtonePreference);
                Ringtone ringtone = RingtoneManager.getRingtone(getActivity(), ringtoneUri);
                try {
                    String title = ringtone.getTitle(getActivity());

                    mPreference.setSummary(title);
                }
                catch(NullPointerException e){

                }
            }

        }
        ((BaseAdapter)getPreferenceScreen().getRootAdapter()).notifyDataSetChanged();
    }

	private class LoadReminderTask extends AsyncTask<Integer, Integer, Void> {

		@Override
		protected void onPreExecute() {

		}

		/**
		 * Note that we do NOT call the callback object's methods
		 * directly from the background thread, as this could result
		 * in a race condition.
		 */
		@Override
		protected Void doInBackground(Integer... id) {
			reminder = getArguments().getParcelable("Reminder");
			initValues();

			return null;
		}

		@Override
		protected void onProgressUpdate(Integer... percent) {


		}

		@Override
		protected void onCancelled() {

		}

		@Override
		protected void onPostExecute(Void ignore) {

			if (progressDialog != null) {
				progressDialog.dismiss();
				progressDialog = null;
			}
			initSummaries();
		}
	}
}
