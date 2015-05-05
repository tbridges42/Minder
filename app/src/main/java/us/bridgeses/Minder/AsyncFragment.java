package us.bridgeses.Minder;

import android.app.Activity;
import android.app.ListFragment;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;

import us.bridgeses.Minder.editor.EditReminder;

/**
 * Created by Tony on 8/9/2014.
 */
public class AsyncFragment extends ListFragment{
    static interface TaskCallbacks {
        void onPreExecute();
        void onProgressUpdate(int percent);
        void onCancelled();
        void onPostExecute();
    }

    private TaskCallbacks mCallbacks;
    private QueryTask mTask;
    protected SimpleCursorAdapter mAdapter;
    private Context context;
	private ProgressDialog progressDialog;
	protected Cursor cursor;

	public void update(){

	}

    /**
     * Hold a reference to the parent Activity so we can report the
     * task's current progress and results. The Android framework
     * will pass us a reference to the newly created Activity after
     * each configuration change.
     */
    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        mCallbacks = (TaskCallbacks) activity;
    }

    @Override
    public void onResume() {
        super.onResume();
        if (progressDialog != null)
            progressDialog.dismiss();
        mTask = new QueryTask();
        mTask.execute();
    }

    /**
     * This method will only be called once when the retained
     * Fragment is first created.
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getActivity() != null) {
            context = getActivity().getApplicationContext();
        }

        // Retain this fragment across configuration changes.
        setRetainInstance(true);

        String[] fromColumns = {ReminderDBHelper.COLUMN_NAME, ReminderDBHelper.COLUMN_DESCRIPTION, ReminderDBHelper.COLUMN_ID};
        int[] toViews = {R.id.list_name, R.id.list_description};

        mAdapter = new ReminderListAdapter(context,
                R.layout.item_reminder, null,
                fromColumns, toViews,getFragmentManager());


        this.setListAdapter(mAdapter);

        // Create and execute the background task.
        if (mTask == null){
            mTask = new QueryTask();
            mTask.execute();
        }
    }

    @Override
    public void onListItemClick(ListView l, View v, int position, long id) {


        LoadReminderTask mTask = new LoadReminderTask();
        mTask.execute((int) id);
    }

    /**
     * Set the callback to null so we don't accidentally leak the
     * Activity instance.
     */
    @Override
    public void onDetach() {
        super.onDetach();
        mCallbacks = null;
    }

    /**
     * A dummy task that performs some (dumb) background work and
     * proxies progress updates and results back to the Activity.
     *
     * Note that we need to check if the callbacks are null in each
     * method in case they are invoked after the Activity's and
     * Fragment's onDestroy() method have been called.
     */
    private class QueryTask extends AsyncTask<Void, Integer, Void> {

        ReminderDBHelper dbHelper;
        SQLiteDatabase database;

        @Override
        protected void onPreExecute() {
            if (mCallbacks != null) {
                mCallbacks.onPreExecute();
                dbHelper = ReminderDBHelper.getInstance(context);
                database = dbHelper.openDatabase();
            }
        }

        /**
         * Note that we do NOT call the callback object's methods
         * directly from the background thread, as this could result
         * in a race condition.
         */
        @Override
        protected Void doInBackground(Void... ignore) {
            if (database.isOpen()) {
                cursor = Reminder.getCursor(database);
            }
            return null;
        }

        @Override
        protected void onProgressUpdate(Integer... percent) {
            if (mCallbacks != null) {
                mCallbacks.onProgressUpdate(percent[0]);
            }
        }

        @Override
        protected void onCancelled() {
            if (mCallbacks != null) {
                mCallbacks.onCancelled();
            }
        }

        @Override
        protected void onPostExecute(Void ignore) {
            if (mCallbacks != null) {

                // mCallbacks.onPostExecute(cursor);
            }
//	        Logger.d("Cursor updated");
            mAdapter.swapCursor(cursor);
	        mAdapter.notifyDataSetChanged();
        }
    }

    private class LoadReminderTask extends AsyncTask<Integer, Integer, Void> {

        Reminder reminder;
        ReminderDBHelper dbHelper;
        SQLiteDatabase database;
        Intent intent = new Intent(context, EditReminder.class);

        @Override
        protected void onPreExecute() {
            if (mCallbacks != null) {
                mCallbacks.onPreExecute();
                dbHelper = ReminderDBHelper.getInstance(context);
                database = dbHelper.openDatabase();
	            progressDialog = new ProgressDialog(getActivity());
	            progressDialog.setIndeterminate(true);
	            progressDialog.setTitle("");
	            progressDialog.setMessage(getResources().getString(R.string.loading));
	            progressDialog.show();
            }
        }

        /**
         * Note that we do NOT call the callback object's methods
         * directly from the background thread, as this could result
         * in a race condition.
         */
        @Override
        protected Void doInBackground(Integer... id) {
            if (database.isOpen()) {
                reminder = Reminder.getReminder(database,id[0]);
            }
            Bundle bundle = new Bundle();
            bundle.putParcelable("Reminder", reminder);
            intent.putExtras(bundle);
            intent.setExtrasClassLoader(Reminder.class.getClassLoader());
            return null;
        }

        @Override
        protected void onProgressUpdate(Integer... percent) {
            if (mCallbacks != null) {
                mCallbacks.onProgressUpdate(percent[0]);
            }

        }

        @Override
        protected void onCancelled() {
            if (mCallbacks != null) {
                mCallbacks.onCancelled();
            }
        }

        @Override
        protected void onPostExecute(Void ignore) {
            if (mCallbacks != null) {

                mCallbacks.onPostExecute();
            }
	        if (progressDialog != null) {
		        progressDialog.dismiss();
		        progressDialog = null;
	        }
            startActivity(intent);
        }
    }
}
