package com.bondfire.app.android.services;

import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Color;
import android.os.Binder;
import android.os.IBinder;
import androidx.annotation.Nullable;
import android.util.Log;

import com.bondfire.app.android.R;
import com.bondfire.app.android.activity.MainActivity;
import com.bondfire.app.android.network.realtime.RealTimeManager;

/**
 * Created by alvaregd on 18/02/16.
 * A service made to keep the Network alive while we are connecte to a Real Time Room
 */
public class NetworkService extends Service {

    /**
     * Debug
     ***************/
    private final static String TAG = NetworkService.class.getName();
    private final static boolean d_onBind = true;
    private final static boolean d_onCreate = true;
    private final static boolean d_onDestroy = true;
    private final static boolean d_onStartCommand = true;
    private final static boolean d_onLowMemory = true;
    private final static boolean d_onUnbind = true;
    private final static boolean d_onRebind = true;
    private final static boolean d_onTaskRemoved = true;
    private final static boolean d_onTrimMemory = true;
    private final static boolean d_onConfigurationChanged = true;
    private final static boolean d_setForegroundNotificiation = true;

    //IBinder object for this service
    private final IBinder mBinder = new LocalBinder();
    private final int NOTIFICATIONID = 1;

    private Intent intent;
    private PendingIntent pIntent;
    private RealTimeManager realTimeManager;

    private static final String ACTION_LEAVE = "LEAVE";

    /***************************************************************************
     * Service LifeCycle
     ****************************************************************************/
    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        if (d_onBind) Log.i(TAG, "onBind() ");
        return mBinder;
    }

    @Override
    public void onCreate() {
        if (d_onCreate) Log.i(TAG, "onCreate() ");

        //create an intent for the notification so that if users press it they are taken into the app
        intent = new Intent(this, MainActivity.class);
        pIntent = PendingIntent.getActivity(this, 0, intent, 0);

        super.onCreate();
    }


    @Override
    public void onDestroy() {
        if (d_onDestroy) Log.i(TAG, "onDestroy() ");
        super.onDestroy();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (d_onStartCommand) Log.i(TAG, "onStartCommand() ");
        stopForeground(true);

        if (intent != null) {
            String action = intent.getAction();
            if (action != null) {
                if (intent.getAction().equals(ACTION_LEAVE)) {
                    if (realTimeManager != null) {
                        realTimeManager.leftAppFromNotification();
                        realTimeManager.leaveRoom();
                        realTimeManager.onDestroy();
                        stopSelf();
                    }
                }

                if (intent.getAction().equals(RealTimeManager.ACTION_DECLINE)) {
                    setForegroundNotificiation();
                }
            }
        }
        return START_NOT_STICKY;
    }

    @Override
    public void onLowMemory() {
        if (d_onLowMemory) Log.i(TAG, "onLowMemory() ");
        super.onLowMemory();
    }

    @Override
    public boolean onUnbind(Intent intent) {
        if (d_onUnbind) Log.i(TAG, "onUnbind() ");
        try {
            realTimeManager.setIsBoundToService(false);
            if (realTimeManager.isConnected()) {
                setForegroundNotificiation();
            } else {
                stopSelf();
            }

        } catch (NullPointerException e) {
            Log.e(TAG, "onUnbind: Tried to Unbind!", e);
            stopSelf();

        } catch (RuntimeException e) {
            Log.e(TAG, "onUnbind: Tried to Unbind but failed!!", e);
            stopSelf();
        }
        //Very important! This makes onRebind and Unbind() be called on subsequent client
        // connections. Always return true
        return true;
    }

    @Override
    public void onRebind(Intent intent) {
        if (d_onRebind) Log.i(TAG, "onRebind() ");
        realTimeManager.setIsBoundToService(true);
        realTimeManager.getStatusAndBroadcast();
        stopForeground(true);
        super.onRebind(intent);
    }

    @Override
    public void onTaskRemoved(Intent rootIntent) {
        if (d_onTaskRemoved) Log.i(TAG, "onTaskRemoved() ");
        super.onTaskRemoved(rootIntent);
    }

    @Override
    public void onTrimMemory(int level) {
        if (d_onTrimMemory) Log.i(TAG, "onTrimMemory() ");
        super.onTrimMemory(level);
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        if (d_onConfigurationChanged) Log.i(TAG, "onConfigurationChanged() ");
        super.onConfigurationChanged(newConfig);
    }

    /****
     * Class that helps us retrieve this service
     */
    public class LocalBinder extends Binder {
        public NetworkService getService() {
            return NetworkService.this;
        }
    }

    private void setForegroundNotificiation() {
        if (d_setForegroundNotificiation) Log.i(TAG, "setForegroundNotificiation() ");

        Intent leaveIntent = new Intent(this, NetworkService.class);
        leaveIntent.setAction(ACTION_LEAVE);
        PendingIntent pLeaveIntent = PendingIntent.getService(this, 0, leaveIntent, 0);

        Notification notification = new Notification.Builder(this)
                .setContentTitle("Bondfire")
                .setContentText("Connected to group")
                .setSmallIcon(R.drawable.bf_bird_icon)
                .setContentIntent(pIntent)
                .setLights(Color.MAGENTA,3000,5000)
                .setAutoCancel(true)
                .addAction(0, "Leave Party", pLeaveIntent)
                .build();

        notification.flags |= Notification.FLAG_ONGOING_EVENT;
        startForeground(NOTIFICATIONID, notification);
    }



    /**
     * get/set
     **/
    public RealTimeManager getRealTimeManager() {
        return realTimeManager;
    }

    public void setRealTimeManager(RealTimeManager realTimeManager) {
        this.realTimeManager = realTimeManager;
    }

    public boolean hasRealTimeManager() {
        return realTimeManager != null;
    }
}
