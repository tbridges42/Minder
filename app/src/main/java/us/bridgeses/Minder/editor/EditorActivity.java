package us.bridgeses.Minder.editor;

import android.app.Fragment;
import android.app.FragmentManager;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

import us.bridgeses.Minder.R;
import us.bridgeses.Minder.util.vandy.LifecycleLoggingActivity;
import us.bridgeses.Minder.controllers.interfaces.Editor;

/**
 * Abstract class from which all editor classes are derived
 */
public abstract class EditorActivity extends LifecycleLoggingActivity implements Editor {

	Fragment mFragment;
	protected Bundle saved;
	protected String type;
	protected int frameID = R.layout.edit_frame;



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
