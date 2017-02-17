package us.bridgeses.Minder.persistence;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.support.annotation.IntDef;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import java.lang.annotation.Retention;

import us.bridgeses.Minder.ReminderDBHelper;

import static java.lang.annotation.RetentionPolicy.SOURCE;
import static us.bridgeses.Minder.persistence.RemindersContract.BASE_URI;
import static us.bridgeses.Minder.persistence.RemindersContract.CONTENT_AUTHORITY;
import static us.bridgeses.Minder.persistence.RemindersContract.Reminder.REMINDER_URI;
import static us.bridgeses.Minder.persistence.RemindersContract.Reminder.TABLE_NAME;
import static us.bridgeses.Minder.persistence.RemindersContract.Reminder.TYPE;

/**
 * Created by tbrid on 2/16/2017.
 */

public class ReminderProvider extends ContentProvider {

    private static final UriMatcher matcher;

    @Retention(SOURCE)
    @IntDef({REMINDER})
    @SuppressWarnings("unused")
    public @interface ContentTypes {}
    public static final int  REMINDER = 100;

    static {
        matcher = new UriMatcher(UriMatcher.NO_MATCH);
        matcher.addURI(CONTENT_AUTHORITY, REMINDER_URI.toString(), REMINDER);
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
            case REMINDER: {
                return TYPE;
            }
            default: {
                throw new IllegalArgumentException("Invalid uri");
            }
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
            case REMINDER: {
                return db.query(TABLE_NAME, projection, selection,
                        selectionArgs, sortOrder, null, null);
            }
            default:
                throw new IllegalArgumentException("Invalid uri");
        }
    }

    @Nullable
    @Override
    public Uri insert(@NonNull Uri uri, ContentValues values) {
        return null;
    }

    @Override
    public int delete(@NonNull Uri uri, String selection, String[] selectionArgs) {
        return 0;
    }

    @Override
    public int update(@NonNull Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        return 0;
    }

    private void openDB() {
        if (helper == null) {
            helper = ReminderDBHelper.getInstance(getContext());
        }
        if (db == null || !db.isOpen()) {
            db = helper.openDatabase();
        }
    }
}
