package com.siarhei.alarmus.views;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroupOverlay;
import android.widget.RelativeLayout;

import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;

import com.siarhei.alarmus.R;

public class CircleSlider extends View implements View.OnTouchListener {

    public static final int ACTION_SUCCESS = 1;
    public static final int ACTION_FAILURE = 2;
    private int ANIMATION_DURATION = 100;
    private int sliderRadius = 100, radius = 0;
    private int padding = 60;
    private OnSliderMoveListener moveListener;
    private int cx, cy;
    private float x, y;
    private boolean motion, first = true;
    private RectF circleRect, backgroundRect;
    private Rect invRect;
    private Paint circlePaint, backgroundPaint;

    public CircleSlider(Context context) {
        super(context);
        init(context);
    }

    public CircleSlider(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public CircleSlider(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public CircleSlider(Context context, @Nullable AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init(context);
    }

    //public void setRadius(int r) { sliderRadius = r; }

    public void init(Context context) {
        setBackgroundColor(getResources().getColor(R.color.colorTransparent));
        setOnTouchListener(this);
        circlePaint = new Paint();
        backgroundPaint = new Paint();
        circlePaint.setColor(Color.WHITE);
        circlePaint.setStyle(Paint.Style.FILL);
        backgroundPaint.setColor(Color.WHITE);
        backgroundPaint.setStyle(Paint.Style.STROKE);
        backgroundPaint.setStrokeWidth(4);
        circleRect = new RectF();
        backgroundRect = new RectF();
        invRect = new Rect();
        Log.d("onLayout", getWidth() + ", " + getHeight()
                + ", " + cx + ", " + cy);
    }

    private float distance2(float x, float y, float cx, float cy) {
        return (x - cx) * (x - cx) + (y - cy) * (y - cy);
    }

    public void setOnSliderMoveListener(OnSliderMoveListener listener) {
        moveListener = listener;
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {

        int dx = 0, dy = 0;
        boolean success = false;
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                x = event.getX();
                y = event.getY();
                dx = (int) (cx - x);
                dy = (int) (cy - y);
                if (dx * dx + dy * dy < sliderRadius * sliderRadius) motion = true;
                break;

            case MotionEvent.ACTION_MOVE:
                if (motion) {
                    x = (int) (event.getX() - dx);
                    y = (int) (event.getY() - dy);
                    circleRect.offsetTo(x - sliderRadius, y - sliderRadius);
                    invRect.offset(dx, dy);
                    if (distance2(x, y, cx, cy) > radius * radius) {
                        motion = false;
                        double direction = Math.atan2(y - cy, x - cx);
                        if (moveListener != null) {
                            moveListener.onSliderMoved(ACTION_SUCCESS,
                                    (int) (direction * 180 / Math.PI + 450) % 360);
                        }
                    }
                    invalidate();
                }
                break;
            case MotionEvent.ACTION_UP:
                motion = false;
                circleRect.offsetTo(cx - sliderRadius, cy - sliderRadius);
                if (moveListener != null)
                    moveListener.onSliderMoved(ACTION_FAILURE, 0);
                break;
            default:
                return false;
        }
        return true;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (first) {
            cx = (int) getWidth() / 2;
            cy = (int) getHeight() / 2;
            circleRect.set(cx - sliderRadius, cy - sliderRadius,
                    cx + sliderRadius, cy + sliderRadius);
            radius = Math.min(getWidth(), getHeight()) / 2 - padding;
            backgroundRect.set(cx - radius, cy - radius,
                    cx + radius, cy + radius);
            invRect.set(cx - sliderRadius, cy - sliderRadius,
                    cx + sliderRadius, cy + sliderRadius);
            first = false;
            Log.d("onDraw", circleRect.left + ", " + circleRect.right
                    + ", " + circleRect.top + ", " + circleRect.bottom + ", " + sliderRadius);
        }
        //int padding = getWidth() / 2 - sliderRadius;
        //canvas.drawRect(backgroundRect, backgroundPaint);
        canvas.drawOval(circleRect, circlePaint);
        canvas.drawOval(backgroundRect, backgroundPaint);
    }

    public int getRadius() {
        return sliderRadius;
    }

    public interface OnSliderMoveListener {
        public void onSliderMoved(int action, int direction);
    }
}
