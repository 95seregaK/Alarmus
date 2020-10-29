package com.siarhei.alarmus.views;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.util.AttributeSet;
import android.widget.Checkable;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;

import com.siarhei.alarmus.R;

public class ImageRadioButton extends LinearLayout implements Checkable {
    private boolean checked;
    private ImageRadioButton.OnCheckedChangeListener onCheckedChangeListener;
    private ImageView imageView;
    private LinearLayout textLayout;
    private TextView mainText;
    private TextView subText;
    private int imageChecked, imageUnchecked, colorChecked, colorUnchecked;

    public ImageRadioButton(Context context) {
        super(context);
    }

    public ImageRadioButton(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs, -1, 0);
    }

    public ImageRadioButton(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs, defStyleAttr, 0);
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public ImageRadioButton(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init(context, attrs, defStyleAttr, defStyleRes);
    }

    public void init(Context context, AttributeSet attributeSet, int defStyle, int defStyleRes) {

        setOrientation(HORIZONTAL);
        textLayout = new LinearLayout(context);
        textLayout.setOrientation(VERTICAL);
        mainText = new TextView(context);
        subText = new TextView(context);
        imageView = new ImageView(context);

        textLayout.addView(mainText);
        textLayout.addView(subText);

        colorChecked = R.color.color_text_image_radio_checked;
        colorUnchecked = R.color.color_text_image_radio_unchecked;
        setOnClickListener((v) -> toggle());

        Typeface face = Typeface.create("sans-serif-light", Typeface.NORMAL);
        mainText.setTypeface(face);
        subText.setTypeface(face);
        mainText.setTextSize(getResources().getDimension(R.dimen.size_text_image_radio));
        TypedArray a = null;
        if (defStyle != -1)
            a = getContext().obtainStyledAttributes(attributeSet, R.styleable.ImageRadioButton, defStyle, defStyleRes);
        else
            a = getContext().obtainStyledAttributes(attributeSet, R.styleable.ImageRadioButton);
        try {
            Drawable dr = a.getDrawable(R.styleable.ImageRadioButton_button);
            if (dr != null) {
                imageView.setImageDrawable(dr);
            }
        } finally {
            a.recycle();
        }
        imageView.setScaleType(ImageView.ScaleType.FIT_XY);
        imageView.setAdjustViewBounds(true);
        addView(imageView);
        addView(textLayout);
        setBackground(false);

    }

    public void setImage(int id) {
        imageView.setImageDrawable(getResources().getDrawable(id));
    }

    public void setTextColor(int id) {
        colorChecked = id;
        mainText.setTextColor(colorChecked);
        subText.setTextColor(colorChecked);
    }

    @Override
    public boolean isChecked() {
        return checked;
    }

    @Override
    public void toggle() {
        if (!checked) setChecked(true);
    }

    private void setBackground(boolean b) {
        if (b) {
            imageView.setAlpha(1f);
            imageView.setColorFilter(0);
            mainText.setTextColor(getResources().getColor(colorChecked));
            subText.setTextColor(getResources().getColor(colorChecked));
        } else {
            imageView.setAlpha(0.4f);
            imageView.setColorFilter(getResources().getColor(R.color.color_filter_disable));
            mainText.setTextColor(getResources().getColor(colorUnchecked));
            subText.setTextColor(getResources().getColor(colorUnchecked));
        }
    }

    @Override
    public void setChecked(boolean b) {
        if (b && !checked) {
            setBackground(true);
            checked = true;
            if (getParent() instanceof ImageRadioGroup) {
                ((ImageRadioGroup) getParent()).onChildChecked(getId());
            }
        } else if (!b && checked) {
            setBackground(false);
            this.checked = false;
        }
    }

    public void setText(CharSequence text) {
        mainText.setText(text);
    }

    public void setSubText(CharSequence text) {
        subText.setText(text);
    }

    public OnCheckedChangeListener getOnCheckedChangeListener() {
        return onCheckedChangeListener;
    }

    public void setOnCheckedChangeListener(OnCheckedChangeListener onCheckedChangeListener) {
        this.onCheckedChangeListener = onCheckedChangeListener;
    }

    public interface OnCheckedChangeListener {
        public void onCheckedChange(ImageRadioButton view, boolean checked);
    }


}
