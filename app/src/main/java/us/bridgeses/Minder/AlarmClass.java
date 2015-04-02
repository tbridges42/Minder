package us.bridgeses.Minder;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Configuration;
import android.database.sqlite.SQLiteDatabase;
import android.location.Location;
import android.net.ConnectivityManager;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.util.Log;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.model.LatLng;

import java.util.Calendar;
import java.util.concurrent.Executors;

import us.bridgeses.Minder.receivers.ReminderReceiver;

/**
 * Created by Laura on 3/1/2015.
 */
public class AlarmClass implements Runnable, GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener, LocationListener {
    private int id = -1;
    private int snoozeNum = 0;
    private Context context;
    private Reminder reminder;
    private boolean hasLocation;
    private boolean hasWiFi;
    private boolean hasBT;
    private boolean urgentRequest;
    private WifiReceiver wifiReceiver;
    private GoogleApiClient mGoogleApiClient;


    public AlarmClass(Context context, int id){
        this.context = context;
        this.id = id;
    }

    public AlarmClass(Context context, int id, int snoozeNum){
        this.context = context;
        this.id = id;
        this.snoozeNum = snoozeNum;
    }

    public AlarmClass(){
    }

    private void retrieveReminder(){
        if (id == -1){
            Log.w("Minder", "Invalid ID");
            return;
        }
        if (context == null){
            Log.e("Minder","Invalid context");
            return;
        }
        ReminderDBHelper dbHelper  = ReminderDBHelper.getInstance(context);
        SQLiteDatabase database = dbHelper.openDatabase();
        reminder = Reminder.getReminder(database,id);
        dbHelper.closeDatabase();
    }

    private void alarm(){
        Intent intent = new Intent(context,AlarmScreen.class);
        intent.putExtra("Id",id);
        intent.putExtra("SnoozeNum",snoozeNum);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(intent);
    }

    protected void startLocationUpdates(LocationRequest mLocationRequest) {
        LocationServices.FusedLocationApi.requestLocationUpdates(
                mGoogleApiClient, mLocationRequest, this);
    }

    protected void stopLocationUpdates() {
        LocationServices.FusedLocationApi.removeLocationUpdates(
                mGoogleApiClient, this);
    }

    protected LocationRequest createLocationRequest() {
        urgentRequest = false;
        LocationRequest mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(Reminder.MINUTE*15);
        mLocationRequest.setFastestInterval(Reminder.MINUTE);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY);
        mLocationRequest.setSmallestDisplacement(reminder.getRadius());
        return mLocationRequest;
    }

    protected LocationRequest createUrgentLocationRequest() {
        urgentRequest = true;
        LocationRequest mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(10000);
        mLocationRequest.setFastestInterval(1000);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        return mLocationRequest;
    }

    private void snooze(int duration){
        Intent intentAlarm = new Intent(context, ReminderReceiver.class);//Create alarm intent
        int id = reminder.getId();
        intentAlarm.putExtra("Id", id);           //Associate intent with specific reminder
        intentAlarm.putExtra("Snooze",snoozeNum);                       //Increment snooze count
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        int alarmType;
        if (reminder.getWakeUp()){
            alarmType = AlarmManager.RTC_WAKEUP;
        }
        else {
            alarmType = AlarmManager.RTC;
        }
        alarmManager.set(alarmType, Calendar.getInstance().getTimeInMillis()+duration,
                PendingIntent.getBroadcast(context, id, intentAlarm, PendingIntent.FLAG_UPDATE_CURRENT));
    }

    @Override
    public void onLocationChanged(Location mLastLocation) {
        Log.i("Minder","Location updated");
        LatLng location = reminder.getLocation();
        float results[] = new float[1];
        Location.distanceBetween(mLastLocation.getLatitude(),mLastLocation.getLongitude()
                ,location.latitude,location.longitude,results);
        if (results[0] <= reminder.getRadius()){
            Log.i("Minder","At Location");
            if (reminder.getOnlyAtLocation()) {
                Log.i("Minder","Supposed to be at location");
                hasLocation = true;
                stopLocationUpdates();
                checkConditions();
            }
            else {
                Log.i("Minder","Not supposed to be at location");
                snooze(Reminder.MINUTE);
            }
        }
        else{
            Log.i("Minder","Not at Location");
            if (reminder.getUntilLocation()){
                Log.i("Minder","Not supposed to be at location");
                hasLocation = true;
                stopLocationUpdates();
                checkConditions();
            }
            else {
                snooze(Reminder.MINUTE);
            }
        }
    }

    @Override
    public void onConnected(Bundle connectionHint) {
        Log.i("Minder","Connected to location service");
        Location mLastLocation = LocationServices.FusedLocationApi.getLastLocation(
                mGoogleApiClient);
        LatLng location = reminder.getLocation();

        long timeDelta = Calendar.getInstance().getTimeInMillis() - mLastLocation.getTime();

        if ((mLastLocation == null) ||  (timeDelta > (Reminder.MINUTE * 10))
                ||mLastLocation.getAccuracy() > 1.5*reminder.getRadius()){
            Log.i("Minder","Location is stale");
            try {
                startLocationUpdates(createUrgentLocationRequest());
            }
            catch (Exception e){
                Log.e("Minder","Unable to start location updates");
            }
        }

        float results[] = new float[1];
        Location.distanceBetween(mLastLocation.getLatitude(),mLastLocation.getLongitude()
                ,location.latitude,location.longitude,results);
        if (results[0] <= reminder.getRadius()){
            Log.i("Minder","At Location");
            if (reminder.getOnlyAtLocation()){
                Log.i("Minder","Supposed to be at location");
                hasLocation = true;
                checkConditions();
            }
            else {
                if (reminder.getUntilLocation()) {
                    Log.i("Minder","Not supposed to be at Location");
                    try {
                        snooze(Reminder.MINUTE);
                    }
                    catch (Exception e){
                        Log.e("Minder","Unable to start location updates");
                    }
                }
            }
        }
        else{
            Log.i("Minder","Not at location");
            if (reminder.getUntilLocation()){
                Log.i("Minder","Not supposed to be at location");
                hasLocation = true;
                checkConditions();
            }
            else {
                if (reminder.getOnlyAtLocation()) {
                    Log.i("Minder","Supposed to be at location");
                    try {
                        snooze(reminder.getSnoozeDuration());
                    }
                    catch (Exception e){
                        Log.e("Minder","Unable to start location updates");
                    }
                }
            }
        }
    }

    @Override
    public void onConnectionSuspended(int i){
        Log.w("Minder","Connection Suspended");
    }

    public void onConnectionFailed(ConnectionResult result){
        Log.w("Minder", "Connection Failed");
    }

    private void checkLocation(){
        Log.v("Minder","Checking for location");
        try {
            mGoogleApiClient = new GoogleApiClient.Builder(context)
                    .addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this)
                    .addApi(LocationServices.API)
                    .build();
            mGoogleApiClient.connect();
            Log.i("Minder","API Client built");
        }
        catch (Exception e) {
            Log.e("Minder","API Client failed to build");
        }
    }

    private void checkWifi(){
        Log.v("Minder","Checking Wifi");
        if (wifiReceiver != null){
            context.unregisterReceiver(wifiReceiver);
            wifiReceiver = null;
        }
        WifiManager wifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
        if (wifiManager.isWifiEnabled()){
            WifiInfo wifiInfo = wifiManager.getConnectionInfo();
            String curSSID = wifiInfo.getSSID();
            curSSID = curSSID.replaceAll("^\"|\"$", "");
            if (curSSID.equals(reminder.getSSID())){
                hasWiFi = true;
                checkConditions();
            }
        }
        else {
            snooze(Reminder.MINUTE);
        }
    }

    private void checkBT(){
        hasBT = true;
        checkConditions();
    }

    private void checkConditions(){
        if (hasLocation && hasBT && hasWiFi){
            alarm();
        }
        else {
            if (!hasLocation) {
                checkLocation();
            }
            if (!hasWiFi) {
                checkWifi();
            }
            if (!hasBT) {
                checkBT();
            }
        }
    }

    private void initConditions(){
        hasLocation = !((reminder.getOnlyAtLocation()) || (reminder.getUntilLocation()));
        hasWiFi = !reminder.getNeedWifi();
        hasBT = !reminder.getNeedBluetooth();
    }

    public void run(){
        Log.v("Minder","Reminder fired");
        retrieveReminder();
        initConditions();
        checkConditions();
    }

    class WifiReceiver extends BroadcastReceiver{
        @Override
        public void onReceive(Context context, Intent intent){
            WifiManager wifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
            WifiInfo wifiInfo = wifiManager.getConnectionInfo();
            if (wifiInfo != null){
                if (wifiInfo.getSSID() != null){
                    Log.i("Minder","Connected to "+wifiInfo.getSSID());
                    checkConditions();
                }
                else{
                    Log.i("Minder","No SSID");
                }
            }
            else {
                Log.i("Minder","No wifi info");
            }
        }
    }
}