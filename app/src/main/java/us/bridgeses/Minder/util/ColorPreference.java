package us.bridgeses.Minder.util;

import android.content.Context;
import android.util.AttributeSet;

import yuku.ambilwarna.widget.AmbilWarnaPreference;

/**
 * Created by Tony on 5/22/2015.
 */
public class ColorPreference extends AmbilWarnaPreference {

    public ColorPreference(Context context, AttributeSet attributeSet){
        super(context,attributeSet);
        forceSetValue(getPersistedInt(0));
    }
}
