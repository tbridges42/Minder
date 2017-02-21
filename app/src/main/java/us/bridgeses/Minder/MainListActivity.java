package us.bridgeses.Minder;

import android.app.Fragment;
import android.app.FragmentManager;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

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
        DataController.ActivityCallback, ReminderListViewFragment.ViewCallback {

    private static final String TAG_ASYNC_FRAGMENT = "Async_fragment";
    private static final String TAG_ABOUT_FRAGMENT = "About_fragment";
    public static final String TAG_DATA_FRAGMENT = "Data_fragment";
    private static final String TAG_AD_FRAGMENT = "Ad_fragment";
    private ReminderListViewFragment mReminderListFragment;
	private Fragment mAboutFragment;
    private DataController dataController;
    private TrackingController trackingController;
    private FragmentManager fragmentManager;
    private AdHandler adHandler;

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
        adHandler = new AdHandler();
        fragmentManager.beginTransaction().replace(R.id.adFrame,adHandler, TAG_AD_FRAGMENT).commit();
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

    //Called if the user creates a new reminder
    @Deprecated
    private void editReminder(View view) {
        Intent intent = new Intent(this, EditReminder.class);
        intent.putExtra("New",true);
        startActivity(intent);
    }
    //</editor-fold>
}
