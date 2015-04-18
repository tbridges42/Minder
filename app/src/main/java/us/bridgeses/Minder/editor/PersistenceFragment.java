package us.bridgeses.Minder.editor;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.preference.PreferenceScreen;
import android.widget.BaseAdapter;

import us.bridgeses.Minder.R;


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
				editor.putString("temp_code",data.getStringExtra("SCAN_RESULT"));
				editor.apply();
				super.findPreference("button_code").setSummary(getResources().getString(R.string.code_set));
			}
			if(resultCode == Activity.RESULT_CANCELED){
				//handle cancel
			}
		}
	}
	@Override
	public boolean onPreferenceClick(Preference preference) {
		String key = preference.getKey();
		if (key.equals("button_code")) {
			try {

				Intent intent = new Intent("com.google.zxing.client.android.SCAN");
				//intent.putExtra("SCAN_MODE", "QR_CODE_MODE"); // "PRODUCT_MODE for bar codes

				startActivityForResult(intent, 0);

			} catch (Exception e) {
                AlertDialog.Builder builder = new AlertDialog.Builder(getActivity() );

                builder.setTitle("No QR scanner");
                builder.setMessage("This function requires the ZXing Bar Code Scanner");
                builder.setPositiveButton("Play Store", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        Uri marketUri = Uri.parse("market://details?id=com.google.zxing.client.android");
                        Intent marketIntent = new Intent(Intent.ACTION_VIEW, marketUri);
                        startActivity(marketIntent);
                    }
                });
                builder.setNegativeButton("Cancel",new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {

                    }
                });
                builder.create();
                builder.show();
			}
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
		codeButton.setEnabled(codeType.isChecked());
		if (sharedPreferences.getString("button_code","").equals("")){
			super.findPreference("button_code").setSummary("");
		}
		else
			super.findPreference("button_code").setSummary(getResources().getString(R.string.code_set));
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
