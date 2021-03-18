package com.siarhei.alarmus.views;

import android.content.Context;
import android.content.res.Resources;
import android.os.Build;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;

import com.siarhei.alarmus.R;

public class FloatingView extends LinearLayout {

    private static int DISPLAY_HEIGHT = Resources.getSystem().getDisplayMetrics().heightPixels;
    private final int toolbarHeight = (int) getResources().getDimension(R.dimen.info_window_toolbar_height);
    private final long floatDuration = 200;
    private int parentHeight;
    private boolean shown = true, motion = false;
    private float y, dy;

    public FloatingView(Context context) {
        super(context);
    }

    public FloatingView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        setView(context);
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
        setOnTouchListener(this::onTouch);
        //setLayoutParams(new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        //addView(new TextView(context),LayoutParams.MATCH_PARENT,100);
        //setY(DISPLAY_HEIGHT);
    }

    public void emerge() {
        parentHeight = ((ViewGroup) getParent()).getHeight();
        animate().y(parentHeight - getHeight()).x(0).setDuration(floatDuration).start();
        shown = true;
    }

    public void hide() {
        parentHeight = ((ViewGroup) getParent()).getHeight();
        animate().y(parentHeight - toolbarHeight)
                .x(0).setDuration(floatDuration).start();
        shown = false;
    }

    public boolean isHidden() {
        return !shown;
    }

    public boolean onTouch(View v, MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                y = event.getRawY();
                dy = getY() - y;
                parentHeight = ((ViewGroup) getParent()).getHeight();
                break;
            case MotionEvent.ACTION_MOVE:
                motion = true;
                y = event.getRawY() + dy;
                if (y <= parentHeight - toolbarHeight && y >= parentHeight - getHeight())
                    setY(y);
                else dy = getY() - event.getRawY();
                break;
            case MotionEvent.ACTION_UP:
                y = event.getRawY() + dy;
                if (!motion && event.getY() > 0 && event.getY() < toolbarHeight) {
                    if (shown) hide();
                    else emerge();
                } else {
                    if (parentHeight - y > getHeight() / 2) emerge();
                    else hide();
                }
                motion = false;
                break;
            default:
                return false;
        }
        return true;
    }
}
