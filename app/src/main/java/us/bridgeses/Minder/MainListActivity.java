package us.bridgeses.Minder;

import android.app.Fragment;
import android.app.FragmentManager;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import us.bridgeses.Minder.controllers.DataController;
import us.bridgeses.Minder.controllers.TrackingController;
import us.bridgeses.Minder.editor.EditReminder;
import us.bridgeses.Minder.exporter.ExportActivity;
import us.bridgeses.Minder.exporter.ImportActivity;
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

        findListView();
        findDataController();

        if (mReminderListFragment == null) {
            setTracker();
            createListView();
        }

        if (dataController == null) {
            dataController = new DataController();
            createDataController();
        }

    }

    @Override
    public void onResume() {
        super.onResume();
        if (fragmentManager == null) {
            fragmentManager = getFragmentManager();
        }
        Fragment fragment = fragmentManager.findFragmentByTag(TAG_ASYNC_FRAGMENT);
        if (fragment instanceof ReminderListViewFragment){
            mReminderListFragment = (ReminderListViewFragment) fragment;
        }
        else{
            if (fragment instanceof AboutFragment){
                mAboutFragment = fragment;
            }
        }
        // If the Fragment is non-null, then it is currently being
        // retained across a configuration change.
        if (mReminderListFragment == null) {
            createListView();
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
    //</editor-fold>

    //<editor-fold desc="Private Methods">
    private void createToolbar() {
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
    }

    private void createAdHandler() {
        adHandler = new AdHandler();
        adHandler.initialize(getApplicationContext());
        adHandler.setUp(findViewById(R.id.adView));
    }

    private void createDataController() {
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

    private void createDefaultEditor() {

    }
    //</editor-fold>
}
