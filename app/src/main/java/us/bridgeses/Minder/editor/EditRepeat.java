package us.bridgeses.Minder.editor;

import android.app.Activity;
import android.app.FragmentManager;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.View;

import us.bridgeses.Minder.R;
import us.bridgeses.Minder.Reminder;

/**
 * Created by Tony on 9/13/2014.
 */
public class EditRepeat extends Activity{

    Reminder reminder;
    RepeatFragment mFragment;
    private static final String TAG_REPEAT_FRAGMENT = "repeat_fragment";
	Bundle saved;

    public void cancel(View view){
        Intent intent = new Intent();
	    SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
	    SharedPreferences.Editor editor = sharedPreferences.edit();

	    //These four are EditTextPreferences and must be handled as strings
	    editor.putString("temp_days", saved.getString("temp_days", "0"));
	    editor.putString("temp_weeks", saved.getString("temp_weeks", "0"));
	    editor.putString("temp_months", saved.getString("temp_months", "0"));
	    editor.putString("temp_years", saved.getString("temp_years", "0"));

	    //These two are ListPreferences and must be handled as strings
	    editor.putString("temp_repeat_type", saved.getString("temp_repeat_type", "0"));
	    editor.putString("temp_monthly_type", saved.getString("temp_monthly_type", "0"));
	    editor.apply();
        setResult(RESULT_CANCELED, intent);
        finish();
    }

    public void save(View view){
        setResult(RESULT_OK);
        finish();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.repeat_preference);

	    if (savedInstanceState == null) {
		    savedInstanceState = new Bundle();
		    SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
		    

		    //These four are EditTextPreferences and must be handled as strings
		    savedInstanceState.putString("temp_years", sharedPreferences.getString("temp_years", "0"));
		    savedInstanceState.putString("temp_days", sharedPreferences.getString("temp_days", "0"));
		    savedInstanceState.putString("temp_weeks", sharedPreferences.getString("temp_weeks", "0"));
		    savedInstanceState.putString("temp_months", sharedPreferences.getString("temp_months", "0"));

		    //These two are ListPreferences and must be handled as strings
		    savedInstanceState.putString("temp_repeat_type", sharedPreferences.getString("temp_repeat_type", "0"));
		    savedInstanceState.putString("temp_monthly_type", sharedPreferences.getString("temp_monthly_type", "0"));
	    }
	    saved = savedInstanceState;

        RepeatFragment fragment = RepeatFragment.newInstance();
        FragmentManager fragmentManager = getFragmentManager();
        mFragment = (RepeatFragment) fragmentManager.findFragmentByTag(TAG_REPEAT_FRAGMENT);

        if (mFragment == null) {
            fragmentManager.beginTransaction().replace(R.id.reminder_frame, fragment,TAG_REPEAT_FRAGMENT).commit();
        }
    }
}
