package us.bridgeses.Minder.editor;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.EditTextPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceCategory;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.preference.PreferenceScreen;
import android.widget.BaseAdapter;

import com.orhanobut.logger.Logger;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

import us.bridgeses.Minder.R;
import us.bridgeses.Minder.model.Reminder;
import us.bridgeses.Minder.model.Repeat;
import us.bridgeses.Minder.views.interfaces.EditorView;

import static us.bridgeses.Minder.model.Reminder.FRIDAY;
import static us.bridgeses.Minder.model.Reminder.MONDAY;
import static us.bridgeses.Minder.model.Reminder.SATURDAY;
import static us.bridgeses.Minder.model.Reminder.SUNDAY;
import static us.bridgeses.Minder.model.Reminder.THURSDAY;
import static us.bridgeses.Minder.model.Reminder.TUESDAY;
import static us.bridgeses.Minder.model.Reminder.WEDNESDAY;
import static us.bridgeses.Minder.model.Repeat.REPEAT_DATE_TYPE_DEFAULT;
import static us.bridgeses.Minder.model.Repeat.REPEAT_PERIOD_DEFAULT;
import static us.bridgeses.Minder.model.Repeat.REPEAT_TYPE_DEFAULT;

/**
 * Created by Tony on 9/13/2014.
 */
public class RepeatFragment extends PreferenceFragment  implements
        SharedPreferences.OnSharedPreferenceChangeListener, EditorView<Repeat> {

    PreferenceScreen repeatScreenPreference;
    SharedPreferences sharedPreferences;
    PreferenceCategory dailyRepeatMenu;
    PreferenceCategory weeklyRepeatMenu;
    PreferenceCategory monthlyRepeatMenu;
    PreferenceCategory yearlyRepeatMenu;
    ListPreference repeatList;
    EditTextPreference numDaysPreference;
    EditTextPreference numWeeksPreference;
    EditTextPreference numMonthsPreference;
    EditTextPreference numYearsPreference;
    ListPreference monthlyTypePreference;
    Repeat repeat;

    private void setWeeklyTitle() {
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getActivity());
        String title;
        if (sharedPreferences.getString("temp_weeks", "0").equals("1"))
            title = "Every week";
        else
            title = "Every " + sharedPreferences.getString("temp_weeks", "0") + " weeks";
        byte daysOfWeek = setDaysOfWeek();
        if (daysOfWeek == 0){
            title = "Please select days for weekly repeat";
            numWeeksPreference.setTitle(title);
            return;
        }
        if (daysOfWeek == Reminder.ALL_WEEK) {
            title += " on each day";
            numWeeksPreference.setTitle(title);
            return;
        }
        if (daysOfWeek == Reminder.WEEKDAYS) {
            title += " on weekdays";
            numWeeksPreference.setTitle(title);
            return;
        }
        if (daysOfWeek == Reminder.WEEKENDS) {
            title += " on weekends";
            numWeeksPreference.setTitle(title);
            return;
        }
        title += " on";
        if (sharedPreferences.getBoolean("temp_sunday", false)) {
            title += " Su";
        }
        if (sharedPreferences.getBoolean("temp_monday", false)) {
            title += " Mo";
        }
        if (sharedPreferences.getBoolean("temp_tuesday", false)) {
            title += " Tu";
        }
        if (sharedPreferences.getBoolean("temp_wednesday", false)) {
            title += " We";
        }
        if (sharedPreferences.getBoolean("temp_thursday", false)) {
            title += " Th";
        }
        if (sharedPreferences.getBoolean("temp_friday", false)) {
            title += " Fr";
        }
        if (sharedPreferences.getBoolean("temp_saturday", false)) {
            title += " Sa";
        }
        numWeeksPreference.setTitle(title);
    }

    private void initSummaries(){
        repeatScreenPreference = (PreferenceScreen) super.findPreference("button_repeat_menu_key");
        dailyRepeatMenu = (PreferenceCategory) super.findPreference("daily_repeat_menu");
        yearlyRepeatMenu = (PreferenceCategory) super.findPreference("yearly_repeat_menu");
        weeklyRepeatMenu = (PreferenceCategory) super.findPreference("weekly_repeat_menu");
        monthlyRepeatMenu = (PreferenceCategory) super.findPreference("monthly_repeat_menu");
        repeatList = (ListPreference) super.findPreference("temp_repeat_type");
        numDaysPreference = (EditTextPreference) super.findPreference("temp_days");
        numWeeksPreference = (EditTextPreference) super.findPreference("temp_weeks");
        numMonthsPreference = (EditTextPreference) super.findPreference("temp_months");
        numYearsPreference = (EditTextPreference) super.findPreference("temp_years");
        monthlyTypePreference = (ListPreference) super.findPreference("temp_monthly_type");

        setRepeatSummary();
        repeatList.setSummary(repeatList.getEntry());
	    @Repeat.RepeatType int repeatType = Integer.parseInt(repeatList.getValue());
	    int numDays = Integer.parseInt(numDaysPreference.getText());
        switch (repeatType) {

            case Repeat.RepeatType.ANNUALLY:
                if (Integer.parseInt(numYearsPreference.getText()) == 1)
                    numYearsPreference.setTitle("Every year");
                else
                    numYearsPreference.setTitle("Every " + Integer.parseInt(numYearsPreference.getText()) + " years");
                break;
            case Repeat.RepeatType.DAILY:
                if (numDays == 1)
                    numDaysPreference.setTitle("Every day");
                else
                    numDaysPreference.setTitle("Every " + numDays + " days");
                break;
            case Repeat.RepeatType.MONTHLY:
                if (Integer.parseInt(numMonthsPreference.getText()) == 1)
                    numMonthsPreference.setTitle("Every month");
                else
                    numMonthsPreference.setTitle("Every " + Integer.parseInt(numMonthsPreference.getText()) + " months");
                break;
            case Repeat.RepeatType.NONE:
                break;
            case Repeat.RepeatType.WEEKLY:
                setWeeklyTitle();
                break;
        }
    }


    private void expandRepeatMenu() {
        @Repeat.RepeatType int repeatType =
                Integer.parseInt(sharedPreferences.getString("temp_repeat_type", "0"));
        switch (repeatType) {
            case Repeat.RepeatType.ANNUALLY:
                repeatScreenPreference.removePreference(dailyRepeatMenu);
                repeatScreenPreference.removePreference(weeklyRepeatMenu);
                repeatScreenPreference.removePreference(monthlyRepeatMenu);
                repeatScreenPreference.addPreference(yearlyRepeatMenu);
                numYearsPreference = (EditTextPreference) super.findPreference("temp_years");
                setPeriodTitle("temp_years","year");
                break;
            case Repeat.RepeatType.DAILY:
                repeatScreenPreference.addPreference(dailyRepeatMenu);
                repeatScreenPreference.removePreference(weeklyRepeatMenu);
                repeatScreenPreference.removePreference(monthlyRepeatMenu);
                repeatScreenPreference.removePreference(yearlyRepeatMenu);
                numDaysPreference = (EditTextPreference) super.findPreference("temp_days");
                if (numDaysPreference.getText().equals("0")){
                    numDaysPreference.setText("1");
                }
                setPeriodTitle("temp_days", "day");
                break;
            case Repeat.RepeatType.MONTHLY:
                repeatScreenPreference.removePreference(dailyRepeatMenu);
                repeatScreenPreference.removePreference(weeklyRepeatMenu);
                repeatScreenPreference.addPreference(monthlyRepeatMenu);
                repeatScreenPreference.removePreference(yearlyRepeatMenu);
                numMonthsPreference = (EditTextPreference) super.findPreference("temp_months");
                setPeriodTitle("temp_months","month");
                break;
            case Repeat.RepeatType.NONE:
                repeatScreenPreference.removePreference(dailyRepeatMenu);
                repeatScreenPreference.removePreference(weeklyRepeatMenu);
                repeatScreenPreference.removePreference(monthlyRepeatMenu);
                repeatScreenPreference.removePreference(yearlyRepeatMenu);
                break;
            case Repeat.RepeatType.WEEKLY:
                repeatScreenPreference.removePreference(dailyRepeatMenu);
                repeatScreenPreference.addPreference(weeklyRepeatMenu);
                repeatScreenPreference.removePreference(monthlyRepeatMenu);
                repeatScreenPreference.removePreference(yearlyRepeatMenu);
                if (setDaysOfWeek()==0) {
                    Calendar date = Calendar.getInstance();
                    SimpleDateFormat timeFormat = new SimpleDateFormat("h:mm aa EEEE, MMMM d, yyyy");
                    try {
                        String newDate = sharedPreferences.getString("temp_time", "") + " " + sharedPreferences.getString("temp_date", "");
                        date.setTime(timeFormat.parse(newDate));
                        int dayOfWeek = date.get(Calendar.DAY_OF_WEEK);
                        SharedPreferences.Editor editor = sharedPreferences.edit();
                        switch (dayOfWeek) {
                            case 1: {
                                editor.putBoolean("temp_sunday", true);
                                CheckBoxPreference dayPreference = (CheckBoxPreference) super.findPreference("temp_sunday");
                                dayPreference.setChecked(true);
                                break;
                            }
                            case 2: {
                                editor.putBoolean("temp_monday", true);
                                CheckBoxPreference dayPreference = (CheckBoxPreference) super.findPreference("temp_monday");
                                dayPreference.setChecked(true);
                                break;
                            }
                            case 3: {
                                editor.putBoolean("temp_tuesday", true);
                                CheckBoxPreference dayPreference = (CheckBoxPreference) super.findPreference("temp_tuesday");
                                dayPreference.setChecked(true);
                                break;
                            }
                            case 4: {
                                editor.putBoolean("temp_wednesday", true);
                                CheckBoxPreference dayPreference = (CheckBoxPreference) super.findPreference("temp_wednesday");
                                dayPreference.setChecked(true);
                                break;
                            }
                            case 5: {
                                editor.putBoolean("temp_thursday", true);
                                CheckBoxPreference dayPreference = (CheckBoxPreference) super.findPreference("temp_thursday");
                                dayPreference.setChecked(true);
                                break;
                            }
                            case 6: {
                                editor.putBoolean("temp_friday", true);
                                CheckBoxPreference dayPreference = (CheckBoxPreference) super.findPreference("temp_friday");
                                dayPreference.setChecked(true);
                                break;
                            }
                            case 7: {
                                editor.putBoolean("temp_saturday", true);
                                CheckBoxPreference dayPreference = (CheckBoxPreference) super.findPreference("temp_saturday");
                                dayPreference.setChecked(true);
                                break;
                            }
                        }
                        editor.apply();
                    } catch (ParseException e) {
                        Logger.e("Parse Error");
                    }
                }
                setWeeklyTitle();
                setPeriodTitle("temp_weeks","week");
                break;
        }
    }

    private byte setDaysOfWeek() {
        byte daysOfWeek = 0;
        if (sharedPreferences.getBoolean("temp_sunday",false)) {
            daysOfWeek += SUNDAY;
        }
        if (sharedPreferences.getBoolean("temp_monday",false)) {
            daysOfWeek += Reminder.MONDAY;
        }
        if (sharedPreferences.getBoolean("temp_tuesday",false)) {
            daysOfWeek += TUESDAY;
        }
        if (sharedPreferences.getBoolean("temp_wednesday",false)) {
            daysOfWeek += Reminder.WEDNESDAY;
        }
        if (sharedPreferences.getBoolean("temp_thursday",false)) {
            daysOfWeek += Reminder.THURSDAY;
        }
        if (sharedPreferences.getBoolean("temp_friday",false)) {
            daysOfWeek += FRIDAY;
        }
        if (sharedPreferences.getBoolean("temp_saturday",false)) {
            daysOfWeek += Reminder.SATURDAY;
        }
        return daysOfWeek;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        addPreferencesFromResource(R.xml.repeat_preference);

        repeat = new Repeat();

        initSummaries();

        expandRepeatMenu();

        getPreferenceScreen().getSharedPreferences()
                .registerOnSharedPreferenceChangeListener(this);
    }

    @Override
    public void onDestroy() {
        getPreferenceScreen().getSharedPreferences()
                .unregisterOnSharedPreferenceChangeListener(this);
        super.onDestroy();
    }

    public static RepeatFragment newInstance(){
        RepeatFragment fragment = new RepeatFragment();
        return fragment;
    }

    private Boolean setMonthlyTitle(int monthType) {
        Boolean set = false;
        Calendar date = Calendar.getInstance();
        SimpleDateFormat timeFormat = new SimpleDateFormat("h:mm aa EEEE, MMMM d, yyyy");
        try {
            String newDate = sharedPreferences.getString("temp_time", "") + " " + sharedPreferences.getString("temp_date", "");
            date.setTime(timeFormat.parse(newDate));
        }
        catch (ParseException e){
            Logger.e("Parse Error");
        }

        switch (monthType) {
            case 0: {
                int dayOfMonth = date.get(Calendar.DAY_OF_MONTH);
                monthlyTypePreference.setTitle("On the " + dayOfMonth + Reminder.appendInt(dayOfMonth) + " day of the month");
                set = true;
                break;
            }
            case 1: {
	            String nameOfDay = date.getDisplayName(Calendar.DAY_OF_WEEK, Calendar.LONG, Locale.getDefault());
                int weekOfMonth = date.get(Calendar.WEEK_OF_MONTH);
                monthlyTypePreference.setTitle("On the " +  weekOfMonth + Reminder.appendInt(weekOfMonth)
		                + " " + nameOfDay);
                set = true;
                break;
            }
            case 2: {
                int day = date.get(Calendar.DAY_OF_MONTH);
                int daysInMonth = date.getActualMaximum(Calendar.DAY_OF_MONTH);
                monthlyTypePreference.setTitle(daysInMonth - day + " days from the end of the month");
                set = true;
                break;
            }
	        case 3: {
		        String nameOfDay = date.getDisplayName(Calendar.DAY_OF_WEEK, Calendar.LONG, Locale.getDefault());
		        int weekOfMonth = date.get(Calendar.WEEK_OF_MONTH);
		        int weeksInMonth = date.getActualMaximum(Calendar.WEEK_OF_MONTH)+1;
		        int weeksFromEnd = weeksInMonth - weekOfMonth;
		        if (weeksFromEnd == 1){
			        monthlyTypePreference.setTitle("The last " + nameOfDay + " of the month");
			        set = true;
		        }
		        if (weeksFromEnd == 2){
			        monthlyTypePreference.setTitle("The next to last " + nameOfDay + " of the month");
			        set = true;
		        }
		        if (weeksFromEnd >= 3) {
			        monthlyTypePreference.setTitle(weeksFromEnd + " " + nameOfDay +
					        "s from the end of the month");
			        set = true;
		        }
		        if (!set){
			        Logger.e("Negative weeks in month");
		        }
		        break;
	        }
        }
        return set;
    }

    private Boolean setPeriodTitle(String key, String period) {
        EditTextPreference textPreference = (EditTextPreference) findPreference(key);
        if (textPreference == null){
            // No matching preference
            return false;
        }
        Boolean set = true;
        String value = sharedPreferences.getString(key,"0");
        if (key.equals("temp_days")){
            if (value.equals("1"))
                textPreference.setTitle("Every day");
            else
                textPreference.setTitle("Every " + value + " days");
        }
        else {
            if (key.equals("temp_weeks")) {
                setWeeklyTitle();
            } else {
                if (key.equals("temp_months")) {
	                if (value.equals("1"))
		                textPreference.setTitle("Every month");
	                else
		                textPreference.setTitle("Every " + value + " months");
                    setMonthlyTitle(monthlyTypePreference.findIndexOfValue(monthlyTypePreference.getValue()));
                } else {
                    if (key.equals("temp_years")) {
                        if (value.equals("1"))
                            textPreference.setTitle("Every year");
                        else
                            textPreference.setTitle("Every " + value + " years");
                    } else
                        set = false;
                }
            }
        }
        return set;
    }

    private void setRepeatSummary(){
	    sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getActivity());
        int repeatType = Integer.parseInt(sharedPreferences.getString("temp_repeat_type","0"));
        switch (repeatType) {
            case 0: {
                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.putString("temp_days", "0");
                editor.apply();
                numDaysPreference.setText("0");
                repeatScreenPreference.setSummary("Do not repeat");
                ((BaseAdapter)getPreferenceScreen().getRootAdapter()).notifyDataSetChanged();
                break;
            }
            case 1: {
                if (sharedPreferences.getString("temp_days","0").equals("0")) {
                    SharedPreferences.Editor editor = sharedPreferences.edit();
                    editor.putString("temp_days", "1");
                    editor.apply();
                    numDaysPreference.setText("1");
                    setPeriodTitle("temp_days","day");
                }
                if (sharedPreferences.getString("temp_days","0").equals("1")) {
                    repeatScreenPreference.setSummary("Repeat every day");
                }
                else
                    repeatScreenPreference.setSummary("Repeat every " + sharedPreferences.getString("temp_days", "0") + " days");
                ((BaseAdapter)getPreferenceScreen().getRootAdapter()).notifyDataSetChanged();
                break;
            }
            case 2: {
                if (sharedPreferences.getString("temp_weeks","0").equals("0")) {
                    SharedPreferences.Editor editor = sharedPreferences.edit();
                    editor.putString("temp_weeks", "1");
                    editor.apply();
                    numWeeksPreference.setText("1");
                    setPeriodTitle("temp_weeks","week");
                }
                if (sharedPreferences.getString("temp_weeks","0").equals("1")) {
                    repeatScreenPreference.setSummary("Repeat every week");
                }
                else
                    repeatScreenPreference.setSummary("Repeat every " + sharedPreferences.getString("temp_weeks", "0") + " weeks");
                ((BaseAdapter)getPreferenceScreen().getRootAdapter()).notifyDataSetChanged();
                break;
            }
            case 3: {
                if (sharedPreferences.getString("temp_months","0").equals("0")) {
                    SharedPreferences.Editor editor = sharedPreferences.edit();
                    editor.putString("temp_months", "1");
                    editor.putString("temp_monthly_type","1");
                    editor.apply();
                    numMonthsPreference.setText("1");
                    setPeriodTitle("temp_months","month");
                    setMonthlyTitle(0);
                }
                if (sharedPreferences.getString("temp_months","0").equals("1")) {
                    repeatScreenPreference.setSummary("Repeat every month");
                }
                else
                    repeatScreenPreference.setSummary("Repeat every " + sharedPreferences.getString("temp_months", "0") + " months");
                ((BaseAdapter)getPreferenceScreen().getRootAdapter()).notifyDataSetChanged();
                break;
            }
            case 4: {
                if (sharedPreferences.getString("temp_years","0").equals("0")) {
                    SharedPreferences.Editor editor = sharedPreferences.edit();
                    editor.putString("temp_years", "1");
                    editor.apply();
                    numYearsPreference.setText("1");
                    setPeriodTitle("temp_years","year");
                }
                if (sharedPreferences.getString("temp_years","0").equals("1")) {
                    repeatScreenPreference.setSummary("Repeat every year");
                }
                else
                    repeatScreenPreference.setSummary("Repeat every " + sharedPreferences.getString("temp_years", "0") + " years");
                ((BaseAdapter)getPreferenceScreen().getRootAdapter()).notifyDataSetChanged();
                break;
            }
        }
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences preference, String key) {

        Preference mPreference = findPreference(key);
        if (mPreference instanceof  EditTextPreference){
            EditTextPreference textPreference = (EditTextPreference) mPreference;
            if (!setPeriodTitle(key,"key")){
                textPreference.setSummary(textPreference.getText());
            }
            else
            setRepeatSummary();
        }
        if (mPreference instanceof CheckBoxPreference) {
            setWeeklyTitle();
        }
        if (key.equals("temp_repeat_type")) {
            repeatList.setSummary(repeatList.getEntry());
            expandRepeatMenu();

        }
        if (key.equals("temp_weeks")) {
            setWeeklyTitle();
        }
        if ((key.equals("temp_monthly_type")) || (key.equals("temp_date"))) {
            setMonthlyTitle(monthlyTypePreference.findIndexOfValue(monthlyTypePreference.getValue()));
        }
        ((BaseAdapter)getPreferenceScreen().getRootAdapter()).notifyDataSetChanged();
    }

    @Override
    public void setup(Repeat model) {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(getActivity());
        SharedPreferences.Editor editor = sp.edit();

        editor.putString("temp_repeat_type", Integer.toString(model.getRepeatType()));
        editor.putString("temp_monthly_type", Integer.toString(model.getDateType()));
        editor.putString("temp_days", Integer.toString(model.getRepeatPeriod()));
        editor.putString("temp_weeks", Integer.toString(model.getRepeatPeriod()));
        editor.putString("temp_months", Integer.toString(model.getRepeatPeriod()));
        editor.putString("temp_years", Integer.toString(model.getRepeatPeriod()));
        editor.putBoolean("temp_sunday", model.hasDayOfWeek(Repeat.DaysOfWeek.SUNDAY));
        editor.putBoolean("temp_monday", model.hasDayOfWeek(Repeat.DaysOfWeek.MONDAY));
        editor.putBoolean("temp_tuesday", model.hasDayOfWeek(Repeat.DaysOfWeek.TUESDAY));
        editor.putBoolean("temp_wednesday", model.hasDayOfWeek(Repeat.DaysOfWeek.WEDNESDAY));
        editor.putBoolean("temp_thursday", model.hasDayOfWeek(Repeat.DaysOfWeek.THURSDAY));
        editor.putBoolean("temp_friday", model.hasDayOfWeek(Repeat.DaysOfWeek.FRIDAY));
        editor.putBoolean("temp_saturday", model.hasDayOfWeek(Repeat.DaysOfWeek.SATURDAY));

        editor.commit();
        initSummaries();
    }

    @Override
    public Repeat getValues() {
        Repeat repeat = new Repeat();
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(getActivity());
        @Repeat.RepeatType int repeatType = Integer.parseInt(sp.getString("temp_repeat_type",
                Integer.toString(REPEAT_TYPE_DEFAULT)));
        repeat.setRepeatType(repeatType);
        @Repeat.DateType int dateType = Integer.parseInt(sp.getString("temp_monthly_type",
                Integer.toString(REPEAT_DATE_TYPE_DEFAULT)));
        repeat.setDateType(dateType);
        switch (repeatType) {
            case Repeat.RepeatType.ANNUALLY:
                repeat.setRepeatPeriod(Integer.parseInt(sp.getString("temp_years",
                        Integer.toString(REPEAT_PERIOD_DEFAULT))));
                break;
            case Repeat.RepeatType.DAILY:
                repeat.setRepeatPeriod(Integer.parseInt(sp.getString("temp_days",
                        Integer.toString(REPEAT_PERIOD_DEFAULT))));
                break;
            case Repeat.RepeatType.MONTHLY:
                repeat.setRepeatPeriod(Integer.parseInt(sp.getString("temp_months",
                        Integer.toString(REPEAT_PERIOD_DEFAULT))));
                break;
            case Repeat.RepeatType.NONE:
                break;
            case Repeat.RepeatType.WEEKLY:
                repeat.setRepeatPeriod(Integer.parseInt(sp.getString("temp_weeks",
                        Integer.toString(REPEAT_PERIOD_DEFAULT))));
                break;
        }
        int daysOfWeek = 0;
        if (sp.getBoolean("temp_sunday", false)) {
            daysOfWeek |= Repeat.DaysOfWeek.SUNDAY;
        }
        if (sp.getBoolean("temp_moday", false)) {
            daysOfWeek |= Repeat.DaysOfWeek.MONDAY;
        }
        if (sp.getBoolean("temp_tuesday", false)) {
            daysOfWeek |= Repeat.DaysOfWeek.TUESDAY;
        }
        if (sp.getBoolean("temp_wednesday", false)) {
            daysOfWeek |= Repeat.DaysOfWeek.WEDNESDAY;
        }
        if (sp.getBoolean("temp_thursday", false)) {
            daysOfWeek |= Repeat.DaysOfWeek.THURSDAY;
        }
        if (sp.getBoolean("temp_friday", false)) {
            daysOfWeek |= Repeat.DaysOfWeek.FRIDAY;
        }
        if (sp.getBoolean("temp_saturday", false)) {
            daysOfWeek |= Repeat.DaysOfWeek.SATURDAY;
        }
        repeat.setDaysOfWeek(daysOfWeek);
        return repeat;
    }
}
