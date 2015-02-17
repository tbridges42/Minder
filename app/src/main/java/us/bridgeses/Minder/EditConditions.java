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


public class EditConditions extends Activity {

	Reminder reminder;
	ConditionsFragment mFragment;
	private static final String TAG_LOCATION_FRAGMENT = "location_fragment";
	Bundle saved;

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

	public void cancel(View view) {
		SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
		SharedPreferences.Editor editor = sharedPreferences.edit();
		editor.putString("location_type", saved.getString("location_type", "0"));
		editor.putFloat("Latitude", saved.getFloat("Latitude", 0));
		editor.putFloat("Longitude", saved.getFloat("Longitude", 0));
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
			savedInstanceState.putString("location_type", sharedPreferences.getString("location_type", "0"));
			savedInstanceState.putFloat("Latitude", sharedPreferences.getFloat("Latitude", 0));
			savedInstanceState.putFloat("Longitude", sharedPreferences.getFloat("Longitude", 0));
		}
		saved = savedInstanceState;
		Intent intent = getIntent();
		Bundle incoming = intent.getExtras();
		if (incoming != null) {
			reminder = incoming.getParcelable("Reminder");
			if (reminder.getId()==-1){
				reminder = new Reminder();
			}
		}
		else
			reminder = new Reminder();

		ConditionsFragment fragment = ConditionsFragment.newInstance();
		FragmentManager fragmentManager = getFragmentManager();
		mFragment = (ConditionsFragment) fragmentManager.findFragmentByTag(TAG_LOCATION_FRAGMENT);

		if (mFragment == null) {
			fragmentManager.beginTransaction().replace(R.id.reminder_frame, fragment,TAG_LOCATION_FRAGMENT).commit();
		}
	}


	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.menu_edit_location, menu);
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
