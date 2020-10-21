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
import com.siarhei.alarmus.data.AlarmData;
import com.siarhei.alarmus.data.AlarmPreferences;
import com.siarhei.alarmus.data.SimpleAlarm;
import com.siarhei.alarmus.data.SunAlarm;
import com.siarhei.alarmus.views.AlarmRecyclerAdapter;
import com.siarhei.alarmus.views.MyRecyclerView;

import java.util.Collections;
import java.util.List;

public class MainActivity extends AppCompatActivity implements View.OnClickListener,
        MyRecyclerView.OnItemSwipeListener, MyRecyclerView.OnItemClickListener,
        AlarmRecyclerAdapter.OnCheckedChangeListener {
    private static final int CODE_ADD_NEW = 3;
    private static final int CODE_EDIT_CURRENT = 4;
    static final String ALARM_EDITING = "alarm_for_editing";
    private static final short SHIFT = 8;
    private MyRecyclerView recycler;
    private AlarmPreferences preferences;
    private List<AlarmData> alarms;
    private FloatingActionButton addBtn;
    private AlarmRecyclerAdapter alarmAdapter;
    private AlertDialog chooseTypeDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toast.makeText(getApplicationContext(), "onCreate()", Toast.LENGTH_SHORT).show();
        preferences = AlarmPreferences.getInstance(this);
        alarms = preferences.readAllAlarms();
        recycler = (MyRecyclerView) findViewById(R.id.recycler);
        LinearLayoutManager llm = new LinearLayoutManager(this);
        alarmAdapter = new AlarmRecyclerAdapter();
        alarmAdapter.setAlarms(alarms);
        recycler.setLayoutManager(llm);
        recycler.setAdapter(alarmAdapter);
        recycler.setOnItemSwipeListener(this);
        recycler.setOnItemClickListener(this);
        addBtn = (FloatingActionButton) findViewById(R.id.add_button);
        addBtn.setOnClickListener(this);
        alarmAdapter.setOnCheckedListener(this);
        chooseTypeDialog = createDialog();

    }

    public AlertDialog createDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this, R.style.CustomDialog);

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
        AlertDialog dialog = builder.create();
        return dialog;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
/*
        if (requestCode == CODE_ADD_NEW && resultCode == EditAlarmActivity.RESULT_NEW_ALARM) {
            AlarmData alarmData = (AlarmData) data.getParcelableExtra(ALARM_EDITING_NAME);
            alarms.add(alarmData);
            alarmAdapter.notifyItemInserted(alarms.size() - 1);
            if (alarmData.isEnabled()) alarmData.setAlarm(this);
            preferences.writeAlarm(alarmData);

        } else if (requestCode >> SHIFT == CODE_EDIT_CURRENT
                && resultCode == EditAlarmActivity.RESULT_NEW_ALARM) {
            AlarmData alarmData = (AlarmData) data.getParcelableExtra(ALARM_EDITING_NAME);
            int i = requestCode - (CODE_EDIT_CURRENT << SHIFT);
            if (!alarms.get(i).isEnabled() && alarmData.isEnabled()) alarmData.setAlarm(this);
            if (alarms.get(i).isEnabled() && !alarmData.isEnabled()) alarmData.cancelAlarm(this);
            alarms.set(i, alarmData);
            alarmAdapter.notifyItemChanged(i);

            preferences.writeAlarm(alarmData);
            Toast.makeText(this, i + "onActivityResult()", Toast.LENGTH_SHORT).show();
        }*/
    }

    @Override
    public void onResume() {
        super.onResume();
        alarms = preferences.readAllAlarms();
        Collections.sort(alarms, (o1, o2) -> (int) (o1.getTimeInMillis() - o2.getTimeInMillis()));
        alarmAdapter.setAlarms(alarms);
        //Toast.makeText(getApplicationContext(), "onResume()", Toast.LENGTH_SHORT).show();
    }

    public void editAlarm(AlarmData alarm) {
        Intent intent = new Intent("android.intent.action.ADD_ALARM");
        if (alarm instanceof SimpleAlarm) intent.putExtra(ALARM_EDITING, (SimpleAlarm) alarm);
        else intent.putExtra(ALARM_EDITING, (SunAlarm) alarm);
        startActivityForResult(intent, CODE_ADD_NEW);
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == addBtn.getId()) {
            chooseTypeDialog.show();
        } else if (v.getId() == R.id.simple_alarm_btn) {
            editAlarm(new SimpleAlarm(getNextId()));
            chooseTypeDialog.cancel();
        } else if (v.getId() == R.id.sun_alarm_btn) {
            editAlarm(new SunAlarm(getNextId()));
            chooseTypeDialog.cancel();
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
            preferences.remove(alarms.get(position).getId());
            alarms.get(position).cancelAlarm(this);
            alarms.remove(position);
        }
        alarmAdapter.notifyDataSetChanged();
    }


    @Override
    public void onCheckedChange(CompoundButton buttonView, int position, boolean isChecked) {
        AlarmData currentAlarm = alarms.get(position);

        if (isChecked && !currentAlarm.isEnabled()) {
            currentAlarm.setEnable(true);
            currentAlarm.setAlarm(this);
        } else if (!isChecked && currentAlarm.isEnabled()) {
            currentAlarm.setEnable(false);
            currentAlarm.cancelAlarm(this);
        }
        preferences.writeAlarm(currentAlarm);
        alarmAdapter.notifyDataSetChanged();
    }

    public int getNextId() {
        int max = 0;
        for (AlarmData alarm : alarms) {
            max = Math.max(max, alarm.getId());
        }
        return max + 1;
    }

}