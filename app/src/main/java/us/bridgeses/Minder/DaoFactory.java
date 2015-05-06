package us.bridgeses.Minder;

import android.content.Context;

/**
 * Created by Tony on 5/5/2015.
 */
public class DaoFactory {
    public static ReminderDAO getDao(Context context){
        ReminderSqlDao newDao = new ReminderSqlDao();
        newDao.setContext(context);
        return newDao;
    }
}
