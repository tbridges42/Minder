package us.bridgeses.Minder;

import android.app.Activity;
import android.app.FragmentManager;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

public class EditPersistence extends Activity {

	PersistenceFragment mFragment;
    Bundle saved;
	private static final String TAG_LOCATION_FRAGMENT = "location_fragment";

	public void save(View view) {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        if ((sharedPreferences.getBoolean("code_type",false)) &&
                (sharedPreferences.getString("temp_code","").equals(""))){
            String toastText = "A code must be set to use Code Restriction";
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

	public void cancel(View view) {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString("code_button", saved.getString("code_button", Reminder.QRDEFAULT));
        editor.putBoolean("code_type", saved.getBoolean("code_type", Reminder.NEEDQRDEFAULT));
        editor.putBoolean("out_loud", saved.getBoolean("out_loud", Reminder.VOLUMEOVERRIDEDEFAULT));
        editor.putBoolean("display_screen", saved.getBoolean("display_screen", Reminder.DISPLAYSCREENDEFAULT));
        editor.putBoolean("wake_up", saved.getBoolean("wake_up", Reminder.WAKEUPDEFAULT));
        editor.apply();
		Intent intent = new Intent();
		setResult(RESULT_CANCELED, intent);
		finish();
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.edit_frame);

        if (savedInstanceState == null) {
            savedInstanceState = new Bundle();
            SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
            savedInstanceState.putString("button_code",
                    sharedPreferences.getString("button_code", Reminder.QRDEFAULT));
            savedInstanceState.putBoolean("code_type",
                    sharedPreferences.getBoolean("code_type", Reminder.NEEDQRDEFAULT));
            savedInstanceState.putBoolean("out_loud",
                    sharedPreferences.getBoolean("out_loud", Reminder.VOLUMEOVERRIDEDEFAULT));
            savedInstanceState.putBoolean("display_screen",
                    sharedPreferences.getBoolean("display_screen", Reminder.DISPLAYSCREENDEFAULT));
            savedInstanceState.putBoolean("wake_up",
                    sharedPreferences.getBoolean("wake_up", Reminder.WAKEUPDEFAULT));
        }
        saved = savedInstanceState;

		PersistenceFragment fragment = (PersistenceFragment) PersistenceFragment.newInstance();
		FragmentManager fragmentManager = getFragmentManager();
		mFragment = (PersistenceFragment) fragmentManager.findFragmentByTag(TAG_LOCATION_FRAGMENT);

		if (mFragment == null) {
			fragmentManager.beginTransaction().replace(R.id.reminder_frame, fragment,TAG_LOCATION_FRAGMENT).commit();
		}
	}


	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.menu_edit_persistence, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();

		//noinspection SimplifiableIfStatement
		/*if (id == R.id.action_settings) {
			return true;
		}*/

		return super.onOptionsItemSelected(item);
	}
}
