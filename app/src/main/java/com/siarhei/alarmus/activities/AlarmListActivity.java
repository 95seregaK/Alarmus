package com.siarhei.alarmus.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.siarhei.alarmus.R;
import com.siarhei.alarmus.data.Alarm;
import com.siarhei.alarmus.data.AlarmPreferences;
import com.siarhei.alarmus.data.SunAlarm;
import com.siarhei.alarmus.data.SunAlarmManager;
import com.siarhei.alarmus.views.AlarmRecyclerAdapter;
import com.siarhei.alarmus.views.MyRecyclerView;

import java.util.Collections;
import java.util.List;

public class AlarmListActivity extends AppCompatActivity implements View.OnClickListener,
        MyRecyclerView.OnItemSwipeListener, MyRecyclerView.OnItemClickListener,
        AlarmRecyclerAdapter.OnCheckedChangeListener {
    static final String ALARM_EDITING = "alarm_for_editing";
    private static final int CODE_ADD_NEW = 3;
    private static final int CODE_EDIT_CURRENT = 4;
    private static List<Alarm> alarms;
    private MyRecyclerView recycler;
    private AlarmPreferences preferences;
    private FloatingActionButton addBtn;
    private AlarmRecyclerAdapter alarmAdapter;
    private SunAlarmManager alarmManager;
    private AlertDialog chooseTypeDialog;

    private static int compare(Alarm o1, Alarm o2) {
        return (o1.getHour() - o2.getHour()) * 60 + o1.getMinute() - o2.getMinute();
    }

    public static void addAlarm(Alarm alarm) {
        if (alarms != null) {
            alarms.add(alarm);
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_alarm_list);
        recycler = findViewById(R.id.recycler);
        LinearLayoutManager llm = new LinearLayoutManager(this);
        alarmAdapter = new AlarmRecyclerAdapter();
        recycler.setLayoutManager(llm);
        recycler.setAdapter(alarmAdapter);
        recycler.setOnItemSwipeListener(this);
        recycler.setOnItemClickListener(this);
        addBtn = findViewById(R.id.add_button);
        addBtn.setOnClickListener(this);
        alarmAdapter.setOnCheckedListener(this);
        alarmManager = SunAlarmManager.getService(this);
    }

    public AlertDialog createDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(AlarmListActivity.this, R.style.CustomDialog);

        LayoutInflater inflater = getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.choose_type_dialog, null);

        // Set the custom layout as alert dialog view
        builder.setView(dialogView);

        // Get the custom alert dialog view widgets reference
        ImageButton simpleAlarmBtn = dialogView.findViewById(R.id.simple_alarm_btn);
        ImageButton sunAlarmBtn = dialogView.findViewById(R.id.sun_alarm_btn);
        simpleAlarmBtn.setOnClickListener(this);
        sunAlarmBtn.setOnClickListener(this);
        // Create the alert dialog
        return builder.create();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == CODE_ADD_NEW && resultCode == EditAlarmActivity.RESULT_NEW_ALARM) {
            //if (alarms.size() == 0) preferences = AlarmPreferences.getInstance(this);
            //alarms = preferences.readAllAlarms();
            //Collections.sort(alarms, MainActivity::compare);
            //alarmAdapter.setAlarms(alarms);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        preferences = AlarmPreferences.getInstance(this);
        alarms = preferences.readAllAlarms();
        Collections.sort(alarms, AlarmListActivity::compare);
        alarmAdapter.setAlarms(alarms);
        //Toast.makeText(getApplicationContext(), "onResume()", Toast.LENGTH_SHORT).show();
    }

    public void editAlarm(Alarm alarm) {
        Intent intent = new Intent("android.intent.action.ADD_ALARM");
        intent.putExtra(ALARM_EDITING, alarm);
        startActivityForResult(intent, CODE_ADD_NEW);
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == addBtn.getId()) {
            chooseTypeDialog = createDialog();
            chooseTypeDialog.show();
        } else if (v.getId() == R.id.simple_alarm_btn) {
            chooseTypeDialog.cancel();
            editAlarm(new Alarm(getNextId()));

        } else if (v.getId() == R.id.sun_alarm_btn) {
            chooseTypeDialog.cancel();
            editAlarm(new SunAlarm(getNextId()));
        }
    }

    @Override
    public void onItemClick(View view, int position) {
        editAlarm(alarms.get(position));
    }

    @Override
    public void onLongItemClick(View view, int position) {

    }

    @Override
    public void onItemSwipe(View view, int position, int dir) {

        if (dir == MyRecyclerView.LEFT) {
            confirmDeleting(position);
        }

    }

    @Override
    public void onCheckedChange(CompoundButton buttonView, int position, boolean isChecked) {
        Alarm alarm = alarms.get(position);
        if (isChecked && !alarm.isEnabled()) {
            if (alarm instanceof SunAlarm && ((SunAlarm) alarm).isUpdate()) {
                MapActivity.defineCurrentLocation(this, (code, location) -> {
                    if (code == MapActivity.CODE_SUCCESS)
                        ((SunAlarm) alarm).setPosition(location.getLatitude(), location.getLongitude());
                    else
                        Toast.makeText(this, R.string.message_location_cannot, Toast.LENGTH_SHORT).show();
                    setAlarm(alarm);
                    alarmAdapter.notifyItemChanged(position);
                });
            } else {
                setAlarm(alarm);
                alarmAdapter.notifyItemChanged(position);
            }
        } else if (!isChecked && alarm.isEnabled()) {
            cancelAlarm(alarm);
            alarmAdapter.notifyItemChanged(position);
        }

    }

    private void setAlarm(Alarm alarm) {
        alarm.setEnable(true);
        alarm.setTimeNext(true);
        alarmManager.set(alarm);
        preferences.writeAlarm(alarm);
        alarmAdapter.notifyDataSetChanged();
       /* Toast.makeText(this, this.getResources().getString(R.string.message_alarm_set)
                + " " + alarm.toString(), Toast.LENGTH_SHORT).show();*/
        EditAlarmActivity.makeToast(this,alarm);
    }

    private void cancelAlarm(Alarm alarm) {
        alarm.setEnable(false);
        alarmManager.cancel(alarm);
        alarmManager.cancelSnoozed(alarm);
        preferences.writeAlarm(alarm);
        Toast.makeText(this, this.getResources().getString(R.string.message_alarm_canceled),
                Toast.LENGTH_SHORT).show();
    }

    public int getNextId() {
        int max = 0;
        for (Alarm alarm : alarms) {
            max = Math.max(max, alarm.getId());
        }
        return max + 1;
    }

    private void removeAlarm(int position) {
        preferences.remove(alarms.get(position).getId());
        alarmManager.cancel(alarms.get(position));
        alarmManager.cancelSnoozed(alarms.get(position));
        alarms.remove(position);
    }

    private void confirmDeleting(int position) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(getResources().getString(R.string.message_confirm_deleting));

      /*
       LayoutInflater inflater = getLayoutInflater();
       View layout = inflater.inflate(R.layout.layout_dialog_confirm, null);
        layout.findViewById(R.id.button_confirm).setOnClickListener(v -> {
            removeAlarm(position);
            //alarmAdapter.notifyDataSetChanged();
        });
        layout.findViewById(R.id.button_cancel).setOnClickListener(v -> {
            alarmAdapter.notifyDataSetChanged();
        });
        builder.setView(layout);*/
        builder.setPositiveButton(R.string.confirm, (dialog, id) -> {
            removeAlarm(position);
            alarmAdapter.notifyDataSetChanged();
        });
        builder.setNegativeButton(R.string.cancel, (dialog, id) -> {
            alarmAdapter.notifyDataSetChanged();
        });
        builder.create();
        builder.show();
    }
}