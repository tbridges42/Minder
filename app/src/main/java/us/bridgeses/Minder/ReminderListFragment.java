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

import static us.bridgeses.Minder.persistence.RemindersContract.Reminder.COLUMN_DESCRIPTION;
import static us.bridgeses.Minder.persistence.RemindersContract.Reminder.COLUMN_ID;
import static us.bridgeses.Minder.persistence.RemindersContract.Reminder.COLUMN_NAME;

/**
 * This fragment retrieves all reminders from a ReminderDAO and displays them in a list
 * Created by Tony on 8/9/2014.
 */
public class ReminderListFragment extends ListFragment implements IFragment{

    private TaskCallbacks mCallbacks;
    private QueryTask mTask;
    protected SimpleCursorAdapter mAdapter;
    private Context context;
	protected Cursor cursor;
    ReminderDAO dao;

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
        context = getActivity();
    }

    /**
     * If the activity has been paused, we need to create a new query
     */
    @Override
    public void onResume() {
        super.onResume();
        if (mTask == null) {
            mTask = new QueryTask();
            mTask.execute();
        }
    }

    /**
     * This method will only be called once when the retained
     * Fragment is first created.
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Retain this fragment across configuration changes.
        setRetainInstance(true);

        String[] fromColumns = {COLUMN_NAME, COLUMN_DESCRIPTION, COLUMN_ID};
        int[] toViews = {R.id.list_name, R.id.list_description};

        mAdapter = new ReminderListAdapter(context,
                R.layout.item_reminder, null,
                fromColumns, toViews);

        this.setListAdapter(mAdapter);

        // Create and execute the background task.
        if (mTask == null){
            mTask = new QueryTask();
            mTask.execute();
        }
    }

    /**
     * When a list item is clicked, begin loading editreminder
     * @param l the listview
     * @param v the view
     * @param position the position in the list
     * @param id the id of the reminder that was clicked
     */
    @Override
    public void onListItemClick(ListView l, View v, int position, long id) {
        Intent intent = new Intent(context, EditReminder.class);
        intent.putExtra("id",Math.round(id));
        startActivity(intent);
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
     * This task queries the DAO for the list of reminders and updates the listadapter
     */
    private class QueryTask extends AsyncTask<Void, Integer, Void> {

        @Override
        protected void onPreExecute() {
            if (mCallbacks != null) {
                mCallbacks.onPreExecute();
            }
        }

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
                mCallbacks.onPostExecute();
            }
            mAdapter.swapCursor(cursor);
	        mAdapter.notifyDataSetChanged();
        }
    }
}
