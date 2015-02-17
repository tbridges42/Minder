package us.bridgeses.Minder;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.preference.PreferenceScreen;
import android.widget.BaseAdapter;


public class ConditionsFragment extends PreferenceFragment implements SharedPreferences.OnSharedPreferenceChangeListener, Preference.OnPreferenceClickListener{

	//Reminder reminder;
	PreferenceScreen locationButton;
	ListPreference locationType;
	private ProgressDialog progressDialog;
	private MapTask mapTask;

	public static ConditionsFragment newInstance(){
		ConditionsFragment fragment = new ConditionsFragment();
		return fragment;
	}

	@Override
	public boolean onPreferenceClick(Preference preference) {
		String key = preference.getKey();
		if (key.equals("button_location_key")) {
			if (super.findPreference(key).isEnabled()) {
				mapTask = new MapTask();
				mapTask.execute();
			}
		}
		return false;
	}

	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode,resultCode,data);
		SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getActivity());
		Boolean isLocation = sharedPreferences.getFloat("Latitude",0)==0;
		isLocation = isLocation || sharedPreferences.getFloat("Longitude",0)==0;
		if (isLocation){
			super.findPreference("button_location_key").setSummary("");
		}
		else
			super.findPreference("button_location_key").setSummary("Location set");
	}

	private void initValues(){

	}

	private void initSummaries(){
		locationButton = (PreferenceScreen) super.findPreference("button_location_key");
		locationButton.setOnPreferenceClickListener(this);
		locationType = (ListPreference) super.findPreference("location_type");
		locationType.setSummary(locationType.getEntry());
		locationButton.setEnabled(!locationType.getValue().equals("0"));
		SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getActivity());
		Boolean isLocation = sharedPreferences.getFloat("Latitude",0)==0;
		isLocation = isLocation || sharedPreferences.getFloat("Longitude",0)==0;
		if (isLocation){
			super.findPreference("button_location_key").setSummary("");
		}
		else
			super.findPreference("button_location_key").setSummary("Location set");
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
//		reminder = getArguments().getParcelable("Reminder");

		addPreferencesFromResource(R.xml.conditions_preference);

		initValues();

		initSummaries();
	}

	@Override
	public void onResume() {
		super.onResume();
		// Set up a listener whenever a key changes
		getPreferenceScreen().getSharedPreferences()
				.registerOnSharedPreferenceChangeListener(this);
	}

	@Override
	public void onPause() {
		super.onPause();
		// Unregister the listener whenever a key changes
		//getPreferenceScreen().getSharedPreferences()
		//		.unregisterOnSharedPreferenceChangeListener(this);
	}

	public static ConditionsFragment newInstance(Reminder reminder){
		ConditionsFragment fragment = new ConditionsFragment();
		Bundle args = new Bundle();
		args.putParcelable("Reminder",reminder);
		fragment.setArguments(args);
		return fragment;
	}

	public void onSharedPreferenceChanged(SharedPreferences preference, String key){
		if (key.equals("location_type")){
			ListPreference mPreference = (ListPreference) findPreference(key);
			mPreference.setSummary(mPreference.getEntry());
			int value = Integer.valueOf(mPreference.getValue());
			super.findPreference("button_location_key").setEnabled(value != 0);
		}
		((BaseAdapter)getPreferenceScreen().getRootAdapter()).notifyDataSetChanged();
	}

	private class MapTask extends AsyncTask<Void, Integer, Void> {

		Intent intent = new Intent(getActivity(), MapsActivity.class);

		@Override
		protected void onPreExecute() {
			progressDialog = new ProgressDialog(getActivity());
			progressDialog.setIndeterminate(true);
			progressDialog.setTitle("");
			progressDialog.setMessage(getResources().getString(R.string.loading));
			progressDialog.show();
		}

		/**
		 * Note that we do NOT call the callback object's methods
		 * directly from the background thread, as this could result
		 * in a race condition.
		 */
		@Override
		protected Void doInBackground(Void... ignore) {
			startActivityForResult(intent, 2);
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

		}
	}
}
