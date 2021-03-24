package com.siarhei.alarmus.views;

import android.content.Context;
import android.os.Build;
import android.util.AttributeSet;
import android.widget.TimePicker;

import androidx.annotation.RequiresApi;

public class MyTimePicker extends TimePicker {
    public MyTimePicker(Context context) {
        super(context);
    }

    public MyTimePicker(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public MyTimePicker(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public MyTimePicker(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init();
    }

    private void init() {
        setIs24HourView(true);
    }
}
