package com.siarhei.alarmus.views;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.RadioGroup;

public class ImageRadioGroup extends RadioGroup {
    private OnCheckedChangedListener onCheckedChangedListener;

    public ImageRadioGroup(Context context) {
        super(context);
    }

    public ImageRadioGroup(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public void setOnCheckedChangedListener(OnCheckedChangedListener onCheckedChangedListener) {
        this.onCheckedChangedListener = onCheckedChangedListener;
    }

    public OnCheckedChangedListener getOnCheckedChangedListener() {
        return onCheckedChangedListener;
    }
    public interface OnCheckedChangedListener {
        public void onCheckedChanged(ImageRadioButton view);
    }
}



