package us.bridgeses.Minder.editor;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.View;

import us.bridgeses.Minder.model.Reminder;

import static us.bridgeses.Minder.model.Repeat.REPEAT_DATE_TYPE_DEFAULT;
import static us.bridgeses.Minder.model.Repeat.REPEAT_PERIOD_DEFAULT;
import static us.bridgeses.Minder.model.Repeat.REPEAT_TYPE_DEFAULT;

/**
 * The activity for displaying, editing and saving options related to the pattern by which
 *  a reminder will repeat
 */
public class EditRepeat extends EditorActivity{

    /**
     * Restores settings to their original configuration, as saved in saved, and then returns
     * RESULT_CANCELED to the calling activity.
     * @param view is passed by the Android system when the cancel button is pressed
     */
    @Override
    public void cancel(View view){
	    SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
	    SharedPreferences.Editor editor = sharedPreferences.edit();

	    //These four are EditTextPreferences and must be handled as strings
	    editor.putString("temp_days", saved.getString("temp_days",
                Integer.toString(REPEAT_PERIOD_DEFAULT)));
	    editor.putString("temp_weeks", saved.getString("temp_weeks",
                Integer.toString(REPEAT_PERIOD_DEFAULT)));
	    editor.putString("temp_months", saved.getString("temp_months",
                Integer.toString(REPEAT_PERIOD_DEFAULT)));
	    editor.putString("temp_years", saved.getString("temp_years",
                Integer.toString(REPEAT_PERIOD_DEFAULT)));

	    //These two are ListPreferences and must be handled as strings
	    editor.putString("temp_repeat_type", saved.getString("temp_repeat_type",
                Integer.toString(REPEAT_TYPE_DEFAULT)));
	    editor.putString("temp_monthly_type", saved.getString("temp_monthly_type",
                Integer.toString(REPEAT_DATE_TYPE_DEFAULT)));
	    editor.apply();

        Intent intent = new Intent();
        setResult(RESULT_CANCELED, intent);
        finish();
    }

    /**
     * Verifies that any required settings have been selected, and if so returns RESULT_OK to the
     * calling activity. Called when the save button is pressed.
     * @param view is passed by the system when the save button is pressed.
     */
    @Override
    public void save(View view){
        setResult(RESULT_OK);
        finish();
    }

    /**
     * This saves existing settings to saved, in order to be restored in the event the user cancels
     * the edit
     */
    @Override
    public void saveState(){
        if (saved == null) {
            saved = new Bundle();
            SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);

            //These four are EditTextPreferences and must be handled as strings
            saved.putString("temp_years", sharedPreferences.getString("temp_years",
                    Integer.toString(REPEAT_PERIOD_DEFAULT)));
            saved.putString("temp_days", sharedPreferences.getString("temp_days",
                    Integer.toString(REPEAT_PERIOD_DEFAULT)));
            saved.putString("temp_weeks", sharedPreferences.getString("temp_weeks",
                    Integer.toString(REPEAT_PERIOD_DEFAULT)));
            saved.putString("temp_months", sharedPreferences.getString("temp_months",
                    Integer.toString(REPEAT_PERIOD_DEFAULT)));

            //These two are ListPreferences and must be handled as strings
            saved.putString("temp_repeat_type", sharedPreferences.getString("temp_repeat_type",
                    Integer.toString(REPEAT_TYPE_DEFAULT)));
            saved.putString("temp_monthly_type", sharedPreferences.getString("temp_monthly_type",
                    Integer.toString(REPEAT_DATE_TYPE_DEFAULT)));
        }
    }

    @Override
    public void initialize(){
        type = "repeat";
    }
}
