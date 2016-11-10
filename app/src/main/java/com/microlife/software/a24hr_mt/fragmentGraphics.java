package com.microlife.software.a24hr_mt;

import android.graphics.Color;
import android.graphics.DashPathEffect;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.github.mikephil.charting.animation.Easing;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.AxisBase;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.LimitLine;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.formatter.IAxisValueFormatter;
import com.github.mikephil.charting.highlight.Highlight;
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet;
import com.github.mikephil.charting.listener.ChartTouchListener;
import com.github.mikephil.charting.listener.OnChartGestureListener;
import com.github.mikephil.charting.listener.OnChartValueSelectedListener;
import com.github.mikephil.charting.utils.*;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.text.SimpleDateFormat;

/**
 * MPAndroidChart: https://github.com/PhilJay/MPAndroidChart
 * Created by tomcat on 2016/9/28.
 */

public class fragmentGraphics extends PagerFragment implements
        OnChartGestureListener, OnChartValueSelectedListener
{
    private final static String TAG = fragmentGraphics.class.getSimpleName();

    ImageView   imgEmail;
    TextView    tvDateInfo;
    View        mViewRoot;
    ImageButton imgBtnDelete;
    LineChart   mChart;

    //BufferedReader  logReader;
    //ArrayList<byte[]>  rawDataList = new ArrayList<>();
    ArrayList<byte[]>  recordTimeList = new ArrayList<>();
    ArrayList<Integer> temperatureList = new ArrayList<>();

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        //return super.onCreateView(inflater, container, savedInstanceState);

        Log.d(TAG, "fragmentGraphics, onCreateView.");

        mViewRoot = inflater.inflate(R.layout.fragment_graphics, container, false);
        initView(mViewRoot);
        return mViewRoot;
    }

    private void initView(View root)
    {
        tvDateInfo = (TextView) root.findViewById(R.id.tvdayInfo);
        imgEmail = (ImageView) root.findViewById(R.id.ivEmail);
        mChart = (LineChart) root.findViewById(R.id.graphChart);
        imgBtnDelete = (ImageButton) root.findViewById(R.id.imBtnDel);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState)
    {
        super.onActivityCreated(savedInstanceState);
        Utils.shortFileName(".log");
        onPageIn();
        //updateGraphics();
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater)
    {
        //super.onCreateOptionsMenu(menu, inflater);
        Log.e(TAG, "onCreateOptionsMenu()");
        menu.clear();
        inflater.inflate(R.menu.line, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        List<ILineDataSet> sets;
        //return super.onOptionsItemSelected(item);

        switch (item.getItemId())
        {
            case R.id.actionToggleValues:
                //List<ILineDataSet> sets = mChart.getData().getDataSets();
                sets = mChart.getData().getDataSets();

                for (ILineDataSet iSet : sets)
                {
                    LineDataSet set = (LineDataSet) iSet;
                    set.setDrawValues(!set.isDrawValuesEnabled());
                }
                mChart.invalidate();
                break;

            case R.id.actionToggleHighlight:
                if (mChart.getData() != null)
                {
                    mChart.getData().setHighlightEnabled(!mChart.getData().isHighlightEnabled());
                    mChart.invalidate();
                }
                break;

            case R.id.actionToggleFilled:
                //List<ILineDataSet> sets = mChart.getData().getDataSets();
                sets = mChart.getData().getDataSets();

                for (ILineDataSet iSet : sets)
                {
                    LineDataSet set = (LineDataSet) iSet;
                    if (set.isDrawFilledEnabled())
                        set.setDrawFilled(false);
                    else
                        set.setDrawFilled(true);
                }
                mChart.invalidate();
                break;

            case R.id.actionToggleCircles:
                //List<ILineDataSet> sets = mChart.getData().getDataSets();
                sets = mChart.getData().getDataSets();

                for (ILineDataSet iSet : sets)
                {
                    LineDataSet set = (LineDataSet) iSet;
                    if (set.isDrawCirclesEnabled())
                        set.setDrawCircles(false);
                    else
                        set.setDrawCircles(true);
                }
                mChart.invalidate();
                break;

            case R.id.actionToggleCubic:
                //List<ILineDataSet> sets = mChart.getData().getDataSets();
                sets = mChart.getData().getDataSets();

                for (ILineDataSet iSet : sets)
                {
                    LineDataSet set = (LineDataSet) iSet;
                    set.setMode(set.getMode() == LineDataSet.Mode.CUBIC_BEZIER
                            ? LineDataSet.Mode.LINEAR
                            :  LineDataSet.Mode.CUBIC_BEZIER);
                }
                mChart.invalidate();
                break;

            case R.id.actionToggleStepped:
                //List<ILineDataSet> sets = mChart.getData().getDataSets();
                sets = mChart.getData().getDataSets();

                for (ILineDataSet iSet : sets)
                {
                    LineDataSet set = (LineDataSet) iSet;
                    set.setMode(set.getMode() == LineDataSet.Mode.STEPPED
                            ? LineDataSet.Mode.LINEAR
                            :  LineDataSet.Mode.STEPPED);
                }
                mChart.invalidate();
                break;

            case R.id.actionToggleHorizontalCubic:
                //List<ILineDataSet> sets = mChart.getData().getDataSets();
                sets = mChart.getData().getDataSets();

                for (ILineDataSet iSet : sets)
                {
                    LineDataSet set = (LineDataSet) iSet;
                    set.setMode(set.getMode() == LineDataSet.Mode.HORIZONTAL_BEZIER
                            ? LineDataSet.Mode.LINEAR
                            :  LineDataSet.Mode.HORIZONTAL_BEZIER);
                }
                mChart.invalidate();
                break;

            case R.id.actionTogglePinch:
                if (mChart.isPinchZoomEnabled())
                    mChart.setPinchZoom(false);
                else
                    mChart.setPinchZoom(true);

                mChart.invalidate();
                break;

            case R.id.actionToggleAutoScaleMinMax:
                mChart.setAutoScaleMinMaxEnabled(!mChart.isAutoScaleMinMaxEnabled());
                mChart.notifyDataSetChanged();
                break;

            case R.id.animateX:
                mChart.animateX(3000);
                break;

            case R.id.animateY:
                mChart.animateY(3000, Easing.EasingOption.EaseInCubic);
                break;

            case R.id.animateXY:
                mChart.animateXY(3000, 3000);
                break;

            case R.id.actionSave:
                if (mChart.saveToPath("title" + System.currentTimeMillis(), ""))
                {
                    Toast.makeText(getActivity().getApplicationContext(), "Saving SUCCESSFUL!",
                            Toast.LENGTH_SHORT).show();
                }
                else
                    Toast.makeText(getActivity().getApplicationContext(), "Saving FAILED!",
                            Toast.LENGTH_SHORT).show();

                // mChart.saveToGallery("title"+System.currentTimeMillis())
                break;
        }
        return true;
    }

    @Override
    public void onDestroy()
    {
        super.onDestroy();
    }

    @Override
    public void onPageIn()
    {
        //super.onPageIn();
        Log.i(TAG, "onPageIn() ...");
        ArrayList<byte[]> rawDataList = readLogFile(Utils.getLogFileName());
        //ArrayList<byte[]> rawDataList = readLogFile("/sdcard/20161024.log");
        if (rawDataList.size() > 0)
        {
            recordTimeList = getTimeList(rawDataList);
            temperatureList = getTemperatureList(rawDataList);
        }
        else
        {
            Log.e(TAG, "Error! log file No data.");
            return;
        }
    }

    @Override
    public void onPageOut()
    {
        Log.i(TAG, "onPageOut() ...");
        super.onPageOut();
    }

    @Override
    public void onChartGestureStart(MotionEvent me, ChartTouchListener.ChartGesture chartGesture)
    {
        Log.i(TAG, "Gesture, START, x: " + me.getX() + ", y: " + me.getY());
    }

    @Override
    public void onChartGestureEnd(MotionEvent motionEvent, ChartTouchListener.ChartGesture lastPerformedGesture)
    {
        Log.i("Gesture", "END, lastGesture: " + lastPerformedGesture);

        // un-highlight values after the gesture is finished and no single-tap
        if(lastPerformedGesture != ChartTouchListener.ChartGesture.SINGLE_TAP)
            mChart.highlightValues(null); // or highlightTouch(null) for callback to onNothingSelected(...)
    }

    @Override
    public void onChartLongPressed(MotionEvent motionEvent)
    {
        Log.i("LongPress", "Chart longpressed.");
    }

    @Override
    public void onChartDoubleTapped(MotionEvent motionEvent)
    {
        Log.i("DoubleTap", "Chart double-tapped.");
    }

    @Override
    public void onChartSingleTapped(MotionEvent motionEvent)
    {
        Log.i("SingleTap", "Chart single-tapped.");
    }

    @Override
    public void onChartFling(MotionEvent motionEvent, MotionEvent motionEvent1, float v, float v1)
    {
        Log.i("Fling", "Chart flinged. VeloX: " + v + ", VeloY: " + v1);
    }

    @Override
    public void onChartScale(MotionEvent motionEvent, float v, float v1)
    {
        Log.i("Scale / Zoom", "ScaleX: " + v + ", ScaleY: " + v1);
    }

    @Override
    public void onChartTranslate(MotionEvent motionEvent, float v, float v1)
    {
        Log.i("Translate / Move", "dX: " + v + ", dY: " + v1);
    }

    @Override
    public void onValueSelected(Entry e, Highlight highlight)
    {
        Log.i("Entry selected", e.toString());
        Log.i("LOWHIGH", "low: " + mChart.getLowestVisibleX() +
                ", high: " + mChart.getHighestVisibleX());
        Log.i("MIN MAX", "xmin: " + mChart.getXChartMin() + ", xmax: " + mChart.getXChartMax() +
                ", ymin: " + mChart.getYChartMin() + ", ymax: " + mChart.getYChartMax());
    }

    @Override
    public void onNothingSelected()
    {
        Log.i("Nothing selected", "Nothing selected.");
    }


    //--- User define function.-----------------------------------------------------//
    public void updateGraphics()
    {
        Log.i(TAG, "updateGraphics() ...");
        Utils.shortFileName(".log");

        //mChart = (LineChart) findViewById(R.id.chart1);
        mChart.setOnChartGestureListener(this);
        mChart.setOnChartValueSelectedListener(this);
        mChart.setDrawGridBackground(false);

        // no description text
        mChart.getDescription().setEnabled(false);

        // enable touch gestures
        mChart.setTouchEnabled(true);

        // enable scaling and dragging
        mChart.setDragEnabled(true);
        mChart.setScaleEnabled(true);
        // mChart.setScaleXEnabled(true);
        // mChart.setScaleYEnabled(true);

        // if disabled, scaling can be done on x- and y-axis separately
        mChart.setPinchZoom(true);

        // set an alternative background color
        // mChart.setBackgroundColor(Color.GRAY);

        // create a custom MarkerView (extend MarkerView) and specify the layout
        // to use for it
        //MyMarkerView mv = new MyMarkerView(this, R.layout.custom_marker_view);
        //mv.setChartView(mChart); // For bounds control
        //mChart.setMarker(mv); // Set the marker to the chart

        // x-axis limit line
        LimitLine llXAxis = new LimitLine(1.0f, "Index 10");
        llXAxis.setLineWidth(1f);
        llXAxis.enableDashedLine(1.0f, 1.0f, 0f);
        //llXAxis.setLabelPosition(LimitLine.LimitLabelPosition.RIGHT_BOTTOM);
        llXAxis.setLabelPosition(LimitLine.LimitLabelPosition.LEFT_BOTTOM);
        llXAxis.setTextSize(10f);

        //IAxisValueFormatter xAxisFormatter = new Ho
        XAxis xAxis = mChart.getXAxis();
        //xAxis.setPosition(XAxis.XAxisPosition.BOTTOM_INSIDE);
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        //xAxis.enableGridDashedLine(1.0f, 1.0f, 0.01f);
        xAxis.enableGridDashedLine(1.0f, 1.0f, 0.1f);
        xAxis.setGranularity(1f);
        //xAxis.setValueFormatter(new MyCustomXAxisValueFormatter());
        //xAxis.addLimitLine(llXAxis); // add x-axis limit line

        xAxis.setValueFormatter(new IAxisValueFormatter()
        {
            //private SimpleDateFormat mFormat = new SimpleDateFormat("dd MMM HH:mm");
            //private SimpleDateFormat mFormat = new SimpleDateFormat("MM/dd HH:mm");
            private SimpleDateFormat mFormat = new SimpleDateFormat("dd/HH:mm");

            @Override
            public String getFormattedValue(float value, AxisBase axisBase)
            {
                //long millis = TimeUnit.HOURS.toMillis((long) value*1000);
                //long millis = TimeUnit.MILLISECONDS.toMillis((long) value);
                long millis = TimeUnit.MILLISECONDS.toSeconds((long) value*1000);
                //long millis = TimeUnit.MILLISECONDS.toMinutes((long) value);
                Log.d(TAG, "SimpleDateFormat: " + mFormat.format(millis) + ", millis: " + millis + ", value: " + value);
                return mFormat.format(new Date(millis));
            }

            @Override
            public int getDecimalDigits()
            {
                return 0;
            }
        });


        Typeface tf = Typeface.createFromAsset(getActivity().getAssets(), "OpenSans-Regular.ttf");

        //LimitLine ll1 = new LimitLine(150f, "Upper Limit");
        LimitLine ll1 = new LimitLine(38f, "Upper Limit");
        ll1.setLineWidth(2f);
        ll1.enableDashedLine(1.0f, 1.0f, 0f);
        ll1.setLabelPosition(LimitLine.LimitLabelPosition.RIGHT_TOP);
        ll1.setTextSize(10f);
        ll1.setTypeface(tf);

        //LimitLine ll2 = new LimitLine(-30f, "Lower Limit");
        LimitLine ll2 = new LimitLine(32f, "Lower Limit");
        ll2.setLineWidth(2f);
        ll2.enableDashedLine(1.0f, 1.0f, 0f);
        ll2.setLabelPosition(LimitLine.LimitLabelPosition.RIGHT_BOTTOM);
        ll2.setTextSize(10f);
        ll2.setTypeface(tf);

        YAxis leftAxis = mChart.getAxisLeft();
        leftAxis.removeAllLimitLines(); // reset all limit lines to avoid overlapping lines
        leftAxis.addLimitLine(ll1);
        leftAxis.addLimitLine(ll2);
        //leftAxis.setAxisMaximum(200f);
        //leftAxis.setAxisMinimum(-50f);
        leftAxis.setAxisMaximum(50f);
        leftAxis.setAxisMinimum(20f);
        //leftAxis.setYOffset(20f);
        leftAxis.enableGridDashedLine(1.0f, 1.0f, 0f);
        leftAxis.setDrawZeroLine(false);

        // limit lines are drawn behind data (and not on top)
        leftAxis.setDrawLimitLinesBehindData(true);

        mChart.getAxisRight().setEnabled(false);

        //mChart.getViewPortHandler().setMaximumScaleY(2f);
        //mChart.getViewPortHandler().setMaximumScaleX(2f);

        onPageIn();
        if ((recordTimeList.size() > 0) && (temperatureList.size() > 0))
        {
            setData(recordTimeList, temperatureList);
        }
        else
        {
            setDataDemo(45, 100);
        }

        //        mChart.setVisibleXRange(20);
        //        mChart.setVisibleYRange(20f, AxisDependency.LEFT);
        //        mChart.centerViewTo(20, 50, AxisDependency.LEFT);

        mChart.animateX(2500);
        //mChart.invalidate();

        // get the legend (only possible after setting data)
        Legend l = mChart.getLegend();

        // modify the legend ...
        l.setForm(Legend.LegendForm.LINE);

        // // dont forget to refresh the drawing
        // mChart.invalidate();
    }

    private ArrayList<byte[]> readLogFile(String name)
    {
        File logFile = new File(name);
        ArrayList<byte[]>  rawDataList = new ArrayList<>();
        int lineCounts = 0;

        rawDataList.clear();
        Log.i(TAG, "readLogFile(), log file name: " + name);
        try
        {
            BufferedReader br = new BufferedReader(new FileReader(logFile));
            String      line, tmpLine;

            tmpLine = "";
            while((line = br.readLine()) != null)
            {
                if(!line.equalsIgnoreCase(tmpLine))
                {
                    tmpLine = line;
                    rawDataList.add(Utils.hexStringToByteArray(tmpLine));
                    lineCounts++;
                    //Log.i(TAG, "raw data[" + (lineCounts) + "] = " + line);
                }
            }
            br.close();
            Log.i(TAG, name + ", to read " + (lineCounts) + " lines");
        }
        catch (IOException e)
        {
            //e.printStackTrace();
            Log.e(TAG, "Error! file state: " + e.toString());
        }
        return rawDataList;
    }

    private ArrayList<byte[]> getTimeList(ArrayList<byte[]> data)
    {
        ArrayList<byte[]> dateTime = new ArrayList<>();
        Log.d(TAG, "getTimeList(), data list size:" + data.size());

        for (int i=0; i<data.size(); i++)
        {
            int records = 0;
            int leng = data.get(i).length;
            byte[] tmpDate = new byte[5];

            if (leng > 8)
                records = (leng - 8) / 3;
            //Log.d(TAG, "data length: " + leng + ", records: " + records);

            //--- separate date/time
            for(int j=0; j<5; j++)
            {
                tmpDate[j] = data.get(i)[j];
            }

            //--- create date/time list
            for (int k=0; k<=records; k++)
            {
                if (k > 0)
                {
                    tmpDate[4]++;
                    if (tmpDate[4] > 0x3B)      // minute > 59
                    {
                        tmpDate[3]++;			// Hour increase
                        tmpDate[4] -= 0x3C;		// minute minus 60.
                    }
                }
                byte[] newTmepTime = tmpDate.clone();
                dateTime.add(newTmepTime);
            }
        }

        //Log.d(TAG, "to read " + dateTime.size() + " records");
        Log.d(TAG, "getTimeList(), dateTime size:" + dateTime.size() + ", dateTime[0]: " + Utils.getHexToString(dateTime.get(0)) +
                ", dateTime[" + (dateTime.size() - 1) + "]: " + Utils.getHexToString(dateTime.get(dateTime.size()-1)) );

        //--- debug message
        //for (int i=0; i<dateTime.size(); i++)
        //    Log.d(TAG, "Date/Time [" + i + "]: " + Utils.getHexToString(dateTime.get(i)));

        return dateTime;
    }

    private ArrayList<Integer> getTemperatureList(ArrayList<byte[]> data)
    {
        int size = data.size();
        ArrayList<Integer> tmplist = new ArrayList<>();
        Log.d(TAG, "getTemperatureList(), data list size: " + size );

        //--- calculate temperature to integer list.
        for(int i=0; i<size; i++)
        {
            int tmp = 0;
            int leng = data.get(i).length-5;
            //Log.d(TAG, " raw data[" + i +"], lengh: " + leng);

            for(int j=0; j<(leng/3); j++)
            {
                int idx= (j*3);
                tmp = Utils.byteToUnsignedInt(data.get(i)[5 + idx]) * 100 +
                        Utils.byteToUnsignedInt(data.get(i)[6+idx]);
                tmplist.add(tmp);
            }
        }

        Log.d(TAG, "getTemperatureList(), tmplist size:" + tmplist.size() + ", tmpList[0]: " + tmplist.get(0) +
                ", tmpList[" + (tmplist.size() - 1) + "]: " + tmplist.get(tmplist.size()-1) );

        //--- debug message
        //for(int i=0; i<tmplist.size(); i++)
        //{
        //    Log.d(TAG, "tmplist[" + i + "]: " + tmplist.get(i));
        //}

        return tmplist;
    }

    private void setData(ArrayList<byte[]> dateTime, ArrayList<Integer> temperature)
    {
        //long now = TimeUnit.MILLISECONDS.toHours(System.currentTimeMillis());
        SimpleDateFormat sdf = new SimpleDateFormat("dd/HH:mm");
        ArrayList<Date>     dateList = Utils.dtToSecond(dateTime);
        ArrayList<Integer>  in3DaysList = Utils.checkOver3Days(dateList);
        ArrayList<Integer>  displayList = Utils.get180Records(in3DaysList, in3DaysList.size()-1);
        ArrayList<Long> mySecondList = Utils.dtDeffrenceList(dateList);

        //long now = TimeUnit.MILLISECONDS.toDays(myDateList.get(0).getTime());
        //long now = TimeUnit.MILLISECONDS.toHours(myDateList.get(0).getTime());
        //long now = TimeUnit.MILLISECONDS.toMinutes(dateList.get(displayList.get(0)).getTime());
        long now = dateList.get(displayList.get(0)).getTime();
        Log.d(TAG, " sdf.format(now) :" + sdf.format(now));
        //Log.d(TAG, " TimeUnit.MILLISECONDS.toMinutes(): " + TimeUnit.MILLISECONDS.toMinutes(myDateList.get(1).getTime()));

        ArrayList<Entry> values = new ArrayList<Entry>();

        float from = now;
        //float to = now + temperature.size();
        float to = now + displayList.size();
        //float to = now + 5;
        //float to = now +  TimeUnit.MILLISECONDS.toHours(myDateList.get(myDateList.size()-1).getTime());
        //float to = now + ;
        Log.d(TAG, "setData(), dateList[0]:" + dateList.get(0).getTime() + ", from: " + now + ", to: " + to);

        //float x=from;
        //float y = ((float) temperature.get((int)(x-from))/100);
        //Log.d(TAG, "setData(), temperature.get(" + (x - from) + "), y: " + y);


        int indx;
        //for (float x=from; x<to; x++)
        for (int i=0; i<displayList.size(); i++)
        {
            indx = displayList.get(i);
            now += (mySecondList.get(indx));
            Log.d(TAG, "[" + i + "] sdf.format(now) :" + sdf.format(now));
            //now +
            float y = ((float) temperature.get(indx)/100);
            //float y = getRandom(30, 50);
            //float y = (float)(Math.random() * 30 + 50);
            //Log.d(TAG, "setData(), temperature.get(" + (x - from) + "), y: " + y);
            //Log.d(TAG, "setData(), x:" + now + ", to: " + to + ",x-form: " + (x-from));
            Log.d(TAG, "setData(), x:" + now );
            //Log.d(TAG, "setData(), temperature.get(" + (x - from) + "), y: " + y);

            //long nowii = TimeUnit.MILLISECONDS.toMinutes(myDateList.get((int)(x-from)).getTime());

            values.add(new Entry(now, y));
        }



        // create a dataset and give it a type
        //LineDataSet set1 = new LineDataSet(values, "DataSet 1");
        LineDataSet set1 = new LineDataSet(values, "℃");
        set1.setAxisDependency(YAxis.AxisDependency.LEFT);
        set1.setColor(ColorTemplate.getHoloBlue());
        set1.setValueTextColor(ColorTemplate.getHoloBlue());
        set1.setLineWidth(1.5f);
        set1.setDrawCircleHole(false);
        set1.setDrawValues(false);
        set1.setFillAlpha(65);
        set1.setFillColor(ColorTemplate.getHoloBlue());
        set1.setHighLightColor(Color.rgb(244, 117, 117));
        set1.setDrawCircleHole(false);

        LineData data = new LineData(set1);
        data.setValueTextColor(Color.WHITE);
        data.setValueTextSize(9f);

       // mChart.getXAxis().setValueFormatter(new MyAxisValueFormatter());

        mChart.setData(data);


        /*
        //--- user display data.
        //for (int i=0; i<temperature.size(); i++)
        //{
        //    float val = ((float) temperature.get(i)/100.0f);
        //    //Log.d(TAG, "temperature: " + val);
        //    values.add(new Entry(i, val));
        //}

        LineDataSet     set1;
        if ((mChart.getData() != null) && (mChart.getData().getDataSetCount() > 0))
        {
            set1 = (LineDataSet)mChart.getData().getDataSetByIndex(0);
            set1.setValues(values);
            mChart.getData().notifyDataChanged();
            mChart.notifyDataSetChanged();
        }
        else
        {
            // create a dataset and give it a type
            set1 = new LineDataSet(values, "DataSet 1");

            // set the line to be drawn like this "- - - - - -"
            set1.enableDashedLine(10f, 5f, 0f);
            set1.enableDashedHighlightLine(10f, 5f, 0f);
            set1.setColor(Color.BLACK);
            set1.setCircleColor(Color.BLACK);
            set1.setLineWidth(1f);
            set1.setCircleRadius(3f);
            set1.setDrawCircleHole(false);
            set1.setValueTextSize(9f);
            set1.setDrawFilled(true);
            set1.setFormLineWidth(1f);
            set1.setFormLineDashEffect(new DashPathEffect(new float[]{10f, 5f}, 0f));
            set1.setFormSize(15.f);

            if (com.github.mikephil.charting.utils.Utils.getSDKInt() >= 18)
            {
                // fill drawable only supported on api level 18 and above
                Drawable drawable = ContextCompat.getDrawable(this.getContext(), R.drawable.fade_red);
                set1.setFillDrawable(drawable);
            }
            else
            {
                set1.setFillColor(Color.BLACK);
            }

            ArrayList<ILineDataSet> dataSets = new ArrayList<ILineDataSet>();
            dataSets.add(set1); // add the datasets

            // create a data object with the datasets
            LineData data = new LineData(dataSets);

            // set data
            mChart.setData(data);

        }
        */
    }

    private void setDataDemo(int count, float range)
    {
        ArrayList<Entry> values = new ArrayList<Entry>();

        for (int i = 0; i < count; i++)
        {
            float val = (float) (Math.random() * range) + 3;
            values.add(new Entry(i, val));
        }

        //for (int i=0; i<temperature.size(); i++)
        //{
        //    float val = ((float) temperature.get(i)/100.0f);
        //    Log.d(TAG, "temperature: " + val);
        //    values.add(new Entry(i, val));
        //}

        LineDataSet set1;

        if (mChart.getData() != null &&
                mChart.getData().getDataSetCount() > 0) {
            set1 = (LineDataSet)mChart.getData().getDataSetByIndex(0);
            set1.setValues(values);
            mChart.getData().notifyDataChanged();
            mChart.notifyDataSetChanged();
        } else {
            // create a dataset and give it a type
            set1 = new LineDataSet(values, "DataSet 1");

            // set the line to be drawn like this "- - - - - -"
            set1.enableDashedLine(10f, 5f, 0f);
            set1.enableDashedHighlightLine(10f, 5f, 0f);
            set1.setColor(Color.BLACK);
            set1.setCircleColor(Color.BLACK);
            set1.setLineWidth(1f);
            set1.setCircleRadius(3f);
            set1.setDrawCircleHole(false);
            set1.setValueTextSize(9f);
            set1.setDrawFilled(true);
            set1.setFormLineWidth(1f);
            set1.setFormLineDashEffect(new DashPathEffect(new float[]{10f, 5f}, 0f));
            set1.setFormSize(15.f);

            if (com.github.mikephil.charting.utils.Utils.getSDKInt() >= 18) {
                // fill drawable only supported on api level 18 and above
                Drawable drawable = ContextCompat.getDrawable(this.getContext(), R.drawable.fade_red);
                set1.setFillDrawable(drawable);
            }
            else
            {
                set1.setFillColor(Color.BLACK);
            }

            ArrayList<ILineDataSet> dataSets = new ArrayList<ILineDataSet>();
            dataSets.add(set1); // add the datasets

            // create a data object with the datasets
            LineData data = new LineData(dataSets);

            // set data
            mChart.setData(data);
        }
    }

}
