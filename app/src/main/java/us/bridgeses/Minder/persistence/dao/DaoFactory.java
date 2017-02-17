package us.bridgeses.Minder.persistence.dao;

import android.content.Context;

/**
 * A factory for getting a reference to a DAO controller object
 *
 * Note: This needs to be severely reworked
 */
public class DaoFactory {

    private static DaoFactory instance;
    private ReminderDAO customDao = null;

    private DaoFactory(){

    }

    @SuppressWarnings("unused")
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
