package com.siarhei.alarmus.views;

import android.content.Context;
import android.graphics.Color;
import android.graphics.ColorFilter;
import android.graphics.Typeface;
import android.os.Build;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioGroup;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;

import com.siarhei.alarmus.R;

public class ImageRadioButton extends LinearLayout {
    private boolean checked;
    private ImageRadioButton.OnCheckedChangeListener onCheckedChangeListener;
    private ImageView imageView;
    private LinearLayout textLayout;
    private TextView mainText;
    private TextView subText;
    private int imageChecked, imageUnchecked, colorChecked, colorUnchecked;

    public ImageRadioButton(Context context) {
        super(context);
        init(context);
    }

    public ImageRadioButton(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public ImageRadioButton(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public ImageRadioButton(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init(context);
    }

    public void init(Context context) {
        setOrientation(HORIZONTAL);
        textLayout = new LinearLayout(context);
        textLayout.setOrientation(VERTICAL);
        mainText = new TextView(context);
        subText = new TextView(context);
        imageView = new ImageView(context);
        addView(imageView);
        textLayout.addView(mainText);
        textLayout.addView(subText);
        addView(textLayout);
        colorChecked = R.color.color_text_image_radio_checked;
        colorUnchecked = R.color.color_text_image_radio_unchecked;
        setOnClickListener((v) -> {
            ((ImageRadioButton) v).setChecked(true);
        });
        imageView.setScaleType(ImageView.ScaleType.FIT_XY);
        imageView.setAdjustViewBounds(true);
        Typeface face = Typeface.create("sans-serif-light", Typeface.NORMAL);
        mainText.setTypeface(face);
        //subText.setTypeface(face);
        mainText.setTextSize(getResources().getDimension(R.dimen.size_text_image_radio));
        setColors(false);
    }

    public void setImage(int id) {
        imageChecked = id;
        imageView.setImageDrawable(getResources().getDrawable(id));
    }

    public void setTextColor(int id) {
        colorChecked = id;
        mainText.setTextColor(colorChecked);
        subText.setTextColor(colorChecked);
    }

    public boolean isChecked() {
        return checked;
    }

    private void setColors(boolean b) {
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

    public void setChecked(boolean b) {
        if (b && !checked) {
            ImageRadioGroup radiogroup = null;
            if (getParent() instanceof ImageRadioGroup) {
                radiogroup = (ImageRadioGroup) getParent();
                for (int i = 0; i < radiogroup.getChildCount(); i++) {
                    View child = radiogroup.getChildAt(i);
                    if (child instanceof ImageRadioButton && ((ImageRadioButton) child).checked) {
                        ((ImageRadioButton) child).checked = false;
                        ((ImageRadioButton) child).setColors(false);
                    }
                }
            }
            setColors(true);
            checked = true;
            if (radiogroup != null)
                radiogroup.onCheckedChange(this.getId());
        } else if (!b && checked) {
            setColors(false);
            this.checked = false;
            //if (onCheckedChangeListener != null) onCheckedChangeListener.onCheckedChange(this, b);
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
