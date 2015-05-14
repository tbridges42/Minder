package us.bridgeses.Minder;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

/**
 * Created by Tony on 5/5/2015.
 */
public interface ReminderDAO {

    void setContext(Context context);
    Reminder[] getReminders();
    Cursor getAndKeepOpen();
    void close();
    Reminder getReminder(int id);
    Reminder saveReminder(Reminder reminder);
    int deleteReminder(int id);
    boolean isOpen();
}
