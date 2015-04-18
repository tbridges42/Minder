package us.bridgeses.Minder.editor;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.widget.BaseAdapter;

import us.bridgeses.Minder.R;

public class StyleFragment extends PreferenceFragment implements 
        SharedPreferences.OnSharedPreferenceChangeListener{

    public static PreferenceFragment newInstance(){
        return new StyleFragment();
    }

    @Override
    public void onResume() {
        super.onResume();
        // Set up a listener whenever a key changes
        getPreferenceScreen().getSharedPreferences()
                .registerOnSharedPreferenceChangeListener(this);
    }

	public void onSharedPreferenceChanged(SharedPreferences preference, String key){
		if (key.equals("temp_vibrate")){
			CheckBoxPreference mPreference = (CheckBoxPreference) findPreference(key);
			super.findPreference("vibrate_repeat").setEnabled(mPreference.isChecked());
		}
		((BaseAdapter)getPreferenceScreen().getRootAdapter()).notifyDataSetChanged();
	}

    private void initSummaries(){
	    CheckBoxPreference vibrateRepeat = (CheckBoxPreference) super.findPreference("vibrate_repeat");
	    SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getActivity());
	    vibrateRepeat.setChecked(sharedPreferences.getBoolean("vibrate_repeat",false));
	    vibrateRepeat.setEnabled(sharedPreferences.getBoolean("temp_vibrate",false));
    }

    @Override
    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.style_preference);
        initSummaries();
    }
}
