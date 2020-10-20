package com.siarhei.alarmus.views;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.MotionEvent;

public class SlideButton extends androidx.appcompat.widget.AppCompatSeekBar {
    private static int MAX_PROGRESS = 100;
    private Drawable thumb;
    private SlideButtonListener listener;

    public SlideButton(Context context, AttributeSet attrs) {
        super(context, attrs);
        setMax(MAX_PROGRESS);
        setProgress(MAX_PROGRESS / 2);
        setClickable(false);

    }

    @Override
    public void setThumb(Drawable thumb) {
        super.setThumb(thumb);
        this.thumb = thumb;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            if (thumb.getBounds().contains((int) event.getX(), (int) event.getY())) {
                super.onTouchEvent(event);
            } else
                return false;
        } else if (event.getAction() == MotionEvent.ACTION_UP) {
            if (getProgress() > 0.8 * MAX_PROGRESS) {
                setProgress(MAX_PROGRESS);
                listener.handleSlide(1);
            } else if (getProgress() < 0.2 * MAX_PROGRESS) {
                setProgress(0);
                listener.handleSlide(-1);
            } else {
                listener.handleSlide(0);
                setProgress(MAX_PROGRESS / 2);
            }
        } else
            super.onTouchEvent(event);

        return true;
    }

    public void setSlideButtonListener(SlideButtonListener listener) {
        this.listener = listener;
    }

    public interface SlideButtonListener {
        public void handleSlide(int res);
    }
}


