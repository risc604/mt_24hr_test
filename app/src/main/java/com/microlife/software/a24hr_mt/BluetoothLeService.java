package com.microlife.software.a24hr_mt;

import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothProfile;
import android.content.Context;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.os.ParcelUuid;
import android.util.Log;

import java.util.List;

public class BluetoothLeService extends Service
{
    private final static String TAG = BluetoothLeService.class.getSimpleName();

    private BluetoothManager    mBLEmanager;
    private BluetoothAdapter    mBLEadapter;
    private BluetoothGatt       mBLEgatt;
    private String              deviceAddress;
    public boolean              mBluetoothGattConnected;

    private static final int STATE_DISCONNECTED = 0;
    private static final int STATE_CONNECTING = 1;
    private static final int STATE_CONNECTED = 2;

    public final static String ACTION_GATT_CONNECTED = "com.mlc.software.ble.ACTION_GATT_CONNECTED";
    public final static String ACTION_GATT_DISCONNECTED = "com.mlc.software.ble.ACTION_GATT_DISCONNECTED";
    public final static String ACTION_GATT_SERVICE_DISCOVERED = "com.mlc.software.ble.ACTION_GATT_SERVICE_DISCOVERED";
    public final static String ACTION_DATA_AVAILABLE = "com.mlc.software.ble.ACTION_DATA_AVAILABLE";
    public final static String ACTION_CONNECT_REQUEST = "com.mlc.software.ble.ACTION_CONNECT_REQUEST";
    public final static String ACTION_ENABLE = "com.mlc.software.ble.ACTION_ENABLE";
    public final static String EXTRA_DATA = "com.mlc.software.ble.EXTRA_DATA";

    private final static String MLC_BLE_CHAR = "0000fff0-0000-1000-8000-00805f9b34fb";
    private final static String MLC_BLE_READ = "0000fff1-0000-1000-8000-00805f9b34fb";
    private final static String MLC_BLE_WRITE = "0000fff2-0000-1000-8000-00805f9b34fb";
    private final static String CLIENT_CHAR_CONFIG = "00002902-0000-1000-8000-00805f9b34fb";
    public final static ParcelUuid UUID_MLC_CHAR = ParcelUuid.fromString(MLC_BLE_CHAR);
    public final static ParcelUuid UUID_MLC_READ = ParcelUuid.fromString(MLC_BLE_READ);
    public final static ParcelUuid UUID_MLC_WRITE = ParcelUuid.fromString(MLC_BLE_WRITE);
    public final static ParcelUuid UUID_CLIENT_CONFIG =  ParcelUuid.fromString(CLIENT_CHAR_CONFIG);


    public BluetoothLeService()
    {
        //mBLEmanager = (BluetoothManager)getSystemService(Context.BLUETOOTH_SERVICE);
    }

    private final IBinder mBinder = new LocalBinder();

    public class LocalBinder extends Binder
    {
        BluetoothLeService  getService()
        {
            return BluetoothLeService.this;
        }
    }

    @Override
    public IBinder onBind(Intent intent)
    {
        Log.d(TAG, "onBind: " + intent.toString() + ", " + mBinder.toString());
        return mBinder;
        // TODO: Return the communication channel to the service.
        //throw new UnsupportedOperationException("Not yet implemented");
    }


    private final BluetoothGattCallback gattCallback = new BluetoothGattCallback()
    {
        @Override
        public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState)
        {
            //super.onConnectionStateChange(gatt, status, newState);
            Log.i(TAG, "onConnectionStateChange(), Status: " + status);

            switch (newState)
            {
                case BluetoothProfile.STATE_CONNECTED:
                    //gatt.discoverServices();
                    broadcastUpdate(ACTION_GATT_CONNECTED);
                    Log.i(TAG, "Connected to GATT server.");
                    Log.i(TAG, "Attempting to start service discovery: " +
                            mBLEgatt.discoverServices());
                    mBluetoothGattConnected = true;
                    break;

                case BluetoothProfile.STATE_DISCONNECTED:
                    Log.i(TAG, "DisConnected from GATT server.");
                    broadcastUpdate(ACTION_GATT_DISCONNECTED);
                    mBluetoothGattConnected = false;
                    break;

                default:
                    Log.e(TAG, "STATE_OTHER");
                    break;
            }

            Log.i(TAG, "gattCallback, finish " + status);
        }

        @Override
        public void onServicesDiscovered(BluetoothGatt gatt, int status)
        {
            //super.onServicesDiscovered(gatt, status);
            List<BluetoothGattService> services = gatt.getServices();
            Log.i(TAG, "onServicesDiscovered(), status: " + status);
            if (status == BluetoothGatt.GATT_SUCCESS)
            {
                broadcastUpdate(ACTION_GATT_SERVICE_DISCOVERED);
                NotifyEnable(gatt);
                //gatt.readCharacteristic(services.get(4).getCharacteristics().get(0));
            }
            else
            {
                Log.w(TAG, "onServiceDiscovered received: " + status);
            }
        }

        @Override
        public void onCharacteristicChanged(BluetoothGatt gatt,
                                            BluetoothGattCharacteristic characteristic)
        {
            //super.onCharacteristicChanged(gatt, characteristic);
            final byte[] data = characteristic.getValue();
            broadcastUpdate(ACTION_DATA_AVAILABLE, characteristic);
            //NotifyEnable(gatt);
            //Log.d(TAG, "onCharacteristicChanged(), getValue: " + characteristic.getValue());
            //final byte[] data = characteristic.getValue();
        }

        @Override
        public void onCharacteristicRead(BluetoothGatt gatt,
                                         BluetoothGattCharacteristic characteristic,
                                         int status)
        {
            //super.onCharacteristicRead(gatt, characteristic, status);
            if (status == BluetoothGatt.GATT_SUCCESS)
            {
                broadcastUpdate(ACTION_DATA_AVAILABLE, characteristic);
            }
            //Log.i(TAG, "onCharacteristicRead(), status: " + status);
        }

        @Override
        public void onCharacteristicWrite(BluetoothGatt gatt,
                                          BluetoothGattCharacteristic characteristic,
                                          int status)
        {
            //super.onCharacteristicWrite(gatt, characteristic, status);
        }

        @Override
        public void onDescriptorWrite(BluetoothGatt gatt,
                                      BluetoothGattDescriptor
                                              descriptor, int status)
        {
            //super.onDescriptorWrite(gatt, descriptor, status);
            final Intent intent = new Intent();
            intent.putExtra(ACTION_ENABLE, descriptor.getCharacteristic().getUuid().toString());
            sendBroadcast(intent);
            Log.i(TAG, "onDescriptorWrite(), status: " + status);
        }
    };

    //---------------- User process function ----------------------------//

    public void readCharacteristic(BluetoothGattCharacteristic characteristic, BluetoothGatt gatt)
    {
        if (mBLEadapter == null || gatt == null)
        {
            Log.w(TAG, "BluetoothAdapter not initialized");
            return;
        }
        gatt.readCharacteristic(characteristic);

    }

    public void NotifyEnable(BluetoothGatt gatt)
    {
        if (mBLEadapter == null || gatt == null)
        {
            Log.w(TAG, "BluetoothAdapter not initialized");
            return;
        }

        for (BluetoothGattService GattService : gatt.getServices())
        {
            List<BluetoothGattCharacteristic> mGattCharacteristics = GattService.getCharacteristics();
            //Log.d(TAG, "Characteristic: " + GattService.getUuid());
            for (BluetoothGattCharacteristic mCharacteristic : mGattCharacteristics)
            {
                //Log.d(TAG, "Characteristic: " + mCharacteristic.getUuid());
                //if (MLC_DEVICE_READ.getUuid().equals(mCharacteristic.getUuid()))
                if (UUID_MLC_READ.getUuid().equals(mCharacteristic.getUuid()))
                //if (mCharacteristic.getUuid().equals(MLC_BLEUUID_READ))
                {
                    setCharacteristicNotification(gatt, mCharacteristic, true);
                    Log.d(TAG, "NotifyEnable(), Characteristic: " + mCharacteristic.getUuid());
                }
            }
        }
    }

    public void setCharacteristicNotification(BluetoothGatt gatt,
                                              BluetoothGattCharacteristic characteristic,
                                              boolean enabled)
    {
        if ((mBLEadapter == null) || (gatt == null))
        {

            Log.w(TAG, "BluetoothAdapter not initialized");
            return;
        }

        gatt.setCharacteristicNotification(characteristic, enabled);
        BluetoothGattDescriptor descriptor = characteristic.getDescriptor(UUID_CLIENT_CONFIG.getUuid());
        descriptor.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
        //Log.d(TAG, "setCharacteristicNotification(), descriptor: " + descriptor);
        gatt.writeDescriptor(descriptor);
    }

    public void broadcastUpdate(final String action)
    {
        final Intent intent = new Intent(action);
        sendBroadcast(intent);
    }

    public void broadcastUpdate(final String action,
                                 final BluetoothGattCharacteristic characteristic)
    {
        final Intent intent = new Intent(action);
        final byte[] data = characteristic.getValue();

        //Log.d(TAG, "broadcastUpdate(s, ch), getValue(): " + data.toString());
        if ((data != null) && (data.length > 0))
        {
            final StringBuilder stringBuilder = new StringBuilder(data.length);
            for (byte byteIndex : data)
                stringBuilder.append(String.format("%02X", byteIndex));
            intent.putExtra(EXTRA_DATA, stringBuilder.toString());
        }
        sendBroadcast(intent);
    }

    public boolean writeCharacteristicCMD(byte[] value)
    {
        boolean state = false;

        if (mBLEadapter == null || mBLEgatt == null)
        {
            Log.w(TAG, "BluetoothAdapter not initialized");
            return state;
        }

        for (BluetoothGattService GattService : mBLEgatt.getServices())
        {
            List<BluetoothGattCharacteristic> mGattCharacteristics = GattService.getCharacteristics();
            for (BluetoothGattCharacteristic mCharacteristic : mGattCharacteristics)
            {
                if (UUID_MLC_WRITE.getUuid().equals(mCharacteristic.getUuid()))
                {
                    state = mCharacteristic.setValue(value);
                    mBLEgatt.writeCharacteristic(mCharacteristic);
                    //Log.d(TAG, "writeCharacteristicCMD(), getUuid(): " + mCharacteristic.getUuid() +
                    //", state: " + state);
                }
            }
        }
        return state;
    }



    @Override
    public void onCreate()
    {
        super.onCreate();
        mBLEmanager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
        mBLEadapter = mBLEmanager.getAdapter();
        Log.d(TAG, "service, onCreate() finish");
    }

    public boolean initialize()
    {
        Log.d(TAG, "service, initialize()");
        if (mBLEmanager == null)
        {
            mBLEmanager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
            if (mBLEmanager == null)
            {
                Log.e(TAG, "Unable to initialize BluetoothManager.");
                return false;
            }
        }

        mBLEadapter = mBLEmanager.getAdapter();
        if (mBLEadapter == null)
        {
            Log.e(TAG, "Unable to obtain BluetoothAdapter.");
            return false;
        }
        Log.d(TAG, "mBLEadapter: " + mBLEadapter.toString());

        return true;
    }

    public boolean connect(final String address)
    {
        //final String deviceAddr = address;
        Log.d(TAG, "mBLEadapter: " + mBLEadapter + ", BLE addr: " + address +
                ", mBluetoothGattConnected: " + mBluetoothGattConnected );

        if ((mBLEadapter == null) || (address == null) /*|| (mBluetoothGattConnected==false)*/)
        {
            Log.w(TAG, "BluetoothAdapter not initialized or unspecified address.");
            return false;
        }

        //Log.d(TAG, "Trying to create a new connection. " + address + ": mBLEgatt: " + mBLEgatt.toString());

        if ( !address.equalsIgnoreCase("") &&
                /*(address.equalsIgnoreCase(deviceAddress)) &&*/
                (mBLEgatt != null) )
        {
            Log.d(TAG, "Trying to use an existing mBLEgatt for connection.");
            if (mBLEgatt.connect()) return true;
            else    return false;
        }

        Log.d(TAG, "service, connect(), address: " + address);


        if (!address.equalsIgnoreCase(""))
        {
            final BluetoothDevice device = mBLEadapter.getRemoteDevice(address);
            if (device == null) {
                Log.w(TAG, "Device not found, Unable to connect.");
                return false;
            }
            mBLEgatt = device.connectGatt(this, false, gattCallback);   // real to connect BLE device.
            deviceAddress = address;
            return true;
        }
        else
        {
            Log.w(TAG, "Device addr:" + address);
            return false;
        }

    }

    public void disconnect()
    {
        if ((mBLEadapter == null) && (mBLEgatt == null) )
        {
            Log.w(TAG, "BluetoothAdapter not initialized.");
            return;
        }
        mBluetoothGattConnected = false;
        mBLEgatt.disconnect();
    }

    public void close()
    {
        if (mBLEgatt == null)
        {
            return;
        }
        mBLEgatt.close();
        mBLEgatt = null;
    }


}
