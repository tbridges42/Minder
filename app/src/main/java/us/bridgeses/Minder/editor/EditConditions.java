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
 *  The activity for displaying, editing and saving options related to the conditions under which
 *  a reminder will fire
 */
public class EditConditions extends EditorActivity {

    /**
     * Verifies that any required settings have been selected, and if so returns RESULT_OK to the
     * calling activity. Called when the save button is pressed.
     * @param view is passed by the system when the save button is pressed.
     */
	@Override
	public void save(View view) {
		SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
		if ((!sharedPreferences.getString("location_type","0").equals("0")) &&
				(sharedPreferences.getFloat("Latitude",0)==0) &&
				(sharedPreferences.getFloat("Longitude",0)==0)){
			String toastText = getResources().getString(R.string.invalid_location);
			int duration = Toast.LENGTH_SHORT;
			Toast toast = Toast.makeText(this, toastText, duration);
			toast.show();
            return;
		}
		if ((sharedPreferences.getBoolean("wifi",false)) &&
                sharedPreferences.getString("ssid","").equals("")){
            String toastText = getResources().getString(R.string.invalid_ssid);
            int duration = Toast.LENGTH_SHORT;
            Toast toast = Toast.makeText(this, toastText, duration);
            toast.show();
            return;
        }
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
		
		//This is a ListPreference, and must be treated as a string
		editor.putString("location_type", saved.getString("location_type", "0"));
		
		editor.putFloat("Latitude",
                saved.getFloat("Latitude",(float) Reminder.LOCATIONDEFAULT.latitude));
		editor.putFloat("Longitude",
                saved.getFloat("Longitude", (float) Reminder.LOCATIONDEFAULT.longitude));

        editor.putBoolean("wifi",
                saved.getBoolean("wifi", Reminder.WIFIDEFAULT));
        editor.putString("ssid",
                saved.getString("ssid", Reminder.SSIDDEFAULT));
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
			
			//This is a ListPreference, and must be treated as a string
			saved.putString("location_type",
					sharedPreferences.getString("location_type", "0"));
			
			saved.putFloat("Latitude",
					sharedPreferences.getFloat("Latitude", (float)Reminder.LOCATIONDEFAULT.latitude));
			saved.putFloat("Longitude",
					sharedPreferences.getFloat("Longitude", (float)Reminder.LOCATIONDEFAULT.longitude));

            saved.putBoolean("wifi",
                    sharedPreferences.getBoolean("wifi", Reminder.WIFIDEFAULT));
            saved.putString("ssid",
                    sharedPreferences.getString("ssid",Reminder.SSIDDEFAULT));
		}
	}

	@Override
	public void initialize(){
		type = "conditions";
	}
}
