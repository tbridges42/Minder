package us.bridgeses.Minder.editor;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.View;

import us.bridgeses.Minder.model.Reminder;

import static us.bridgeses.Minder.model.Style.IMAGE_PATH_DEFAULT;
import static us.bridgeses.Minder.model.Style.LED_COLOR_DEFAULT;
import static us.bridgeses.Minder.model.Style.TEXT_COLOR_DEFAULT;

/**
 * The activity for displaying, editing and saving options related to the manner
 * in which the reminder is shown
 */
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

    /**
     * Restores settings to their original configuration, as saved in saved, and then returns
     * RESULT_CANCELED to the calling activity.
     * @param view is passed by the Android system when the cancel button is pressed
     */
	@Override
    public void cancel(View view) {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        SharedPreferences.Editor editor = sharedPreferences.edit();
		editor.putBoolean("temp_vibrate", saved.getBoolean("temp_vibrate", true));
		editor.putBoolean("vibrate_repeat", saved.getBoolean("vibrate_repeat", false));
		editor.putBoolean("led", saved.getBoolean("led", false));
        editor.putInt("led_color", saved.getInt("led_color", LED_COLOR_DEFAULT));
        editor.putString("image", saved.getString("image", IMAGE_PATH_DEFAULT));
        editor.putInt("font_color", saved.getInt("font_color", TEXT_COLOR_DEFAULT));
        editor.apply();
        Reminder.loadImage(this, sharedPreferences.getInt("temp_id", -1));
        userEnded = true;
        Intent intent = new Intent();
        setResult(RESULT_CANCELED, intent);
        finish();
    }

	@Override
    public void saveState(){
		if (saved == null) {
			saved = new Bundle();
			SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
			saved.putBoolean("temp_vibrate",
					sharedPreferences.getBoolean("temp_vibrate", true));
			saved.putBoolean("vibrate_repeat",
					sharedPreferences.getBoolean("vibrate_repeat", false));
			saved.putBoolean("led",
					sharedPreferences.getBoolean("led", false));
            saved.putInt("led_color",
                    sharedPreferences.getInt("led_color", LED_COLOR_DEFAULT));
            saved.putString("image",
                    sharedPreferences.getString("image", IMAGE_PATH_DEFAULT));
            saved.putInt("font_color",
                    sharedPreferences.getInt("font_color", TEXT_COLOR_DEFAULT));
		}
	}

    @Override
    public void initialize(){
	    type = "style";
    }
}
