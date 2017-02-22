package us.bridgeses.Minder;

import android.app.AlertDialog;
import android.app.Fragment;
import android.app.FragmentManager;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.CheckBox;
import android.widget.CompoundButton;

import us.bridgeses.Minder.controllers.DataController;
import us.bridgeses.Minder.controllers.TrackingController;
import us.bridgeses.Minder.editor.EditReminder;
import us.bridgeses.Minder.util.vandy.LifecycleLoggingActivity;
import us.bridgeses.Minder.views.ReminderListViewFragment;
import us.bridgeses.Minder.views.interfaces.ReminderListView;

/**
 * The main Activity of Minder. Controls and provides Fragments
 */
public class MainListActivity extends LifecycleLoggingActivity implements
        DataController.ActivityCallback, ReminderListViewFragment.ViewCallback, CompoundButton.OnCheckedChangeListener {

    private static final String TAG_ASYNC_FRAGMENT = "Async_fragment";
    private static final String TAG_ABOUT_FRAGMENT = "About_fragment";
    public static final String TAG_DATA_FRAGMENT = "Data_fragment";
    private static final String TAG_AD_FRAGMENT = "Ad_fragment";
    // TODO: 2/21/2017 Does retaining these here result in leaks?
    private ReminderListViewFragment mReminderListFragment;
	private Fragment mAboutFragment;
    private DataController dataController;
    private TrackingController trackingController;
    private FragmentManager fragmentManager;

    //<editor-fold desc="DataController.ActivityCallback Methods">
    @Override
    public ReminderListView getListView() {
        return mReminderListFragment;
    }

    @Override
    public TrackingController getTracker() {
        return trackingController;
    }

    public void createEditor(long l) {
        if (l == -1) {
            editReminder(null);
        }
        else {
            Intent intent = new Intent(this, EditReminder.class);
            intent.putExtra("id",Math.round(l));
            startActivity(intent);
        }
    }
    //</editor-fold>

    //<editor-fold desc="ReminderListViewFragment.ViewCallback Methods">
    @Override
    public DataController getDataController() {
        return dataController;
    }

    @Override
    public void notifyReady() {
        dataController.loadAll();
    }

    //</editor-fold>

    //<editor-fold desc="Lifecycle Methods">

	@Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mainlist);

        createAdHandler();
        createToolbar();

        // TODO: Figure out how to break out tutorial

        findDataController();
        if (dataController == null) {
            createDataController();
        }
        findListView();
        if (mReminderListFragment == null) {
            setTracker();
            createListView();
        }
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        if (!preferences.getBoolean("do_not_display", false)) {
            displayAdNotice();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if(isFinishing() && dataController != null) {
            FragmentManager fm = getFragmentManager();
            fm.beginTransaction().remove(dataController).commit();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.action_about) {
            loadAbout();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed()
    {
        Log.d(TAG, "onBackPressed: ");
        fragmentManager = getFragmentManager();
        Fragment fragment = fragmentManager.findFragmentByTag(TAG_ABOUT_FRAGMENT);
        if (!(fragment instanceof AboutFragment)){
            finish();
        }
        fragmentManager.popBackStackImmediate();
        if (mReminderListFragment != null && mReminderListFragment.isAdded()) {
            Log.d(TAG, "onBackPressed: loading");
            dataController.loadAll();
        }
    }
    //</editor-fold>

    //<editor-fold desc="Private Methods">
    private void loadAbout() {
        if (fragmentManager == null) {
            fragmentManager = getFragmentManager();
        }
        if (mAboutFragment == null) {
            mAboutFragment = fragmentManager.findFragmentByTag(TAG_ABOUT_FRAGMENT);
            if (mAboutFragment == null) {
                mAboutFragment = new AboutFragment();
            }
        }
        if (!mAboutFragment.isAdded()) {
            fragmentManager.beginTransaction()
                    .replace(R.id.list,mAboutFragment,TAG_ABOUT_FRAGMENT)
                    .addToBackStack(null).commit();
        }
    }

    private void createToolbar() {
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
    }

    private void createAdHandler() {
        if (fragmentManager == null) {
            fragmentManager = getFragmentManager();
        }
        AdHandler adHandler = new AdHandler();
        fragmentManager.beginTransaction().replace(R.id.adFrame, adHandler, TAG_AD_FRAGMENT).commit();
    }

    private void createDataController() {
        if (fragmentManager == null) {
            fragmentManager = getFragmentManager();
        }
        dataController = new DataController();
        fragmentManager.beginTransaction()
                .add(dataController, TAG_DATA_FRAGMENT).commit();
    }

    private void createListView() {
        mReminderListFragment = new ReminderListViewFragment();
        fragmentManager.beginTransaction()
            .replace(R.id.list, mReminderListFragment,TAG_ASYNC_FRAGMENT)
            .addToBackStack(null).commit();
    }

    private void findDataController() {
        if (fragmentManager == null) {
            fragmentManager = getFragmentManager();
        }
        Fragment fragment = fragmentManager.findFragmentByTag(TAG_DATA_FRAGMENT);
        if (fragment instanceof DataController) {
            dataController = (DataController) fragment;
        }
    }

    private void findListView() {
        if (fragmentManager == null) {
            fragmentManager = getFragmentManager();
        }
        Fragment fragment = fragmentManager.findFragmentByTag(TAG_ASYNC_FRAGMENT);
        if (fragment instanceof ReminderListViewFragment){
            mReminderListFragment = (ReminderListViewFragment) fragment;
        }
    }

    private void setTracker(){
        trackingController = TrackingController.getInstance(this);
    }

    private void displayAdNotice() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Important Notice");
        builder.setMessage(getString(R.string.advertising_notice));
        builder.setNeutralButton(getString(R.string.accept),
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                });
        View doNotDisplay = View.inflate(this, R.layout.checkbox_do_not_display, null);
        ((CheckBox)doNotDisplay.findViewById(R.id.do_not_display)).setOnCheckedChangeListener(this);
        builder.setView(doNotDisplay);
        builder.show();
    }

    //Called if the user creates a new reminder
    private void editReminder(@SuppressWarnings("unused") View view) {
        Intent intent = new Intent(this, EditReminder.class);
        intent.putExtra("New",true);
        startActivity(intent);
    }

    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(this).edit();
        editor.putBoolean("do_not_display", isChecked);
        editor.apply();
    }
    //</editor-fold>
}
