package com.siarhei.alarmus.map;

import android.content.Context;
import android.content.res.Resources;
import android.os.Build;
import android.util.AttributeSet;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;

import com.siarhei.alarmus.R;

public class FloatingLinearLayout extends LinearLayout {

    private static final int DISPLAY_HEIGHT = Resources.getSystem().getDisplayMetrics().heightPixels;
    private static final long FLOAT_DURATION = 250;
    private boolean shown = false;

    public FloatingLinearLayout(Context context) {
        super(context);
        setView(context);
    }

    public FloatingLinearLayout(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        setView(context);
    }

    public FloatingLinearLayout(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        setView(context);
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public FloatingLinearLayout(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        setView(context);
    }

    private void setView(Context context) {
        //addView((LinearLayout) inflate(context, R.layout.info_window, null));
        //addView(new TextView(context),LayoutParams.MATCH_PARENT,100);
        setY(DISPLAY_HEIGHT);
    }

    public void emerge() {
        if (!shown) {
            animate().y(DISPLAY_HEIGHT - getHeight()).x(0).setDuration(FLOAT_DURATION).start();
            shown = true;
        }
    }

    public void hide() {

        if (shown) {
            animate().y(DISPLAY_HEIGHT).x(0).setDuration(FLOAT_DURATION).start();
            shown = false;
        }
    }
}
