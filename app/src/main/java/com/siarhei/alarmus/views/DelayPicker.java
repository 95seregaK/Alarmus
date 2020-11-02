package com.siarhei.alarmus.views;

import android.content.Context;
import android.util.AttributeSet;

public class DelayPicker extends androidx.appcompat.widget.AppCompatSeekBar {
    private int maxDelay = 120;
    private String[] displayedValues;

    public DelayPicker(Context context) {
        super(context);
    }

    public DelayPicker(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public DelayPicker(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init();
    }

    private void init() {
     /*   displayedValues = new String[maxDelay * 2 + 1];
        displayedValues[maxDelay] = "0:00";
        for (int i = 1; i <= maxDelay; i++) {
            int hour = i / 60;
            int minute = i % 60;
            displayedValues[maxDelay - i] = "-" + hour + (minute > 9 ? ":" : ":0") + minute;
            displayedValues[maxDelay + i] = "+" + hour + (minute > 9 ? ":" : ":0") + minute;
        }
        setDisplayedValues(displayedValues);*/
        setMax(maxDelay * 2);
        setValue(0);
    }

    public int getValue() {
        return getProgress() - maxDelay;
    }

    public void setValue(int value) {
        setProgress(value + maxDelay);
        //setDisplayedValues(displayedValues);
    }

}
