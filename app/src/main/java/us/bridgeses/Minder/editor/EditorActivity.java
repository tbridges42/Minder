package us.bridgeses.Minder.editor;

import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentManager;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import us.bridgeses.Minder.R;

/**
 * Abstract class from which all editor classes are derived
 */
public abstract class EditorActivity extends Activity{

	Fragment mFragment;
	protected Bundle saved;
	protected String type;
	protected int frameID = R.layout.edit_frame;

	/**
	 * Verifies that any required settings have been selected, and if so returns RESULT_OK to the
	 * calling activity. Called when the save button is pressed.
	 * @param view is passed by the system when the save button is pressed.
	 */
	public abstract void save(View view);

	/**
	 * Restores settings to their original configuration, as saved in saved, and then returns
	 * RESULT_CANCELED to the calling activity.
	 * @param view is passed by the Android system when the cancel button is pressed
	 */
	public abstract void cancel(View view);

	protected abstract void initialize();

	/**
	 * This saves existing settings to saved, in order to be restored in the event the user cancels
	 * the edit
	 */
	protected abstract void saveState();

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		initialize();
		setContentView(frameID);
		saved = savedInstanceState;
		saveState();

		Fragment fragment = EditorFragmentFactory.newInstance(type);
		FragmentManager fragmentManager = getFragmentManager();
		mFragment = fragmentManager.findFragmentByTag(type);

		if (mFragment == null) {
			fragmentManager.beginTransaction().replace(R.id.reminder_frame, fragment,type).commit();
		}
	}


	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();

		switch (id)
		{
			case android.R.id.home:
			{
				cancel(null);
				return true;
			}
		}

		return super.onOptionsItemSelected(item);
	}
}
