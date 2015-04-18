package us.bridgeses.Minder.editor;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.widget.BaseAdapter;

import us.bridgeses.Minder.R;
import us.bridgeses.Minder.Reminder;

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

        ((BaseAdapter)getPreferenceScreen().getRootAdapter()).notifyDataSetChanged();
    }

    private void initSummaries(){
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getActivity());
        CheckBoxPreference fade = (CheckBoxPreference) super.findPreference("fade");
        fade.setChecked(sharedPreferences.getBoolean("fade", Reminder.FADEDEFAULT));
    }

    @Override
    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.style_preference);
        initSummaries();
    }
}
