package us.bridgeses.Minder.views;

import android.app.Activity;
import android.app.Fragment;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.List;

import us.bridgeses.Minder.R;
import us.bridgeses.Minder.Reminder;
import us.bridgeses.Minder.adapters.ReminderRecyclerAdapter;
import us.bridgeses.Minder.controllers.DataController;
import us.bridgeses.Minder.views.interfaces.ReminderListView;

/**
 * A fragment for displaying a RecyclerView full of Reminders and an FAB
 */

public class ReminderListViewFragment extends Fragment implements ReminderListView,
        ReminderRecyclerAdapter.OnFinishClickedListener,
        ReminderRecyclerAdapter.OnItemClickedListener, View.OnClickListener {

    private static final String TAG = "ReminderView";

    public interface ViewCallback {
        DataController getDataController();
    }

    private RecyclerView reminderView;
    private ReminderRecyclerAdapter reminderAdapter;
    private ViewCallback callback;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_fab_list, container, false);
        reminderView = (RecyclerView) view.findViewById(R.id.reminder_list);
        reminderView.setLayoutManager(new LinearLayoutManager(getActivity()));
        view.findViewById(R.id.reminder_fab).setOnClickListener(this);
        return view;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        try {
            Activity activity = getActivity();
            callback = (ViewCallback) activity;
        }
        catch (ClassCastException e) {
            throw new ClassCastException("The caller of View must implement ViewCallback");
        }
        catch (NullPointerException e) {
            throw new NullPointerException("Activity was not ready");
        }
    }

    @Override
    public void setReminders(List<Reminder> reminders) {
        Log.d(TAG, "setReminders: received " + reminders.size() + " reminders");
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
    public void displayProgress() {

    }

    @Override
    public void removeProgress() {

    }

    @Override
    public void onFinishClicked(long id) {
        // TODO: Display confirmation dialog
    }

    @Override
    public void onItemClicked(long id) {
        callback.getDataController().onReminderSelected(id);
    }

    public void onFABClicked() {
        callback.getDataController().createNew();
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.reminder_fab) {
            onFABClicked();
        }
    }
}
