package com.siarhei.alarmus.views;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.RadioGroup;

public class ImageRadioGroup extends LinearLayout {
    private OnCheckedChangeListener onCheckedChangeListener;

    public ImageRadioGroup(Context context) {
        super(context);
    }

    public ImageRadioGroup(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public void setOnCheckedChangeListener(OnCheckedChangeListener onCheckedChangeListener) {
        this.onCheckedChangeListener = onCheckedChangeListener;
    }

    public void onChildChecked(int id) {
        for (int i = 0; i < getChildCount(); i++) {
            View child = getChildAt(i);
            if (child instanceof ImageRadioButton && child.getId() != id && ((ImageRadioButton) child).isChecked()) {
                ((ImageRadioButton) child).setChecked(false);
            }
        }
        if (onCheckedChangeListener != null) onCheckedChangeListener.onCheckedChange(id);
    }

    public interface OnCheckedChangeListener {
        public void onCheckedChange(int id);
    }
}