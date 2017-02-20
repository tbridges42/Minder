package us.bridgeses.Minder.editor;

import android.app.Fragment;
import android.app.FragmentManager;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;

import us.bridgeses.Minder.AdHandler;
import us.bridgeses.Minder.R;
import us.bridgeses.Minder.util.vandy.LifecycleLoggingActivity;
import us.bridgeses.Minder.controllers.interfaces.Editor;

/**
 * Abstract class from which all editor classes are derived
 */
public abstract class EditorActivity extends LifecycleLoggingActivity implements Editor {


	private static final String TAG_AD_FRAGMENT = "Ad_fragment";
	Fragment mFragment;
	protected Bundle saved;
	protected String type;
	protected int frameID = R.layout.edit_frame;
	private AdHandler adHandler;
	protected FragmentManager fragmentManager;



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
		Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
		setSupportActionBar(toolbar);
		createAdHandler();

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

	private void createAdHandler() {
		if (fragmentManager == null) {
			fragmentManager = getFragmentManager();
		}
		adHandler = new AdHandler();
		fragmentManager.beginTransaction().add(adHandler, TAG_AD_FRAGMENT).commit();
	}
}
