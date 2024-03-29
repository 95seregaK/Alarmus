package com.siarhei.alarmus.views;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.Switch;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.siarhei.alarmus.R;
import com.siarhei.alarmus.activities.AlarmActivity;
import com.siarhei.alarmus.data.Alarm;
import com.siarhei.alarmus.data.SunAlarm;
import com.siarhei.alarmus.sun.SunInfo;

import java.util.List;

public class AlarmRecyclerAdapter extends RecyclerView.Adapter<AlarmRecyclerAdapter.ViewHolder> {
    private List<Alarm> alarms;
    private OnCheckedChangeListener onCheckedChangeListener;
    private Context context;
    private int colorAlarmDisable, colorAlarmEnable, colorDaysEnable, colorDaysDisable;

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        context = parent.getContext();
        colorAlarmEnable = context.getResources().getColor(R.color.color_text_default);
        colorDaysEnable = context.getResources().getColor(R.color.color_days_default);
        colorDaysDisable = context.getResources().getColor(R.color.color_days_disable);
        colorAlarmDisable = context.getResources().getColor(R.color.color_text_inactive);
        final View v = LayoutInflater.from(context).inflate(R.layout.alarm_row, parent, false);
        return new ViewHolder(v);
    }


    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        final Alarm alarm = alarms.get(position);
        holder.time.setText(alarm.toTime());
        holder.date.setText(alarm.toDate());
        holder.label.setText(alarm.getLabel() + "");
        for (int i = 0; i < 7; i++) {
            holder.days[i].setText(null);
        }
        if (!alarm.isRepeat()) holder.days[0].setText(R.string.once);

        else {
            boolean everyday = true;
            for (int i = 0; i < 7; i++) {
                everyday = everyday && alarm.getDays()[i];
            }
            if (everyday) holder.days[0].setText(R.string.every_day);
            else {
                String days = "";
                for (int i = 0; i < 7; i++) {
                    holder.days[i].setText(Alarm.DAYS_SHORT[i]);
                }
            }
        }
        holder.enabled.setChecked(alarm.isEnabled());
        holder.enabled.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onCheckedChangeListener.onCheckedChange((CompoundButton) v,
                        position, ((CompoundButton) v).isChecked());
            }
        });

      /*  holder.enabled.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                onCheckedChangeListener.onCheckedChange(buttonView, position, buttonView.isChecked());
            }
        });*/
        if (alarm instanceof SunAlarm) {
            SunAlarm sunAlarm = (SunAlarm) alarm;
            if (sunAlarm.getCity() != null && sunAlarm.getCity().length() > 1)
                holder.location.setText(sunAlarm.getCity());
            else
                holder.location.setText(SunInfo.toLocationString(sunAlarm.getLatitude(), sunAlarm.getLongitude(), 3));

            if (sunAlarm.getSunMode() == SunAlarm.MODE_SUNRISE)
                holder.sunMode.setImageResource(R.drawable.ic_sunrise);
            else if (sunAlarm.getSunMode() == SunAlarm.MODE_NOON)
                holder.sunMode.setImageResource(R.drawable.ic_noon);
            else if (sunAlarm.getSunMode() == SunAlarm.MODE_SUNSET)
                holder.sunMode.setImageResource(R.drawable.ic_sunset);
            int d = sunAlarm.getDelay();
            holder.delay.setText((d < 0 ? "-" : "+") + AlarmActivity.delayToString(d,false));
        } else {
            holder.sunMode.setImageDrawable(null);
            holder.location.setText("");
            holder.delay.setText("");
        }
        if (alarm.isEnabled()) {
            //holder.layout.setBackgroundColor(context.getResources().getColor(R.color.colorPrimary));
            holder.time.setTextColor(colorAlarmEnable);
            holder.date.setTextColor(colorAlarmEnable);
            holder.label.setTextColor(colorAlarmEnable);
            holder.sunMode.setImageAlpha(context.getResources().getInteger(R.integer.enable_alpha));
            holder.sunMode.setColorFilter(0);
            holder.location.setTextColor(colorAlarmEnable);
            holder.delay.setTextColor(colorAlarmEnable);
            for (int i = 0; i < 7; i++) {
                if (alarm.getDays()[i]) holder.days[i].setTextColor(colorDaysEnable);
                else holder.days[i].setTextColor(colorDaysDisable);
            }
        } else {
            //holder.layout.setBackgroundColor(context.getResources().getColor(R.color.color_text_inactive));
            holder.time.setTextColor(colorAlarmDisable);
            holder.date.setTextColor(colorAlarmDisable);
            holder.label.setTextColor(colorAlarmDisable);
            for (int i = 0; i < 7; i++) {
                if (alarm.getDays()[i]) holder.days[i].setTextColor(colorAlarmDisable);
                else holder.days[i].setTextColor(colorDaysDisable);
            }
            holder.sunMode.setImageAlpha(context.getResources().getInteger(R.integer.disable_alpha));
            holder.sunMode.setColorFilter(colorAlarmDisable);
            holder.location.setTextColor(colorAlarmDisable);
            holder.delay.setTextColor(colorAlarmDisable);
        }
    }

    public void setOnCheckedListener(OnCheckedChangeListener onCheckedChangeListener) {
        this.onCheckedChangeListener = onCheckedChangeListener;
    }

    @Override
    public int getItemCount() {
        return alarms == null ? 0 : alarms.size();
    }

    public void setAlarms(List<Alarm> alarms) {
        this.alarms = alarms;
        notifyDataSetChanged();
    }

    public interface OnCheckedChangeListener {
        void onCheckedChange(CompoundButton buttonView, int position, boolean isChecked);
    }

    static final class ViewHolder extends RecyclerView.ViewHolder {

        final TextView time, date, label, location, delay;
        final TextView days[];
        final Switch enabled;
        final ImageView sunMode;
        final View layout;

        ViewHolder(View itemView) {
            super(itemView);
            layout = itemView;
            time = itemView.findViewById(R.id.row_time);
            date = itemView.findViewById(R.id.row_date);
            label = itemView.findViewById(R.id.row_label);
            enabled = itemView.findViewById(R.id.row_switch);
            sunMode = itemView.findViewById(R.id.row_sun_mode);
            location = itemView.findViewById(R.id.row_location);
            delay = itemView.findViewById(R.id.row_delay);
            days = new TextView[]{itemView.findViewById(R.id.row_day1),
                    itemView.findViewById(R.id.row_day2),
                    itemView.findViewById(R.id.row_day3),
                    itemView.findViewById(R.id.row_day4),
                    itemView.findViewById(R.id.row_day5),
                    itemView.findViewById(R.id.row_day6),
                    itemView.findViewById(R.id.row_day7)};

        }
    }
}
