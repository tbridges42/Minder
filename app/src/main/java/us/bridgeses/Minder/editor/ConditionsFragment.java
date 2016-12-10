package us.bridgeses.Minder.editor;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.preference.PreferenceScreen;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.widget.BaseAdapter;

import com.orhanobut.logger.Logger;

import java.util.List;

import us.bridgeses.Minder.MapsActivity;
import us.bridgeses.Minder.R;
import us.bridgeses.Minder.Reminder;
import us.bridgeses.Minder.util.ActivityLoader;

/**
 * Displays options to the user that effect under what conditions the reminder will fire
 * Uses the contents of the default preference file and the layout defined in conditions_preference.xml
 * Creating without setting the values of the default preference file may have unexpected results
 */
//TODO: Is there any way to make these editor classes agnostic of the preferences referenced in xml?
//TODO: Move all editor activities into one with multiple fragments?
public class ConditionsFragment extends PreferenceFragment implements SharedPreferences.OnSharedPreferenceChangeListener, Preference.OnPreferenceClickListener{

    final static int MAP_ACTIVITY_CODE = 1;

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
    Activity activity;

    /**
     * Factory method to return a new ConditionsFragment.
     * The factory pattern is used here to allow flexibility in overriding for unit tests or
     * future modifications
     * @return a new ConditionsFragment
     */
	protected static ConditionsFragment newInstance(){
		return new ConditionsFragment();
	}

    private void createProgressDialog(String message){
        Logger.d("Creating conditions progress dialog");
        progressDialog = ProgressDialog.show(activity, "", message, true, true);
    }

    private void cancelProgressDialog(){
        if ((progressDialog != null)&&(progressDialog.isShowing())){
            progressDialog.dismiss();
        }
    }

    private String[] resultsToStrings(List<ScanResult> results){
        String[] ssidArray = new String[results.size()];
        for(int i=0; i<results.size(); i++){
            ssidArray[i] = results.get(i).SSID;
        }
        return ssidArray;
    }

    /**
     * Given a list of scan results, display a picker dialog for the user to choose from
     * @param results is the list of scan results
     */
    private void createSsidPicker(List<ScanResult> results){
        wifiBuilder = new AlertDialog.Builder(activity);
        wifiBuilder.setTitle("Select WiFi Network");

        final String[] finalSSID = resultsToStrings(results);

        wifiBuilder.setItems(finalSSID, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                setSSID(finalSSID[which]);
                activity.unregisterReceiver(wifiReceiver);
                Logger.e(finalSSID[which]);
            }
        });
        wifiBuilder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                activity.unregisterReceiver(wifiReceiver);
            }
        });
        wifiDialog = wifiBuilder.create();
        cancelProgressDialog();
        wifiBuilder.show();
    }

    private void ssidDialog(){
        List<ScanResult> results = wifiManager.getScanResults();
        boolean valid = !results.isEmpty() && !(results.size() == 0);
        if (valid) {
            // If the wifiManager already has scan results, go ahead and display the picker
            createSsidPicker(results);
        }
        else {
            // If the wifiManager does not have scan results, start scanning
            wifiManager.startScan();
            createProgressDialog("Scanning SSIDs...");
        }
    }

    private void setSSID(String ssid){
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(activity);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString("ssid",ssid);
        ssidButton.setSummary(ssid);
        editor.apply();
    }

    private void checkWifi(){
        activity.registerReceiver(wifiReceiver, new IntentFilter(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION));
        wifiManager = (WifiManager) activity.getSystemService(Context.WIFI_SERVICE);
        if (wifiManager.isWifiEnabled()){
            // If wifi is enabled, display ssid picker
            ssidDialog();
        }
        else {
            // If wifi is disabled, prompt for wifi
            wifiBuilder = new AlertDialog.Builder(activity);
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
            //User wants to choose a location
			if (super.findPreference(key).isEnabled()) {
                Intent intent = new Intent(activity, MapsActivity.class);
                ActivityLoader.ActivityListener listener = new ActivityLoader.ActivityListener() {
                    @Override
                    public void onStart() {
                        createProgressDialog(getResources().getString(R.string.loading));
                    }

                    @Override
                    public void onCancel() {
                        cancelProgressDialog();
                    }

                    @Override
                    public void onFinish() {
                        cancelProgressDialog();
                    }
                };
                ActivityLoader activityLoader = new ActivityLoader(listener, intent, this, MAP_ACTIVITY_CODE);
                activityLoader.execute();
				//mapTask = new MapTask();
				//mapTask.execute();
			}
		}
		if (key.equals("button_wifi")){
            //User wants to choose an ssid
            wifiReceiver = new WifiReceiver();
			checkWifi();
		}
		return false;
	}

    // Called when user finishes selecting a location
    @Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
        Logger.d("Conditions activity result");
		super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == Activity.RESULT_CANCELED){
            return;
        }
        if (requestCode == MAP_ACTIVITY_CODE) {
            super.findPreference("button_location_key").setSummary("Location set");
        }
	}

    /**
     * Ensure that all display values match stored values when fragment is created
     */
	private void initSummaries(){
        locationType = (ListPreference) super.findPreference("location_type");
        locationType.setSummary(locationType.getEntry());

		locationButton = (PreferenceScreen) super.findPreference("button_location_key");
		locationButton.setOnPreferenceClickListener(this);
        locationButton.setEnabled(!locationType.getValue().equals("0"));

        wifiRequired = (CheckBoxPreference) super.findPreference("wifi");

        ssidButton = (PreferenceScreen) super.findPreference("button_wifi");
        ssidButton.setEnabled(wifiRequired.isChecked());
        ssidButton.setOnPreferenceClickListener(this);

		SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(activity);

        wifiRequired.setChecked(sharedPreferences.getBoolean("wifi", Reminder.WIFIDEFAULT));
        ssidButton.setSummary(sharedPreferences.getString("ssid",Reminder.SSIDDEFAULT));

        Boolean isInvalidLocation = sharedPreferences.getFloat("Latitude",
                (float)Reminder.LOCATIONDEFAULT.latitude)==(float)Reminder.LOCATIONDEFAULT.latitude;
        isInvalidLocation = isInvalidLocation || sharedPreferences.getFloat("Longitude",
                (float)Reminder.LOCATIONDEFAULT.longitude)==(float)Reminder.LOCATIONDEFAULT.longitude;
		if (isInvalidLocation){
			super.findPreference("button_location_key").setSummary("");
		}
		else {
            super.findPreference("button_location_key").setSummary("Location set");
        }
	}

    @Override
    public void onAttach(Activity activity){
        super.onAttach(activity);
        this.activity = activity;
    }

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		addPreferencesFromResource(R.xml.conditions_preference);

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
        getPreferenceScreen().getSharedPreferences().unregisterOnSharedPreferenceChangeListener(this);
		super.onPause();
	}

    @Override
	public void onSharedPreferenceChanged(SharedPreferences preference, String key){
		if (key.equals("location_type")){
            // If the location type is set to anything other than none, we should enable the
            // map picker preference
			ListPreference mPreference = (ListPreference) findPreference(key);
			mPreference.setSummary(mPreference.getEntry());
			int value = Integer.valueOf(mPreference.getValue());
            if (value != 0) {
                int permissionCheck = ContextCompat.checkSelfPermission(getActivity(),
                        Manifest.permission.ACCESS_FINE_LOCATION);
                if (permissionCheck == PackageManager.PERMISSION_GRANTED) {
                    super.findPreference("button_location_key").setEnabled(true);
                }
                else {
                    Logger.d("Requesting permissions");
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                        requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION,
                                Manifest.permission.ACCESS_COARSE_LOCATION},
                                0);
                    }

                }
            }
            else {
                super.findPreference("button_location_key").setEnabled(false);
            }
		}
        if (key.equals("wifi")){
            // If wifi is required, we should enable the wifi picker preference
            ssidButton.setEnabled(wifiRequired.isChecked());
        }
		((BaseAdapter)getPreferenceScreen().getRootAdapter()).notifyDataSetChanged();
	}

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        Logger.d("received permission");
        switch (requestCode) {
            case 0: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    super.findPreference("button_location_key").setEnabled(true);

                } else {
                    super.findPreference("button_location_key").setEnabled(true);
                    ListPreference mPreference = (ListPreference) findPreference("location_type");
                    mPreference.setValue("0");
                    mPreference.setSummary(mPreference.getEntry());
                }
                return;
            }
        }
    }

    /**
     * This AsyncTask displays a progress dialog while the map activity is loading
     */
	private class MapTask extends AsyncTask<Void, Integer, Void> {

		Intent intent = new Intent(activity, MapsActivity.class);

		@Override
		protected void onPreExecute() {
			createProgressDialog(getResources().getString(R.string.loading));
		}

		@Override
		protected Void doInBackground(Void... ignore) {
			startActivityForResult(intent, MAP_ACTIVITY_CODE);
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
