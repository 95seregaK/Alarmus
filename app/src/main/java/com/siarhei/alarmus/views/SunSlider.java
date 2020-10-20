package com.siarhei.alarmus.views;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.os.Build;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.widget.RelativeLayout;

import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;

import com.siarhei.alarmus.R;

public class SunSlider extends RelativeLayout {

    public static final int ACTION_SUCCESS = 1;
    public static final int ACTION_FAILURE = 2;
    private int ANIMATION_DURATION = 100;
    private View sliderView;
    private int padding = 60;
    private OnSliderMoveListener moveListener;
    private int radius;

    public SunSlider(Context context) {
        super(context);
        init(context);
    }

    public SunSlider(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public SunSlider(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public SunSlider(Context context, @Nullable AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init(context);
    }

    public void setRadius(int r) {
        radius = r;
    }

    public void init(Context context) {
        //setBackgroundColor(getResources().getColor(R.color.white));
        sliderView = new View(getContext());
        sliderView.setBackgroundResource(R.drawable.ic_alarm);
        LayoutParams layoutParams = new LayoutParams(200, 200);
        sliderView.setLayoutParams(layoutParams);
        addView(sliderView);
        sliderView.setOnTouchListener(new MyOnTouchListener());

    }

    private float distance2(float x, float y, float cx, float cy) {
        return (x - cx) * (x - cx) + (y - cy) * (y - cy);
    }

    public void setOnSliderMoveListener(OnSliderMoveListener listener) {
        moveListener = listener;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        Paint paint = new Paint();
        //paint.setColor(Color.YELLOW);
        paint.setStrokeWidth(3);
        paint.setStyle(Paint.Style.STROKE);
        int padding = getWidth() / 2 - radius;
        RectF rectF = new RectF(padding, getHeight() / 2 - radius, getWidth() - padding,
                getHeight() / 2 + radius);
        canvas.drawOval(rectF, paint);
       /* int cx = getHeight() / 2;
        int cy = getWidth() / 2;
        Paint paintT = new Paint();
        paintT.setColor(Color.BLACK);
        paintT.setTextSize(100);
        canvas.drawText("2", cx + (float) Math.cos(Math.PI / 3),
                cy + (float) Math.sin(Math.PI / 3), paintT);*/
    }

    public int getRadius() {
        return radius;
    }

    class MyOnTouchListener implements View.OnTouchListener {
        float dX = 0;
        float dY = 0;
        float cx = 0;
        float cy = 0;
        float x, y;
        boolean success = false;

        @Override
        public boolean onTouch(View v, MotionEvent event) {

            if (!success) {
                switch (event.getAction()) {

                    case MotionEvent.ACTION_DOWN:
                        cx = v.getX();
                        cy = v.getY();
                        dX = event.getRawX() - cx;
                        dY = event.getRawY() - cy;
                        break;

                    case MotionEvent.ACTION_MOVE:
                        x = event.getRawX() - dX;
                        y = event.getRawY() - dY;
                        v.setX(x);
                        v.setY(y);
                        if (distance2(x, y, cx, cy) > radius * radius) {
                            success = true;
                            double direction = Math.atan2(y - cy, x - cx);
                            v.animate()
                                    .x((float) (cx + 2 * radius * Math.cos(direction)))
                                    .y((float) (cy + 2 * radius * Math.sin(direction)))
                                    .setDuration(ANIMATION_DURATION).start();
                            if (moveListener != null) {
                                moveListener.onMoved(ACTION_SUCCESS,
                                        (int) (direction * 180 / Math.PI + 450) % 360);
                            }
                        }
                        break;
                    case MotionEvent.ACTION_UP:
                        //Log.d("ACTION_UP", dX + " " + dY + " " + cx + " " + cy);
                        v.animate().x(cx).y(cy).setDuration(ANIMATION_DURATION).start();
                        if (moveListener != null)
                            moveListener.onMoved(ACTION_FAILURE, 0);
                        break;
                    default:
                        return false;
                }
            }
            return true;
        }
    }

    public interface OnSliderMoveListener {
        public void onMoved(int action, int direction);
    }
}
