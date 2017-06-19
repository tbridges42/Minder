package us.bridgeses.Minder.editor;

import android.app.Activity;
import android.app.Fragment;
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
import android.util.Log;
import android.widget.BaseAdapter;

import com.orhanobut.logger.Logger;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;

import us.bridgeses.Minder.R;
import us.bridgeses.Minder.model.Reminder;
import us.bridgeses.Minder.util.DatePreference;
import us.bridgeses.Minder.util.TimePreference;
import us.bridgeses.Minder.views.interfaces.EditorView;

import static us.bridgeses.Minder.model.Repeat.REPEAT_PERIOD_DEFAULT;
import static us.bridgeses.Minder.model.Repeat.REPEAT_TYPE_DEFAULT;

/**
 * Created by Tony on 8/27/2014.
 */

public class EditReminderFragment extends PreferenceFragment
        implements SharedPreferences.OnSharedPreferenceChangeListener,
        Preference.OnPreferenceClickListener,
        EditorView<Reminder> {

    private Reminder reminder;
    private SimpleDateFormat dateFormat = new SimpleDateFormat("EEEE, MMMM d, yyyy");
    private SimpleDateFormat timeFormat = new SimpleDateFormat("h:mm aa");

    private SharedPreferences sharedPreferences;

    private EditTextPreference namePreference;
    private EditTextPreference descriptionPreference;
    private TimePreference timePreference;
    private DatePreference datePreference;
    private PreferenceScreen repeatScreenPreference;
    private CheckBoxPreference vibratePreference;
    private RingtonePreference ringtonePreference;

    /**
     * Some preferences require additional handling. When they are clicked, this method is called
     * It determines what preference was called and handles it appropriately
     * @param preference The preference that was called
     * @return false if the click was not handled by this method
     */
    @Override
    public boolean onPreferenceClick(Preference preference) {
        String key = preference.getKey();
        if (key.equals("button_repeat_menu_key")) {
            Intent intent = new Intent(getActivity(), EditRepeat.class);
            startActivityForResult(intent, 1);
        }
        if (key.equals("button_conditions")) {
            Intent intent = new Intent(getActivity(), EditConditions.class);
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

    /**
     * This method handles any special cases that need to be handled when exiting child activities
     * @param requestCode a code designating which child activity finished
     * @param resultCode whether or not the activity finished successfully
     * @param data any data returned by the activity
     */
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == Activity.RESULT_CANCELED){
            // If the child activity was cancelled, do nothing
            return;
        }
        switch (requestCode) {
            case 1: {
                // If the repeat setting has been changed, the summary should be updated
                setRepeatSummary();
                break;
            }
            case 4: {
                // Ensure that the vibrate setting on this page matches the one in the child activity
                vibratePreference.setChecked(sharedPreferences.getBoolean("temp_vibrate",reminder.getVibrate()));
                break;
            }
        }
    }

    /**
     * The factory pattern is the preferred way to instantiate a fragment with parameters
     * @param reminder The reminder to be edited
     * @return the fragment with the arguments attached
     */
    public static EditReminderFragment newInstance(Reminder reminder){
        EditReminderFragment fragment = new EditReminderFragment();
        Bundle args = new Bundle();
        args.putParcelable("Reminder", reminder);
        fragment.setArguments(args);
        return fragment;
    }

    public static EditReminderFragment newInstance() {
        EditReminderFragment fragment = new EditReminderFragment();
        return fragment;
    }

    /**
     * Set preference click listener where needed, initialize values if needed
     */
	private void initPreferences(){
		namePreference = (EditTextPreference) super.findPreference("temp_name");
		descriptionPreference = (EditTextPreference) super.findPreference("temp_description");
		timePreference = (TimePreference) super.findPreference("temp_time");
		datePreference = (DatePreference) super.findPreference("temp_date");
		repeatScreenPreference = (PreferenceScreen) super.findPreference("button_repeat_menu_key");
		repeatScreenPreference.setOnPreferenceClickListener(this);
		vibratePreference = (CheckBoxPreference) super.findPreference("temp_vibrate");
		ringtonePreference = (RingtonePreference) super.findPreference("temp_ringtone");
		PreferenceScreen conditionsPreference = (PreferenceScreen) super.findPreference("button_conditions");
		conditionsPreference.setOnPreferenceClickListener(this);
        PreferenceScreen stylePreference = (PreferenceScreen) super.findPreference("button_style");
		stylePreference.setOnPreferenceClickListener(this);
        PreferenceScreen persistencePreference = (PreferenceScreen) super.findPreference("button_persistence");
		persistencePreference.setOnPreferenceClickListener(this);
	}

    /**
     * Ensure that summaries match values
     */
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
	    addPreferencesFromResource(R.xml.reminder_preference);

        getPreferenceScreen().getSharedPreferences()
                .registerOnSharedPreferenceChangeListener(this);

        LoadReminderTask mTask = new LoadReminderTask();
	    mTask.execute();
    }

    /**
     * Sets the summary for the repeat preference to an appropriate English language summary of the
     * current repeat setting
     */
    private void setRepeatSummary(){
	    if (sharedPreferences == null){
		    sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getActivity());
	    }
        int repeatType = Integer.parseInt(sharedPreferences.getString("temp_repeat_type",
                Integer.toString(REPEAT_TYPE_DEFAULT)));
        switch (repeatType) {
            case 0: {
                repeatScreenPreference.setSummary("Do not repeat");
                ((BaseAdapter)getPreferenceScreen().getRootAdapter()).notifyDataSetChanged();
                break;
            }
            case 1: {
                if (sharedPreferences.getString("temp_days",
                        Integer.toString(REPEAT_PERIOD_DEFAULT)).equals("1")) {
                    repeatScreenPreference.setSummary("Repeat every day");
                }
                else {
                    repeatScreenPreference.setSummary("Repeat every " +
                            sharedPreferences.getString("temp_days", Integer.toString(REPEAT_PERIOD_DEFAULT)) + " days");
                }
                ((BaseAdapter)getPreferenceScreen().getRootAdapter()).notifyDataSetChanged();
                break;
            }
            case 2: {
                if (sharedPreferences.getString("temp_weeks",
                        Integer.toString(REPEAT_PERIOD_DEFAULT)).equals("1")) {
                    repeatScreenPreference.setSummary("Repeat every week");
                }
                else {
                    repeatScreenPreference.setSummary("Repeat every " +
                            sharedPreferences.getString("temp_weeks", Integer.toString(REPEAT_PERIOD_DEFAULT)) + " weeks");
                }
                ((BaseAdapter)getPreferenceScreen().getRootAdapter()).notifyDataSetChanged();
                break;
            }
            case 3: {
                if (sharedPreferences.getString("temp_months",
                        Integer.toString(REPEAT_PERIOD_DEFAULT)).equals("1")) {
                    repeatScreenPreference.setSummary("Repeat every month");
                }
                else {
                    repeatScreenPreference.setSummary("Repeat every " +
                            sharedPreferences.getString("temp_months", Integer.toString(REPEAT_PERIOD_DEFAULT)) + " months");
                }
                ((BaseAdapter)getPreferenceScreen().getRootAdapter()).notifyDataSetChanged();
                break;
            }
            case 4: {
                if (sharedPreferences.getString("temp_years",
                        Integer.toString(REPEAT_PERIOD_DEFAULT)).equals("1")) {
                    repeatScreenPreference.setSummary("Repeat every year");
                }
                else {
                    repeatScreenPreference.setSummary("Repeat every " +
                            sharedPreferences.getString("temp_years",
                                    Integer.toString(REPEAT_PERIOD_DEFAULT)) + " years");
                }
                ((BaseAdapter)getPreferenceScreen().getRootAdapter()).notifyDataSetChanged();
                break;
            }
        }
    }

    /**
     * Some preferences require additional handling when their values change
     * That is handled here
     * @param preference The preference that changed
     * @param key The key of the preference that changed
     */
    @Override
    public void onSharedPreferenceChanged(SharedPreferences preference, String key) {
        Preference mPreference = findPreference(key);
        if (mPreference instanceof  EditTextPreference){
            EditTextPreference textPreference = (EditTextPreference) mPreference;
            textPreference.setSummary(textPreference.getText());

        }
        if (mPreference instanceof RingtonePreference) {
            String strRingtonePreference = preference.getString(key,"");
            if (strRingtonePreference.equals("")){
                mPreference.setSummary("Silent");
            }
            else {
                try {
                    Uri ringtoneUri = Uri.parse(strRingtonePreference);

                    Ringtone ringtone = RingtoneManager.getRingtone(getActivity(), ringtoneUri);
                    String title = ringtone.getTitle(getActivity());
                    mPreference.setSummary(title);
                }
                catch (NullPointerException e){
                    mPreference.setSummary("ERROR");
                }
            }
        }
        ((BaseAdapter)getPreferenceScreen().getRootAdapter()).notifyDataSetChanged();
    }

    @Override
    public void setup(Reminder model) {
        reminder = model;
        initPreferences();
        initSummaries();
    }

    @Override
    public Reminder getValues() {
        Reminder reminder = new Reminder();
        reminder.setId(sharedPreferences.getInt("temp_id", Reminder.IDDEFAULT));
        reminder.setName(sharedPreferences.getString("temp_name", Reminder.NAMEDEFAULT));
        reminder.setDescription(sharedPreferences.getString("temp_description", Reminder.DESCRIPTIONDEFAULT));
        Calendar date = Calendar.getInstance();
        SimpleDateFormat timeFormat = new SimpleDateFormat("h:mm aa EEEE, MMMM d, yyyy");
        try {
            String newDate = sharedPreferences.getString("temp_time", "") + " " + sharedPreferences.getString("temp_date", "");
            if (!newDate.equals(" ")) {
                Logger.d("Date set: "+newDate);
                date.setTime(timeFormat.parse(newDate));
            }
        }
        catch (ParseException e){
            Log.e("Minder","Parse Error");
        }
        if ((!Reminder.checkDayOfWeek(reminder.getDaysOfWeek(),          //If initial day is not in
                date.get(Calendar.DAY_OF_WEEK)))&&(reminder.getRepeatType()==2)){                       //repeat pattern, skip
            Reminder.nextRepeat(reminder).save(getActivity());
        }
        reminder.setDate(date);                                         //Store reminder date + time
        return reminder;
    }

    @Override
    public Fragment getFragment() {
        return this;
    }

    /**
     * This class displays a progressdialog while unpacking the reminder on a background thread
     */
	private class LoadReminderTask extends AsyncTask<Integer, Integer, Void> {

        private ProgressDialog progressDialog;

		@Override
		protected void onPreExecute() {
            progressDialog = ProgressDialog.show(getActivity(), "",
                    getResources().getString(R.string.loading), true, true);
		}

		@Override
		protected Void doInBackground(Integer... id) {
			reminder = getArguments().getParcelable("Reminder");
			//initValues();

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
