package us.bridgeses.Minder;

import android.app.Activity;
import android.app.ListFragment;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;

import com.orhanobut.logger.Logger;

import us.bridgeses.Minder.editor.EditReminder;

/**
 * This fragment retrieves all reminders from a ReminderDAO and displays them in a list
 * Created by Tony on 8/9/2014.
 */
public class ReminderListFragment extends ListFragment{
    interface TaskCallbacks {
        void onPreExecute();
        void onProgressUpdate(int percent);
        void onCancelled();
        void onPostExecute();
    }

    private TaskCallbacks mCallbacks;
    private QueryTask mTask;
    protected SimpleCursorAdapter mAdapter;
    private Context context;
	protected Cursor cursor;
    ReminderDAO dao;

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
        if (mTask == null) {
            mTask = new QueryTask();
            mTask.execute();
        }
    }

    @Override
    public void onDestroy(){
        super.onDestroy();
    }

    /**
     * This method will only be called once when the retained
     * Fragment is first created.
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getActivity() != null) {
            context = getActivity();
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
    public void onPause() {
        super.onPause();
        mTask = null;
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

        SQLiteDatabase database;

        @Override
        protected void onPreExecute() {
            if (mCallbacks != null) {
                mCallbacks.onPreExecute();
            }
        }

        /**
         * Note that we do NOT call the callback object's methods
         * directly from the background thread, as this could result
         * in a race condition.
         */
        @Override
        protected Void doInBackground(Void... ignore) {
            dao = DaoFactory.getInstance().getDao(context);
            cursor = dao.getAndKeepOpen();
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
                Logger.d("Post Execute");
                mCallbacks.onPostExecute();
            }
//	        Logger.d("Cursor updated");
            mAdapter.swapCursor(cursor);
	        mAdapter.notifyDataSetChanged();
        }
    }

    private class LoadReminderTask extends AsyncTask<Integer, Integer, Void> {

        Reminder reminder;
        Intent intent = new Intent(context, EditReminder.class);

        @Override
        protected void onPreExecute() {
            if (mCallbacks != null) {
                mCallbacks.onPreExecute();
            }
        }

        /**
         * Note that we do NOT call the callback object's methods
         * directly from the background thread, as this could result
         * in a race condition.
         */
        @Override
        protected Void doInBackground(Integer... id) {
            reminder = Reminder.get(context,id[0]);
            intent.putExtra("Reminder", reminder);
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
            if (dao.isOpen()){
                dao.close();
            }
            if (mCallbacks != null) {
                mCallbacks.onPostExecute();
            }
            startActivity(intent);
        }
    }
}
