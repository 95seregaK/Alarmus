package com.siarhei.alarmus.activities;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
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
    private static final int CODE_ADD_NEW = 3;
    private static final int CODE_EDIT_CURRENT = 4;
    static final String ALARM_EDITING = "alarm_for_editing";
    private static final short SHIFT = 8;
    private MyRecyclerView recycler;
    private AlarmPreferences preferences;
    private static List<Alarm> alarms;
    private FloatingActionButton addBtn;
    private AlarmRecyclerAdapter alarmAdapter;
    private SunAlarmManager alarmManager;
    private AlertDialog chooseTypeDialog;

    private static int compare(Alarm o1, Alarm o2) {
        return (o1.getHour() - o2.getHour()) * 60 + o1.getMinute() - o2.getMinute();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_alarm_list);
        requestPermissions();
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
        if (alarm instanceof Alarm) intent.putExtra(ALARM_EDITING, alarm);
        else intent.putExtra(ALARM_EDITING, alarm);
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
            preferences.remove(alarms.get(position).getId());
            alarmManager.cancel(alarms.get(position));
            alarms.remove(position);
        }
        alarmAdapter.notifyDataSetChanged();
    }

    @Override
    public void onCheckedChange(CompoundButton buttonView, int position, boolean isChecked) {
        Alarm currentAlarm = alarms.get(position);

        if (isChecked && !currentAlarm.isEnabled()) {
            currentAlarm.setEnable(true);
            currentAlarm.setTimeNext(true);
            alarmManager.set(currentAlarm);
        } else if (!isChecked && currentAlarm.isEnabled()) {
            currentAlarm.setEnable(false);
            alarmManager.cancel(currentAlarm);
        }
        preferences.writeAlarm(currentAlarm);
        alarmAdapter.notifyDataSetChanged();
    }

    public int getNextId() {
        int max = 0;
        for (Alarm alarm : alarms) {
            max = Math.max(max, alarm.getId());
        }
        return max + 1;
    }

    public static void addAlarm(Alarm alarm) {
        if (alarms != null) {
            alarms.add(alarm);
        }
    }
    public void requestPermissions(){
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION,Manifest.permission.ACCESS_COARSE_LOCATION}, 1);
            }
        }
    }

}