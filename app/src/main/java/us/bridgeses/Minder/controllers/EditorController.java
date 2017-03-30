package us.bridgeses.Minder.controllers;

import android.app.Fragment;
import android.support.annotation.IntDef;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

import us.bridgeses.Minder.Reminder;

/**
 * Created by bridgtxcdf on 3/30/2017.
 */

public class EditorController extends Fragment {

    private Reminder reminder;
    private Reminder lastSavedReminder;
    private ActivityCallback callback;

    public interface ActivityCallback {
        public Fragment launchEditor(@EditorType int type);
        public Fragment dismissEditor();
        public DataController getDataController();
        public Fragment doneEditing();
    }

    @Retention(RetentionPolicy.SOURCE)
    @IntDef({MAIN, REPEAT, CONDITIONS, STYLE, PERSISTENCE})
    public @interface EditorType {}
    public static final int MAIN = 0;
    public static final int REPEAT = 1;
    public static final int CONDITIONS = 2;
    public static final int STYLE = 3;
    public static final int PERSISTENCE = 4;


    public void save() {
        Fragment editorFragment = callback.dismissEditor();
        if (editorFragment == null) {
            callback.getDataController().save(reminder);
            callback.doneEditing();
        }
        else {
            lastSavedReminder = reminder;
            // Populate fragment with lastSavedReminder values
        }
    }

    public void cancel() {
        Fragment editorFragment = callback.dismissEditor();
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
        Fragment editorFragment = callback.launchEditor(type);
        // Populate fragment with reminder values
    }
}
