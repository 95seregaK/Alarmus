package com.siarhei.alarmus.views;

import android.content.Context;
import android.content.res.Resources;
import android.os.Build;
import android.util.AttributeSet;
import android.util.Log;
import android.util.TypedValue;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;

import com.siarhei.alarmus.R;

import static androidx.core.content.ContextCompat.getDrawable;

public class FloatingView extends LinearLayout {

    private static int DISPLAY_HEIGHT = Resources.getSystem().getDisplayMetrics().heightPixels;
    private final int toolbarHeight = (int) getResources().getDimension(R.dimen.info_window_toolbar_height);
    private final long floatDuration = 200;
    private int parentHeight;
    private boolean shown = true, motion = false;
    private float y, dy;
    private TextView title;
    private ImageButton hideButton;

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
        LinearLayout toolbar = new LinearLayout(context);
        toolbar.setLayoutParams(new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, toolbarHeight));
        toolbar.setOrientation(HORIZONTAL);
        toolbar.setBackgroundColor(getResources().getColor(R.color.white));
        hideButton = new ImageButton(context);
        hideButton.setLayoutParams(new ViewGroup.LayoutParams(toolbarHeight, ViewGroup.LayoutParams.MATCH_PARENT));
        hideButton.setScaleType(ImageView.ScaleType.CENTER_CROP);
        hideButton.setImageResource(R.drawable.ic_down);
        int padding = (int) getResources().getDimension(R.dimen.padding_hide_button);
        Log.d("padding_hide_button", padding + "");
        hideButton.setPadding(padding, padding, padding, padding);
        TypedValue outValue = new TypedValue();
        context.getTheme().resolveAttribute(
                android.R.attr.selectableItemBackground, outValue, true);
        hideButton.setBackground(getDrawable(context, outValue.resourceId));
        hideButton.setOnClickListener(this::onClick);
        setOnTouchListener(this::onTouch);
        title = new TextView(context);
        title.setLayoutParams(new LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.MATCH_PARENT));
        title.setTextAppearance(context, R.style.TextStyle);
        title.setTextColor(getResources().getColor(R.color.colorPrimaryDark));
        int size = (int) (getResources().getDimension(R.dimen.toolbar_text_size)
                / getResources().getDisplayMetrics().density);
        title.setTextSize(size);
        Log.d("padding_hide_button", size + "");
        toolbar.addView(hideButton);
        toolbar.addView(title);

        addView(toolbar);

    }

    private void onClick(View view) {
        if (shown) hide();
        else emerge();
    }

    public void setTitle(CharSequence text) {
        title.setText(text);
    }

    public void emerge() {
        parentHeight = ((ViewGroup) getParent()).getHeight();
        animate().y(parentHeight - getHeight()).x(0).setDuration(floatDuration).start();
        shown = true;
        hideButton.setImageResource(R.drawable.ic_down);
    }

    public void hide() {
        parentHeight = ((ViewGroup) getParent()).getHeight();
        animate().y(parentHeight - toolbarHeight)
                .x(0).setDuration(floatDuration).start();
        shown = false;
        hideButton.setImageResource(R.drawable.ic_up);
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
                y = event.getRawY() + dy;
                if (y <= parentHeight - toolbarHeight && y >= parentHeight - getHeight())
                    setY(y);
                else dy = getY() - event.getRawY();
                break;
            case MotionEvent.ACTION_UP:
                y = event.getRawY() + dy;
                if (parentHeight - y > getHeight() / 2) emerge();
                else hide();
                break;
            default:
                return false;
        }
        return true;
    }
}
