package com.siarhei.alarmus.activities;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.CompoundButton;
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
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
/*
        if (requestCode == CODE_ADD_NEW && resultCode == AddAlarmActivity.RESULT_NEW_ALARM) {
            AlarmData alarmData = (AlarmData) data.getParcelableExtra(ALARM_EDITING_NAME);
            alarms.add(alarmData);
            alarmAdapter.notifyItemInserted(alarms.size() - 1);
            if (alarmData.isEnabled()) alarmData.setAlarm(this);
            preferences.writeAlarm(alarmData);

        } else if (requestCode >> SHIFT == CODE_EDIT_CURRENT
                && resultCode == AddAlarmActivity.RESULT_NEW_ALARM) {
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
        intent.putExtra(ALARM_EDITING, (SimpleAlarm)alarm);
        startActivityForResult(intent, CODE_ADD_NEW);
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == addBtn.getId()) {
            //editAlarm(new SimpleAlarm(getNextId()));

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