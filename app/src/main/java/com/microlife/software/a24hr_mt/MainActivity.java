package com.microlife.software.a24hr_mt;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanFilter;
import android.bluetooth.le.ScanResult;
import android.bluetooth.le.ScanSettings;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.viewpagerindicator.CirclePageIndicator;
import com.viewpagerindicator.PageIndicator;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static com.microlife.software.a24hr_mt.BluetoothLeService.ACTION_ENABLE;
import static java.lang.String.format;

public class MainActivity extends AppCompatActivity implements fragmentBady.updateTempListener
{
    private final static String TAG = MainActivity.class.getSimpleName();

    public  BluetoothLeService  mBluetoothLeService;
    private BluetoothAdapter    mBluetoothAdapter;
    private BluetoothLeScanner  mLEScanner;
    private ScanSettings        settings;
    private List<ScanFilter>    filters;
    private boolean             gattConnectFlag = false;
    private static final long   SCAN_PERIOD = (5*1000);
    private Handler             mHandler;
    private final String        DEV_ADDRESS = "18:7A:93:03:51:01";
    private String              deviceAddr;

    static final int        DEFAULT_PAGE = 0;
    ImageView               imgUserProfile;
    TextView                UserName;
    FragmentManager         mManager = getSupportFragmentManager();
    ViewPager               mPager;
    ViewPagerAdapter        mAdapter;
    private PageIndicator   mIndicator;
    static int              vBatValue = 0;

    private String          bleParserInfo = "";
    int totalLength = 0;
    int calLength = 0;
    int changeFramge=0xff;
    static String           checkWirteTimeString = "";



    private final BroadcastReceiver mGattUpdateReceiver = new BroadcastReceiver()
    {
        @Override
        public void onReceive(Context context, Intent intent)
        {
            final String action = intent.getAction();
            if(BluetoothLeService.ACTION_GATT_CONNECTED.equals(action))
            {
                gattConnectFlag = true;
                setBTicontoUI(true);
                Log.d(TAG, "GATT connected.");
            }
            else if(BluetoothLeService.ACTION_GATT_DISCONNECTED.equals(action))
            {
                gattConnectFlag = false;
                showTemperatureUI("BT disConnected ...");
                //startScanDevice(true);

                Log.d(TAG, "GATT disConnected...");
            }
            else if(BluetoothLeService.ACTION_GATT_SERVICE_DISCOVERED.equals(action))
            {
                Log.d(TAG, "GATT Service Discovered.");
            }
            else if(BluetoothLeService.ACTION_DATA_AVAILABLE.equals(action))
            {
                gattConnectFlag = true;
                receiveBLEData(intent.getStringExtra(BluetoothLeService.EXTRA_DATA));
            }
            else if(BluetoothLeService.ACTION_CONNECT_REQUEST.equals(action))
            {
                //mBluetoothLeService.connect(intent.getStringExtra(BluetoothLeService.ACTION_CONNECT_REQUEST));
                mBluetoothLeService.connect(intent.getStringExtra(BluetoothLeService.EXTRA_DATA));
            }
            else if(ACTION_ENABLE.equals(action))
            {
                //textView.append("Device Enable Read/Write.\r\n");
            }
        }
    };

    private ScanCallback mScanCallback = new ScanCallback()
    {
        @Override
        public void onScanResult(int callbackType, ScanResult result)
        {
            //super.onScanResult(callbackType, result);
            /* Connect to device found */
            Log.i(TAG, "callbackType: " + String.valueOf(callbackType) +
                    "mScanCallback, onScanResult(), "+ result.toString());
            connectToDevice(result.getDevice(), result.getRssi());
        }

        @Override
        public void onBatchScanResults(List<ScanResult> results)
        {
            //super.onBatchScanResults(results);
            /* Process a batch scan results */
            for (ScanResult sr : results)
            {
                Log.i(TAG, "onBatchScanResults(), "+ sr.toString());
            }
        }

        @Override
        public void onScanFailed(int errorCode)
        {
            //super.onScanFailed(errorCode);
            Log.e(TAG, "onScanFailed(), Error Code: " + errorCode);
        }
    };

    private BluetoothAdapter.LeScanCallback mLeScanCallback = new BluetoothAdapter.LeScanCallback()
    {
        @Override
        public void onLeScan(final BluetoothDevice device, final int rssi, byte[] scanRecord)
        {
            runOnUiThread(new Runnable()
            {
                @Override
                public void run()
                {
                    Log.i("onLeScan", device.toString());
                    if (device != null)
                        connectToDevice(device, rssi);
                }
            });

            Log.i(TAG, "mLeScanCallback, onLeScan(), device: " + device.toString());
        }
    };

    private ServiceConnection mServiceConnection = new ServiceConnection()
    {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service)
        {
            Log.e(TAG, "onServiceConnected(). running");
            mBluetoothLeService = ((BluetoothLeService.LocalBinder) service).getService();
            Log.e(TAG, "onServiceConnected(). running. " + mBluetoothLeService.toString());
            if(!mBluetoothLeService.initialize())
            {
                Log.e(TAG, "onServiceConnected(). Unable to initialize Bluetooth");
                finish();
            }

            Log.d(TAG, "BLE device Address: " + deviceAddr );

            //final String address = deviceAddr;
            //mBLEService.connect(address);
            mBluetoothLeService.connect(deviceAddr);
        }

        @Override
        public void onServiceDisconnected(ComponentName name)
        {
            mBluetoothLeService = null;
        }
    };

    private static IntentFilter makeGattUpdateIntentFilter()
    {
        final IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(BluetoothLeService.ACTION_GATT_CONNECTED);
        intentFilter.addAction(BluetoothLeService.ACTION_GATT_DISCONNECTED);
        intentFilter.addAction(BluetoothLeService.ACTION_GATT_SERVICE_DISCOVERED);
        intentFilter.addAction(BluetoothLeService.ACTION_CONNECT_REQUEST);
        intentFilter.addAction(BluetoothLeService.ACTION_DATA_AVAILABLE);
        intentFilter.addAction(ACTION_ENABLE);
        return intentFilter;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Log.d(TAG, "onCreate().");

        initMainView();
        //Utils.shortFileName(".log");

        /*
        mHandler = new Handler();
        deviceAddr = "";

        //Intent gattServiceIntent = new Intent(this, BluetoothLeService.class);
        //boolean state = bindService(gattServiceIntent, mServiceConnection, BIND_AUTO_CREATE);
        //Log.d(TAG, "onStart(), mServiceConnection state: " + state +
        //        ", mServiceConnection: " + mServiceConnection.toString() );

        final BluetoothManager bluetoothManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
        mBluetoothAdapter = bluetoothManager.getAdapter();
        if (mBluetoothAdapter == null)
        {
            finish();
            return;
        }

        try
        {
            Thread.sleep(2000);
            startScanDevice(true);
        }
        catch (InterruptedException e)
        {
            e.printStackTrace();
        }
        */

        //initMainView();
    }

    @Override
    protected void onStart()
    {
        /*
        Intent gattServiceIntent = new Intent(this, BluetoothLeService.class);
        boolean state = bindService(gattServiceIntent, mServiceConnection, BIND_AUTO_CREATE);
        Log.d(TAG, "onStart(), mServiceConnection state: " + state +
                ", mServiceConnection: " + mServiceConnection.toString() );
        super.onStart();
        Utils.shortFileName(".log");
        */
        super.onStart();
    }

    @Override
    protected void onResume()
    {
        super.onResume();

        /*
        registerReceiver(mGattUpdateReceiver, makeGattUpdateIntentFilter());
        if (mBluetoothLeService != null)
        {
            boolean result = mBluetoothLeService.connect(deviceAddr);
            Log.d(TAG, "Connect request result=" + result);
        }
        */

    }

    @Override
    protected void onPause()
    {
        super.onPause();

        /*
        if ((mBluetoothAdapter != null) && (mBluetoothAdapter.isEnabled()))
        {
            scanLeDevice(false);
        }
        //if (mServiceConnection != null)
        //if (!deviceAddr.equalsIgnoreCase(""))
        {
            unbindService(mServiceConnection);
            mBluetoothLeService = null;
            Log.d(TAG, "onPause(), unbind Service, mBluetoothLeService = null");
        }
        */
    }

    @Override
    protected void onDestroy()
    {
        super.onDestroy();

        /*
        if (mBluetoothLeService != null)
            mBluetoothLeService.close();
        unregisterReceiver(mGattUpdateReceiver);
        */
    }

    @Override
    public void updateTemp(String tmp)
    {
    }

    private void startScanDevice(boolean enable)
    {
        if (Build.VERSION.SDK_INT >= 21)
        {
            mLEScanner = mBluetoothAdapter.getBluetoothLeScanner();
            settings = new ScanSettings.Builder().setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY).build();

            Log.d("filters", " new filter list.");
            filters = new ArrayList<ScanFilter>();
            //ScanFilter bleFilter = new ScanFilter.Builder().setServiceUuid(
            //                        mBluetoothLeService.UUID_MLC_CHAR).build();
            ScanFilter bleFilterAddr = new ScanFilter.Builder().setDeviceAddress(DEV_ADDRESS).build();
            //filters.add(bleFilter);
            filters.add(bleFilterAddr);
        }
        scanLeDevice(enable);
        Log.d(TAG, "startScanDevice() finish.");
    }

    private void scanLeDevice(final boolean enable)
    {
        if (enable)
        {
            mHandler.postDelayed(new Runnable()
            {
                @Override
                public void run()
                {
                    stopScanBLE();
                }
            }, SCAN_PERIOD);

            if (Build.VERSION.SDK_INT < 21)
            {
                mBluetoothAdapter.startLeScan(mLeScanCallback);
            }
            else
            {
                mLEScanner.startScan(filters, settings, mScanCallback);
            }
        }
        else
        {
            stopScanBLE();
        }

        Log.d(TAG, "scanLeDevice() finish.");
    }

    private  void stopScanBLE()
    {
        if (Build.VERSION.SDK_INT < 21)
        {
            mBluetoothAdapter.stopLeScan(mLeScanCallback);
        }
        else
        {
            mLEScanner.stopScan(mScanCallback);
            gattConnectFlag = false;
            showTemperatureUI("Stop scan BT device.");
        }

        Log.d(TAG, "stopScanBLE() finish.");
    }

    public void connectToDevice(BluetoothDevice device, int rssi)
    {
        deviceAddr = device.getAddress();
        Log.d(TAG, "connectToDevice(), device.getAddress: " + deviceAddr);
        if (mBluetoothLeService != null)
        {
            Log.d(TAG, "2 deviceAddr: " + device.getAddress());
        }

        if (device.getAddress().equalsIgnoreCase(DEV_ADDRESS))
        {
            final Intent intent = new Intent(BluetoothLeService.ACTION_CONNECT_REQUEST);
            intent.putExtra(BluetoothLeService.EXTRA_DATA, device.getAddress());
            sendBroadcast(intent);
        }
        //scanLeDevice(false);    // will stop after first device detection
    }


    public void receiveBLEData(String data)
    {
        int dataLength = 0;

        Log.d(TAG, "receiveBLEData(), receive data: " + data + ", Length: " + data.length());
        if (!data.equalsIgnoreCase(""))
        {
            dataLength = checkReceiveLength(data);
        }

        Log.d(TAG, "receiveBLEData(), message Length: " + dataLength);
        if (dataLength > 0)
        {
            totalLength = dataLength + 4;   // 4: is Header length.
        }

        bleParserInfo += data;
        calLength += data.length();
        Log.d(TAG,  "receiveBLEData(), totalLength: " + totalLength +
                    ", calLength/2: " + (calLength/2) +
                    ",  bleParserInfo(" + bleParserInfo.length() +"): " + bleParserInfo);

        if ((calLength/2) >= totalLength)
        {
            byte[] tmpData = Utils.hexStringToByteArray(bleParserInfo);
            int csCode = (Utils.countCS(tmpData) & 0x00ff);
            Log.d(TAG, "csCode: " + format("%02X", csCode) + ",  tmpData[" +
                        (tmpData.length-1) + "] = " + format("%02X", tmpData[tmpData.length-1]));

            if (csCode == (tmpData[tmpData.length-1] & 0x00ff))
            {
                cmdProcess(tmpData);
            }

            bleParserInfo = "";
            totalLength = 0;
            calLength = 0;
        }
        mBluetoothLeService.broadcastUpdate(BluetoothLeService.ACTION_GATT_SERVICE_DISCOVERED);
    }

    public int checkReceiveLength(String data)
    {
        byte[] tmpData = Utils.hexStringToByteArray(data);
        int dataLength = 0;

        if ((tmpData[0] == 'M') && (tmpData[1] == 'Q'))
        {
            dataLength = (Utils.byteToUnsignedInt(tmpData[2]) * 256) +
                         (Utils.byteToUnsignedInt(tmpData[3]));
        }
        else
        {
            Log.d(TAG, "NOT Header Message.");
        }

        Log.d(TAG, "checkReceiveLength(), Data Length: " + dataLength);
        return dataLength;
    }

    public void cmdProcess(byte[] data)
    {
        byte    cmdWord = data[4];
        byte[]  responseInfo = new byte[0];

        switch (cmdWord)
        {
            case (byte) 0xA0:
                cmdA0Parser(data);
                break;

            case (byte) 0xA1:
                responseInfo = cmdA1Parser(data);
                break;

            case (byte) 0xA2:
                responseInfo = cmdA2Parser(data);
                break;

            case (byte) 0xA3:
                responseInfo = cmdA3Parser(data);
                break;

            default:
                break;
        }

        if (responseInfo.length > 0)
        {
            Log.d(TAG,  "CMD 0x" + format("%02X", cmdWord) +
                        " ACK [" + Utils.getHexToString(responseInfo) + "] to 24hr MT device");
            boolean state = ackCommandToDevice(responseInfo);
            if (state == false)
            {
                Log.e(TAG, "CMD 0x" + format("%02X", cmdWord) + ", ACK action fail.(" + state + ")");
            }
            else
            {
                Log.e(TAG, "CMD 0x" + format("%02X", cmdWord) + ", send OK. (" + state + ")");
            }
        }
        else
        {
            Log.d(TAG, "CMD 0x" + format("%02X", cmdWord) + ", don't ACK to device.");
        }
    }

    public void cmdA0Parser(byte[] rawData)
    {
        int intVBat = (Utils.byteToUnsignedInt(rawData[7]));
        float vbat = ((float)intVBat / 100);
        String tmp = (int)rawData[5] + "." + (int)rawData[6];

        setVBat(intVBat);
        showTemperatureUI(tmp);
        Log.d(TAG, "cmdAction_A0: " + tmp + "℃" + ", vBat: " + vbat + "v");
    }

    public byte[] cmdA1Parser(byte[] data)
    {
        byte[] ackInfo = new byte[0];
        float voltage = ( (float)(Utils.byteToUnsignedInt(data[12]) + 100) / 100.0f);
        Log.d(TAG, "CMD A1, Mode: " + format("%02Xh", (byte)data[11]) +
                ", DevBat: " + format("%3.2fv", voltage));

        switch ((byte)(data[11] & 0x00ff))  // work mode
        {
            case (byte)0x00:    // stand by
            case (byte)0x01:    // measure
                ackInfo = Utils.ackDateTime((byte) 0xA1);
                break;

            case (byte)0x02:    // CAL
                ackInfo = Utils.ackMACAddress((byte)0xA1, deviceAddr);
                break;

            default:
                break;
        }
        Log.d(TAG, "cmdA1Parser(), Ack Info: " + Utils.getHexToString(ackInfo));
        return ackInfo;
    }

    public byte[] cmdA2Parser(byte[] data)
    {
        int calVoltage = Utils.byteToUnsignedInt(data[5]) * 256 +
                         Utils.byteToUnsignedInt(data[6]);

        int calCelsius37P1 = Utils.byteToUnsignedInt(data[7]) * 256 +
                             Utils.byteToUnsignedInt(data[8]);

        int calCelsius37P2 = Utils.byteToUnsignedInt(data[9]) * 256 +
                             Utils.byteToUnsignedInt(data[10]);

        Log.d(TAG, "cmdA2Parser(), data[5][6]: " + format("%02X%02X", data[5], data[6]) +
                    ", data[7][8]: " + format("%02X%02X", data[7], data[8]) +
                    ", data[9][10]: " + format("%02X%02X", data[9], data[10]));

        Log.d(TAG, "cmd 0xA2, CAL Voltag: " + calVoltage +
                     ", c37 P1: " + calCelsius37P1 + ", c37 P2: " + calCelsius37P2);

        byte[] ackInfo = new byte[]{0x4D, (byte) 0xFD, 0x00, 0x02, (byte)0xA2, (byte)0xEF};

        return  ackInfo;
    }

    public byte[] cmdA3Parser(byte[] data)
    {
        String          msgString;

        //--- write DRecord raw data to log file for graphics.
        writeLogFile(data);

        //--- Parser DRecord Date / Time.
        msgString = getDRecordDateTime(data);
        Log.d(TAG, "DRecord Data/Time: " + msgString);

        //--- Parser Data Type
        msgString = getDRecordDataType((byte) (data[5] & 0x00ff));
        Log.d(TAG, "DRecord Data Type: " + msgString);

        //--- Parser DRecord data
        saveDRecord(data);

        //--- ACK 0xA3 command.
        byte[] responseInfo = new byte[]{   0x4D, (byte) 0xFD, 0x00, 0x04, (byte) 0xA3,
                (byte)(data[11] & 0x00ff), (byte)(data[12] & 0x00ff), 0x00};

        int tmpCS = Utils.countCS(responseInfo);
        responseInfo[responseInfo.length-1] = (byte)(tmpCS & 0x00ff);
        Log.d(TAG, "cmdA3Parser(), Ack: " + Utils.getHexToString(responseInfo));

        return responseInfo;
    }

    public void writeLogFile(byte[] data)
    {
        String fileName = Utils.shortFileName(".log");
        Log.d(TAG, "fileName: " + fileName);

        String dateTime = Utils.convertArrayToString(data, 6, 5);
        int records = (Utils.byteToUnsignedInt(data[11]) * 256) + Utils.byteToUnsignedInt(data[12]);
        String temperature = Utils.convertArrayToString(data, 13, (records * 3));
        try
        {
            if (!checkWirteTimeString.equalsIgnoreCase(dateTime))
            {
                checkWirteTimeString = dateTime;
                BufferedWriter logFile = new BufferedWriter(new FileWriter(fileName, true)); // append write.
                logFile.write(dateTime + temperature + "\r\n");
                logFile.close();
                Log.d(TAG, fileName + ", has written OK ...");
            }
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
    }

    public String getDRecordDateTime(byte[] data)
    {
        String tmpStr = "";

        for (int i=0; i<5; i++)
        {
            //tmpStr += String.valueOf(Utils.byteToUnsignedInt(data[6 + i]));
            tmpStr += String.format("%02d", Utils.byteToUnsignedInt(data[6 + i]));
        }

        Log.d(TAG, "dRecord Date/Time : " + tmpStr);
        return  tmpStr;
    }

    public String getDRecordDataType(byte info)
    {
        int historyEnd = (info & 0x0081);
        int transmit = (info & 0x0001);
        String strTemp;

        strTemp = "History record is";
        if (historyEnd == 0x80)
        {
            strTemp += " end";
            Log.d(TAG, "(" + format("%02X", historyEnd) + "H )history record is end.");
        }
        else
        {
            strTemp += " NOT end";
            Log.d(TAG, "(" + format("%02X", historyEnd) + "H )history record is NOT end.");
        }

        if (transmit == 0x01)
        {
            strTemp += ", re-transmit.";
            Log.d(TAG, "(" + format("%02X", transmit) + "H ) re-transmit.");
        }
        else
        {
            strTemp += ", is first transmit.";
            Log.d(TAG, "(" + format("%02X", transmit) + "H ) is first transmit.");
        }
        return  strTemp;
    }

    public void saveDRecord(byte[] data)
    {
        final int   recdStart = 13;
        List<Integer>   dRecord = new ArrayList<>();
        List<Integer>   errCode = new ArrayList<>();

        dRecord.clear();
        errCode.clear();
        int records = (Utils.byteToUnsignedInt(data[11]) * 256) + Utils.byteToUnsignedInt(data[12]);
        Log.d(TAG, "data[11]: " + data[11] + ", data[12]: " + data[12]  + ", records: " + records);

        for (int i=0; i<records; i++ )
        {
            int idx = recdStart + (i*3);
            int tmpData = (Utils.byteToUnsignedInt(data[idx]) * 100) + Utils.byteToUnsignedInt(data[idx+1]);
            dRecord.add(tmpData);
            errCode.add(Utils.byteToUnsignedInt(data[idx+2]));
        }

        Log.d(TAG, "dRecord List size: " + dRecord.size());
        ////--- debug message.
        //for (int i=0; i<dRecord.size(); i++)
        //{
        //    Log.d(TAG, "dRecord[" + i + "]= " + dRecord.get(i) + ", errCode: " + errCode.get(i));
        //}
    }


    public boolean ackCommandToDevice(byte[] responInfo)
    {
        boolean state = mBluetoothLeService.writeCharacteristicCMD(responInfo);

        String sb = Utils.getHexToString(responInfo);
        Log.d(TAG, "ackCommandToDevice(), App. send response info to 24hr MT: " + sb);
        return state;
    }

    public void showTemperatureUI(String temperature)
    {
        if ((mAdapter.fBady != null) && gattConnectFlag)
        {
            mAdapter.fBady.BdtvTRUnit.setVisibility(View.VISIBLE);
            mAdapter.fBady.BdtvTemprature.setTextSize(60f);
            mAdapter.fBady.BdtvTemprature.setText(temperature);
            mAdapter.fBady.BdtvYearDate.setTextSize(20f);
            mAdapter.fBady.BdtvYearDate.setText(Utils.getCurrentDateTime());
        }
        else if ((mAdapter.fBady != null) && (!gattConnectFlag))
        {
            //int tvWidth = mAdapter.fBady.BdtvTemprature.getMaxWidth();
            //int tvHigh = mAdapter.fBady.BdtvTemprature.getMaxHeight();
            mAdapter.fBady.BdtvTRUnit.setVisibility(View.INVISIBLE);
            mAdapter.fBady.BdtvTemprature.setTextSize(14);
            mAdapter.fBady.BdtvTemprature.setText(temperature);
            mAdapter.fBady.BdtvYearDate.setText("");
            Log.e(TAG, "Error, gattService disconnected ... ");
        }
        else
        {
            Log.e(TAG, "Error, fragmentBady is " + mAdapter.fBady);
        }
    }

    public void setBTicontoUI(boolean state)
    {
        Log.i(TAG, "setBTicontoUI(), state: " + state);

        ImageView ivBTIcon = (ImageView) mAdapter.fBady.getView().findViewById(R.id.ivBT);
        if (state)
        {
            ivBTIcon.setVisibility(View.VISIBLE);
        }
        else
        {
            ivBTIcon.setVisibility(View.INVISIBLE);
        }
    }

    public void setVBat(int value)
    {
        vBatValue = value;
    }

    public int getVBat()
    {
        return vBatValue ;
    }

    private void initMainView()
    {
        Log.i(TAG, "initMainView() ...");

        imgUserProfile = (ImageView) findViewById(R.id.ivUserProfile);
        UserName = (TextView) findViewById(R.id.tvUserName);

        mAdapter = new ViewPagerAdapter(mManager);
        mPager = (ViewPager) findViewById(R.id.ViewPager_Main);
        mPager.setAdapter(mAdapter);
        mPager.setOnPageChangeListener(new ViewPagerChangeListener());

        mIndicator = (CirclePageIndicator) findViewById(R.id.indicator);
        mIndicator.setViewPager(mPager);
        setCurrentItem(DEFAULT_PAGE);
    }

    private void setCurrentItem(int defaultPage)
    {
        Log.i(TAG, "setCurrentItem(), item: " + defaultPage);

        if (defaultPage == mPager.getCurrentItem())
        {
            notifyPageChangeToFragments(defaultPage);
        }
        else
        {
            mPager.setCurrentItem(defaultPage);
        }
    }

    private void notifyPageChangeToFragments(int item)
    {
        Log.i(TAG, "notifyPageChangeToFragments(), item: " + item);

        for (int page=0; page!=mAdapter.getCount(); ++page)
        {
            final Fragment fragment = mAdapter.getItem(page);
            if (fragment instanceof  PagerFragment)
            {
                if (page == item)
                {
                    ((PagerFragment)fragment).onPageIn();
                }
                else
                {
                    ((PagerFragment)fragment).onPageOut();
                }
            }
        }
    }

    private class ViewPagerAdapter extends FragmentPagerAdapter
    {
        private ArrayList<PagerFragment> mFragments = new ArrayList<PagerFragment>();
        public fragmentBady fBady;
        public fragmentGraphics  fGraphics;

        public ViewPagerAdapter(FragmentManager fm)
        {
            super(fm);
            Log.i(TAG, "ViewPagerAdapter(), fm: " + fm.toString());

            fBady = new fragmentBady();
            fGraphics = new fragmentGraphics();
            mFragments.add(fBady);
            mFragments.add(fGraphics);
            //mFragments.add(new fragmentBady());
            //mFragments.add(new fragmentGrapghs());
        }

        @Override
        public Fragment getItem(int position)
        {
            Log.i(TAG, "getItem(), position: " + position);
            return mFragments.get(position);
        }

        @Override
        public int getCount()
        {
            Log.i(TAG, "getCount(), size: " + mFragments.size());
            return mFragments.size();
        }

        @Override
        public void startUpdate(ViewGroup container)
        {
            Log.e(TAG, "startUpdate(), mPager.getCurrentItem(): " +  mPager.getCurrentItem());
            int tmpItemId = mPager.getCurrentItem();

            if (tmpItemId != changeFramge)  // NOT every times to update.
            {
                changeFramge = tmpItemId;
                switch (changeFramge)
                {
                    case 0:
                        break;

                    case 1:     //update graphics data by log file.
                        //mAdapter.fGraphics.onPageIn();
                        mAdapter.fGraphics.updateGraphics();
                        break;
                }
            }
            //super.startUpdate(container);
        }
    }

    private class ViewPagerChangeListener implements ViewPager.OnPageChangeListener
    {
        @Override
        public void onPageSelected(int position)
        {
            Log.i(TAG, "onPageSelected(), position: " + position);
            setCurrentItem(position);
        }

        @Override
        public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels)
        {
            Log.i(TAG, "onPageScrolled(), position: " + position);

        }

        @Override
        public void onPageScrollStateChanged(int state)
        {
            Log.i(TAG, "onPageScrollStateChanged(), state: " + state);
        }
    }


}
