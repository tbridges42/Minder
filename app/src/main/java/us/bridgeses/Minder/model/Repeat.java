package us.bridgeses.Minder.model;

import android.content.ContentValues;
import android.database.Cursor;
import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.IntDef;
import android.support.annotation.NonNull;

import java.lang.annotation.Retention;

import static java.lang.annotation.RetentionPolicy.SOURCE;
import static us.bridgeses.Minder.model.Repeat.DateType.DATE;
import static us.bridgeses.Minder.model.Repeat.DateType.DAY_OF_PERIOD;
import static us.bridgeses.Minder.model.Repeat.DateType.DAY_OF_PERIOD_REVERSED;
import static us.bridgeses.Minder.model.Repeat.DateType.DAY_OF_WEEK;
import static us.bridgeses.Minder.model.Repeat.DateType.DAY_OF_WEEK_REVERSED;
import static us.bridgeses.Minder.model.Repeat.DaysOfWeek.ALL_WEEK;
import static us.bridgeses.Minder.model.Repeat.DaysOfWeek.FRIDAY;
import static us.bridgeses.Minder.model.Repeat.DaysOfWeek.MONDAY;
import static us.bridgeses.Minder.model.Repeat.DaysOfWeek.MWF;
import static us.bridgeses.Minder.model.Repeat.DaysOfWeek.SATURDAY;
import static us.bridgeses.Minder.model.Repeat.DaysOfWeek.SUNDAY;
import static us.bridgeses.Minder.model.Repeat.DaysOfWeek.THURSDAY;
import static us.bridgeses.Minder.model.Repeat.DaysOfWeek.TTH;
import static us.bridgeses.Minder.model.Repeat.DaysOfWeek.TUESDAY;
import static us.bridgeses.Minder.model.Repeat.DaysOfWeek.WEDNESDAY;
import static us.bridgeses.Minder.model.Repeat.DaysOfWeek.WEEKDAY;
import static us.bridgeses.Minder.model.Repeat.DaysOfWeek.WEEKEND;
import static us.bridgeses.Minder.model.Repeat.RepeatType.ANNUALLY;
import static us.bridgeses.Minder.model.Repeat.RepeatType.DAILY;
import static us.bridgeses.Minder.model.Repeat.RepeatType.MONTHLY;
import static us.bridgeses.Minder.model.Repeat.RepeatType.NONE;
import static us.bridgeses.Minder.model.Repeat.RepeatType.WEEKLY;
import static us.bridgeses.Minder.persistence.RemindersContract.Reminder.COLUMN_DAYSOFWEEK;
import static us.bridgeses.Minder.persistence.RemindersContract.Reminder.COLUMN_MONTHTYPE;
import static us.bridgeses.Minder.persistence.RemindersContract.Reminder.COLUMN_REPEATLENGTH;
import static us.bridgeses.Minder.persistence.RemindersContract.Reminder.COLUMN_REPEATTYPE;

/**
 * Created by Tony on 6/8/2015.
 */
public class Repeat implements ReminderComponent, Parcelable, Cloneable {
    @Retention(SOURCE)
    @IntDef({NONE, DAILY, WEEKLY, MONTHLY, ANNUALLY})
    public @interface RepeatType {
        int NONE = 0;
        int DAILY = 1;
        int WEEKLY = 2;
        int MONTHLY = 3;
        int ANNUALLY = 4;
    }

    @Retention(SOURCE)
    @IntDef({DATE, DAY_OF_PERIOD, DAY_OF_PERIOD_REVERSED, DAY_OF_WEEK, DAY_OF_WEEK_REVERSED})
    public @interface DateType {
        int DATE = 0;
        int DAY_OF_WEEK = 1;
        int DAY_OF_PERIOD_REVERSED = 2;
        int DAY_OF_WEEK_REVERSED = 3;
        int DAY_OF_PERIOD = 4;
    }

    @Retention(SOURCE)
    @IntDef(flag = true, value = {DaysOfWeek.NONE, SUNDAY, MONDAY, TUESDAY, WEDNESDAY, THURSDAY, FRIDAY, SATURDAY,
            WEEKEND, WEEKDAY, MWF, TTH, ALL_WEEK})
    public @interface DaysOfWeek {
        int NONE = 0;
        int SUNDAY = 1;
        int MONDAY = 1<<1;
        int TUESDAY = 1<<2;
        int WEDNESDAY = 1<<3;
        int THURSDAY = 1<<4;
        int FRIDAY = 1<<5;
        int SATURDAY = 1<<6;
        int WEEKEND = SUNDAY|SATURDAY;
        int WEEKDAY = MONDAY|TUESDAY|WEDNESDAY|THURSDAY|FRIDAY;
        int ALL_WEEK = SUNDAY|MONDAY|TUESDAY|WEDNESDAY|THURSDAY|FRIDAY|SATURDAY;
        int MWF = MONDAY|WEDNESDAY|FRIDAY;
        int TTH = TUESDAY|THURSDAY;
    }

    public static final @RepeatType int REPEAT_TYPE_DEFAULT = NONE;
    public static final @Repeat.DateType int REPEAT_DATE_TYPE_DEFAULT = DATE;
    public static final @DaysOfWeek int DAYS_OF_WEEK_DEFAULT = 0;
    public static final int REPEAT_PERIOD_DEFAULT = 0;

    private @RepeatType int repeatType = REPEAT_TYPE_DEFAULT;
    private @DateType int dateType = REPEAT_DATE_TYPE_DEFAULT;
    private int daysOfWeek = DAYS_OF_WEEK_DEFAULT;
    private int repeatPeriod = REPEAT_PERIOD_DEFAULT;

    public Repeat() {

    }

    public Repeat(Repeat that) {
        setRepeatType(that.getRepeatType());
        setDateType(that.getDateType());
        setDaysOfWeek(that.getDaysOfWeek());
        setRepeatPeriod(that.getRepeatPeriod());
    }

    protected Repeat(Parcel in) {
        @RepeatType int repeatType = in.readInt();
        setRepeatType(repeatType);
        @DateType int dateType = in.readInt();
        setDateType(dateType);
        setDaysOfWeek(in.readInt());
        setRepeatPeriod(in.readInt());
    }

    public Repeat(@NonNull Cursor cursor) {
        if (cursor.isAfterLast() || cursor.isBeforeFirst()) {
            throw new IllegalArgumentException("Cursor is not pointing to a valid row");
        }
        @RepeatType int repeatType = cursor.getInt(cursor.getColumnIndex(COLUMN_REPEATTYPE));
        setRepeatType(repeatType);
        @DateType int dateType = cursor.getInt(cursor.getColumnIndex(COLUMN_MONTHTYPE));
        setDateType(dateType);
        setDaysOfWeek(cursor.getInt(cursor.getColumnIndex(COLUMN_DAYSOFWEEK)));
        setRepeatPeriod(cursor.getInt(cursor.getColumnIndex(COLUMN_REPEATLENGTH)));
    }

    public ContentValues toContentValues() {
        ContentValues values = new ContentValues();
        return toContentValues(values);
    }

    public ContentValues toContentValues(ContentValues values) {
        values.put(COLUMN_REPEATTYPE, repeatType);
        values.put(COLUMN_MONTHTYPE, dateType);
        values.put(COLUMN_DAYSOFWEEK, daysOfWeek);
        values.put(COLUMN_REPEATLENGTH, repeatPeriod);
        return values;
    }

    public void setRepeatType(@RepeatType int repeatType) {
        this.repeatType = repeatType;
    }

    public void setDateType(@DateType int dateType) {
        this.dateType = dateType;
    }

    public void setDaysOfWeek(int daysOfWeek) {
        if (daysOfWeek < 0 || daysOfWeek > ALL_WEEK) {
            throw new IllegalArgumentException("daysOfWeek out of range: "
                    + Integer.toBinaryString(daysOfWeek));
        }
        this.daysOfWeek = daysOfWeek;
    }

    public void setDayOfWeek(@DaysOfWeek int dayOfWeek, boolean value) {
        if (value) {
            setDaysOfWeek(daysOfWeek|dayOfWeek);
        }
        else {
            setDaysOfWeek(daysOfWeek&~dayOfWeek);
        }
    }

    public void setRepeatPeriod(int period) {
        if (period < 0) {
            throw new IllegalArgumentException("Period must be positive");
        }
    }

    public @RepeatType int getRepeatType() {
        return repeatType;
    }

    public @DateType int getDateType() {
        return dateType;
    }

    public int getDaysOfWeek() {
        return daysOfWeek;
    }

    public boolean hasDayOfWeek(@DaysOfWeek int dayOfWeek) {
        @DaysOfWeek int flagValue = daysOfWeek & dayOfWeek;
        return flagValue == dayOfWeek;
    }

    public int getRepeatPeriod() {
        return repeatPeriod;
    }

    public static final Creator<Repeat> CREATOR = new Creator<Repeat>() {
        @Override
        public Repeat createFromParcel(Parcel in) {
            return new Repeat(in);
        }

        @Override
        public Repeat[] newArray(int size) {
            return new Repeat[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(repeatType);
        dest.writeInt(dateType);
        dest.writeInt(daysOfWeek);
        dest.writeInt(repeatPeriod);
    }

    @Override
    public void addTo(Reminder reminder) {
        reminder.setRepeat(this);
    }

    @Override
    public boolean equals(Object other) {
        if (!(other instanceof Repeat)) {
            return false;
        }
        Repeat otherRepeat = (Repeat) other;
        return otherRepeat.getDateType() == getDateType() &&
                otherRepeat.getRepeatType() == getRepeatType() &&
                otherRepeat.getDaysOfWeek() == getDaysOfWeek() &&
                otherRepeat.getRepeatPeriod() == getRepeatPeriod();
    }
}
