package us.bridgeses.Minder.editor;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.View;

import us.bridgeses.Minder.Reminder;

public class EditStyle extends EditorActivity {

	@Override
    public void save(View view) {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        Intent intent = new Intent();
        setResult(RESULT_OK, intent);
        finish();
    }

	@Override
    public void cancel(View view) {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        SharedPreferences.Editor editor = sharedPreferences.edit();
		editor.putBoolean("temp_vibrate", saved.getBoolean("temp_vibrate", Reminder.VIBRATEDEFAULT));
		editor.putBoolean("vibrate_repeat", saved.getBoolean("vibrate_repeat", Reminder.VIBRATEREPEATDEFAULT));
		editor.putBoolean("led", saved.getBoolean("led", Reminder.LEDDEFAULT));
        editor.apply();
        Intent intent = new Intent();
        setResult(RESULT_CANCELED, intent);
        finish();
    }

	@Override
	protected void saveState(){
		if (saved == null) {
			saved = new Bundle();
			SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
			saved.putBoolean("temp_vibrate",
					sharedPreferences.getBoolean("temp_vibrate", Reminder.VIBRATEDEFAULT));
			saved.putBoolean("vibrate_repeat",
					sharedPreferences.getBoolean("vibrate_repeat", Reminder.VIBRATEREPEATDEFAULT));
			saved.putBoolean("led",
					sharedPreferences.getBoolean("led", Reminder.LEDDEFAULT));
		}
	}

    @Override
	protected void initialize(){
	    type = "style";
    }
}
