package us.bridgeses.Minder.editor;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.View;
import android.widget.Toast;

import us.bridgeses.Minder.R;
import us.bridgeses.Minder.model.Reminder;

/**
 *  The activity for displaying, editing and saving options related to how persistent the reminder
 *  will be in getting the user's attention
 */
public class EditPersistence extends EditorActivity {

	/**
	 * Verifies that any required settings have been selected, and if so returns RESULT_OK to the
	 * calling activity. Called when the save button is pressed.
	 * @param view is passed by the system when the save button is pressed.
	 */
	@Override
	public void save(View view) {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        if ((sharedPreferences.getBoolean("code_type",false)) &&
                (sharedPreferences.getString("temp_code","").equals(""))){
            String toastText = getResources().getString(R.string.invalid_code);
            int duration = Toast.LENGTH_SHORT;
            Toast toast = Toast.makeText(this, toastText, duration);
            toast.show();
        }
        else {
            Intent intent = new Intent();
            setResult(RESULT_OK, intent);
            finish();
        }
	}

	/**
	 * Restores settings to their original configuration, as saved in saved, and then returns
	 * RESULT_CANCELED to the calling activity.
	 * @param view is passed by the Android system when the cancel button is pressed
	 */
	@Override
	public void cancel(View view) {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString("code_button", saved.getString("code_button", Reminder.QRDEFAULT));
        editor.putBoolean("code_type", saved.getBoolean("code_type", Reminder.NEEDQRDEFAULT));
        editor.putBoolean("out_loud", saved.getBoolean("out_loud", Reminder.VOLUMEOVERRIDEDEFAULT));
        editor.putBoolean("display_screen", saved.getBoolean("display_screen", Reminder.DISPLAYSCREENDEFAULT));
        editor.putBoolean("wake_up", saved.getBoolean("wake_up", Reminder.WAKEUPDEFAULT));
        editor.putBoolean("fade", saved.getBoolean("fade", Reminder.FADEDEFAULT));
		editor.putInt("volume", saved.getInt("volume", Reminder.VOLUMEDEFAULT));
        editor.apply();
		Intent intent = new Intent();
		setResult(RESULT_CANCELED, intent);
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
			saved.putString("button_code",
					sharedPreferences.getString("button_code", Reminder.QRDEFAULT));
			saved.putBoolean("code_type",
					sharedPreferences.getBoolean("code_type", Reminder.NEEDQRDEFAULT));
			saved.putBoolean("out_loud",
					sharedPreferences.getBoolean("out_loud", Reminder.VOLUMEOVERRIDEDEFAULT));
			saved.putBoolean("display_screen",
					sharedPreferences.getBoolean("display_screen", Reminder.DISPLAYSCREENDEFAULT));
			saved.putBoolean("wake_up",
					sharedPreferences.getBoolean("wake_up", Reminder.WAKEUPDEFAULT));
			saved.putBoolean("fade",
					sharedPreferences.getBoolean("fade", Reminder.FADEDEFAULT));
			saved.putInt("volume",sharedPreferences.getInt("volume", 80));
		}
	}
	
	@Override
	public void initialize(){
		type = "persistence";
	}
}
