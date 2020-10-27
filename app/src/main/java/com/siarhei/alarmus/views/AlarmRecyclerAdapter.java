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
import com.siarhei.alarmus.data.Alarm;
import com.siarhei.alarmus.data.SunAlarm;

import java.util.List;

public class AlarmRecyclerAdapter extends RecyclerView.Adapter<AlarmRecyclerAdapter.ViewHolder> {
    private List<Alarm> alarms;
    private OnCheckedChangeListener onCheckedChangeListener;
    private Context context;

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        context = parent.getContext();
        final View v = LayoutInflater.from(context).inflate(R.layout.alarm_row, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        final Alarm alarm = alarms.get(position);
        holder.time.setText(alarm.toTime());
        holder.date.setText(alarm.toDate());
        holder.label.setText(alarm.getId() + "");
        holder.days.setText(alarm.isRepeat() ? R.string.repeat : R.string.once);
        holder.enabled.setChecked(alarm.isEnabled());
        holder.enabled.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                /*if (event.getAction() == MotionEvent.ACTION_BUTTON_RELEASE
                        && onCheckedChangeListener != null) {
                    holder.enabled.setChecked(!holder.enabled.isChecked());
                    onCheckedChangeListener.onCheckedChange((CompoundButton) v,
                            position, ((CompoundButton) v).isChecked());
                }*/
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
            if (sunAlarm.getSunMode() == SunAlarm.MODE_SUNRISE)
                holder.sunMode.setImageResource(R.drawable.ic_sunrise);
            else if (sunAlarm.getSunMode() == SunAlarm.MODE_NOON)
                holder.sunMode.setImageResource(R.drawable.ic_noon);
            else if (sunAlarm.getSunMode() == SunAlarm.MODE_SUNSET)
                holder.sunMode.setImageResource(R.drawable.ic_sunset);

        } else holder.sunMode.setImageDrawable(null);
        if (alarm.isEnabled()) {
            holder.time.setTextColor(context.getResources().getColor(R.color.colorAlarmEnable));
            holder.date.setTextColor(context.getResources().getColor(R.color.colorAlarmEnable));
            holder.label.setTextColor(context.getResources().getColor(R.color.colorAlarmEnable));
            holder.days.setTextColor(context.getResources().getColor(R.color.colorAlarmEnable));
            holder.sunMode.setImageAlpha(context.getResources().getInteger(R.integer.enable_alpha));
            holder.sunMode.setColorFilter(0);
        } else {
            holder.time.setTextColor(context.getResources().getColor(R.color.colorAlarmDisable));
            holder.date.setTextColor(context.getResources().getColor(R.color.colorAlarmDisable));
            holder.label.setTextColor(context.getResources().getColor(R.color.colorAlarmDisable));
            holder.days.setTextColor(context.getResources().getColor(R.color.colorAlarmDisable));
            holder.sunMode.setImageAlpha(context.getResources().getInteger(R.integer.disable_alpha));
            holder.sunMode.setColorFilter(context.getResources().getColor(R.color.color_filter_disable));
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

    static final class ViewHolder extends RecyclerView.ViewHolder {

        final TextView time, date, label, days;
        final Switch enabled;
        final ImageView sunMode;

        ViewHolder(View itemView) {
            super(itemView);
            time = itemView.findViewById(R.id.row_time);
            date = itemView.findViewById(R.id.row_date);
            label = itemView.findViewById(R.id.row_label);
            days = itemView.findViewById(R.id.row_days);
            enabled = itemView.findViewById(R.id.row_switch);
            sunMode = itemView.findViewById(R.id.row_sun_mode);
        }
    }

    public interface OnCheckedChangeListener {
        public void onCheckedChange(CompoundButton buttonView, int position, boolean isChecked);
    }
}
