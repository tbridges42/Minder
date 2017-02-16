package us.bridgeses.Minder.views.interfaces;

import android.database.Cursor;

import java.util.List;

import us.bridgeses.Minder.Reminder;

/**
 * Created by bridgtxcdf on 2/15/2017.
 */

public interface ReminderListView {

    void setReminders(List<Reminder> reminders);

    void addReminder(Reminder reminder);

    void updateReminder(Reminder reminder);

    void removeReminder(Reminder reminder);
}
