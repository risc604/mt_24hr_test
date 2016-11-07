package com.microlife.software.a24hr_mt;

import android.Manifest;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.Toast;

public class LoadingActivity extends Activity
{
    private final static String TAG = LoadingActivity.class.getSimpleName();

    private static final int REQUEST_ENABLE_BT = 1;
    private static final int PERMISSION_REQUEST_COARSE_LOCATION = 1;

    private BluetoothAdapter    mBluetoothAdapter;

    ProgressBar pb;
    //ProgressDialog pd1;
    CountDownTimer  cdt;
    Intent loginIntent;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        //requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_loading);

        if (!getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE))
        {
            Toast.makeText(this, "BLE Not Supported", Toast.LENGTH_SHORT).show();
            finish();
        }

        timerEvent();

        final BluetoothManager bluetoothManager =
                (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);

        mBluetoothAdapter = bluetoothManager.getAdapter();  // Checks if Bluetooth is supported on the device.
        if (mBluetoothAdapter == null)
        {
            finish();
            return;
        }
        marshmallowPermission();

        Log.d(TAG, "LoadingActivity create.");
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        if (requestCode == REQUEST_ENABLE_BT)
        {
            if (resultCode == Activity.RESULT_CANCELED)
            {
                //Bluetooth not enabled.
                finish();
                return;
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    protected void onResume()
    {
        super.onResume();
        if (!mBluetoothAdapter.isEnabled())
        {
            //if (!mBluetoothAdapter.isEnabled())
            {
                Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
            }
        }
        //TimerEvent();
    }

    private void marshmallowPermission()
    {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)
                != PackageManager.PERMISSION_GRANTED)
        {
            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    Manifest.permission.ACCESS_COARSE_LOCATION))
            {
            }
            else
            {
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.ACCESS_COARSE_LOCATION},
                        PERMISSION_REQUEST_COARSE_LOCATION);
            }
        }
    }

    public void timerEvent()
    {
        //pd1 = new ProgressDialog(this);
        pb = (ProgressBar)findViewById(R.id.progressBar);
        //pd1.setTitle("Loading ...");
        //pd1.show();
        cdt = new CountDownTimer(5000, 1000)
        {
            @Override
            public void onTick(long l)
            {
                Log.d("CountTimer ", l/1000 + " s");
                //pd1.setCancelable(false);
                //pd1.show();
                pb.setVisibility(View.VISIBLE);
            }

            @Override
            public void onFinish()
            {
                //pd1.dismiss();
                //Utils.setLogFileName("log");
                pb.setVisibility(View.GONE);
                loginIntent = new Intent(getBaseContext(), LoginActivity.class);
                startActivity(loginIntent);
                cdt.cancel();
                finish();
            }
        }.start();
    }

    public void timerStop()
    {
        cdt.cancel();
    }
}

