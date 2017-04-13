package us.bridgeses.Minder.model;

import android.content.ContentValues;
import android.database.Cursor;
import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.IntDef;
import android.support.annotation.NonNull;

import java.io.Serializable;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.EnumSet;

import static us.bridgeses.Minder.model.Style.StyleFlags.BUILD_VOLUME;
import static us.bridgeses.Minder.model.Style.StyleFlags.FADE;
import static us.bridgeses.Minder.model.Style.StyleFlags.LED;
import static us.bridgeses.Minder.model.Style.StyleFlags.NONE;
import static us.bridgeses.Minder.model.Style.StyleFlags.REPEAT_VIBRATE;
import static us.bridgeses.Minder.model.Style.StyleFlags.VIBRATE;
import static us.bridgeses.Minder.persistence.RemindersContract.Reminder.COLUMN_IMAGE;
import static us.bridgeses.Minder.persistence.RemindersContract.Reminder.COLUMN_LEDCOLOR;
import static us.bridgeses.Minder.persistence.RemindersContract.Reminder.COLUMN_STYLE;
import static us.bridgeses.Minder.persistence.RemindersContract.Reminder.COLUMN_TEXTCOLOR;

/**
 * Created by Laura on 7/9/2015.
 */
public class Style implements ReminderComponent, Parcelable, Serializable {

    @Retention(RetentionPolicy.SOURCE)
    @IntDef(flag = true, value = {StyleFlags.NONE, VIBRATE, REPEAT_VIBRATE, LED, FADE, BUILD_VOLUME})
    public @interface StyleFlags {
        int NONE = 0;
        int LED = 1;
        int VIBRATE = 1<<1;
        int REPEAT_VIBRATE = 1<<2;
        int FADE = 1<<3;
        int BUILD_VOLUME = 1<<4;
    }

    public static final int FLAGS_DEFAULT = NONE;
    public static final int LED_COLOR_DEFAULT = 0xff0000;
    public static final String IMAGE_PATH_DEFAULT = "";
    public static final int FONT_COLOR_DEFAULT = 0xffffff;

    private int flags = FLAGS_DEFAULT;
    private int ledColor = LED_COLOR_DEFAULT;
    private String imagePath = IMAGE_PATH_DEFAULT;
    private int fontColor = FONT_COLOR_DEFAULT;

    public Style() {}

    public Style(Style that) {
        setFlags(that.getFlags());
        setLedColor(that.getLedColor());
        setImagePath(that.getImagePath());
        setFontColor(that.getFontColor());
    }

    public Style(@NonNull Cursor cursor) {
        if (cursor.isAfterLast() || cursor.isBeforeFirst()) {
            throw new IllegalArgumentException("Cursor is not pointing to a valid row");
        }
        setFlags(cursor.getInt(cursor.getColumnIndex(COLUMN_STYLE)));
        setLedColor(cursor.getInt(cursor.getColumnIndex(COLUMN_LEDCOLOR)));
        setImagePath(cursor.getString(cursor.getColumnIndex(COLUMN_IMAGE)));
        setFontColor(cursor.getInt(cursor.getColumnIndex(COLUMN_TEXTCOLOR)));
    }

    public ContentValues toContentValues() {
        ContentValues values = new ContentValues();
        return toContentValues(values);
    }

    public ContentValues toContentValues(ContentValues values) {
        values.put(COLUMN_STYLE, flags);
        values.put(COLUMN_LEDCOLOR, ledColor);
        values.put(COLUMN_IMAGE, imagePath);
        values.put(COLUMN_TEXTCOLOR, fontColor);
        return values;
    }

    protected Style(Parcel in) {
        flags = in.readInt();
        ledColor = in.readInt();
        imagePath = in.readString();
        fontColor = in.readInt();
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

    public void setFlags(int flags) {
        if (flags < NONE || flags >= 1<<4) {
            throw new IllegalArgumentException("Flags out of range: " + flags);
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

    public boolean hasFlag(@StyleFlags int flag){
        @StyleFlags int flagValue = flag & flags;
        return flagValue == flag;
    }

    public int getFlags() {
        return flags;
    }

    public int getLedColor() {
        return ledColor;
    }

    public String getImagePath() {
        return imagePath;
    }

    public int getFontColor() {
        return fontColor;
    }

    @Override
    public void addTo(Reminder reminder) {
        reminder.setStyle(this);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int i) {
        dest.writeInt(flags);
        dest.writeInt(ledColor);
        dest.writeString(imagePath);
        dest.writeInt(fontColor);
    }

    public void setLedColor(int color){
        if (color < 0){
            throw new IllegalArgumentException("Color must be non-negative");
        }
        this.ledColor = color;
    }

    public void setFontColor(int color){
        if (color < 0){
            throw new IllegalArgumentException("Color must be non-negative");
        }
        this.fontColor = color;
    }

    public void setImagePath(@NonNull String path){
        this.imagePath = path;
    }


}
