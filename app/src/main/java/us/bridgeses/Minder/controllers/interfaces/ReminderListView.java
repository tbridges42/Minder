package us.bridgeses.Minder.controllers.interfaces;

import java.util.List;

import us.bridgeses.Minder.Reminder;

/**
 * Created by bridgtxcdf on 2/13/2017.
 */

public interface ReminderListView {

    void refreshList(List<Reminder> reminderList);

    void refreshItem(long id);
}
