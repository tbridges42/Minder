package us.bridgeses.Minder;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.preference.PreferenceScreen;
import android.provider.Settings;
import android.util.Log;
import android.widget.BaseAdapter;

import java.util.List;


public class ConditionsFragment extends PreferenceFragment implements SharedPreferences.OnSharedPreferenceChangeListener, Preference.OnPreferenceClickListener{

	//Reminder reminder;
	PreferenceScreen locationButton;
	ListPreference locationType;
    CheckBoxPreference wifiRequired;
    PreferenceScreen ssidButton;
	private ProgressDialog progressDialog;
	private MapTask mapTask;
    private WifiManager wifiManager;
    AlertDialog.Builder wifiBuilder;
    AlertDialog wifiDialog;
    WifiReceiver wifiReceiver;

	public static ConditionsFragment newInstance(){
		ConditionsFragment fragment = new ConditionsFragment();
		return fragment;
	}

    public void createProgressDialog(String message){
        progressDialog = new ProgressDialog(getActivity());
        progressDialog.setTitle("");
        progressDialog.setMessage(message);
        progressDialog.setIndeterminate(true);
        progressDialog.show();
    }

    public void cancelProgressDialog(){
        if (progressDialog != null){
            if (progressDialog.isShowing()) {
                progressDialog.dismiss();
            }
        }
    }

    private void ssidDialog(){
        boolean valid = true;
        List<ScanResult> results = wifiManager.getScanResults();
        if ((results.isEmpty())||(results.size()==0)){
            valid = false;
        }
        else {

        }
        if (valid) {
            wifiBuilder = new AlertDialog.Builder(getActivity());
            wifiBuilder.setTitle("Select SSID");

            String[] ssidArray = new String[results.size()];

            for(int i=0; i<results.size(); i++){
                ssidArray[i] = results.get(i).SSID;
            }

            final String[] finalSSID = ssidArray;

            wifiBuilder.setItems(ssidArray, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    setSSID(finalSSID[which]);
                    getActivity().unregisterReceiver(wifiReceiver);
                    Log.e("Minder",finalSSID[which]);
                }
            });
            wifiBuilder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    getActivity().unregisterReceiver(wifiReceiver);
                }
            });
            wifiDialog = wifiBuilder.create();
            cancelProgressDialog();
            wifiBuilder.show();
        }
        else {
            wifiManager.startScan();
            createProgressDialog("Scanning SSIDs...");
        }
    }

    public void setSSID(String ssid){
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getActivity());
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString("ssid",ssid);
        ssidButton.setSummary(ssid);
        editor.apply();
    }

    private void checkWifi(){
        getActivity().registerReceiver(wifiReceiver, new IntentFilter(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION));
        wifiManager = (WifiManager) getActivity().getSystemService(Context.WIFI_SERVICE);
        if (wifiManager.isWifiEnabled()){
            ssidDialog();
        }
        else {
            wifiBuilder = new AlertDialog.Builder(getActivity());
            wifiBuilder.setTitle("WiFi Unavailable");
            wifiBuilder.setMessage("WiFi is disabled. Enable WiFi to use this function.");
            wifiBuilder.setPositiveButton("Enable", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    startActivity(new Intent(Settings.ACTION_WIFI_SETTINGS));
                }
            });
            wifiBuilder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {

                }
            });
            wifiBuilder.show();
        }
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
        if (key.equals("button_wifi")) {
            if (super.findPreference(key).isEnabled()) {
                checkWifi();
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
        ssidButton = (PreferenceScreen) super.findPreference("button_wifi");
        wifiRequired = (CheckBoxPreference) super.findPreference("wifi");
        ssidButton.setEnabled(wifiRequired.isChecked());
        ssidButton.setOnPreferenceClickListener(this);
		SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getActivity());
        wifiRequired.setChecked(sharedPreferences.getBoolean("wifi",Reminder.WIFIDEFAULT));
        ssidButton.setSummary(sharedPreferences.getString("ssid",Reminder.SSIDDEFAULT));
		Boolean isLocation = sharedPreferences.
                getFloat("Latitude",(float)Reminder.LOCATIONDEFAULT.latitude)==0;
		isLocation = isLocation || sharedPreferences.
                getFloat("Longitude",(float)Reminder.LOCATIONDEFAULT.longitude)==0;
		if (isLocation){
			super.findPreference("button_location_key").setSummary("");
		}
		else
			super.findPreference("button_location_key").setSummary("Location set");
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
        wifiReceiver = new WifiReceiver();
		addPreferencesFromResource(R.xml.conditions_preference);

		initValues();

		initSummaries();
	}

	@Override
	public void onResume() {
		super.onResume();
		// Set up a listener for key changes
		getPreferenceScreen().getSharedPreferences()
				.registerOnSharedPreferenceChangeListener(this);

	}

	@Override
	public void onPause() {
		super.onPause();
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
        if (key.equals("wifi")){
            ssidButton.setEnabled(wifiRequired.isChecked());
        }
		((BaseAdapter)getPreferenceScreen().getRootAdapter()).notifyDataSetChanged();
	}

	private class MapTask extends AsyncTask<Void, Integer, Void> {

		Intent intent = new Intent(getActivity(), MapsActivity.class);

		@Override
		protected void onPreExecute() {
			createProgressDialog(getResources().getString(R.string.loading));
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
			cancelProgressDialog();
		}
	}

    class WifiReceiver extends BroadcastReceiver {
        public void onReceive(Context c, Intent intent) {
            ssidDialog();
        }
    }

}
