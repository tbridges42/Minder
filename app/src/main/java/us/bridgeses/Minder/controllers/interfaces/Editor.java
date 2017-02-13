package us.bridgeses.Minder.controllers.interfaces;

import android.view.View;

/**
 * Created by tbrid on 2/12/2017.
 */

public interface Editor {

    /**
     * Verifies that any required settings have been selected, and if so returns RESULT_OK to the
     * calling activity. Called when the save button is pressed.
     * @param view is passed by the system when the save button is pressed.
     */
    void save(View view);

    /**
     * Restores settings to their original configuration, as saved in saved, and then returns
     * RESULT_CANCELED to the calling activity.
     * @param view is passed by the Android system when the cancel button is pressed
     */
    void cancel(View view);

    void initialize();

    /**
     * This saves existing settings to saved, in order to be restored in the event the user cancels
     * the edit
     */
    void saveState();
}
