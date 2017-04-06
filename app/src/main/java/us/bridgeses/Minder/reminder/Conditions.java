package us.bridgeses.Minder.reminder;

import android.content.ContentValues;
import android.database.Cursor;
import android.location.Location;
import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.NonNull;

import com.google.android.gms.maps.model.LatLng;

import java.io.Serializable;

import static us.bridgeses.Minder.Reminder.ONLY_AT_LOCATION;
import static us.bridgeses.Minder.Reminder.UNTIL_LOCATION;
import static us.bridgeses.Minder.Reminder.WIFINEEDED;
import static us.bridgeses.Minder.persistence.RemindersContract.Reminder.COLUMN_BLUETOOTH_MAC_ADDRESS;
import static us.bridgeses.Minder.persistence.RemindersContract.Reminder.COLUMN_BLUETOOTH_PREFERENCE;
import static us.bridgeses.Minder.persistence.RemindersContract.Reminder.COLUMN_LATITUDE;
import static us.bridgeses.Minder.persistence.RemindersContract.Reminder.COLUMN_LOCATION_PREFERENCE;
import static us.bridgeses.Minder.persistence.RemindersContract.Reminder.COLUMN_LONGITUDE;
import static us.bridgeses.Minder.persistence.RemindersContract.Reminder.COLUMN_RADIUS;
import static us.bridgeses.Minder.persistence.RemindersContract.Reminder.COLUMN_SSID;
import static us.bridgeses.Minder.persistence.RemindersContract.Reminder.COLUMN_WIFI_PREFERENCE;
import static us.bridgeses.Minder.reminder.Conditions.LocationPreference.AT_LOCATION;
import static us.bridgeses.Minder.reminder.Conditions.LocationPreference.AWAY_FROM_LOCATION;
import static us.bridgeses.Minder.reminder.Conditions.WifiPreference.CONNECTED;

/**
 * Created by Laura on 7/9/2015.
 */
public final class Conditions implements Parcelable, Serializable {

    public int getRadius() {
        return radius;
    }

    public static enum LocationPreference{
        NONE,
        AT_LOCATION,
        AWAY_FROM_LOCATION
    }

    public static enum WifiPreference{
        NONE,
        NEAR,
        CONNECTED,
        NOT_NEAR,
        NOT_CONNECTED
    }

    public static enum BluetoothPreference{
        NONE,
        NEAR,
        CONNECTED,
        NOT_NEAR,
        NOT_CONNECTED
    }

    public static final LocationPreference LOCATION_PREFERENCE_DEFAULT = LocationPreference.NONE;
    public static final double LATITUDE_DEFAULT = 0.0;
    public static final double LONGITUDE_DEFAULT = 0.0;
    public static final WifiPreference WIFI_PREFERENCE_DEFAULT = WifiPreference.NONE;
    public static final String SSID_DEFAULT = "";
    public static final BluetoothPreference BLUETOOTH_PREFERENCE_DEFAULT = BluetoothPreference.NONE;
    public static final String BT_MAC_ADDRESS_DEFAULT = "";
    public static final int RADIUS_DEFAULT = 200;

    private LocationPreference locationPreference = LOCATION_PREFERENCE_DEFAULT;
    private double latitude = LATITUDE_DEFAULT;
    private double longitude = LONGITUDE_DEFAULT;
    private int radius = RADIUS_DEFAULT;
    private WifiPreference wifiPreference = WIFI_PREFERENCE_DEFAULT;
    private String ssid = SSID_DEFAULT;
    private BluetoothPreference bluetoothPreference = BLUETOOTH_PREFERENCE_DEFAULT;
    private String btMacAddress = BT_MAC_ADDRESS_DEFAULT;

    public Conditions() {

    }

    public Conditions(byte conditions) {
        if (getBitwise(conditions, UNTIL_LOCATION)) {
            locationPreference = AWAY_FROM_LOCATION;
        }
        if (getBitwise(conditions, ONLY_AT_LOCATION)) {
            locationPreference = AT_LOCATION;
        }
        if (getBitwise(conditions, WIFINEEDED)) {
            wifiPreference = CONNECTED;
        }
    }

    public Conditions(Parcel parcel){
        setLocationPreference(LocationPreference.valueOf(parcel.readString()));
        setLatitude(parcel.readDouble());
        setLongitude(parcel.readDouble());
        setWifiPreference(WifiPreference.valueOf(parcel.readString()));
        setSsid(parcel.readString());
        setBluetoothPreference(BluetoothPreference.valueOf(parcel.readString()));
        setBtMacAddress(parcel.readString());
    }

    public Conditions(Conditions conditions){
        setLocationPreference(conditions.locationPreference);
        setLongitude(conditions.longitude);
        setLatitude(conditions.latitude);
        setWifiPreference(conditions.wifiPreference);
        setSsid(conditions.ssid);
        setBluetoothPreference(conditions.bluetoothPreference);
        setBtMacAddress(conditions.btMacAddress);
    }

    public Conditions(@NonNull Cursor cursor) {
        if (cursor.isAfterLast() || cursor.isBeforeFirst()) {
            throw new IllegalArgumentException("Cursor is not pointing to a valid row");
        }
        setLocationPreference(LocationPreference.valueOf(
                cursor.getString(cursor.getColumnIndex(COLUMN_LOCATION_PREFERENCE))
        ));
        setLatitude(cursor.getFloat(cursor.getColumnIndex(COLUMN_LATITUDE)));
        setLongitude(cursor.getFloat(cursor.getColumnIndex(COLUMN_LONGITUDE)));
        setRadius(cursor.getInt(cursor.getColumnIndex(COLUMN_RADIUS)));
        setWifiPreference(WifiPreference.valueOf(
                cursor.getString(cursor.getColumnIndex(COLUMN_WIFI_PREFERENCE))
        ));
        setSsid(cursor.getString(cursor.getColumnIndex(COLUMN_SSID)));
        setBluetoothPreference(BluetoothPreference.valueOf(
                cursor.getString(cursor.getColumnIndex(COLUMN_BLUETOOTH_PREFERENCE))
        ));
        setBtMacAddress(cursor.getString(cursor.getColumnIndex(COLUMN_BLUETOOTH_MAC_ADDRESS)));
    }

    public ContentValues toContentValues() {
        ContentValues values = new ContentValues();
        return toContentValues(values);
    }

    public ContentValues toContentValues(@NonNull ContentValues contentValues) {
        contentValues.put(COLUMN_LOCATION_PREFERENCE, locationPreference.name());
        contentValues.put(COLUMN_LATITUDE, latitude);
        contentValues.put(COLUMN_LONGITUDE, longitude);
        contentValues.put(COLUMN_RADIUS, radius);
        contentValues.put(COLUMN_WIFI_PREFERENCE, wifiPreference.name());
        contentValues.put(COLUMN_SSID, ssid);
        contentValues.put(COLUMN_BLUETOOTH_PREFERENCE, bluetoothPreference.name());
        contentValues.put(COLUMN_BLUETOOTH_MAC_ADDRESS, btMacAddress);
        return contentValues;
    }

    private boolean getBitwise(byte store, byte key){
        return (store & key) == key;
    }

    public LocationPreference getLocationPreference() {
        return locationPreference;
    }

    public double getLatitude() {
        return latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public LatLng getLatLng() {
        return new LatLng(latitude, longitude);
    }

    public WifiPreference getWifiPreference() {
        return wifiPreference;
    }

    public String getSsid() {
        return ssid;
    }

    public BluetoothPreference getBluetoothPreference() {
        return bluetoothPreference;
    }

    public String getBtMacAddress() {
        return btMacAddress;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int returnCode)
    {
        parcel.writeString(locationPreference.name());
        parcel.writeDouble(latitude);
        parcel.writeDouble(longitude);
        parcel.writeString(wifiPreference.name());
        parcel.writeString(ssid);
        parcel.writeString(bluetoothPreference.name());
        parcel.writeString(btMacAddress);
    }

    public static final Creator<Conditions> CREATOR = new Creator<Conditions>() {
        @Override
        public Conditions createFromParcel(Parcel parcel) {
            return new Conditions(parcel);
        }

        @Override
        public Conditions[] newArray(int i) {
            return new Conditions[i];
        }
    };

    public void setLocationPreference(@NonNull LocationPreference locationPreference){
        this.locationPreference = locationPreference;
    }

    public void setLatitude(double latitude){
        if ((latitude > 90)||(latitude < -90)){
            throw new IllegalArgumentException("Min: -90, Max: 90, value: " + latitude);
        }
        this.latitude = latitude;
    }

    public void setLongitude(double longitude){
        if ((longitude > 180)||(longitude < -180)){
            throw new IllegalArgumentException("Min: -180, Max: 180, value: " + longitude);
        }
        this.longitude = longitude;
    }

    public void setLatLng(LatLng latLng){
        if (latLng == null) {
            setLatitude(LATITUDE_DEFAULT);
            setLongitude(LONGITUDE_DEFAULT);
        }
        else {
            setLatitude(latLng.latitude);
            setLongitude(latLng.longitude);
        }
    }

    public void setLocation(Location location){
        if (location == null){
            setLatitude(LATITUDE_DEFAULT);
            setLongitude(LONGITUDE_DEFAULT);
        }
        else {
            setLatitude(location.getLatitude());
            setLongitude(location.getLongitude());
        }
    }

    public void setWifiPreference(@NonNull WifiPreference wifiPreference){
        this.wifiPreference = wifiPreference;
    }

    public void setSsid(@NonNull String ssid){
        if (ssid.isEmpty() && wifiPreference != null && wifiPreference != WifiPreference.NONE) {
            throw new IllegalArgumentException("SSID cannot be blank if wifi preference is not NONE or null");
        }
        if (ssid.length() > 32) {
            throw new IllegalArgumentException("SSID cannot be more than 32 characters");
        }
        this.ssid = ssid;
    }

    public void setBluetoothPreference(@NonNull BluetoothPreference bluetoothPreference){
        this.bluetoothPreference = bluetoothPreference;
    }

    public void setBtMacAddress(@NonNull String btMacAddress){
        if (btMacAddress.isEmpty() && bluetoothPreference != null
                && bluetoothPreference != BluetoothPreference.NONE) {
            throw new IllegalArgumentException("BT Mac Address cannot be blank if " +
                    "BT preference is not NONE or null");
        }
        if (!btMacAddress.isEmpty() &&
                !btMacAddress.matches("(?i)^([0-9A-F]{2}[:-]){5}([0-9A-F]{2})$")) {
            throw new IllegalArgumentException("Invalid Mac address");
        }
        this.btMacAddress = btMacAddress;
    }

    public void setRadius(int radius){
        if (0 < radius){
            this.radius = radius;
        }
        else {
            throw new IllegalArgumentException("Radius must be greater than zero");
        }
    }
}