package com.toggler.foreground;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.os.IBinder;
import android.service.notification.StatusBarNotification;
import android.util.Log;
import android.widget.Toast;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.core.app.NotificationCompat;

// Service for bluetooth
public class BluetoothService extends Service {

    private static final String CHANNEL_ID = "ChannelId1";
    BluetoothAdapter bluetoothadapter;
    String content = "Bluetooth is not connected!";
    public static final int  FOREGROUND_NOTE_ID = 1;

    // Broadcast receiver to check bluetooth status in background
    private BroadcastReceiver mBroadcastReceiver1 = new BroadcastReceiver() {

        // When receiver catches the status
        @RequiresApi(api = Build.VERSION_CODES.M)
        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();

            // Things to do for different states of bluetooth
            if (action.equals(BluetoothAdapter.ACTION_STATE_CHANGED)) {
                final int state = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE,
                        BluetoothAdapter.ERROR);
                switch(state) {
                    case BluetoothAdapter.STATE_OFF:
                        content = "Bluetooth is not connected!";
                        // change the notification when the state is changed
                        notify1();
                        break;

                    case BluetoothAdapter.STATE_TURNING_OFF:
                        Toast.makeText(context, "Bluetooth is turning off", Toast.LENGTH_SHORT
                        ).show();
                        Log.d("BroadcastActions", "Bluetooth is turning off");
                        break;

                    case BluetoothAdapter.STATE_ON:
                        content = "Bluetooth is connected!";
                        notify1();
                        break;

                    case BluetoothAdapter.STATE_TURNING_ON:
                        Toast.makeText(BluetoothService.this, "bluetooth may take a " +
                                "moment to turn on", Toast.LENGTH_LONG).show();

                        break;
                }
            }
        }
    };


    // when the service starts
    // status of bluetooth will be checked
    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        bluetoothadapter = BluetoothAdapter.getDefaultAdapter();
        if (bluetoothadapter.isEnabled()) {
            content = "Bluetooth is connected!";
        }
        else {
            content = "Bluetooth is not connected!";
        }

        // intent filter will be declared so that receiver can  catch it
        IntentFilter filter1 = new IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED);
        // register the filter into receiver
        registerReceiver(mBroadcastReceiver1, filter1);

        // start the notification
        notify1();

        return START_REDELIVER_INTENT;

    }

    // Building the foreground notification
    private Notification buildForegroundNotification() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            createNotificationChannel(CHANNEL_ID,"foreground_2");
        }

        Intent intent1 = new Intent(this, MainActivity.class);

        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0,
                intent1, 0);

        //then build the notification
        return new NotificationCompat.Builder(this
                , CHANNEL_ID)
                .setContentTitle("Bluetooth Service")
                .setContentText(content)
                .setSmallIcon(R.drawable.ic_bluetooth)
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
    private void notify1() {
        // variable for checking an existing notification
        boolean isForegroundNotificationVisible = false;

        NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        StatusBarNotification[] notifications = notificationManager.getActiveNotifications();


        for (StatusBarNotification notification1 : notifications) {
            if (notification1.getId() == FOREGROUND_NOTE_ID) {
                isForegroundNotificationVisible = true;
                break;
            }
        }

        Log.v(getClass().getSimpleName(), "Is foreground visible: " + isForegroundNotificationVisible);
        if (isForegroundNotificationVisible){
            // if there is an existing notification override it
            notificationManager.notify(FOREGROUND_NOTE_ID, buildForegroundNotification());
        } else {
            // or else start a new one
            startForeground(FOREGROUND_NOTE_ID, buildForegroundNotification());
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
