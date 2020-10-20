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
import com.siarhei.alarmus.data.AlarmData;

import java.util.List;

public class AlarmRecyclerAdapter extends RecyclerView.Adapter<AlarmRecyclerAdapter.ViewHolder> {
    private List<AlarmData> alarms;
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
        final AlarmData alarm = alarms.get(position);
        holder.time.setText(alarm.toString());
        holder.label.setText(alarm.getId() + "");
        holder.days.setText(alarm.isOnce() ? R.string.once : R.string.every_day);
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
        holder.sunMode.setImageResource(R.drawable.ic_sunset);

        if (alarm.isEnabled()) {
            holder.time.setTextColor(context.getResources().getColor(R.color.colorAlarmEnable));
            holder.label.setTextColor(context.getResources().getColor(R.color.colorAlarmEnable));
            holder.days.setTextColor(context.getResources().getColor(R.color.colorAlarmEnable));
            holder.sunMode.setImageAlpha(context.getResources().getInteger(R.integer.enable_alpha));
        } else {
            holder.time.setTextColor(context.getResources().getColor(R.color.colorAlarmDisable));
            holder.label.setTextColor(context.getResources().getColor(R.color.colorAlarmDisable));
            holder.days.setTextColor(context.getResources().getColor(R.color.colorAlarmDisable));
            holder.sunMode.setImageAlpha(context.getResources().getInteger(R.integer.disable_alpha));
        }
        if (alarm.getSunMode() == 0) holder.sunMode.setImageAlpha(0);
    }

    public void setOnCheckedListener(OnCheckedChangeListener onCheckedChangeListener) {
        this.onCheckedChangeListener = onCheckedChangeListener;
    }

    @Override
    public int getItemCount() {
        return alarms == null ? 0 : alarms.size();
    }

    public void setAlarms(List<AlarmData> alarms) {
        this.alarms = alarms;
        notifyDataSetChanged();
    }

    static final class ViewHolder extends RecyclerView.ViewHolder {

        final TextView time, label, days;
        final Switch enabled;
        final ImageView sunMode;

        ViewHolder(View itemView) {
            super(itemView);
            time = itemView.findViewById(R.id.row_time);
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
