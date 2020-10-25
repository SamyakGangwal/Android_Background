package com.toggler.foreground;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import android.bluetooth.BluetoothAdapter;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.widget.CompoundButton;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {

    // code for enabling bluetooth
    public static final int BLUETOOTH_REQUEST_CODE = 1;

    // Declaring the variables
    BluetoothAdapter bluetoothadapter;

    TextView bluetoothStatus;
    Switch bluetoothToggle;
    TextView wifiStatus;
    WifiManager wifiManager;

    // BroadcastReceiver to check for status of bluetooth
    private BroadcastReceiver mBroadcastReceiver1 = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();

            if (action.equals(BluetoothAdapter.ACTION_STATE_CHANGED)) {
                final int state = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE,
                        BluetoothAdapter.ERROR);
                switch(state) {
                    case BluetoothAdapter.STATE_OFF:
                        bluetoothStatus.setText("Bluetooth is currently OFF");
                        bluetoothToggle.setChecked(false);
                        break;
                    case BluetoothAdapter.STATE_TURNING_OFF:
                        Toast.makeText(context, "Bluetooth is turning off", Toast.LENGTH_SHORT
                        ).show();
                        Log.d("BroadcastActions", "Bluetooth is turning off");
                        break;
                    case BluetoothAdapter.STATE_ON:
                        bluetoothStatus.setText("Bluetooth is currently ON");
                        bluetoothToggle.setChecked(true);
                        break;
                    case BluetoothAdapter.STATE_TURNING_ON:
                        Toast.makeText(MainActivity.this, "bluetooth may take a " +
                                "moment to turn on", Toast.LENGTH_LONG).show();

                        break;
                }
            }
        }
    };

    // BroadcastReceiver to check for status of wifi
    private BroadcastReceiver wBroadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            int wifiStateExtra = intent.getIntExtra(WifiManager.EXTRA_WIFI_STATE,
                    WifiManager.WIFI_STATE_UNKNOWN);
            switch (wifiStateExtra) {
                case WifiManager.WIFI_STATE_ENABLED:
                    wifiStatus.setText("Wifi is enabled");
                    break;
                case WifiManager.WIFI_STATE_DISABLED:
                    wifiStatus.setText("Wifi is disabled");
                    break;
            }
        }
    };

    // on starting the app/service
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Declaring 2 intents for wifi and bluetooth
        Intent intent = new Intent(this, BluetoothService.class);
        Intent intent1 = new Intent(this, WifiService.class);

        // Variables
        bluetoothadapter = BluetoothAdapter.getDefaultAdapter();
        bluetoothStatus = (TextView) findViewById(R.id.bluetooth_status_text_view);
        bluetoothToggle = (Switch) findViewById(R.id.bluetooth_switch);
        wifiStatus = (TextView) findViewById(R.id.wifi_status_view);
        wifiManager = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);

        // Filtering out so that broadcastreceiver can catch wifi and bluetooth separately
        IntentFilter intentFilter = new IntentFilter(WifiManager.WIFI_STATE_CHANGED_ACTION);
        registerReceiver(wBroadcastReceiver, intentFilter);

        IntentFilter filter1 = new IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED);
        registerReceiver(mBroadcastReceiver1, filter1);

        //Check if the bluetooth is supported or not
        if(bluetoothadapter == null) {
            Toast.makeText(MainActivity.this, "This device does not support " +
                    "bluetooth", Toast.LENGTH_LONG).show();
        }

        // If initially bluetooth in enabled
        if (bluetoothadapter.isEnabled()) {
            bluetoothStatus.setText("Bluetooth is currently ON");
            bluetoothToggle.setChecked(true);
        }
        else {
            bluetoothStatus.setText("Bluetooth is currently OFF");
            bluetoothToggle.setChecked(false);

        }


        // Listen to the changes in the toggle button
        bluetoothToggle.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {

            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    Intent bluetoothintent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                    Toast.makeText(MainActivity.this, "bluetooth may take a moment to" +
                            " turn on", Toast.LENGTH_LONG).show();
                    startActivityForResult(bluetoothintent, BLUETOOTH_REQUEST_CODE);

                }
                else {
                    bluetoothadapter.disable();
                    bluetoothStatus.setText("Bluetooth is currently OFF");

                }
            }
        });

        // For the versions of android above 8
        // Start the foreground service
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            ContextCompat.startForegroundService(this, intent);
            ContextCompat.startForegroundService(this, intent1);

        }
        else
        {
            startService(intent);
            startService(intent1);
        }
    }
}