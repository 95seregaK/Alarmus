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
    private int backgroundChecked, backgroundUnchecked, colorChecked, colorUnchecked;

    public CircleCheckBox(Context context) {
        super(context);
        init();
    }

    public CircleCheckBox(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        // TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.CircleCheckBox, 0);

        // String str = a.getString(R.styleable.CircleCheckBox_source);
        //do something with str
        //   a.recycle();

        init();
    }

    public CircleCheckBox(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    public void init() {
        setOnClickListener(this);
        setGravity(Gravity.CENTER);
        backgroundChecked = R.drawable.circle_check_checked;
        backgroundUnchecked = R.drawable.circle_check_unchecked;
        colorChecked = R.color.color_text_circle_check_checked;
        colorUnchecked = R.color.color_text_circle_check_unchecked;
        setChecked(false);
    }

    public boolean isChecked() {
        return checked;
    }

    public void setChecked(boolean b) {
        if (b) {
            setBackground(getResources().getDrawable(backgroundChecked));
            setTextColor(getResources().getColor(colorChecked));
        } else {
            setBackground(getResources().getDrawable(backgroundUnchecked));
            setTextColor(getResources().getColor(colorUnchecked));
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

    public void setBackgroundChecked(int id) {
        backgroundChecked = id;
    }

    public void setBackgroundUnchecked(int id) {
        backgroundUnchecked = id;
    }

    public interface OnCheckedChangeListener {
        public void onCheckedChanged(CircleCheckBox view, boolean checked);
    }
}
