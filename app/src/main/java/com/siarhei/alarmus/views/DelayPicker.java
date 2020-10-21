package com.siarhei.alarmus.views;

import android.content.Context;
import android.util.AttributeSet;

import com.wefika.horizontalpicker.HorizontalPicker;

public class DelayPicker extends HorizontalPicker {
    private int maxDelay = 120;

    public DelayPicker(Context context) {
        super(context);
        init();
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
        String str[] = new String[maxDelay * 2 + 1];
        for (int i = -maxDelay; i <= maxDelay; i++) {
            str[i + maxDelay] = (i > 0 ? "+" : "") + i;
        }
        setValues(str);
        setSideItems(1);
        setSelectedItem(maxDelay);
    }
}
