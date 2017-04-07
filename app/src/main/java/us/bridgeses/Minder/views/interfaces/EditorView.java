package us.bridgeses.Minder.views.interfaces;

import us.bridgeses.Minder.controllers.EditorController;
import us.bridgeses.Minder.reminder.ReminderComponent;

/**
 * Created by bridgtxcdf on 4/7/2017.
 */

public interface EditorView<EditorType extends ReminderComponent> {

    interface ViewCallback {
        EditorController getEditorController();
    }

    void setup(EditorType model);

    EditorType getValues();
}
