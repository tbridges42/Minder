package us.bridgeses.Minder.controllers;

import android.app.Activity;
import android.app.Fragment;
import android.os.Bundle;
import android.support.annotation.IntDef;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

import us.bridgeses.Minder.Reminder;
import us.bridgeses.Minder.reminder.ReminderComponent;
import us.bridgeses.Minder.views.interfaces.EditorView;

/**
 * Created by bridgtxcdf on 3/30/2017.
 */

public class EditorController extends Fragment {

    private Reminder reminder;
    private ActivityCallback callback;

    public interface ActivityCallback {
        EditorView launchEditor(ReminderComponent component);
        EditorView dismissEditor();
        EditorView getCurrentEditor();
        DataController getDataController();
        Fragment doneEditing();
    }

    @Retention(RetentionPolicy.SOURCE)
    @IntDef({MAIN, REPEAT, CONDITIONS, STYLE, PERSISTENCE})
    public @interface EditorType {}
    public static final int MAIN = 0;
    public static final int REPEAT = 1;
    public static final int CONDITIONS = 2;
    public static final int STYLE = 3;
    public static final int PERSISTENCE = 4;

    public static EditorController getInstance(Reminder reminder) {
        EditorController editorController = new EditorController();
        Bundle bundle = new Bundle();
        bundle.putParcelable("reminder", reminder);
        editorController.setArguments(bundle);
        return editorController;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
        reminder = getArguments().getParcelable("reminder");
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // This fragment has no UI. Do not attempt to inflate it
        return null;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        try {
            Activity activity = getActivity();
            callback = (EditorController.ActivityCallback) activity;
        }
        catch (ClassCastException e) {
            throw new IllegalStateException(
                    "The caller of EditorController must implement ActivityCallback", e
            );
        }
        catch (NullPointerException e) {
            throw new IllegalStateException("Activity was not ready", e);
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        callback = null;
    }

    public void save() {
        callback.getCurrentEditor().getValues().addTo(reminder);
        EditorView editorFragment = callback.dismissEditor();
        if (editorFragment == null) {
            callback.getDataController().save(reminder);
            callback.doneEditing();
        }
        else {
            // Populate fragment with lastSavedReminder values
        }
    }

    public void cancel() {
        EditorView editorFragment = callback.dismissEditor();
        if (editorFragment == null) {
            callback.doneEditing();
        }
        else {
            // Populate fragment with lastSavedReminder values
        }
    }

    public Reminder getReminder() {
        return reminder;
    }

    public void onEditButtonClicked(@EditorType int type) {
        switch (type) {
            case CONDITIONS:
                callback.launchEditor(reminder.getConditions());
                break;
            case MAIN:
                break;
            case PERSISTENCE:
                callback.launchEditor(reminder.getPersistence());
                break;
            case REPEAT:
                break;
            case STYLE:
                break;
        }
    }
}
