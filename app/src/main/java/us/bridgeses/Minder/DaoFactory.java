package us.bridgeses.Minder;

import android.content.Context;

/**
 * Created by Tony on 5/5/2015.
 */
public class DaoFactory {

    private static DaoFactory instance;
    ReminderDAO customDao = null;

    private DaoFactory(){

    }

    public void setTest(ReminderDAO customDao){
        this.customDao = customDao;
    }

    public static synchronized DaoFactory getInstance(){
        if (instance == null){
            instance = new DaoFactory();
        }
        return instance;
    }

    public ReminderDAO getDao(Context context){
        ReminderDAO newDao;
        if (customDao != null){
            return customDao;
        }
        else {
            newDao = new ReminderSqlDao();
            newDao.setContext(context);
        }
        return newDao;
    }
}
