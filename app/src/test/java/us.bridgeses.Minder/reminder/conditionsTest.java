package us.bridgeses.Minder.reminder;

import android.os.Parcel;

import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;

import dalvik.annotation.TestTargetClass;
import us.bridgeses.Minder.reminder.Conditions;

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
        assertEquals(conditions.getLatitude(), Conditions.LATITUDE_DEFAULT, 0.001);
        assertEquals(conditions.getLongitude(), Conditions.LONGITUDE_DEFAULT, 0.001);
        assertEquals(conditions.getLocationPreference().name(),
                Conditions.LOCATION_PREFERENCE_DEFAULT.name());
        assertEquals(conditions.getWifiPreference().name(),
                Conditions.WIFI_PREFERENCE_DEFAULT.name());
        assertEquals(conditions.getSsid(),Conditions.SSID_DEFAULT);
        assertEquals(conditions.getBluetoothPreference().name(),
                Conditions.WIFI_PREFERENCE_DEFAULT.name());
        assertEquals(conditions.getBtMacAddress(), Conditions.BT_MAC_ADDRESS_DEFAULT);
    }

    @Test
    public void testSerializable() throws IOException {
        Conditions.Builder builder = new Conditions.Builder();
        assertNotNull(builder);
        Conditions conditions = builder.build();
        assertNotNull(conditions);
        ByteArrayOutputStream byteStream = new ByteArrayOutputStream();
        ObjectOutputStream objectStream = new ObjectOutputStream(byteStream);
        objectStream.writeObject(conditions);
    }

    @Test
    public void testParcelable() {
        Conditions.Builder builder = new Conditions.Builder();
        assertNotNull(builder);
        builder.setLocationPreference(Conditions.LocationPreference.AT_LOCATION)
                .setLatitude(20)
                .setLongitude(20)
                .setWifiPreference(Conditions.WifiPreference.CONNECTED)
                .setSsid("test")
                .setBluetoothPreference(Conditions.BluetoothPreference.CONNECTED)
                .setBtMacAddress("test");
        Conditions conditions = builder.build();
        assertNotNull(conditions);
        Parcel parcel = Parcel.obtain();
        conditions.writeToParcel(parcel, 0);
        builder = new Conditions.Builder();
        Conditions result = builder.readFromParcel(parcel).build();
        assertEquals(conditions.getLatitude(), result.getLatitude(), 0.001);
        assertEquals(conditions.getLongitude(), result.getLongitude(), 0.001);
        assertEquals(conditions.getLocationPreference().name(),
                result.getLocationPreference().name());
        assertEquals(conditions.getWifiPreference().name(),
                result.getWifiPreference().name());
        assertEquals(conditions.getSsid(), result.getSsid());
        assertEquals(conditions.getBluetoothPreference().name(),
                result.getBluetoothPreference().name());
        assertEquals(conditions.getBtMacAddress(), result.getBtMacAddress());
    }
}
