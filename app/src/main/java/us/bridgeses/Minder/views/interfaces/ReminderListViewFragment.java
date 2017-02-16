package us.bridgeses.Minder.views.interfaces;

import android.support.v7.widget.RecyclerView;
import android.view.View;

import java.util.List;

import us.bridgeses.Minder.Reminder;
import us.bridgeses.Minder.adapters.ReminderRecyclerAdapter;

/**
 * Created by bridgtxcdf on 2/15/2017.
 */

public class ReminderListViewFragment implements ReminderListView,
        ReminderRecyclerAdapter.OnFinishClickedListener,
        ReminderRecyclerAdapter.OnItemClickedListener {

    private RecyclerView reminderView;
    private ReminderRecyclerAdapter reminderAdapter;

    @Override
    public void setReminders(List<Reminder> reminders) {
        reminderAdapter = new ReminderRecyclerAdapter(reminders, this, this);
        reminderView.setAdapter(reminderAdapter);
    }

    @Override
    public void addReminder(Reminder reminder) {
        if (reminderAdapter != null) {
            reminderAdapter.addReminder(reminder);
        }
    }

    @Override
    public void updateReminder(Reminder reminder) {
        if (reminderAdapter != null) {
            reminderAdapter.addReminder(reminder);
        }
    }

    @Override
    public void removeReminder(Reminder reminder) {
        if (reminderAdapter != null) {
            reminderAdapter.removeReminder(reminder);
        }
    }

    @Override
    public void onFinishClicked(long id) {
        // TODO: Display confirmation dialog
    }

    @Override
    public void onItemClicked(long id) {
        // TODO: Open editor
    }

    // Assign to onClick in XML
    public void onFABClicked(View view) {

    }
}
