package us.bridgeses.Minder.editor;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.preference.PreferenceScreen;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.widget.BaseAdapter;

import com.orhanobut.logger.Logger;

import us.bridgeses.Minder.R;
import us.bridgeses.Minder.Reminder;
import us.bridgeses.Minder.util.Scanner.ScannerActivity;
import us.bridgeses.Minder.util.SeekbarPreference;


public class PersistenceFragment extends PreferenceFragment implements
        SharedPreferences.OnSharedPreferenceChangeListener, Preference.OnPreferenceClickListener{


	public static PreferenceFragment newInstance(){
		return new PersistenceFragment();
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (requestCode == 0) {

			if (resultCode == Activity.RESULT_OK) {
				SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getActivity());
				SharedPreferences.Editor editor = sharedPreferences.edit();
				String code = data.getStringExtra("SCAN_RESULT");
				editor.putString("temp_code",code);
				editor.apply();
				super.findPreference("button_code").setSummary(getResources().getString(R.string.code_set));
			}
			if(resultCode == Activity.RESULT_CANCELED){
				//handle cancel
			}
		}
	}

	private boolean checkCamera(){
		PackageManager pm = getActivity().getPackageManager();
		return pm.hasSystemFeature(PackageManager.FEATURE_CAMERA);
	}

	@Override
	public boolean onPreferenceClick(Preference preference) {
		String key = preference.getKey();
		Logger.d(key);
		if (key.equals("button_code")) {
			Logger.d("Checking for camera permissions");
			int permissionCheck = ContextCompat.checkSelfPermission(getActivity(),
					Manifest.permission.CAMERA);
			if (permissionCheck == PackageManager.PERMISSION_GRANTED) {
				// If permission is already granted, indicate that we are not handling this click
				// here and return control to preference fragment
				Logger.d("Permission granted");

				Intent intent = new Intent(getActivity(),ScannerActivity.class);
				startActivityForResult(intent, 0);
				return true;
			}
			else {
				Logger.d("Requesting permissions");
				if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
					requestPermissions(new String[]{Manifest.permission.CAMERA},
                            1);
				}
				CheckBoxPreference mPreference = (CheckBoxPreference) findPreference(key);
				mPreference.setChecked(false);
			}
		}
		return false;
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
					CheckBoxPreference codeType = (CheckBoxPreference) super.findPreference("code_type");
					codeType.setChecked(true);

				} else {
					CheckBoxPreference codeType = (CheckBoxPreference) super.findPreference("code_type");
					codeType.setSummary(R.string.no_camera);
					codeType.setEnabled(false);
				}
				return;
			}
			case 1: {
				// If request is cancelled, the result arrays are empty.
				if (grantResults.length > 0
						&& grantResults[0] == PackageManager.PERMISSION_GRANTED) {
					CheckBoxPreference codeType = (CheckBoxPreference) super.findPreference("code_type");
					codeType.setChecked(true);

				} else {

					CheckBoxPreference codeType = (CheckBoxPreference) super.findPreference("code_type");
					codeType.setSummary(R.string.no_camera);
					codeType.setChecked(false);
					codeType.setEnabled(false);
				}
				return;
			}
		}
	}

	public void onSharedPreferenceChanged(SharedPreferences preference, String key){
		if (key.equals("code_type") && ((CheckBoxPreference)findPreference(key)).isChecked()){
			Logger.d("Checking for camera permissions");
			int permissionCheck = ContextCompat.checkSelfPermission(getActivity(),
					Manifest.permission.CAMERA);
			if (permissionCheck == PackageManager.PERMISSION_GRANTED) {
				// If permission is already granted, indicate that we are not handling this click
				// here and return control to preference fragment
				Logger.d("Permission granted");
			}
			else {
				Logger.d("Requesting permissions");
				if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
					requestPermissions(new String[]{Manifest.permission.CAMERA},
                            0);
				}
				CheckBoxPreference mPreference = (CheckBoxPreference) findPreference(key);
				mPreference.setChecked(false);
			}
		}
		if (key.equals("code_type")) {
			CheckBoxPreference mPreference = (CheckBoxPreference) findPreference(key);
			super.findPreference("button_code").setEnabled(mPreference.isChecked());
		}
		((BaseAdapter)getPreferenceScreen().getRootAdapter()).notifyDataSetChanged();
	}

	@Override
	public void onResume() {
		super.onResume();
		// Set up a listener whenever a key changes
		getPreferenceScreen().getSharedPreferences()
				.registerOnSharedPreferenceChangeListener(this);
	}

	private void initSummaries() {
		CheckBoxPreference codeType = (CheckBoxPreference) super.findPreference("code_type");
		PreferenceScreen codeButton = (PreferenceScreen) super.findPreference("button_code");
		SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getActivity());
        CheckBoxPreference outLoudPreference = (CheckBoxPreference) super.findPreference("out_loud");
        CheckBoxPreference displayScreenPreference = (CheckBoxPreference) super.findPreference("display_screen");
        CheckBoxPreference wakeUpPreference = (CheckBoxPreference) super.findPreference("wake_up");
		SeekbarPreference seekbarPreference = (SeekbarPreference) super.findPreference("volume");
		seekbarPreference.setPosition(sharedPreferences.getInt("volume", Reminder.VOLUMEDEFAULT));
		if (checkCamera()) {
			codeButton.setEnabled(codeType.isChecked());
			if (sharedPreferences.getString("button_code", "").equals("")) {
				super.findPreference("button_code").setSummary("");
			} else
				super.findPreference("button_code").setSummary(getResources().getString(R.string.code_set));
		}
		else{
			codeButton.setEnabled(false);
			codeType.setEnabled(false);
			codeType.setChecked(false);
			codeType.setSummary(R.string.no_camera);
			codeButton.setSummary("");
		}
        outLoudPreference.setChecked(sharedPreferences.getBoolean("out_loud",false));
        displayScreenPreference.setChecked(sharedPreferences.getBoolean("display_screen",false));
        wakeUpPreference.setChecked(sharedPreferences.getBoolean("wake_up",false));

	}

	@Override
	public void onCreate(Bundle savedInstanceState){
		super.onCreate(savedInstanceState);
		addPreferencesFromResource(R.xml.persistence_preference);
		super.findPreference("button_code").setOnPreferenceClickListener(this);
		initSummaries();
	}
}
