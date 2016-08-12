package com.example.jordon_rules.jet_3_controller;


import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.graphics.Point;
import android.os.Bundle;
import android.util.Log;
import android.view.Display;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;
import java.io.IOException;
import java.io.OutputStream;
import java.util.UUID;


public class MainActivity extends Activity {

    public TextView tv1, tv2;
    //public ImageView iv;
    public SeekBar bar1, bar2;
    byte x_val;
    byte y_val;

    private static final String TAG = "Bluetooth Controller";
    private static final int REQUEST_ENABLE_BT = 1;
    private BluetoothAdapter btAdapter = null;
    private BluetoothSocket btSocket = null;
    private OutputStream outStream = null;

    // Well known SPP UUID
    private static final UUID MY_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");

    // Server's MAC address
    private static String address = "20:14:08:05:27:21";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        //Set ContentView
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //DisplayMetrics used for finding the width and height of screen
        Display display = getWindowManager().getDefaultDisplay();
        Point size = new Point();

        //Width and Height of screen
        final int width = display.getWidth();;
        final int height = display.getHeight();;

        //Declare TextViews on page
        tv1 = (TextView)findViewById(R.id.tv1);
        tv2 = (TextView)findViewById(R.id.tv2);

        //Declare Seekbars on page
        bar1 = (SeekBar) findViewById(R.id.seekBar1);
        bar2 = (SeekBar) findViewById(R.id.seekBar2);

        //Attach listeners to SeekBars
        bar1.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                //Get values from the ImageView with user presses on it
                x_val = (byte) seekBar.getProgress();
                y_val = (byte) (seekBar.getProgress()/2);
                //Convert coordinates to values that the POT can use on the JET 3 Ultra
                x_val = (byte) ((128/width)  * x_val); //Max value is 128 on POT
                y_val = (byte) ((128/height) * y_val); //Max value is 128 on POT
                //Byte Array to use for storing the converted values
                byte[] buffer = {x_val, y_val};
                //Try statement using OutputStream
                try{
                    outStream.write(buffer);
                }
                //simply output to user that nothing could be done
                catch (IOException ex){
                    //Send message saying that there was no data send to OutputStream or Bluetooth device
                    Toast.makeText(getApplicationContext(), "Didn't write to OutStream", Toast.LENGTH_SHORT).show();
                }
            }
        });

        //Create an adapter object used for later
        btAdapter = BluetoothAdapter.getDefaultAdapter();
        //Check to see if the device can establish a bluetooth connection
        checkBTState();
    }


    @Override
    public void onResume() {
        super.onResume();

        // Set up a pointer to the remote node using it's address.
        BluetoothDevice device = btAdapter.getRemoteDevice(address);
        // Two things are needed to make a connection:
        //   A MAC address, which we got above.
        //   A Service ID or UUID.  In this case we are using the
        //     UUID for SPP.
        try {
            btSocket = device.createRfcommSocketToServiceRecord(MY_UUID);
        } catch (IOException e) {
        }

        // Discovery is resource intensive.  Make sure it isn't going on
        // when you attempt to connect and pass your message.
        btAdapter.cancelDiscovery();

        // Establish the connection.  This will block until it connects.
        try {
            btSocket.connect();

        } catch (IOException e) {
            try {
                btSocket.close();
            } catch (IOException e2) {

            }
        }

        // Create a data stream so we can talk to server.
        Log.d(TAG, "...Creating Socket...");

        try {
            outStream = btSocket.getOutputStream();
        } catch (IOException e) {

        }
    }

    @Override
    public void onPause() {
        super.onPause();

        Log.d(TAG, "...In onPause()...");

        if (btSocket != null) {
            try {
                outStream.flush();
            } catch (IOException e) {

            }
        }

        try     {
            btSocket.close();
        } catch (IOException e2) {

        }
    }

    private void checkBTState() {
        // Check for Bluetooth support and then check to make sure it is turned on

        // Emulator doesn't support Bluetooth and will return null
        if(btAdapter==null) {

        } else {
            if (btAdapter.isEnabled()) {

            } else {
                //Prompt user to turn on Bluetooth
                Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
            }
        }
    }

    private void sendData(String message) {
        byte[] msgBuffer = message.getBytes();

        try {
            outStream.write(msgBuffer);
        } catch (IOException e) {

        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
