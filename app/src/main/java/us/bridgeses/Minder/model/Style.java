package us.bridgeses.Minder.model;

import android.content.ContentValues;
import android.database.Cursor;
import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.IntDef;
import android.support.annotation.NonNull;

import java.io.Serializable;
import java.lang.annotation.Retention;
import java.util.EnumSet;

import static java.lang.annotation.RetentionPolicy.SOURCE;
import static us.bridgeses.Minder.model.Style.StyleFlags.BUILD_VOLUME;
import static us.bridgeses.Minder.model.Style.StyleFlags.LED;
import static us.bridgeses.Minder.model.Style.StyleFlags.NONE;
import static us.bridgeses.Minder.model.Style.StyleFlags.REPEAT_VIBRATE;
import static us.bridgeses.Minder.model.Style.StyleFlags.VIBRATE;
import static us.bridgeses.Minder.persistence.RemindersContract.Reminder.COLUMN_IMAGE;
import static us.bridgeses.Minder.persistence.RemindersContract.Reminder.COLUMN_LEDCOLOR;
import static us.bridgeses.Minder.persistence.RemindersContract.Reminder.COLUMN_LEDPATTERN;
import static us.bridgeses.Minder.persistence.RemindersContract.Reminder.COLUMN_RINGTONE;
import static us.bridgeses.Minder.persistence.RemindersContract.Reminder.COLUMN_STYLE;
import static us.bridgeses.Minder.persistence.RemindersContract.Reminder.COLUMN_TEXTCOLOR;

/**
 * Created by Laura on 7/9/2015.
 */
public class Style implements ReminderComponent, Parcelable, Serializable {

    @Retention(SOURCE)
    @IntDef(flag = true, value = {NONE, LED, VIBRATE, REPEAT_VIBRATE, BUILD_VOLUME})
    public @interface StyleFlags {
        int NONE = 0;
        int LED = 1;
        int VIBRATE = 1<<1;
        int REPEAT_VIBRATE = 1<<2;
        int BUILD_VOLUME = 1<<3;
    }

    public static final int FLAGS_DEFAULT = NONE;
    public static final int LED_COLOR_DEFAULT = 0xFF0000;
    public static final int LED_PATTERN_DEFAULT = 0;
    public static final String RINGTONE_DEFAULT = "";
    public static final String IMAGE_PATH_DEFAULT = "";
    public static final int TEXT_COLOR_DEFAULT = 0xffffff;

    private int flags = FLAGS_DEFAULT;
    private int ledColor = LED_COLOR_DEFAULT;
    private int ledPattern = LED_PATTERN_DEFAULT;
    private String ringtone = RINGTONE_DEFAULT;
    private String imagePath = IMAGE_PATH_DEFAULT;
    private int textColor = TEXT_COLOR_DEFAULT;

    public Style() {

    }

    public Style(Style that) {
        setFlags(that.getFlags());
        setLedColor(that.getLedColor());
        setLedPattern(that.getLedPattern());
        setRingtone(that.getRingtone());
        setImagePath(that.getImagePath());
        setTextColor(that.getTextColor());
    }

    public Style(@NonNull Cursor cursor) {
        if (cursor.isAfterLast() || cursor.isBeforeFirst()) {
            throw new IllegalArgumentException("Cursor is not pointing to a valid row");
        }
        setFlags(cursor.getInt(cursor.getColumnIndex(COLUMN_STYLE)));
        setLedColor(cursor.getInt(cursor.getColumnIndex(COLUMN_LEDCOLOR)));
        setLedPattern(cursor.getInt(cursor.getColumnIndex(COLUMN_LEDPATTERN)));
        setRingtone(cursor.getString(cursor.getColumnIndex(COLUMN_RINGTONE)));
        setImagePath(cursor.getString(cursor.getColumnIndex(COLUMN_IMAGE)));
        setTextColor(cursor.getInt(cursor.getColumnIndex(COLUMN_TEXTCOLOR)));
    }

    protected Style(Parcel in) {
        ledColor = in.readInt();
        ledPattern = in.readInt();
        ringtone = in.readString();
        imagePath = in.readString();
        textColor = in.readInt();
    }

    public ContentValues toContentValues() {
        ContentValues values = new ContentValues();
        return toContentValues(values);
    }

    public ContentValues toContentValues(ContentValues values) {
        values.put(COLUMN_STYLE, flags);
        values.put(COLUMN_LEDCOLOR, ledColor);
        values.put(COLUMN_LEDPATTERN, ledPattern);
        values.put(COLUMN_RINGTONE, ringtone);
        values.put(COLUMN_IMAGE, imagePath);
        values.put(COLUMN_TEXTCOLOR, textColor);
        return values;
    }

    public void addTo(Reminder reminder) {
        reminder.setStyle(this);
    }

    public void setFlags(int flags) {
        if (flags < 0 || flags >= BUILD_VOLUME<<1) {
            throw new IllegalArgumentException("Style flags out of range: flags");
        }
        this.flags = flags;
    }

    public void setFlag(@StyleFlags int flag, boolean value) {
        if (value) {
            setFlags(flags|flag);
        }
        else {
            setFlags(flags&~flag);
        }
    }

    public void setLedColor(int color){
        this.ledColor = color;
    }

    public void setLedPattern(int pattern) {
        this.ledPattern = pattern;
    }

    public void setRingtone(String ringtone) {
        this.ringtone = ringtone;
    }

    public void setTextColor(int color){
        this.textColor = color;
    }

    public void setImagePath(@NonNull String path){
        this.imagePath = path;
    }

    public int getFlags() {
        return flags;
    }

    public int getLedColor() {
        return ledColor;
    }

    public int getLedPattern() {
        return ledPattern;
    }

    public String getRingtone() {
        return ringtone;
    }

    public String getImagePath() {
        return imagePath;
    }

    public int getTextColor() {
        return textColor;
    }

    public boolean hasFlag(@StyleFlags int flag){
        @StyleFlags int flagValue = flag & flags;
        return flagValue == flag;
    }

    public static final Creator<Style> CREATOR = new Creator<Style>() {
        @Override
        public Style createFromParcel(Parcel in) {
            return new Style(in);
        }

        @Override
        public Style[] newArray(int size) {
            return new Style[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(ledColor);
        dest.writeInt(ledPattern);
        dest.writeString(ringtone);
        dest.writeString(imagePath);
        dest.writeInt(textColor);
    }
}
