package us.bridgeses.Minder.views;

import android.support.annotation.IntDef;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

import static us.bridgeses.Minder.views.ViewStatus.DETACHED;
import static us.bridgeses.Minder.views.ViewStatus.LOADING;
import static us.bridgeses.Minder.views.ViewStatus.PAUSED;
import static us.bridgeses.Minder.views.ViewStatus.READY;

/**
 * Created by bridgtxcdf on 2/21/2017.
 */
@Retention(RetentionPolicy.SOURCE)
@IntDef({LOADING, READY, DETACHED, PAUSED})
public @interface ViewStatus {
    int LOADING = 0;
    int READY = 1;
    int DETACHED = -1;
    int PAUSED = 2;
}
