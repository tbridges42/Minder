package us.bridgeses.Minder.reminder;

import android.location.Location;
import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.NonNull;

import com.google.android.gms.maps.model.LatLng;

import java.io.Serializable;

/**
 * Created by Laura on 7/9/2015.
 */
public final class Conditions implements Parcelable, Serializable {

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

    private LocationPreference locationPreference;
    private double latitude;
    private double longitude;
    private WifiPreference wifiPreference;
    private String ssid;
    private BluetoothPreference bluetoothPreference;
    private String btMacAddress;

    public LocationPreference getLocationPreference() {
        return locationPreference;
    }

    public double getLatitude() {
        return latitude;
    }

    public double getLongitude() {
        return longitude;
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
            return new Builder().readFromParcel(parcel).build();
        }

        @Override
        public Conditions[] newArray(int i) {
            return new Conditions[0];
        }
    };

    private Conditions(Builder builder){
        this.locationPreference = builder.locationPreference;
        this.latitude = builder.latitude;
        this.longitude = builder.longitude;
        this.wifiPreference = builder.wifiPreference;
        this.ssid = builder.ssid;
        this.bluetoothPreference = builder.bluetoothPreference;
        this.btMacAddress = builder.btMacAddress;
    }

    public static final class Builder{
        private LocationPreference locationPreference = LocationPreference.NONE;
        private double latitude = 0.0;
        private double longitude = 0.0;
        private WifiPreference wifiPreference = WifiPreference.NONE;
        private String ssid = "";
        private BluetoothPreference bluetoothPreference = BluetoothPreference.NONE;
        private String btMacAddress = "";

        public Builder(){ }

        public Builder setLocationPreference(@NonNull LocationPreference locationPreference){
            this.locationPreference = locationPreference;
            return this;
        }

        public Builder setLatitude(double latitude){
            if ((latitude > 90)||(latitude < -90)){
                throw new IllegalArgumentException("Min: -90, Max: 90, value: " + latitude); 
            }
            this.latitude = latitude;
            return this;
        }

        public Builder setLongitude(double longitude){
            if ((longitude > 180)||(longitude < -180)){
                throw new IllegalArgumentException("Min: -180, Max: 180, value: " + longitude);
            }
            this.longitude = longitude;
            return this;
        }

        public Builder setLatLng(@NonNull LatLng latLng){
            return setLatitude(latLng.latitude).setLongitude(latLng.longitude);
        }

        public Builder setLocation(Location location){
            if (location == null){
                throw new NullPointerException();
            }
            return setLatitude(location.getLatitude()).setLongitude(location.getLongitude());
        }

        public Builder setWifiPreference(@NonNull WifiPreference wifiPreference){
            this.wifiPreference = wifiPreference;
            return this;
        }

        public Builder setSsid(@NonNull String ssid){
            this.ssid = ssid;
            return this;
        }

        public Builder setBluetoothPreference(@NonNull BluetoothPreference bluetoothPreference){
            this.bluetoothPreference = bluetoothPreference;
            return this;
        }

        public Builder setBtMacAddress(@NonNull String btMacAddress){
            this.btMacAddress = btMacAddress;
            return this;
        }

        public Builder copyConditions(Conditions conditions){
            this.locationPreference = conditions.locationPreference;
            this.longitude = conditions.longitude;
            this.latitude = conditions.latitude;
            this.wifiPreference = conditions.wifiPreference;
            this.ssid = conditions.ssid;
            this.bluetoothPreference = conditions.bluetoothPreference;
            this.btMacAddress = conditions.btMacAddress;
            return this;
        }

        public Builder readFromParcel(Parcel parcel){
            this.setLocationPreference(LocationPreference.valueOf(parcel.readString()));
            this.setLatitude(parcel.readDouble());
            this.setLongitude(parcel.readDouble());
            this.setWifiPreference(WifiPreference.valueOf(parcel.readString()));
            this.setSsid(parcel.readString());
            this.setBluetoothPreference(BluetoothPreference.valueOf(parcel.readString()));
            this.setBtMacAddress(parcel.readString());
            return this;
        }

        public Conditions build(){
            return new Conditions(this);
        }
    }
}