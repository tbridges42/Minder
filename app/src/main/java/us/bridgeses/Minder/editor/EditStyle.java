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
        editor.putBoolean("fade",saved.getBoolean("fade", Reminder.FADEDEFAULT));
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
			saved.putBoolean("fade", sharedPreferences.getBoolean("fade", Reminder.FADEDEFAULT));
		}
	}

    @Override
	protected void initialize(){
	    type = "style";
    }
}
