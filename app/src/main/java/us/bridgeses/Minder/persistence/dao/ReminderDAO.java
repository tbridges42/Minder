package us.bridgeses.Minder.persistence.dao;

import android.content.Context;
import android.database.Cursor;

import us.bridgeses.Minder.Reminder;

/**
 * An interface to be implemented by any data source for Reminders
 * Created by Tony on 5/5/2015.
 */
public interface ReminderDAO {

    /**
     * Many data sources require a context for their initialization. Set it here before calling other
     * methods
     * @param context The application context
     */
    void setContext(Context context);

    /**
     * Get all reminders in data source
     * @return A list of reminders in the data source
     */
    Reminder[] getReminders();

    /**
     * Some patterns require a cursor and its source to stay open. This method will not close after
     * the cursor is created until close is called
     * @return an open cursor to reminders
     */
    Cursor getAndKeepOpen();

    /**
     * Get a single reminder, identified by id
     * @param id the unique id of the reminder
     * @return the reminder with id
     */
    Reminder getReminder(long id);

    /**
     * Persist a reminder to the data source. If the reminder does not already exist in the data source
     * this will also assign it a unique id
     * @param reminder The reminder to be persisted
     * @return the reminder with an updated id
     */
    Reminder saveReminder(Reminder reminder);

    /**
     * Delete a reminder, identified by id
     * @param id the unique id of the reminder
     * @return the number of items deleted. Should be 1 if successful or 0 if failed
     */
    int deleteReminder(int id);
}