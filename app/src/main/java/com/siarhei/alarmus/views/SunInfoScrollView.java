package com.siarhei.alarmus.views;

import android.content.Context;
import android.graphics.ColorSpace;
import android.os.Build;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
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

    public SunInfoScrollView(Context context) {
        super(context);
        init(context);
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
        container = new LinearLayout(context);
        container.setOrientation(LinearLayout.HORIZONTAL);
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        setHorizontalScrollBarEnabled(false);

        for (int i = 0; i < 10; i++) {
            View column = inflater.inflate(R.layout.sun_info_column, null);
            container.addView(column);
        }
        addView(container);
        updateView();
    }

    private void updateView() {
        if (sunInfo != null) {
            SunInfo info = sunInfo;
            for (int i = 0; i < container.getChildCount(); i++) {
                View column = container.getChildAt(i);

                TextView textDay = column.findViewById(R.id.text_day);
                TextView textSunrise = column.findViewById(R.id.text_sunrise);
                TextView textSunset = column.findViewById(R.id.text_sunset);
                TextView textNoon = column.findViewById(R.id.text_noon);
                TextView textDayDuration = column.findViewById(R.id.text_day_duration);
                textDay.setText(info.getDateString());
                textSunrise.setText(SunInfo.timeToString(info.getSunriseLocalTime(), SunInfo.HH_MM));
                textNoon.setText(SunInfo.timeToString(info.getNoonLocalTime(), SunInfo.HH_MM));
                textSunset.setText(SunInfo.timeToString(info.getSunsetLocalTime(), SunInfo.HH_MM));
                textDayDuration.setText(SunInfo.timeToString(info.getDayDuration(), SunInfo.HH_MM));
                if (!SunInfo.afterNow(info, SunInfo.SUNRISE_MODE)) {
                    textSunrise.setTextColor(getResources().getColor(R.color.info_color_passive));
                }
                if (!SunInfo.afterNow(info, SunInfo.NOON_MODE)) {
                    textNoon.setTextColor(getResources().getColor(R.color.info_color_passive));
                }
                if (!SunInfo.afterNow(info, SunInfo.SUNSET_MODE)) {
                    textSunset.setTextColor(getResources().getColor(R.color.info_color_passive));
                }
                info = SunInfo.nextDaySunInfo(info, 1);
            }
        }
    }

    public void setSunInfo(SunInfo sunInfo) {
        this.sunInfo = sunInfo;
        updateView();
    }

    public LinearLayout getContainer() {
        return container;
    }
}
