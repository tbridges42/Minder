package us.bridgeses.Minder.util;

import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.res.TypedArray;
import android.os.Parcel;
import android.os.Parcelable;
import android.preference.DialogPreference;
import android.util.AttributeSet;
import android.view.View;
import android.widget.TimePicker;

import com.orhanobut.logger.Logger;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

/**
 * Created by Tony on 8/30/2014.
 */
public class TimePreference extends DialogPreference implements TimePicker.OnTimeChangedListener{
    private String timeString;
    private String changedValueCanBeNull;
    private TimePicker timePicker;
	public static String time = "h:mm aa";

    public TimePreference(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }
    public TimePreference(Context context, AttributeSet attrs) {
        super(context, attrs);
    }
    public static SimpleDateFormat formatter() {
        return new SimpleDateFormat(time);
    }
    public static SimpleDateFormat summaryFormatter() {
        return new SimpleDateFormat(time);
    }
    public static String defaultCalendarString() {
        return formatter().format(defaultCalendar().getTime());
    }
    /**
     * Set the selected date to the specified string.
     *
     * @param timeString
     * The date, represented as a string, in the format specified by
     * {@link #formatter()}.
     */
    public void setTime(String timeString) {
        this.timeString = timeString;
    }
    private String defaultValue() {
        if (this.timeString == null)
            setTime(defaultCalendarString());
        return this.timeString;
    }
    public static Calendar defaultCalendar() {
        return new GregorianCalendar(1970, 0, 1);
    }
    @Override
    protected Object onGetDefaultValue(TypedArray a, int index) {
        return a.getString(index);
    }
    public Calendar getTime() {
        try {
            Date date = formatter().parse(defaultValue());

            Calendar cal = Calendar.getInstance();
            cal.setTime(date);
            return cal;
        } catch (java.text.ParseException e) {
            return Calendar.getInstance();
        }
    }
    /**
     * Called when the user changes the date.
     */
    public void onTimeChanged(TimePicker view, int hour, int minute) {
        Calendar selected = Calendar.getInstance();
        selected.set(Calendar.HOUR_OF_DAY,hour);
        selected.set(Calendar.MINUTE,minute);
        this.changedValueCanBeNull = formatter().format(selected.getTime());
    }
    protected View onCreateDialogView() {
        this.timePicker = new TimePicker(getContext());
        Calendar calendar = getTime();
        timePicker.setCurrentHour(calendar.get(Calendar.HOUR_OF_DAY));
        timePicker.setCurrentMinute(calendar.get(Calendar.MINUTE));
        return timePicker;
    }
    /**
     * Called when the date picker is shown or restored. If it's a restore it gets
     * the persisted value, otherwise it persists the value.
     */
    @Override
    protected void onSetInitialValue(boolean restoreValue, Object def) {
        if (restoreValue) {
            this.timeString = getPersistedString(defaultValue());
            setTheTime(this.timeString);
        } else {
            boolean wasNull = this.timeString == null;
            setTime((String) def);
            if (!wasNull)
                persistTime(this.timeString);
        }
    }
    /**
     * Called when Android pauses the activity.
     */
    @Override
    protected Parcelable onSaveInstanceState() {
        if (isPersistent())
            return super.onSaveInstanceState();
        else
            return new TimeSavedState(super.onSaveInstanceState());
    }
    /**
     * Called when Android restores the activity.
     */
    @Override
    protected void onRestoreInstanceState(Parcelable state) {
        if (state == null || !state.getClass().equals(TimeSavedState.class)) {
            super.onRestoreInstanceState(state);
            try {
                setTheTime(((TimeSavedState) state).timeValue);
            }
            catch (ClassCastException e) {
                Logger.e("Reset Time");
                setTheTime(defaultValue());
            }
        } else {
            Logger.e("Something weird happening");
            TimeSavedState s = (TimeSavedState) state;
            super.onRestoreInstanceState(s.getSuperState());
            setTheTime(s.timeValue);
        }
    }
    /**
     * Called when the dialog is closed. If the close was by pressing "OK" it
     * saves the value.
     */
    @Override
    protected void onDialogClosed(boolean shouldSave) {
        if (shouldSave && this.changedValueCanBeNull != null) {
            setTheTime(this.changedValueCanBeNull);
            this.changedValueCanBeNull = null;
        }
    }
    private void setTheTime(String s) {
        setTime(s);
        persistTime(s);
    }
    private void persistTime(String s) {
        persistString(s);
        setSummary(summaryFormatter().format(getTime().getTime()));
    }
    /**
     * Called whenever the user clicks on a button. Invokes {@link #onTimeChanged(TimePicker, int, int)}
     * and {@link #onDialogClosed(boolean)}. Be sure to call the super when overriding.
     */
    @Override
    public void onClick(DialogInterface dialog, int which) {
        super.onClick(dialog, which);
        timePicker.clearFocus();
        onTimeChanged(timePicker, timePicker.getCurrentHour(), timePicker.getCurrentMinute());
        onDialogClosed(which == DialogInterface.BUTTON1); // OK?
    }
    /**
     * Produces the date the user has selected for the given preference, as a
     * calendar.
     *
     * @param preferences
     * the SharedPreferences to get the date from
     * @param field
     * the name of the preference to get the date from
     * @return a Calendar that the user has selected
     */
    public static Calendar getDateFor(SharedPreferences preferences, String field) {
        Date date = stringToTime(preferences.getString(field,
                defaultCalendarString()));
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        return cal;
    }
    private static Date stringToTime(String dateString) {
        try {
            return formatter().parse(dateString);
        } catch (ParseException e) {
            return defaultCalendar().getTime();
        }
    }
    private static class TimeSavedState extends BaseSavedState {
        String timeValue;
        public TimeSavedState(Parcel p) {
            super(p);
            timeValue = p.readString();
        }
        public TimeSavedState(Parcelable p) {
            super(p);
        }
        @Override
        public void writeToParcel(Parcel out, int flags) {
            super.writeToParcel(out, flags);
            out.writeString(timeValue);
        }
        @SuppressWarnings("unused")
        public static final Parcelable.Creator<TimeSavedState> CREATOR = new Parcelable.Creator<TimeSavedState>() {
            public TimeSavedState createFromParcel(Parcel in) {
                return new TimeSavedState(in);
            }
            public TimeSavedState[] newArray(int size) {
                return new TimeSavedState[size];
            }
        };
    }
}
