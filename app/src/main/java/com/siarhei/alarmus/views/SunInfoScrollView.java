package com.siarhei.alarmus.views;

import android.content.Context;
import android.graphics.Canvas;
import android.os.Build;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.HorizontalScrollView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;

import com.siarhei.alarmus.R;
import com.siarhei.alarmus.sun.SunInfo;

public class SunInfoScrollView extends HorizontalScrollView {
    private SunInfo sunInfo;
    private LinearLayout container;
    private int itemCount = 40;
    private int centralItem = 10;
    private float columnWidth;
    private boolean first = true;
    private int colorActive, colorInactive;

    public SunInfoScrollView(Context context) {
        super(context);
    }

    public SunInfoScrollView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public SunInfoScrollView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public SunInfoScrollView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init(context);
    }

    public void init(Context context) {
        colorActive = getResources().getColor(R.color.color_info_active);
        colorInactive = getResources().getColor(R.color.color_info_inactive);
        container = new LinearLayout(context);
        container.setOrientation(LinearLayout.HORIZONTAL);
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        setHorizontalScrollBarEnabled(false);
        for (int i = 0; i < itemCount; i++) {
            View column = inflater.inflate(R.layout.sun_info_column, null);
            setParams(column);
            container.addView(column);
        }
        addView(container);
    }

    public void setSelectedItem(int i) {
        smoothScrollTo((int) (columnWidth * i), 0);
    }

    private void updateView() {
        if (sunInfo != null) {
            SunInfo info = sunInfo;
            int n = container.getChildCount();
            updateColumn(centralItem, info);
            for (int i = centralItem + 1; i < n; i++) {
                info = SunInfo.nextDaySunInfo(info, 1);
                updateColumn(i, info);
            }
            info = sunInfo;
            for (int i = centralItem - 1; i >= 0; i--) {
                info = SunInfo.nextDaySunInfo(info, -1);
                updateColumn(i, info);
            }
        }
    }

    private void setParams(View column) {
        TextView textDay = column.findViewById(R.id.text_day);
        TextView textSunrise = column.findViewById(R.id.text_sunrise);
        TextView textSunset = column.findViewById(R.id.text_sunset);
        TextView textNoon = column.findViewById(R.id.text_noon);
        TextView textDayDuration = column.findViewById(R.id.text_day_duration);
        textDay.setTextAlignment(TEXT_ALIGNMENT_VIEW_END);
        textSunrise.setTextAlignment(TEXT_ALIGNMENT_VIEW_END);
        textSunset.setTextAlignment(TEXT_ALIGNMENT_VIEW_END);
        textNoon.setTextAlignment(TEXT_ALIGNMENT_VIEW_END);
        textDayDuration.setTextAlignment(TEXT_ALIGNMENT_VIEW_END);
    }

    private void updateColumn(int i, SunInfo info) {
        View column = container.getChildAt(i);
        TextView textDay = column.findViewById(R.id.text_day);
        TextView textSunrise = column.findViewById(R.id.text_sunrise);
        TextView textSunset = column.findViewById(R.id.text_sunset);
        TextView textNoon = column.findViewById(R.id.text_noon);
        TextView textDayDuration = column.findViewById(R.id.text_day_duration);
        textDay.setText(info.toDateString());
        textSunrise.setText(SunInfo.toTimeString(info.getSunriseLocalTime(), SunInfo.HH_MM));
        textNoon.setText(SunInfo.toTimeString(info.getNoonLocalTime(), SunInfo.HH_MM));
        textSunset.setText(SunInfo.toTimeString(info.getSunsetLocalTime(), SunInfo.HH_MM));
        textDayDuration.setText(SunInfo.toTimeString(info.getDayDuration(), SunInfo.HH_MM));

        if (!SunInfo.afterNow(info, SunInfo.SUNRISE_MODE))
            textSunrise.setTextColor(colorInactive);
        else textSunrise.setTextColor(colorActive);
        if (!SunInfo.afterNow(info, SunInfo.NOON_MODE))
            textNoon.setTextColor(colorInactive);
        else textNoon.setTextColor(colorActive);
        if (!SunInfo.afterNow(info, SunInfo.SUNSET_MODE))
            textSunset.setTextColor(colorInactive);
        else textSunset.setTextColor(colorActive);
    }

    @Override
    public void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (first) {
            columnWidth = container.getChildAt(0).getMeasuredWidth();
            setLayoutParams(new LinearLayout.LayoutParams((int) columnWidth * 3, ViewGroup.LayoutParams.WRAP_CONTENT));
            goToCentre();
            first = false;
        }
    }

    public void setSunInfo(SunInfo info) {
        sunInfo = info;
        updateView();

    }

    public void goToCentre() {
        setSelectedItem(centralItem);
    }

    public LinearLayout getContainer() {
        return container;
    }
}
