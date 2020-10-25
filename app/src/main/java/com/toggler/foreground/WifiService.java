package com.toggler.foreground;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.IBinder;
import android.service.notification.StatusBarNotification;
import android.util.Log;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.core.app.NotificationCompat;

// Service for Wifi
public class WifiService extends Service {

    public static final String CHANNEL_ID_1 = "ChannelId2";
    public static final int FOREGROUND_NOTE_ID_1 = 2;
    String content1 = "Wifi is disabled";

    // Broadcast receiver to check wifi status in background
    private BroadcastReceiver wBroadcastReceiver1 = new BroadcastReceiver() {

        // Things to do for different states of wifi
        @RequiresApi(api = Build.VERSION_CODES.M)
        @Override
        public void onReceive(Context context, Intent intent) {
            int wifiStateExtra = intent.getIntExtra(WifiManager.EXTRA_WIFI_STATE,
                    WifiManager.WIFI_STATE_UNKNOWN);
            switch (wifiStateExtra) {
                case WifiManager.WIFI_STATE_ENABLED:
                    content1 = "Wifi is enabled";
                    // change the notification when the state is changed
                    notify2();
                    break;
                case WifiManager.WIFI_STATE_DISABLED:
                    content1= "Wifi is disabled";
                    notify2();
                    break;
            }
        }
    };

    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        IntentFilter filter2 = new IntentFilter(WifiManager.WIFI_STATE_CHANGED_ACTION);
        registerReceiver(wBroadcastReceiver1, filter2);
        notify2();

        return START_REDELIVER_INTENT;

    }

    // Building the foreground notification
    private Notification buildForegroundNotification1() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            createNotificationChannel(CHANNEL_ID_1,"Foreground_1");
        }

        Intent intent1 = new Intent(this, MainActivity.class);

        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0,
                intent1, 0);

        //then build the notification
        return new NotificationCompat.Builder(this
                , CHANNEL_ID_1)
                .setContentTitle("Wifi Service")
                .setContentText(content1)
                .setSmallIcon(R.drawable.ic_wifi)
                .setContentIntent(pendingIntent)
                .build();
    }

    // make a notification channel
    private void createNotificationChannel(String channel,String name) {
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel notificationChannel = new NotificationChannel(
                    channel, name
                    , NotificationManager.IMPORTANCE_DEFAULT);

            NotificationManager manager = getSystemService(NotificationManager.class);
            assert manager != null;
            manager.createNotificationChannel(notificationChannel);

        }
    }

    // creating a notification or overriding the existing with a new one
    @RequiresApi(api = Build.VERSION_CODES.M)
    private void notify2() {
        // variable for checking an existing notification
        boolean isForegroundNotificationVisible = false;

        NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        StatusBarNotification[] notifications = notificationManager.getActiveNotifications();

        for (StatusBarNotification notification1 : notifications) {
            if (notification1.getId() == FOREGROUND_NOTE_ID_1) {
                isForegroundNotificationVisible = true;
                break;
            }
        }
        Log.v(getClass().getSimpleName(), "Is foreground visible: " + isForegroundNotificationVisible);
        if (isForegroundNotificationVisible){
            // if there is an existing notification override it
            notificationManager.notify(FOREGROUND_NOTE_ID_1, buildForegroundNotification1());
        } else {
            // or else start a new one
            startForeground(FOREGROUND_NOTE_ID_1, buildForegroundNotification1());
        }
    }


    // when the app is closed remove the notification and stop the service
    // NOTE: you might have to close the app forcefully
    // since removing it from background won't work
    public void onDestroy() {
        stopForeground(true);
        stopSelf();
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

}
