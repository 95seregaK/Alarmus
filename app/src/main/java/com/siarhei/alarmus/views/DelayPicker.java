package com.siarhei.alarmus.views;

import android.content.Context;
import android.util.AttributeSet;

public class DelayPicker extends androidx.appcompat.widget.AppCompatSeekBar {
    private int maxDelay = 120;
    private int zeroOffset = 5;

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
        setMax((maxDelay + zeroOffset) * 2);
        setValue(0);
    }

    public int getValue() {
        if (getProgress() - maxDelay < 0)
            return getProgress() - maxDelay;
        else if (getProgress() - maxDelay > 2 * zeroOffset)
            return getProgress() - 2 * zeroOffset - maxDelay;
        return 0;
    }

    public void setValue(int value) {
        if (value == 0) setProgress(maxDelay + zeroOffset);
        else if (value < 0) setProgress(value + maxDelay);
        else setProgress(value + maxDelay + 2 * zeroOffset);
    }
}
