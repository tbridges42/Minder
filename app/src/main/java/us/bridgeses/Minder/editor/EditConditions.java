package us.bridgeses.Minder.editor;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.View;
import android.widget.Toast;

import us.bridgeses.Minder.R;
import us.bridgeses.Minder.Reminder;


public class EditConditions extends EditorActivity {

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
		}
		else {
			Intent intent = new Intent();
			setResult(RESULT_OK, intent);
			finish();
		}
	}

	@Override
	public void cancel(View view) {
		SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
		SharedPreferences.Editor editor = sharedPreferences.edit();
		editor.putString("location_type", saved.getString("location_type", "-1"));
		editor.putFloat("Latitude",
                saved.getFloat("Latitude",(float) Reminder.LOCATIONDEFAULT.latitude));
		editor.putFloat("Longitude",
                saved.getFloat("Longitude", (float) Reminder.LOCATIONDEFAULT.longitude));
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
			saved.putString("location_type",
					sharedPreferences.getString("location_type", "-1"));
			saved.putFloat("Latitude",
					sharedPreferences.getFloat("Latitude", (float)Reminder.LOCATIONDEFAULT.latitude));
			saved.putFloat("Longitude",
					sharedPreferences.getFloat("Longitude", (float)Reminder.LOCATIONDEFAULT.longitude));
		}
	}

	@Override
	protected void initialize(){
		type = "conditions";
	}
}
