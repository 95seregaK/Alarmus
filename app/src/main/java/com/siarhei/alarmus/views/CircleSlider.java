package com.siarhei.alarmus.views;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;

import androidx.annotation.Nullable;

import com.siarhei.alarmus.R;

public class CircleSlider extends androidx.appcompat.widget.AppCompatImageView implements View.OnTouchListener {

    public static final int ACTION_SUCCESS = 1;
    public static final int ACTION_FAILURE = 2;
    private final int ANIMATION_DURATION = 100;
    private int sliderRadius, radius = 0;
    private int padding;
    private OnSliderMoveListener moveListener;
    private float x, y, cx, cy, dx, dy;
    private boolean motion, first = true;
    private RectF circleRect, backgroundRect;
    private Paint circlePaint, backgroundPaint;
    private Drawable image;

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

   /* @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public CircleSlider(Context context, @Nullable AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init(context);
    }*/

    //public void setRadius(int r) { sliderRadius = r; }

    public void init(Context context) {
        //setBackgroundColor(getResources().getColor(R.color.colorTransparent));
        setOnTouchListener(this);
        sliderRadius = (int) getResources().getDimension(R.dimen.slider_radius);
        padding = (int) getResources().getDimension(R.dimen.slider_padding);

        setImageResource(R.drawable.snooze_clock);
        //image = getResources().getDrawable(R.drawable.snooze_clock);
        //setScaleType(ScaleType.FIT_XY);
        circlePaint = new Paint();
        backgroundPaint = new Paint();
        circlePaint.setColor(Color.WHITE);
        circlePaint.setStyle(Paint.Style.FILL);
        backgroundPaint.setColor(Color.WHITE);
        backgroundPaint.setStyle(Paint.Style.STROKE);
        backgroundPaint.setStrokeWidth(4);
        circleRect = new RectF();
        backgroundRect = new RectF();
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
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                x = event.getX();
                y = event.getY();
                dx = cx - x;
                dy = cy - y;
                if (dx * dx + dy * dy < sliderRadius * sliderRadius) motion = true;
                break;

            case MotionEvent.ACTION_MOVE:
                if (motion) {
                    x = event.getX() + dx;
                    y = event.getY() + dy;
                    circleRect.offsetTo(x - sliderRadius, y - sliderRadius);
                    if (distance2(x, y, cx, cy) > radius * radius) {
                        motion = false;
                        double direction = (Math.atan2(y - cy, x - cx) * 180 / Math.PI + 450) % 360;
                        if (moveListener != null) {
                            moveListener.onSliderMoved(ACTION_SUCCESS, (float) direction);
                        }
                    }
                    invalidate();
                }
                break;
            case MotionEvent.ACTION_UP:
                motion = false;
                circleRect.offsetTo(cx - sliderRadius, cy - sliderRadius);
                invalidate();
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
            cx = getWidth() / 2;
            cy = getHeight() / 2;
            circleRect.set(cx - sliderRadius, cy - sliderRadius,
                    cx + sliderRadius, cy + sliderRadius);
            radius = Math.min(getWidth(), getHeight()) / 2 - padding;
            backgroundRect.set(cx - radius, cy - radius,
                    cx + radius, cy + radius);
            first = false;

            Log.d("onDraw", circleRect.left + ", " + circleRect.right
                    + ", " + circleRect.top + ", " + circleRect.bottom + ", " + sliderRadius);
        }
        canvas.drawOval(circleRect, circlePaint);
        //canvas.drawOval(backgroundRect, backgroundPaint);
    }

    public int getRadius() {
        return sliderRadius;
    }

    public interface OnSliderMoveListener {
        void onSliderMoved(int action, float direction);
    }
}
