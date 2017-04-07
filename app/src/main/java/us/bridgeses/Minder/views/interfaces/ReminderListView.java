package us.bridgeses.Minder.views.interfaces;

import java.util.List;

import us.bridgeses.Minder.Reminder;
import us.bridgeses.Minder.controllers.DataController;
import us.bridgeses.Minder.views.ViewStatus;

/**
 * Created by bridgtxcdf on 2/15/2017.
 */

public interface ReminderListView {

    interface ViewCallback {
        DataController getDataController();
        void notifyReady();
    }

    void setReminders(List<Reminder> reminders);

    void addReminder(Reminder reminder);

    void updateReminder(Reminder reminder);

    void removeReminder(Reminder reminder);

    void displayProgress();

    void removeProgress();

    @ViewStatus int getStatus();
}
