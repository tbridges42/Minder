package us.bridgeses.Minder;

import android.content.Context;

/**
 * Created by Tony on 5/5/2015.
 */
public class DaoFactory {

    private static DaoFactory instance;
    private ReminderDAO customDao = null;

    private DaoFactory(){

    }

    public void setTest(ReminderDAO customDao){
        this.customDao = customDao;
    }

    // TODO: This is a bad pattern for a factory; it should be passed into the using class
    // by a controlling class, rather than getting a singleton instance when used
    public static synchronized DaoFactory getInstance(){
        if (instance == null){
            instance = new DaoFactory();
        }
        return instance;
    }

    public ReminderDAO getDao(Context context){
        ReminderDAO newDao;
        if (customDao != null){
            newDao = customDao;
        }
        else {
            newDao = new ReminderSqlDao();
            newDao.setContext(context);
        }
        return newDao;
    }
}
