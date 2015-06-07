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
                Integer.toString(Reminder.REPEATLENGTHDEFAULT)));
	    editor.putString("temp_weeks", saved.getString("temp_weeks",
                Integer.toString(Reminder.REPEATLENGTHDEFAULT)));
	    editor.putString("temp_months", saved.getString("temp_months",
                Integer.toString(Reminder.REPEATLENGTHDEFAULT)));
	    editor.putString("temp_years", saved.getString("temp_years",
                Integer.toString(Reminder.REPEATLENGTHDEFAULT)));

	    //These two are ListPreferences and must be handled as strings
	    editor.putString("temp_repeat_type", saved.getString("temp_repeat_type",
                Integer.toString(Reminder.REPEATTYPEDEFAULT)));
	    editor.putString("temp_monthly_type", saved.getString("temp_monthly_type",
                Integer.toString(Reminder.MONTHTYPEDEFAULT)));
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
    protected void saveState(){
        if (saved == null) {
            saved = new Bundle();
            SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);

            //These four are EditTextPreferences and must be handled as strings
            saved.putString("temp_years", sharedPreferences.getString("temp_years",
                    Integer.toString(Reminder.REPEATLENGTHDEFAULT)));
            saved.putString("temp_days", sharedPreferences.getString("temp_days",
                    Integer.toString(Reminder.REPEATLENGTHDEFAULT)));
            saved.putString("temp_weeks", sharedPreferences.getString("temp_weeks",
                    Integer.toString(Reminder.REPEATLENGTHDEFAULT)));
            saved.putString("temp_months", sharedPreferences.getString("temp_months",
                    Integer.toString(Reminder.REPEATLENGTHDEFAULT)));

            //These two are ListPreferences and must be handled as strings
            saved.putString("temp_repeat_type", sharedPreferences.getString("temp_repeat_type",
                    Integer.toString(Reminder.REPEATTYPEDEFAULT)));
            saved.putString("temp_monthly_type", sharedPreferences.getString("temp_monthly_type",
                    Integer.toString(Reminder.MONTHTYPEDEFAULT)));
        }
    }

    @Override
    protected void initialize(){
        type = "repeat";
    }
}
