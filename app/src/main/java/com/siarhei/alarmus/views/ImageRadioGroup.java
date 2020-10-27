package com.siarhei.alarmus.views;

import android.content.Context;
import android.util.AttributeSet;
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

    public void onCheckedChange(int id) {
        if (onCheckedChangeListener != null) onCheckedChangeListener.onCheckedChange(id);
    }


    public interface OnCheckedChangeListener {
        public void onCheckedChange(int id);
    }
}



