package us.bridgeses.Minder.reminder;

import android.content.Context;
import android.content.res.Resources;

import us.bridgeses.Minder.R;

/**
 * Created by Tony on 6/8/2015.
 */
public class Repeat {
    static Resources resources;
    public String NONE;
    public String DAILY;
    public String WEEKLY;
    public String MONTHLY;
    public String YEARLY;

    public String MONTHLY_BY_DATE;
    public String MONTHLY_BY_DAY_OF_WEEK;
    public String MONTHLY_BY_COUNT_FROM_END;
    public String MONTHLY_BY_DAY_OF_WEEK_FROM_END;

    public Repeat(Context context){
        resources = context.getResources();
        NONE = resources.getString(R.string.repeat_none);
        DAILY = resources.getString(R.string.repeat_daily);
        WEEKLY = resources.getString(R.string.repeat_weekly);
        MONTHLY = resources.getString(R.string.repeat_monthly);
        YEARLY = resources.getString(R.string.repeat_yearly);

        MONTHLY_BY_DATE = resources.getString(R.string.by_date);
        MONTHLY_BY_DAY_OF_WEEK = resources.getString(R.string.by_day_week);
        MONTHLY_BY_COUNT_FROM_END = resources.getString(R.string.by_end_month);
        MONTHLY_BY_DAY_OF_WEEK_FROM_END = resources.getString(R.string.by_end_month_weekly);
    }
}
