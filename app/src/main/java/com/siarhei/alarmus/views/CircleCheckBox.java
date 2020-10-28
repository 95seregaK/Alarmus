package com.siarhei.alarmus.views;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Checkable;

import androidx.annotation.Nullable;

import com.siarhei.alarmus.R;

public class CircleCheckBox extends androidx.appcompat.widget.AppCompatTextView
        implements View.OnClickListener, Checkable {
    private boolean checked;
    private OnCheckedChangeListener onCheckedChangeListener;
    private Drawable backgroundChecked, backgroundUnchecked;
    private int colorChecked, colorUnchecked;

    public CircleCheckBox(Context context) {
        super(context);
    }

    public CircleCheckBox(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs, -1);
    }

    public CircleCheckBox(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs, defStyleAttr);
    }

    public void init(Context context, AttributeSet attributeSet, int defStyle) {

        backgroundChecked = getResources().getDrawable(R.drawable.circle_check_checked);
        backgroundUnchecked = getResources().getDrawable(R.drawable.circle_check_unchecked);
        setOnClickListener(this);
        setGravity(Gravity.CENTER);

        colorChecked = R.color.color_text_circle_check_checked;
        colorUnchecked = R.color.color_text_circle_check_unchecked;
        TypedArray a = null;
        if (defStyle != -1)
            a = getContext().obtainStyledAttributes(attributeSet, R.styleable.CircleCheckBox, defStyle, 0);
        else
            a = getContext().obtainStyledAttributes(attributeSet, R.styleable.CircleCheckBox);
        try {
            Drawable dr = a.getDrawable(R.styleable.CircleCheckBox_button_checked);
            if (dr != null) {
                backgroundUnchecked = dr;
            }
            dr = a.getDrawable(R.styleable.CircleCheckBox_button_unchecked);
            if (dr != null)
                backgroundChecked = dr;

        } finally {
            a.recycle();
        }
        setBackground(false);
    }

    private void setBackground(boolean b) {
        if (b) {
            setBackground(backgroundChecked);
            setTextColor(getResources().getColor(colorChecked));
        } else {
            setBackground(backgroundUnchecked);
            setTextColor(getResources().getColor(colorUnchecked));
        }
    }

    public boolean isChecked() {
        return checked;
    }

    @Override
    public void toggle() {
        if (checked && control()) setChecked(false);
        else if (!checked) setChecked(true);
    }

    private boolean control() {
        if (getParent() instanceof ViewGroup) {
            ViewGroup group = (ViewGroup) getParent();
            for (int i = 0; i < group.getChildCount(); i++) {
                View child = group.getChildAt(i);
                if (child.getId() != getId() && child instanceof CircleCheckBox && ((CircleCheckBox) child).checked) {
                    return true;
                }
            }
        }
        return false;
    }

    public void setChecked(boolean b) {
        if (b && !checked) {
            setBackground(b);
            this.checked = b;
            invalidate();
            if (onCheckedChangeListener != null) onCheckedChangeListener.onCheckedChanged(this, b);
        } else if (!b && checked) {
            setBackground(b);
            this.checked = b;
            invalidate();
            if (onCheckedChangeListener != null) onCheckedChangeListener.onCheckedChanged(this, b);
        }
    }

    @Override
    public void onClick(View v) {
        toggle();
    }

    public void setOnCheckedChangeListener(OnCheckedChangeListener onCheckedChangeListener) {
        this.onCheckedChangeListener = onCheckedChangeListener;
    }

    public void setBackgroundChecked(int id) {
        backgroundChecked = getResources().getDrawable(id);
    }

    public void setBackgroundUnchecked(int id) {
        backgroundUnchecked = getResources().getDrawable(id);
    }

    public interface OnCheckedChangeListener {
        public void onCheckedChanged(CircleCheckBox view, boolean checked);
    }
}
