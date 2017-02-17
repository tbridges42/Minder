package us.bridgeses.Minder.persistence.provider;

import android.content.ContentProvider;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.support.annotation.IntDef;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import java.lang.annotation.Retention;

import us.bridgeses.Minder.persistence.sqlite.ReminderDBHelper;

import static java.lang.annotation.RetentionPolicy.SOURCE;
import static us.bridgeses.Minder.persistence.RemindersContract.CONTENT_AUTHORITY;
import static us.bridgeses.Minder.persistence.RemindersContract.Reminder.COLUMN_ID;
import static us.bridgeses.Minder.persistence.RemindersContract.Reminder.MULTI_TYPE;
import static us.bridgeses.Minder.persistence.RemindersContract.Reminder.REMINDER_URI;
import static us.bridgeses.Minder.persistence.RemindersContract.Reminder.TABLE_NAME;
import static us.bridgeses.Minder.persistence.RemindersContract.Reminder.SINGLE_TYPE;

/**
 * A content provider for Reminders
 */

public class ReminderProvider extends ContentProvider {

    private static final UriMatcher matcher;

    @Retention(SOURCE)
    @IntDef({REMINDER, REMINDERS})
    public @interface ContentTypes {}
    public static final int REMINDER = 100;
    public static final int REMINDERS = 101;

    static {
        matcher = new UriMatcher(UriMatcher.NO_MATCH);
        matcher.addURI(CONTENT_AUTHORITY, TABLE_NAME, REMINDERS);
        matcher.addURI(CONTENT_AUTHORITY, TABLE_NAME + "/#", REMINDER);
    }

    private ReminderDBHelper helper;
    private SQLiteDatabase db;


    @Override
    public boolean onCreate() {
        return true;
    }

    @Nullable
    @Override
    public String getType(@NonNull Uri uri) {
        @ContentTypes
        int type = matcher.match(uri);
        switch (type) {
            case REMINDER:
                return SINGLE_TYPE;
            case REMINDERS:
                return MULTI_TYPE;
            default:
                throw new IllegalArgumentException("Invalid uri");
        }
    }

    @Nullable
    @Override
    public Cursor query(@NonNull Uri uri, String[] projection, String selection,
                        String[] selectionArgs, String sortOrder) {
        openDB();
        @ContentTypes
        int type = matcher.match(uri);
        switch (type) {
            // TODO: 2/16/2017 Properly implement individual querying
            case REMINDER:
                selection = uri.getLastPathSegment();
                selectionArgs = new String[]{ COLUMN_ID };
                notify();
            case REMINDERS:
                Cursor cursor = db.query(TABLE_NAME, projection, selection,
                        selectionArgs, sortOrder, null, null);
                Context context = getContext();
                if (context != null) {
                    ContentResolver resolver = context.getContentResolver();
                    cursor.setNotificationUri(resolver,
                            uri);
                }
                else {
                    throw new IllegalStateException("Context not ready");
                }
                return cursor;
            default:
                throw new IllegalArgumentException("Invalid uri");
        }
    }

    @Nullable
    @Override
    public Uri insert(@NonNull Uri uri, ContentValues values) {
        Long newId;
        openDB();
        @ContentTypes
        int type = matcher.match(uri);
        switch (type) {
            case REMINDER: case REMINDERS:
                newId = db.insert(TABLE_NAME, null, values);
                break;
            default:
                throw new IllegalArgumentException("Invalid uri");
        }
        notifyChanged(uri);
        return Uri.withAppendedPath(REMINDER_URI, Long.toString(newId));
    }

    @Override
    public int delete(@NonNull Uri uri, String selection, String[] selectionArgs) {
        openDB();
        @ContentTypes
        int type = matcher.match(uri);
        switch (type) {
            case REMINDER:
                selection = uri.getLastPathSegment();
                selectionArgs = new String[]{ COLUMN_ID };
            case REMINDERS:
                int rows = db.delete(TABLE_NAME, selection, selectionArgs);
                notifyChanged(uri);
                return rows;
            default:
                throw new IllegalArgumentException("Invalid uri");
        }
    }

    @Override
    public int update(@NonNull Uri uri, ContentValues values,
                      String selection, String[] selectionArgs) {
        openDB();
        @ContentTypes
        int type = matcher.match(uri);
        switch (type) {
            case REMINDER:
                selection = uri.getLastPathSegment();
                selectionArgs = new String[]{ COLUMN_ID };
            case REMINDERS:
                int rows = db.update(TABLE_NAME, values, selection, selectionArgs);
                notifyChanged(uri);
                return rows;
            default:
                throw new IllegalArgumentException("Invalid uri");
        }
    }

    private void openDB() {
        if (helper == null) {
            helper = ReminderDBHelper.getInstance(getContext());
        }
        if (db == null || !db.isOpen()) {
            db = helper.openDatabase();
        }
    }

    private void notifyChanged(Uri uri) {
        Context context = getContext();
        if (context != null) {
            ContentResolver resolver = context.getContentResolver();
            resolver.notifyChange(uri, null);
        }
        else {
            throw new IllegalStateException("Context not ready");
        }
    }
}
