package us.bridgeses.Minder.adapters;

import android.support.v7.util.SortedList;
import android.support.v7.widget.RecyclerView;
import android.util.Log;

import us.bridgeses.Minder.model.Reminder;

/**
 * Created by tbrid on 2/15/2017.
 */

public class ReminderSorter extends SortedList.Callback<Reminder> {

    private static final String TAG = "ReminderSorter";

    private RecyclerView.Adapter adapter;

    public ReminderSorter(RecyclerView.Adapter adapter) {
        this.adapter = adapter;
    }

    /**
     * Compare two Reminders. The standard sort is active reminders should always be sorted before
     * inactive reminders, and if both are active or both are inactive, the reminder that occurs
     * first chronologically should come first
     * @param o1
     * @param o2
     * @return -1 if 01 should come before 02, 1 if 02 should come before 01, or 0 if they are equal
     */
    public int compare(Reminder o1, Reminder o2) {
        if (o1.getActive() && !o2.getActive()) {
            Log.d(TAG, "compare: Not equal");
            return -1;
        }
        if (!o1.getActive() && o2.getActive()) {
            Log.d(TAG, "compare: Not equal");
            return 1;
        }
        long o1millis = o1.getDate().getTimeInMillis();
        long o2millis = o2.getDate().getTimeInMillis();
        if (o1millis < o2millis) {
            return -1;
        }
        if (o1millis > o2millis) {
            return -1;
        }
        Log.d(TAG, "compare: Equal");
        return 0;
    }

    @Override
    public void onChanged(int position, int count) {
        Log.d(TAG, "onChanged: Notify changed " + position);
        adapter.notifyItemRangeChanged(position, count);
    }

    @Override
    public boolean areContentsTheSame(Reminder oldItem, Reminder newItem) {
        Log.d(TAG, "areContentsTheSame: " + oldItem.displayEquals(newItem));
        return oldItem.displayEquals(newItem);
    }

    @Override
    public boolean areItemsTheSame(Reminder item1, Reminder item2) {
        Log.d(TAG, "areItemsTheSame: " + item1.getId() + ", " + item2.getId());
        return item1.getId() == item2.getId();
    }

    @Override
    public void onInserted(int position, int count) {
        adapter.notifyItemRangeInserted(position, count);
    }

    @Override
    public void onRemoved(int position, int count) {
        adapter.notifyItemRangeRemoved(position, count);
    }

    @Override
    public void onMoved(int fromPosition, int toPosition) {
        Log.d(TAG, "onMoved: " + fromPosition + " " + toPosition);
        adapter.notifyDataSetChanged();
    }
}
