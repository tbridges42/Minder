package us.bridgeses.Minder.reminder;

import org.junit.Test;
import  static org.junit.Assert.*;
/**
 * Created by Laura on 7/9/2015.
 */
public class conditionsTest {

    @Test
    public void defaultBuilderWorks(){
        Conditions.Builder builder = new Conditions.Builder();
        assertNotNull(builder);
        Conditions conditions = builder.build();
        assertNotNull(conditions);
        assertEquals(conditions.getLatitude(), 0.0, 0.001);
        assertEquals(conditions.getLongitude(), 0.0, 0.001);
        assertEquals(conditions.getLocationPreference().name(), Conditions.LocationPreference.NONE.name());
        assertEquals(conditions.getWifiPreference().name(), Conditions.WifiPreference.NONE.name());
        assertEquals(conditions.getSsid(),"");
        assertEquals(conditions.getBluetoothPreference().name(), Conditions.WifiPreference.NONE.name());
        assertEquals(conditions.getBtMacAddress(), "");
    }
}
