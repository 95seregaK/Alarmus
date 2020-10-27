package com.siarhei.alarmus.views;

import android.content.Context;
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

public class ImageRadioButton extends LinearLayout implements View.OnClickListener {
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
        setOnClickListener(this);
        imageView.setScaleType(ImageView.ScaleType.FIT_START);
    }

    public void setImage(int id) {
        imageChecked = id;
        imageView.setImageDrawable(getResources().getDrawable(id));
        imageView.setLayoutParams(new LinearLayout.LayoutParams(120,
                ViewGroup.LayoutParams.MATCH_PARENT));
    }

    public void setTextColor(int id) {
        colorChecked = id;
        mainText.setTextColor(colorChecked);
        subText.setTextColor(colorChecked);
    }

    public boolean isChecked() {
        return checked;
    }

    public void setChecked(boolean b) {
        if (b) {
            // imageView.setImageDrawable(getResources().getDrawable(imageChecked));
            imageView.setAlpha(1f);
            mainText.setTextColor(getResources().getColor(colorChecked));
            subText.setTextColor(getResources().getColor(colorChecked));
            if (getParent() instanceof ImageRadioGroup) {
                Log.d("RadioGroup", "" + getParent());
                ImageRadioGroup radiogroup = (ImageRadioGroup) getParent();
                for (int i = 0; i < radiogroup.getChildCount(); i++) {
                    View child = radiogroup.getChildAt(i);
                    if (child instanceof ImageRadioButton && child != this) {
                        ((ImageRadioButton) child).setChecked(false);
                    }
                }
                if (radiogroup.getOnCheckedChangedListener() != null)
                    radiogroup.getOnCheckedChangedListener().onCheckedChanged(this);
            }

        } else {
            // imageView.setImageDrawable(getResources().getDrawable(imageChecked));
            imageView.setAlpha(0.4f);
            imageView.setColorFilter(0);
            mainText.setTextColor(getResources().getColor(colorUnchecked));
            subText.setTextColor(getResources().getColor(colorUnchecked));
        }
        this.checked = b;
        if (onCheckedChangeListener != null) onCheckedChangeListener.onCheckedChanged(this, b);

    }

    public void setText(CharSequence text) {
        mainText.setText(text);
    }

    public void setSubText(CharSequence text) {
        subText.setText(text);
    }

    @Override
    public void onClick(View v) {
        if (!checked) setChecked(true);
    }

    public OnCheckedChangeListener getOnCheckedChangeListener() {
        return onCheckedChangeListener;
    }

    public void setOnCheckedChangeListener(OnCheckedChangeListener onCheckedChangeListener) {
        this.onCheckedChangeListener = onCheckedChangeListener;
    }

    public interface OnCheckedChangeListener {
        public void onCheckedChanged(ImageRadioButton view, boolean checked);
    }


}
