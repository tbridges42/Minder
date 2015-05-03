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
 * Created by Tony on 4/18/2015.
 */
public abstract class EditorActivity extends Activity{

	Fragment mFragment;
	protected Bundle saved;
	protected String type;
	protected int frameID = R.layout.edit_frame;

	public abstract void save(View view);

	public abstract void cancel(View view);

	protected abstract void initialize();

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

		//noinspection SimplifiableIfStatement
		/*if (id == R.id.action_settings) {
			return true;
		}*/

		return super.onOptionsItemSelected(item);
	}
}
