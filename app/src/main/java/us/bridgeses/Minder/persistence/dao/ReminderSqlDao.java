package us.bridgeses.Minder.persistence.dao;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;

import us.bridgeses.Minder.model.Reminder;

import static us.bridgeses.Minder.persistence.RemindersContract.Reminder.COLUMN_ACTIVE;
import static us.bridgeses.Minder.persistence.RemindersContract.Reminder.COLUMN_BLUETOOTH_MAC_ADDRESS;
import static us.bridgeses.Minder.persistence.RemindersContract.Reminder.COLUMN_BLUETOOTH_PREFERENCE;
import static us.bridgeses.Minder.persistence.RemindersContract.Reminder.COLUMN_CONDITIONS;
import static us.bridgeses.Minder.persistence.RemindersContract.Reminder.COLUMN_DATE;
import static us.bridgeses.Minder.persistence.RemindersContract.Reminder.COLUMN_DAYSOFWEEK;
import static us.bridgeses.Minder.persistence.RemindersContract.Reminder.COLUMN_DESCRIPTION;
import static us.bridgeses.Minder.persistence.RemindersContract.Reminder.COLUMN_ID;
import static us.bridgeses.Minder.persistence.RemindersContract.Reminder.COLUMN_IMAGE;
import static us.bridgeses.Minder.persistence.RemindersContract.Reminder.COLUMN_LATITUDE;
import static us.bridgeses.Minder.persistence.RemindersContract.Reminder.COLUMN_LEDCOLOR;
import static us.bridgeses.Minder.persistence.RemindersContract.Reminder.COLUMN_LEDPATTERN;
import static us.bridgeses.Minder.persistence.RemindersContract.Reminder.COLUMN_LOCATION_PREFERENCE;
import static us.bridgeses.Minder.persistence.RemindersContract.Reminder.COLUMN_LONGITUDE;
import static us.bridgeses.Minder.persistence.RemindersContract.Reminder.COLUMN_MONTHTYPE;
import static us.bridgeses.Minder.persistence.RemindersContract.Reminder.COLUMN_NAME;
import static us.bridgeses.Minder.persistence.RemindersContract.Reminder.COLUMN_PERSISTENCE;
import static us.bridgeses.Minder.persistence.RemindersContract.Reminder.COLUMN_QR;
import static us.bridgeses.Minder.persistence.RemindersContract.Reminder.COLUMN_RADIUS;
import static us.bridgeses.Minder.persistence.RemindersContract.Reminder.COLUMN_REPEATLENGTH;
import static us.bridgeses.Minder.persistence.RemindersContract.Reminder.COLUMN_REPEATTYPE;
import static us.bridgeses.Minder.persistence.RemindersContract.Reminder.COLUMN_RINGTONE;
import static us.bridgeses.Minder.persistence.RemindersContract.Reminder.COLUMN_SNOOZEDURATION;
import static us.bridgeses.Minder.persistence.RemindersContract.Reminder.COLUMN_SNOOZENUM;
import static us.bridgeses.Minder.persistence.RemindersContract.Reminder.COLUMN_SSID;
import static us.bridgeses.Minder.persistence.RemindersContract.Reminder.COLUMN_STYLE;
import static us.bridgeses.Minder.persistence.RemindersContract.Reminder.COLUMN_TEXTCOLOR;
import static us.bridgeses.Minder.persistence.RemindersContract.Reminder.COLUMN_VOLUME;
import static us.bridgeses.Minder.persistence.RemindersContract.Reminder.COLUMN_WIFI_PREFERENCE;
import static us.bridgeses.Minder.persistence.RemindersContract.Reminder.REMINDER_URI;

/**
 * This class stores and retrieves {@link Reminder}s from a SQL database
 */
public class ReminderSqlDao implements ReminderDAO {

    private ContentResolver resolver;

    /**
     * Using SQLite in Android requires access to an application context
     * Use this method to set the context from the calling class
     * @param context an Application context
     */
    @Override
    public void setContext(Context context){
        resolver = context.getContentResolver();
    }

    /**
     * Convert a cursor into a list of {@link Reminder}s
     * @param cursor A populated cursor with reminders
     * @return All of the reminders contained in cursor
     */
    public static Reminder[] cursorToReminders(Cursor cursor){
        cursor.moveToFirst();
        Reminder[] reminders = new Reminder[cursor.getCount()];
        for (int i=0; i<cursor.getCount(); i++) {
            reminders[i] = new Reminder(cursor);
            cursor.moveToNext();
        }
        return reminders;
    }

    /**
     * Retrieve a cursor from the database
     * @param resolver The ContentResolver to retrieve the cursor from
     * @return The cursor
     */
    private Cursor getCursor(ContentResolver resolver){
        String[] projection = {
                COLUMN_ID,
                COLUMN_ACTIVE,
                COLUMN_NAME,
                COLUMN_DESCRIPTION,
                COLUMN_DATE,
                COLUMN_DAYSOFWEEK,
                COLUMN_MONTHTYPE,
                COLUMN_REPEATTYPE,
                COLUMN_REPEATLENGTH,
                COLUMN_LATITUDE,
                COLUMN_LONGITUDE,
                COLUMN_RINGTONE,
                COLUMN_PERSISTENCE,
                COLUMN_RADIUS,
                COLUMN_QR,
                COLUMN_SSID,
                COLUMN_CONDITIONS,
                COLUMN_STYLE,
                COLUMN_SNOOZEDURATION,
                COLUMN_LEDCOLOR,
                COLUMN_LEDPATTERN,
                COLUMN_VOLUME,
                COLUMN_SNOOZENUM,
                COLUMN_TEXTCOLOR,
                COLUMN_LEDCOLOR,
                COLUMN_IMAGE,
                COLUMN_LOCATION_PREFERENCE,
                COLUMN_WIFI_PREFERENCE,
                COLUMN_BLUETOOTH_PREFERENCE,
                COLUMN_BLUETOOTH_MAC_ADDRESS
        };
        String sortOrder = COLUMN_ACTIVE + " DESC, " + COLUMN_DATE + " ASC";
        return resolver.query(
                REMINDER_URI,
                projection,
                null,
                null,
                sortOrder,
                null);
    }

    /**
     * Retrieve a list of all reminders in the database
     * @return The reminders in the database
     */
    @Override
    public Reminder[] getReminders(){
        Cursor cursor = getCursor(resolver);
        Reminder[] reminders = cursorToReminders(cursor);
        cursor.close();
        return reminders;
    }

    /**
     * Returns a Cursor that will not be managed by this class. This cursor must be closed manually
     * @return A cursor with all the reminders in the database
     */
    @Override
    public Cursor getAndKeepOpen(){
        return getCursor(resolver);
    }

    /**
     * Get a single reminder from the database
     * @param id The id of the reminder to be retrieved
     * @return The reminder
     */
    public Reminder getReminder(long id){
        Uri requestUri = Uri.withAppendedPath(REMINDER_URI, Long.toString(id));
        Cursor cursor = resolver.query(requestUri, null, null, null, null);
        Reminder reminder;
        if (cursor != null  && cursor.getCount()>=1) {
            Reminder[] reminders = cursorToReminders(cursor);
            reminder = reminders[0];
        }
        else {
            // TODO: 2/17/2017 Once we've decoupled preference management from Reminder, remember to
            // set this back to using user-set defaults
            reminder = new Reminder();
        }
        if (cursor != null) {
            cursor.close();
        }
        return reminder;
    }

    /**
     * Persist a reminder to the database. If the reminder does not exist in the database, the
     * reminder's id is assigned to the next available value
     * @param reminder The reminder to be persisted
     * @return The reminder with an updated id, if applicable
     */
    @Override
    public Reminder saveReminder(Reminder reminder) {
        ContentValues values = reminder.toContentValues();

        Uri response = resolver.insert(
                REMINDER_URI,
                values);
        if (response != null) {
                reminder.setId(Integer.parseInt( response.getLastPathSegment()));
        }
        else {
            throw new IllegalStateException("Unable to save Reminder");
        }
        return reminder;
    }

    /**
     * Delete the reminder from the database, if it exists
     * @param id The id of the reminder to be deleted
     * @return the number of rows deleted. Should be 1 if successful, 0 if reminder does not exist
     */
    @Override
    public int deleteReminder(int id){
        Uri requestUri = Uri.withAppendedPath(REMINDER_URI, Long.toString(id));
        return resolver.delete(requestUri, null, null);
    }
}
