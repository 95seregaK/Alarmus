package com.siarhei.alarmus.views;

import android.content.Context;
import android.content.res.Resources;
import android.os.Build;
import android.util.AttributeSet;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;

import com.siarhei.alarmus.R;

public class FloatingView extends LinearLayout {

    private static int DISPLAY_HEIGHT = Resources.getSystem().getDisplayMetrics().heightPixels;
    private int parentHeight;
    private int toolbarHeight = (int) getResources().getDimension(R.dimen.info_window_toolbar_height);
    private long floatDuration = 200;
    private boolean shown = false;

    public FloatingView(Context context) {
        super(context);
    }

    public FloatingView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        setView( context);
    }

    public FloatingView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        setView(context);
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public FloatingView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        setView(context);
    }

    private void setView(Context context) {
        int height = (int) getResources().getDimension(R.dimen.info_window_toolbar_height);
        //setLayoutParams(new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        //addView(new TextView(context),LayoutParams.MATCH_PARENT,100);
        //setY(DISPLAY_HEIGHT);
    }

    @Override
    public void onFinishTemporaryDetach() {
        super.onFinishTemporaryDetach();

    }

    ;

    public void emerge() {
        if (!shown) {
            parentHeight = ((ViewGroup) getParent()).getHeight();
            animate().y(parentHeight - getHeight()).x(0).setDuration(floatDuration).start();
            shown = true;
        }
    }

    public void hide() {

        if (shown) {
            parentHeight = ((ViewGroup) getParent()).getHeight();
            animate().y(parentHeight - toolbarHeight)
                    .x(0).setDuration(floatDuration).start();
            shown = false;
        }
    }

    public boolean isHiden() {
        return !shown;
    }
}
