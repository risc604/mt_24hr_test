package com.microlife.software.a24hr_mt;

import android.app.Dialog;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.NumberPicker;
import android.widget.TextView;

/**
 * Created by tomcat on 2016/9/28.
 */

public class fragmentBady extends PagerFragment implements NumberPicker.OnValueChangeListener
{
    //public static fragmentBady newInstance(int index)
    //{
    //    fragmentBady f = new fragmentBady();
    //
    //    // Supply index input as an argument.
    //    Bundle args = new Bundle();
    //    args.putInt("index", index);
    //    f.setArguments(args);
    //
    //    return f;
    //}

    private final static String TAG = fragmentBady.class.getSimpleName();
    public TextView    BdtvYearDate, BdtvTemprature, BdtvTRUnit, BdtvUnitH, BdtvUnitL, BdtvHigh, BdtvLow;
    updateTempListener mCallback;
    private View        mViewRoot;
    private ImageView   imgSmile, imgAlarm, imgBT, imgBattery;
    private int         itemId ;
    //Dialog              dialog ;

    int[] limitValue = new int[]{33, 38, 37, 42};

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        //return super.onCreateView(inflater, container, savedInstanceState);
        Log.d(TAG, "fragmentBady onCreateView()");
        mViewRoot = inflater.inflate(R.layout.fragment_bady, container, false);
        initView(mViewRoot);
        //int resId = getArguments().getInt("resId");

        BdtvHigh.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                itemId = v.getId();
                //dialog.setTitle("High Limit Set");
                show(mViewRoot);
            }
        });

        BdtvLow.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                itemId = v.getId();
                //dialog.setTitle("Low Limit Set");
                show(mViewRoot);
            }
        });


        return mViewRoot;
    }

    //public int getShownIndex()
    //{
    //    return getArguments().getInt("index", 0);
    //}

    public void initView(View root)
    {
        imgSmile = (ImageView) root.findViewById(R.id.ivSmile);
        BdtvTemprature = (TextView) root.findViewById(R.id.tvTemprature);
        BdtvYearDate = (TextView) root.findViewById(R.id.tvYear);
        BdtvTRUnit = (TextView) root.findViewById(R.id.tvTRUnit);

        BdtvHigh = (TextView) root.findViewById(R.id.tvTPLow);
        BdtvUnitH = (TextView) root.findViewById(R.id.tvUnitH);
        imgAlarm = (ImageView) root.findViewById(R.id.ivAlarm);
        BdtvLow = (TextView) root.findViewById(R.id.tvTPHigh);
        BdtvUnitL = (TextView) root.findViewById(R.id.tvUnitL);

        imgBT = (ImageView) root.findViewById(R.id.ivUserProfile);
        imgBattery = (ImageView) root.findViewById(R.id.ivBattery);
        imgBattery.setVisibility(View.INVISIBLE);

        //BdtvTemprature.setText("--.--");
    }

    public void show(View view)
    {
        //final Dialog dialog = new Dialog(MainActivity.this);
        final Dialog dialog = new Dialog(view.getContext());
        //dialog = new Dialog(view.getContext());
        Button setBTN, cancelBTN;
        int valueH=42, valueL=36;

        switch (itemId)
        {
            case R.id.tvTPLow:
                dialog.setTitle("Low Limit setting");
                valueL = limitValue[0];
                valueH = limitValue[1];
                break;

            case R.id.tvTPHigh:
                dialog.setTitle("High Limit setting");
                valueL = limitValue[2];
                valueH = limitValue[3];
                break;
        }

        dialog.setContentView(R.layout.dialog_numberpicker);

        setBTN = (Button) dialog.findViewById(R.id.btn_Set);
        cancelBTN = (Button) dialog.findViewById(R.id.btn_Cancel);

        final NumberPicker intPicker = (NumberPicker) dialog.findViewById(R.id.numberPicker);
        intPicker.setMinValue(valueL);
        intPicker.setMaxValue(valueH);
        intPicker.setWrapSelectorWheel(true);
        intPicker.setOnValueChangedListener(this);

        final NumberPicker floatPicker = (NumberPicker) dialog.findViewById(R.id.numberPicker2);
        floatPicker.setMinValue(0);
        floatPicker.setMaxValue(9);
        floatPicker.setWrapSelectorWheel(true);
        floatPicker.setOnValueChangedListener(this);

        setBTN.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                Log.e(TAG, "view getId:" + v.getId() + ", R.id.tvTPHigh: " + R.id.tvTPLow +
                            ", R.id.tvTPLow: " + R.id.tvTPHigh);

                String newText = String.valueOf(intPicker.getValue()) + "." +
                        String.valueOf(floatPicker.getValue());

                switch (itemId)
                {
                    case R.id.tvTPLow:
                        BdtvHigh.setText(newText);
                        Log.e(TAG, "BdtvHigh.text : " + BdtvHigh.getText());
                        break;

                    case R.id.tvTPHigh:
                        BdtvLow.setText(newText);
                        Log.e(TAG, "BdtvLow.text : " + BdtvLow.getText());
                        break;

                    default:
                        BdtvHigh.setText(newText);
                        BdtvLow.setText(newText);
                        break;
                }
                    dialog.dismiss();
            }
        });

        cancelBTN.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                dialog.dismiss();
            }
        });

        dialog.show();
    }


    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "onCreate()");
    }

    @Override
    public void onStart()
    {
        super.onStart();
    }

    @Override
    public void onDestroy()
    {
        super.onDestroy();
    }

    @Override
    public void onPageIn()
    {
        super.onPageIn();
    }

    @Override
    public void onPageOut()
    {
        super.onPageOut();
    }

    @Override
    public void onValueChange(NumberPicker picker, int oldVal, int newVal)
    {

    }

    public interface updateTempListener
    {
        public void updateTemp(String tmp);
    }


}
