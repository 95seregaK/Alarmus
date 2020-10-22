package com.siarhei.alarmus.views;

import android.content.Context;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.View;

import androidx.annotation.Nullable;

import com.siarhei.alarmus.R;

public class CircleCheckBox extends androidx.appcompat.widget.AppCompatTextView
        implements View.OnClickListener {
    private boolean checked;
    private OnCheckedChangeListener onCheckedChangeListener;

    public CircleCheckBox(Context context) {
        super(context);
        init();
    }

    public CircleCheckBox(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public CircleCheckBox(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    public void init() {
        setOnClickListener(this);
        setChecked(true);
        setGravity(Gravity.CENTER);
    }

    public boolean isChecked() {
        return checked;
    }

    public void setChecked(boolean b) {
        if (b) {
            setBackground(getResources().getDrawable(R.drawable.circle_check_checked));
            setTextColor(getResources().getColor(R.color.color_text_circle_check_checked));
        } else {
            setBackground(getResources().getDrawable(R.drawable.circle_check_unchecked));
            setTextColor(getResources().getColor(R.color.color_text_circle_check_unchecked));
        }
        this.checked = b;
        if (onCheckedChangeListener != null) onCheckedChangeListener.onCheckedChanged(this, b);
    }

    @Override
    public void onClick(View v) {
        if (checked) setChecked(false);
        else setChecked(true);
    }

    public OnCheckedChangeListener getOnCheckedChangeListener() {
        return onCheckedChangeListener;
    }

    public void setOnCheckedChangeListener(OnCheckedChangeListener onCheckedChangeListener) {
        this.onCheckedChangeListener = onCheckedChangeListener;
    }

    public interface OnCheckedChangeListener {
        public void onCheckedChanged(CircleCheckBox view, boolean checked);
    }
}
