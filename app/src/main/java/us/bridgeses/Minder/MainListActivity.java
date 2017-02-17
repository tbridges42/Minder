package us.bridgeses.Minder;

import android.app.DialogFragment;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import us.bridgeses.Minder.controllers.DataController;
import us.bridgeses.Minder.controllers.TrackingController;
import us.bridgeses.Minder.editor.EditReminder;
import us.bridgeses.Minder.exporter.ExportActivity;
import us.bridgeses.Minder.exporter.ImportActivity;
import us.bridgeses.Minder.util.ConfirmDialogFragment;
import us.bridgeses.Minder.util.vandy.LifecycleLoggingActivity;
import us.bridgeses.Minder.views.ReminderListViewFragment;

/**
 * Created by Tony on 8/8/2014.
 */
public class MainListActivity extends LifecycleLoggingActivity implements TaskCallbacks,
        ConfirmDialogFragment.NoticeDialogListener,ReminderListAdapter.ListClicksListener{
// TODO: Investigate crash when rotating

    private static final String TAG_ASYNC_FRAGMENT = "Async_fragment";
    private static final String TAG_ABOUT_FRAGMENT = "About_fragment";
    private Fragment mReminderListFragment;
	private Fragment mAboutFragment;
    private DataController dataController;
    private TrackingController trackingController;
	private Boolean firstRun;
    private FragmentManager fragmentManager;
    private ProgressDialog progressDialog;
    private AdHandler adHandler;

    @Override
    public void SkipClick(int id){
        ConfirmDialogFragment df = ConfirmDialogFragment.newInstance("Skip Reminder",
                "Skip the next instance of this Reminder","Skip",
                getResources().getString(R.string.edit_cancel),id);
        df.show(fragmentManager, "SkipDialog");
    }

    @Override
    public void IconClick(int id){
    }

	@Override
	public void onDialogNegativeClick(DialogFragment dialog) {

	}

	@Override
	public void onDialogPositiveClick(DialogFragment dialog, int id) {
		dataController.skipNext(id);
	}

	private void setTracker(){
		trackingController = TrackingController.getInstance(this);
	}

    //Called if the user creates a new reminder
    @Deprecated
    public void editReminder(View view) {
        Intent intent = new Intent(this, EditReminder.class);
        intent.putExtra("New",true);
        startActivity(intent);
    }

    public void onNewClicked(View view) {
        createEditor(-1L);
    }

    private void createEditor(long l) {

    }

    @Override
	public void onBackPressed()
	{
		fragmentManager = getFragmentManager();
        Fragment fragment = fragmentManager.findFragmentByTag(TAG_ABOUT_FRAGMENT);
        if (!(fragment instanceof AboutFragment)){
            finish();
        }
		fragmentManager.popBackStack();
	}



	@Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mainlist);

        SharedPreferences defaultPreferences = getApplication().
                getSharedPreferences("Defaults", Context.MODE_PRIVATE);
        firstRun = defaultPreferences.getBoolean("first_run-0.7.3",true);

        // TODO: Figure out how to break out tutorial
        // TODO: Break out preference handling
        if (firstRun){
            SharedPreferences.Editor editor = defaultPreferences.edit();
            Reminder reminder = Reminder.reminderFactory(this);
            Reminder.reminderToPreference(this,defaultPreferences, reminder);

            editor.putBoolean("first_run-0.7.1", false);
            editor.apply();
            editor =  PreferenceManager.getDefaultSharedPreferences(this).edit();
            editor.clear().apply();
        }

        fragmentManager = getFragmentManager();
	    Fragment fragment = fragmentManager.findFragmentByTag(TAG_ASYNC_FRAGMENT);
	    if (fragment instanceof ReminderListFragment){
		    mReminderListFragment = fragment;
	    }
	    else{
		    if (fragment instanceof AboutFragment){
			    mAboutFragment = fragment;
		    }
	    }
        // If the Fragment is non-null, then it is currently being
        // retained across a configuration change.
        if (fragment == null) {
	        // create new fragment
            setTracker();
            mReminderListFragment = new ReminderListViewFragment();
	        fragmentManager.beginTransaction()
                    .replace(R.id.list, mReminderListFragment,TAG_ASYNC_FRAGMENT)
                    .addToBackStack(null).commit();
        }
        adHandler = new AdHandler();
        adHandler.initialize(getApplicationContext());
        adHandler.setUp(findViewById(R.id.adView));
    }

    @Override
    public void onResume() {
        super.onResume();
        if (fragmentManager == null) {
            fragmentManager = getFragmentManager();
        }
	    Fragment fragment = fragmentManager.findFragmentByTag(TAG_ASYNC_FRAGMENT);
	    if (fragment instanceof ReminderListFragment){
		    mReminderListFragment = fragment;
	    }
	    else{
		    if (fragment instanceof AboutFragment){
			    mAboutFragment = fragment;
		    }
	    }
        // If the Fragment is non-null, then it is currently being
        // retained across a configuration change.
        if (mReminderListFragment == null) {
            mReminderListFragment = new ReminderListFragment();
            fragmentManager.beginTransaction()
                    .replace(R.id.list, (Fragment) mReminderListFragment,TAG_ASYNC_FRAGMENT)
                    .addToBackStack(null).commit();
            // TODO: Conditionally add editor fragment based on screen layout
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        switch (item.getItemId()) {
            case R.id.defaults:
                createDefaultEditor();
                return true;
            case R.id.action_export:
                new ExportActivity(this).export();
                return true;
            case R.id.action_import:
                new ImportActivity(this).importBackup();
                // TODO: Figure out how to refresh the list after
                return true;
	        case R.id.action_about:
				FragmentManager fm = getFragmentManager();
		        mAboutFragment = new AboutFragment();
		        fm.beginTransaction().replace(R.id.list,(Fragment)mAboutFragment,TAG_ABOUT_FRAGMENT).addToBackStack(null).commit();
		        return true;
            case R.id.action_new:
                editReminder(findViewById(android.R.id.content));
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }

    }

    private void createDefaultEditor() {

    }

    @Override
    public void onPreExecute() {
        progressDialog = ProgressDialog.show(this, "", "Loading. . .", true, true);
    }

    @Override
    public void onCancelled() {
        if (progressDialog != null) {
            progressDialog.dismiss();
            progressDialog = null;
        }
    }

    @Override
    public void onPostExecute() {
        if (progressDialog != null) {
            progressDialog.dismiss();
            progressDialog = null;
        }
    }

    @Override
    public void onProgressUpdate(int percent) {

    }
}
