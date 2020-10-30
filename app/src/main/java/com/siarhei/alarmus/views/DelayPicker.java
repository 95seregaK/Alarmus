package com.siarhei.alarmus.views;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.NumberPicker;

public class DelayPicker extends NumberPicker {
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
        displayedValues = new String[maxDelay * 2 + 1];
        displayedValues[maxDelay] = "0:00";
        for (int i = 1; i <= maxDelay; i++) {
            int hour = i / 60;
            int minute = i % 60;
            displayedValues[maxDelay - i] = "-" + hour + (minute > 9 ? ":" : ":0") + minute;
            displayedValues[maxDelay + i] = "+" + hour + (minute > 9 ? ":" : ":0") + minute;
        }
        setDisplayedValues(displayedValues);
        setMaxValue(maxDelay * 2);
        setSelectedValue(0);
    }

    public int getSelectedValue() {
        return getValue() - maxDelay;
    }

    public void setSelectedValue(int value) {
        setValue(value + maxDelay);
        //setDisplayedValues(displayedValues);
    }

    @Override
    public void setValue(int value) {
        super.setValue(value);
        //setDisplayedValues(displayedValues);
    }
}
