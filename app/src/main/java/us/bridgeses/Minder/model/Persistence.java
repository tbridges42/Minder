package us.bridgeses.Minder.model;

import android.content.ContentValues;
import android.database.Cursor;
import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.IntDef;
import android.support.annotation.NonNull;

import java.io.Serializable;
import java.lang.annotation.Retention;
import java.util.concurrent.TimeUnit;

import static java.lang.annotation.RetentionPolicy.SOURCE;
import static us.bridgeses.Minder.model.Persistence.PersistenceFlags.NONE;
import static us.bridgeses.Minder.persistence.RemindersContract.Reminder.COLUMN_PERSISTENCE;
import static us.bridgeses.Minder.persistence.RemindersContract.Reminder.COLUMN_QR;
import static us.bridgeses.Minder.persistence.RemindersContract.Reminder.COLUMN_SNOOZEDURATION;
import static us.bridgeses.Minder.persistence.RemindersContract.Reminder.COLUMN_SNOOZENUM;
import static us.bridgeses.Minder.persistence.RemindersContract.Reminder.COLUMN_VOLUME;
import static us.bridgeses.Minder.model.Persistence.PersistenceFlags.CONFIRM_DISMISS;
import static us.bridgeses.Minder.model.Persistence.PersistenceFlags.DISPLAY_SCREEN;
import static us.bridgeses.Minder.model.Persistence.PersistenceFlags.KEEP_TRYING;
import static us.bridgeses.Minder.model.Persistence.PersistenceFlags.OVERRIDE_VOLUME;
import static us.bridgeses.Minder.model.Persistence.PersistenceFlags.REQUIRE_CODE;
import static us.bridgeses.Minder.model.Persistence.PersistenceFlags.WAKE_UP;

/**
 * Created by Laura on 7/9/2015.
 */
public class Persistence implements ReminderComponent, Serializable, Parcelable{

    // TODO: 4/11/2017 Consider finding a more extensible way to implement this. Decorators?

    @Retention(SOURCE)
    @IntDef(flag=true, value = {NONE, REQUIRE_CODE,
            OVERRIDE_VOLUME,
            DISPLAY_SCREEN,
            WAKE_UP,
            CONFIRM_DISMISS,
            KEEP_TRYING})
    public @interface PersistenceFlags {
        int NONE = 0;
        int REQUIRE_CODE = 1;
        int OVERRIDE_VOLUME = 1<<1;
        int DISPLAY_SCREEN = 1<<2;
        int WAKE_UP = 1<<3;
        int CONFIRM_DISMISS = 1<<4;
        int KEEP_TRYING = 1<<5;
    }

    public static final int FLAGS_DEFAULT = DISPLAY_SCREEN|WAKE_UP;
    public static final String CODE_DEFAULT = "";
    public static final int VOLUME_DEFAULT = 75;
    public static final int SNOOZE_LIMIT_DEFAULT = -1;
    public static final long SNOOZE_TIME_DEFAULT = TimeUnit.MINUTES.toMillis(5);

    private int flags = FLAGS_DEFAULT;
    private String code = CODE_DEFAULT;
    private int volume = VOLUME_DEFAULT;
    private int snoozeLimit = SNOOZE_LIMIT_DEFAULT;
    private long snoozeTime = SNOOZE_TIME_DEFAULT;

    public Persistence() {

    }

    public Persistence(Persistence that) {
        setFlags(flags);
        setCode(that.code);
        setVolume(that.volume);
        setSnoozeLimit(that.snoozeLimit);
        setSnoozeTime(that.snoozeTime);
    }

    public Persistence(Parcel parcel) {
        setFlags(parcel.readInt());
        setCode(parcel.readString());
        setVolume(parcel.readInt());
        setSnoozeLimit(parcel.readInt());
        setSnoozeTime(parcel.readLong());
    }

    public Persistence(@NonNull Cursor cursor) {
        if (cursor.isAfterLast() || cursor.isBeforeFirst()) {
            throw new IllegalArgumentException("Cursor is not pointing to a valid row");
        }
        setFlags(cursor.getInt(cursor.getColumnIndex(COLUMN_PERSISTENCE)));
        setCode(cursor.getString(cursor.getColumnIndex(COLUMN_QR)));
        setVolume(cursor.getInt(cursor.getColumnIndex(COLUMN_VOLUME)));
        setSnoozeTime(cursor.getLong(cursor.getColumnIndex(COLUMN_SNOOZEDURATION)));
        setSnoozeLimit(cursor.getInt(cursor.getColumnIndex(COLUMN_SNOOZENUM)));
    }

    public ContentValues toContentValues() {
        ContentValues values = new ContentValues();
        return toContentValues(values);
    }

    public ContentValues toContentValues(@NonNull ContentValues contentValues) {
        contentValues.put(COLUMN_PERSISTENCE, flags);
        contentValues.put(COLUMN_QR, code);
        contentValues.put(COLUMN_VOLUME, volume);
        contentValues.put(COLUMN_SNOOZEDURATION, snoozeTime);
        contentValues.put(COLUMN_SNOOZENUM, snoozeLimit);
        return contentValues;
    }

    @Override
    public void addTo(Reminder reminder) {
        // defensive copy
        reminder.setPersistence(new Persistence(this));
    }

    public boolean hasFlag(@PersistenceFlags int flag){
        @PersistenceFlags int flagValue = flag & flags;
        return flagValue == flag;
    }

    public String getCode() {
        return code;
    }

    public int getVolume() {
        return volume;
    }

    public int getSnoozeLimit() {
        return snoozeLimit;
    }

    public long getSnoozeTime() {
        return snoozeTime;
    }

    public void setFlags(int flags) {
        if ((flags < 0) || (flags > 1<<6)) {
            throw new IllegalArgumentException("Flags is outside of range: " + flags);
        }
        this.flags = flags;
    }

    public void setFlag(@PersistenceFlags int flag, boolean value) {
        if (value) {
            setFlags(flags|flag);
        }
        else {
            setFlags(flags&~flag);
        }
    }

    public void setCode(@NonNull String code){
        this.code = code;
    }

    public void setVolume(int volume){
        if ((volume < 0)||(volume > 100)){
            throw new IllegalArgumentException("Volume must be between 0 and 100. Was: " + volume);
        }
        this.volume = volume;
    }

    public void setSnoozeLimit(int snoozeLimit){
        this.snoozeLimit = snoozeLimit;
    }

    public void setSnoozeTime(long snoozeTime){
        if (snoozeTime <= 0){
            throw new IllegalArgumentException("Time must be greater than zero");
        }
        this.snoozeTime = snoozeTime;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeSerializable(flags);
        parcel.writeString(code);
        parcel.writeInt(volume);
        parcel.writeInt(snoozeLimit);
        parcel.writeLong(snoozeTime);
    }

    public static final Creator<Persistence> CREATOR = new Creator<Persistence>() {
        @Override
        public Persistence createFromParcel(Parcel parcel) {
            return new Persistence(parcel);
        }

        @Override
        public Persistence[] newArray(int i) {
            return new Persistence[0];
        }
    };
}
