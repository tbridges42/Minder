package us.bridgeses.Minder.views;

import android.app.Activity;
import android.app.Fragment;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.IntDef;
import android.support.annotation.Nullable;
import android.support.v7.widget.DefaultItemAnimator;
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

import static us.bridgeses.Minder.views.ViewStatus.DETACHED;
import static us.bridgeses.Minder.views.ViewStatus.LOADING;
import static us.bridgeses.Minder.views.ViewStatus.PAUSED;
import static us.bridgeses.Minder.views.ViewStatus.READY;

/**
 * A fragment for displaying a RecyclerView full of Reminders and an FAB
 */

public class ReminderListViewFragment extends Fragment implements ReminderListView,
        ReminderRecyclerAdapter.OnFinishClickedListener,
        ReminderRecyclerAdapter.OnItemClickedListener, View.OnClickListener {

    private static final String TAG = "ReminderView";
    @ViewStatus public static int status = DETACHED;

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
        reminderView.setItemAnimator(null);
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
    public void onAttach(Context context) {
        super.onAttach(context);
        status = LOADING;
    }

    @Override
    public void onDetach() {
        super.onDetach();
        status = DETACHED;
    }

    @Override
    public void onPause() {
        super.onPause();
        status = PAUSED;
    }

    @Override
    public void onResume() {
        super.onResume();
        status = READY;
        callback.notifyReady();
    }

    @Override
    public void setReminders(List<Reminder> reminders) {
        Log.d(TAG, "setReminders: received " + reminders.size() + " reminders");
        if (reminderAdapter == null) {
            reminderAdapter = new ReminderRecyclerAdapter(reminders, this, this);

            reminderView.setLayoutManager(new LinearLayoutManager(getActivity()));
            reminderView.setItemAnimator(new DefaultItemAnimator());
            reminderView.setAdapter(reminderAdapter);
        }
        else {
            for (Reminder reminder : reminders) {
                reminderAdapter.addReminder(reminder);
            }
        }
    }

    @Override
    public void addReminder(Reminder reminder) {
        if (reminderAdapter != null) {
            reminderAdapter.addReminder(reminder);
        }
    }

    @Override
    public void updateReminder(Reminder reminder) {
        Log.d(TAG, "updateReminder: ");
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
    public int getStatus() {
        return status;
    }

    @Override
    public void onFinishClicked(Reminder reminder) {
        callback.getDataController().skipNext(reminder);
    }

    @Override
    public void onItemClicked(Reminder reminder) {
        callback.getDataController().onReminderSelected(reminder);
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
