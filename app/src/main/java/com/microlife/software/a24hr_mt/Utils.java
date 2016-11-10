package com.microlife.software.a24hr_mt;

import android.util.Log;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static java.lang.String.format;


/**
 * Created by tomcat on 2016/6/3.
 */
public class Utils
{
    private static final String TAG = Utils.class.getSimpleName();
    static final String HEXES = "0123456789ABCDEF";
    private static String logFileName = "";

    public Utils()
    {
        logFileName = shortFileName(".log");
    }

    public static byte[] ackDateTime(byte fnCMDByte)
    {
        Calendar mCal = Calendar.getInstance();
        //final int   cmdLength=12;
        byte[]  cmdByte = {0x4D, (byte)0xFD, 0x00, 0x08, (byte)fnCMDByte, 0x01
                ,(byte)(mCal.get(Calendar.YEAR)-2000)
                ,(byte)(mCal.get(Calendar.MONTH)+1)
                ,(byte)(mCal.get(Calendar.DATE))
                ,(byte)(mCal.get(Calendar.HOUR_OF_DAY))
                ,(byte)(mCal.get(Calendar.MINUTE))
                //,(byte)(mCal.get(Calendar.SECOND))
                ,0x00
        };

        cmdByte[cmdByte.length-1] = (byte) countCS(cmdByte);        // check sum

        return cmdByte;
    }

    public static byte[] ackMACAddress(byte cmdByte, String macAddr)
    {
        String strAddr = removeColon(macAddr);
        byte[] byteAddr  = hexStringToByteArray(strAddr);
        byte[] tmpInfo = new byte[]{  0x4D, (byte) 0xFD, 0x00, 0x08, (byte) 0xA1,
                (byte) byteAddr[0], (byte) byteAddr[1], (byte) byteAddr[2],
                (byte) byteAddr[3], (byte) byteAddr[4], (byte) byteAddr[5], 0x00};

        tmpInfo[tmpInfo.length-1] = (byte)(countCS(tmpInfo) & 0x00ff);   //CS

        return tmpInfo;
    }

    public static void setLogFileName(String name)
    {
        logFileName = name;
    }

    public static String getLogFileName()
    {
        return logFileName;
    }

    public static String shortFileName(String subName)
    {
        int[] tmpByte = currentDateTime();
        String utilsFilename = Integer.toString(tmpByte[0]) +
                        Integer.toString(tmpByte[1]) +
                        Integer.toString(tmpByte[6]) ;
                        //Integer.toString(tmpByte[2]) ;
        String SDPathFileName = "/sdcard/" + utilsFilename + subName;
        setLogFileName(SDPathFileName);
        Log.d(TAG, "shortFileName(): " + SDPathFileName);
        return (SDPathFileName);
    }

    public static int[] currentDateTime()
    {
        Calendar    mCal = Calendar.getInstance();
        int[]       tmp = new int[7];

        tmp[0] = mCal.get(Calendar.YEAR);
        tmp[1] = mCal.get(Calendar.MONTH)+1;
        tmp[2] = mCal.get(Calendar.DATE);
        tmp[3] = mCal.get(Calendar.HOUR_OF_DAY);
        tmp[4] = mCal.get(Calendar.MINUTE);
        tmp[5] = mCal.get(Calendar.SECOND);
        tmp[6] = mCal.get(Calendar.WEEK_OF_MONTH);
        return tmp;
    }

    public static String makeFileName()
    {
        int[] tmp = currentDateTime();

        return (format("%04d%02d%02d%02d%02d%02d",
                tmp[0], tmp[1], tmp[2], tmp[3], tmp[4], tmp[5]) );
    }

    public static String getCurrentDateTime()
    {
        int[] tmp = currentDateTime();

        return (format("%04d/%02d/%02d  %02d:%02d:%02d",
                tmp[0], tmp[1], tmp[2], tmp[3], tmp[4], tmp[5]) );
    }

    public static void writeLogFile(List<byte[]> DataList)
    {
        //Environment.getExternalStorageDirectory().getPath()
        String  fileName = "/sdcard/" + makeFileName() + ".log";
        byte[]  nextLine = {0x0D, 0x0A};

        Log.d(TAG, "log file: " + fileName);
        try
        {
            FileOutputStream    fOut = new FileOutputStream(new File(fileName), true);
            for (int i=0; i<DataList.size(); i++)
            {
                fOut.write(getHexToString(DataList.get(i)).getBytes());
                fOut.write(nextLine);
            }

            fOut.close();
            Log.d(TAG, "write log file Ok.");
        }
        catch (FileNotFoundException e)
        {
            //e.printStackTrace();
            Log.d(TAG, "File or Path Not found !");
        }
        catch (IOException e)
        {
            //e.printStackTrace();
            Log.d(TAG, "write File fail !");
        }
    }

    //public static ArrayList<Long> dtToSecond(ArrayList<byte[]> data)
    public static ArrayList<Date> dtToSecond(ArrayList<byte[]> data)
    {
        //Date today = new Date();
        ArrayList<Date> dateList = new ArrayList<>();
        SimpleDateFormat sdf = new SimpleDateFormat("yyMMddHHmm");

        //--- convert raw data time to Java Date Object.
        for(int i=0; i<data.size(); i++)
        //for(int i=0; i<40; i++)
        {
            String tmpDate = String.format("%02d%02d%02d%02d%02d", data.get(i)[0],
                    data.get(i)[1], data.get(i)[2], data.get(i)[3], data.get(i)[4]);
            Date tmpTime = new Date();
            try
            {
                tmpTime = sdf.parse(tmpDate);
            }
            catch (java.text.ParseException e)
            {
                e.printStackTrace();
            }

            dateList.add(tmpTime);
            Log.d(TAG, "date[" + i + "]: " + sdf.format(tmpTime));
            //System.out.printf("date[%d]: %s%n", i, sdf.format(tmpTime));
        }

        //Log.d(TAG, "dtToSecond(), covert date List size: " + data.size() );
        //Log.d(TAG, "dtToSecond(), dateList size:" + dateList.size() + ", dateList[0]:{" + dateList.get(0) +
        //        "}, dateList[" + (dateList.size() - 1) + "]:{" + dateList.get(dateList.size()-1) + "}" );
        Log.d(TAG, "dtToSecond(), dateList size:" + dateList.size() + ", dateList[0]: " + sdf.format(dateList.get(0)) +
                ", dateList[" + (dateList.size() - 1) + "]: " + sdf.format(dateList.get(dateList.size()-1)) );


        /*
        //--- Calculate time difference by seconds from first record.
        ArrayList<Long> diffSecondList = new ArrayList<>();
        for(int i=0; i<dateList.size(); i++)
        //for(int i=0; i<40; i++)
        {
            long tmpDiffSec = dateList.get(i).getTime() - dateList.get(0).getTime();
            diffSecondList.add(tmpDiffSec);
            System.out.printf("diff Sec[%02d]: %d%n", i, tmpDiffSec/1000);
        }
        return diffSecondList;
        */

        return dateList;
    }

    public static ArrayList<Integer> get180Records(ArrayList<Integer> srcList, int lastIndex)
    {
        ArrayList<Integer> indexList = new ArrayList<>();
        int start =0 ;

        //callAgain++;

        //if(srcList.size() >= 180)
        if(lastIndex >= 180)
        {
            start = lastIndex - 180;
        }
        else if(lastIndex < 180)
        {
            start = 0;
        }

        //for(int i=start; i < srcList.size(); i++)
        for(int i=start; i<=lastIndex ; i++)
        {
            indexList.add(srcList.get(i));
        }

        //--- debug massage
        //for(int i=0; i<indexList.size(); i++)
        //   System.out.printf("get180Records(), indexList[%02d] = %04d,  callAgain: %02d %n", i, indexList.get(i), callAgain );
        for(int i=0; i<indexList.size(); i++)
           Log.d(TAG, "get180Records(), indexList[" + i + "] = " + indexList.get(i));

        return indexList;
    }

    public static ArrayList<Integer> checkOver3Days(ArrayList<Date> dateList)
    {
        ArrayList<Integer> indexList = new ArrayList<>();
        long timeStemp = 3 * 86400;	//over 3 days
        int k;

        for(int i=0; i<dateList.size(); i++)
        {
            if(i>=(dateList.size()-1))
                k = dateList.size()-1;
            else
                k = i+1;

            long now = TimeUnit.MILLISECONDS.toSeconds(dateList.get(k).getTime() - dateList.get(i).getTime());
            if((now < timeStemp) && (i<dateList.size()))
            {
                indexList.add(i);
            }

            //System.out.printf("get180RecordList(), indexList[%02d]: %d %n", i, indexList.get(i));
            //System.out.printf("diffSecList(), MILLISECONDS.toSeconds.[%02d]: %d, dateList[%02d]:{%s} %n",
            //			i, now, i, sdf.format((dateList.get(0).getTime() + longList.get(i-1))));
        }

        //--- debug massage
        for(int j=0; j<indexList.size(); j++)
        {
            //System.out.printf("getLast3DaysList(), indexList[%02d]: %d %n", j, indexList.get(j));
            Log.d(TAG, "getLast3DaysList(), indexList[" + j + "]: " + indexList.get(j) );
        }

        return indexList;
    }


    public static ArrayList<Long> dtDeffrenceList(ArrayList<Date> data)
    {
        ArrayList<Long> diffSecondList = new ArrayList<>();
        int k;

        for(int i=0; i<data.size(); i++)
        //for(int i=0; i<40; i++)
        {
            if(i>=(data.size()-1))
                k = data.size()-1;
            else
                k = i+1;

            long tmpDiffSec = data.get(k).getTime() - data.get(i).getTime();
            diffSecondList.add(tmpDiffSec);
            //System.out.printf("diff Sec[%02d]: %d%n", i, tmpDiffSec/1000);
            Log.d(TAG, "diffSecondList[" + i + "]: " + tmpDiffSec);
        }

        Log.d(TAG, "dtDeffrenceList(), diffSecondList size:" + diffSecondList.size() + ", diffSecondList[0]: " + diffSecondList.get(0) +
                ", diffSecondList[" + (diffSecondList.size() - 1) + "]: " + diffSecondList.get(diffSecondList.size()-1) );
        return diffSecondList;
    }


    public static int byteToUnsignedInt(byte b)
    {
        return 0x00 << 24 | b & 0xff;
    }

    public static int countCS(byte[] data)
    {
        int tmpCS=0;
        for (int i=0; i<(data.length-1); i++)
        {
            int tmpInt = byteToUnsignedInt(data[i]);
            tmpCS += tmpInt;
            //Log.d(TAG, format("countCS(), [%d]: %04X, %04X", i, tmpInt, tmpCS));
        }
        Log.d(TAG, format("countCS(): %04X H", tmpCS));
        //System.out.println(String.format("countCS(): %04Xh", (tmpCS & 0x00ff)));
        return (tmpCS);
    }

    public static String convertArrayToString(byte[] data, int start, int length)
    {
        byte[] tmpbyte = new byte[length];

        for (int i=0; i<length; i++)
            tmpbyte[i] = data[start+i];
        String tmpStr = getHexToString(tmpbyte);

        return(tmpStr);
    }

    public static String getHexToString(byte[] raw)
    {
        //StringBuilder sb= new StringBuilder(responInfo.length);
        //for (byte indx: responInfo)
        //{
        //    sb.append(format("%02X", indx));
        //}

        if (raw == null)
        {
            return null;
        }

        final StringBuilder hex = new StringBuilder(2 * raw.length);
        for (final byte b : raw)
        {
            hex.append(HEXES.charAt((b & 0xF0) >> 4))
                    .append(HEXES.charAt((b & 0x0F)));
        }
        return hex.toString();
    }

    public static byte[] hexStringToByteArray(String s)
    {
        int len = s.length();
        byte[] data = new byte[len/2];

        for(int i = 0; i < len; i+=2)
        {
            data[i/2] = (byte) ((Character.digit(s.charAt(i), 16) << 4) +
                                 Character.digit(s.charAt(i+1), 16));
        }

        return data;
    }

    public static String removeColon(String s)
    {
        String strArray[] = s.split(":");
        String tmpStr = "";

        Log.d("Colon", "string Array: " + strArray.toString());

        for (int i=0; i<strArray.length; i++)
        {
            tmpStr += strArray[i];
        }
        Log.d("Colon", "no Colon string: " + tmpStr);

        return tmpStr;
    }
}
