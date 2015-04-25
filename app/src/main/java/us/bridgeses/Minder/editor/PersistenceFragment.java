package us.bridgeses.Minder.editor;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.preference.PreferenceScreen;
import android.widget.BaseAdapter;
import android.widget.Toast;

import us.bridgeses.Minder.R;
import us.bridgeses.Minder.util.scanner.ScannerActivity;


public class PersistenceFragment extends PreferenceFragment implements
        SharedPreferences.OnSharedPreferenceChangeListener, Preference.OnPreferenceClickListener{


	public static PreferenceFragment newInstance(){
		return new PersistenceFragment();
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if (requestCode == 0) {

			if (resultCode == Activity.RESULT_OK) {
				SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getActivity());
				SharedPreferences.Editor editor = sharedPreferences.edit();
				String code = data.getStringExtra("SCAN_RESULT");
				editor.putString("temp_code",code);
				editor.apply();
				super.findPreference("button_code").setSummary(getResources().getString(R.string.code_set));
				int duration = Toast.LENGTH_SHORT;
				Toast toast = Toast.makeText(getActivity(), code, duration);
				toast.show();
			}
			if(resultCode == Activity.RESULT_CANCELED){
				//handle cancel
			}
		}
	}

	private boolean checkCamera(){
		PackageManager pm = getActivity().getPackageManager();
		return pm.hasSystemFeature(PackageManager.FEATURE_CAMERA)
				&& (pm.checkPermission(Manifest.permission.CAMERA,getActivity().getPackageName())
					== PackageManager.PERMISSION_GRANTED);
	}

	@Override
	public boolean onPreferenceClick(Preference preference) {
		String key = preference.getKey();
		if (key.equals("button_code")) {
			Intent intent = new Intent(getActivity(),ScannerActivity.class);
			startActivityForResult(intent, 0);
		}
		return false;
	}

	public void onSharedPreferenceChanged(SharedPreferences preference, String key){
		if (key.equals("code_type")){
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
