package us.bridgeses.Minder.editor;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.View;
import android.widget.Toast;

import java.io.File;

import us.bridgeses.Minder.Reminder;

public class EditStyle extends EditorActivity {

    public static final String tempFile = "temp";
    private boolean userEnded = false;

    /**
     * Verifies that any required settings have been selected, and if so returns RESULT_OK to the
     * calling activity. Called when the save button is pressed.
     * @param view is passed by the system when the save button is pressed.
     */
	@Override
    public void save(View view) {
        userEnded = true;
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
        editor.putInt("led_color", saved.getInt("led_color", Reminder.LEDCOLORDEFAULT));
        editor.putString("image", saved.getString("image", Reminder.IMAGEDEFAULT));
        editor.putInt("font_color", saved.getInt("font_color", Reminder.TEXTCOLORDEFAULT));
        editor.apply();
        Reminder.loadImage(this,sharedPreferences.getInt("temp_id",-1));
        userEnded = true;
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
            saved.putInt("led_color",
                    sharedPreferences.getInt("led_color", Reminder.LEDCOLORDEFAULT));
            saved.putString("image",
                    sharedPreferences.getString("image", Reminder.IMAGEDEFAULT));
            saved.putInt("font_color",
                    sharedPreferences.getInt("font_color", Reminder.TEXTCOLORDEFAULT));
		}
	}

    @Override
	protected void initialize(){
	    type = "style";
    }
}
