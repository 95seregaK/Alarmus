package com.siarhei.alarmus.activities;

import android.app.TimePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageButton;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.siarhei.alarmus.R;
import com.siarhei.alarmus.data.Alarm;
import com.siarhei.alarmus.data.AlarmPreferences;
import com.siarhei.alarmus.data.SunAlarm;
import com.siarhei.alarmus.sun.SunInfo;
import com.siarhei.alarmus.views.CircleCheckBox;
import com.siarhei.alarmus.views.DelayPicker;

import java.util.Calendar;

public class EditAlarmActivity extends AppCompatActivity implements CompoundButton.OnCheckedChangeListener, View.OnClickListener {
    public static final String APP_PREFERENCES = "my_settings";
    public static final int RESULT_LOCATION_CHOSEN = 2;
    public static final int RESULT_NEW_ALARM = 1;
    public static final int RESULT_EDIT_ALARM = 4;
    public static final String LONGITUDE = "longitude";
    public static final String LATITUDE = "latitude";
    public static final String ALARM_NAME = "alarm";
    public static final String ID = "id";
    private TextView timeView;
    private TextView locationView;
    private Switch alarmSwitch;
    private Alarm currentAlarm;
    private TimePicker timePicker;
    public AlarmPreferences preferences;
    private ImageButton plusBtn;
    private ImageButton minusBtn;
    private ImageButton saveBtn;
    private CheckBox repeatChk;
    private RadioGroup radioSunMode;
    private RadioButton radioSunrise;
    private RadioButton radioSunset;
    private DelayPicker delayPicker;
    private double lat;
    private double lon;
    private int alarm_type;
    private CircleCheckBox checkDay1, checkDay2, checkDay3, checkDay4, checkDay5, checkDay6, checkDay7;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_alarm);
        Toolbar toolbar = (Toolbar) findViewById(R.id.add_alarm_toolbar);
        setSupportActionBar(toolbar);

        alarmSwitch = (Switch) findViewById(R.id.alarmSwitch);
        timeView = (TextView) findViewById(R.id.timeView);
        locationView = (TextView) findViewById(R.id.btn_location);
        timePicker = (TimePicker) findViewById(R.id.timePicker);
        repeatChk = (CheckBox) findViewById(R.id.repeatCheck);
        saveBtn = (ImageButton) findViewById(R.id.save_button);
        radioSunMode = (RadioGroup) findViewById(R.id.radioSunMode);
        radioSunrise = (RadioButton) findViewById(R.id.radioSunrise);
        radioSunset = (RadioButton) findViewById(R.id.radioSunset);
        delayPicker = (DelayPicker) findViewById(R.id.delay_picker);
        checkDay1 = (CircleCheckBox) findViewById(R.id.check_day1);
        checkDay2 = (CircleCheckBox) findViewById(R.id.check_day2);
        checkDay3 = (CircleCheckBox) findViewById(R.id.check_day3);
        checkDay4 = (CircleCheckBox) findViewById(R.id.check_day4);
        checkDay5 = (CircleCheckBox) findViewById(R.id.check_day5);
        checkDay6 = (CircleCheckBox) findViewById(R.id.check_day6);
        checkDay7 = (CircleCheckBox) findViewById(R.id.check_day7);

        timePicker.setIs24HourView(true);
        //plusBtn.setOnClickListener(this);
        //minusBtn.setOnClickListener(this);
        repeatChk.setOnCheckedChangeListener(this);
        saveBtn.setOnClickListener(this);
        locationView.setOnClickListener(this);

        preferences = AlarmPreferences.getInstance(this);
        currentAlarm = (Alarm) getIntent().getParcelableExtra(MainActivity.ALARM_EDITING);
        Toast.makeText(getApplicationContext(), currentAlarm.getId() + "",
                Toast.LENGTH_SHORT).show();
        alarm_type = (currentAlarm instanceof SunAlarm) ?
                AlarmPreferences.SUN_TYPE : AlarmPreferences.SIMPLE_TYPE;
        updateView(alarm_type);
        Log.d("WEEK", AlarmPreferences.toInteger(
                new boolean[]{false, false, false, true, false, false, false}) + " day of week");
        //alarmSwitch.setOnCheckedChangeListener(this);
    }

    @Override
    public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
        if (compoundButton.getId() == R.id.repeatCheck) {
            findViewById(R.id.view_week).setVisibility(b ? View.VISIBLE : View.GONE);
        }
    }

    @Override
    public void onClick(View view) {
        if (view.getId() == R.id.timeView) {
            TimePickerDialog timePickerDialog = new TimePickerDialog(this,
                    (TimePickerDialog.OnTimeSetListener) (view1, hourOfDay, minute) ->
                    {
                        currentAlarm.setTime(hourOfDay, minute);
                        timeView.setText(currentAlarm.toString());
                    },
                    currentAlarm.getTime().get(Calendar.HOUR_OF_DAY),
                    currentAlarm.getTime().get(Calendar.MINUTE), true);
            timePickerDialog.show();
        } /*else if (view.getId() == R.id.timeMinus) {
            timePicker.setCurrentMinute(timePicker.getCurrentMinute() - 1);
            if (timePicker.getCurrentMinute() == 59) {
                timePicker.setCurrentHour(timePicker.getCurrentHour() - 1);
            }
        } else if (view.getId() == R.id.timePlus) {
            timePicker.setCurrentMinute(timePicker.getCurrentMinute() + 1);
            if (timePicker.getCurrentMinute() == 0) {
                timePicker.setCurrentHour(timePicker.getCurrentHour() + 1);
            }
        }*/ else if (view.getId() == saveBtn.getId()) {
            save();
            setResult(RESULT_NEW_ALARM);
            finish();
        } else if (view.getId() == R.id.btn_location) {
            chooseLocation();
        }
    }

    private void save() {
        currentAlarm.setEnable(alarmSwitch.isChecked());
        currentAlarm.setTime(timePicker.getCurrentHour(), timePicker.getCurrentMinute());
        currentAlarm.setRepeat(repeatChk.isChecked());
        boolean days[] = {checkDay1.isChecked(), checkDay2.isChecked(), checkDay3.isChecked(),
                checkDay4.isChecked(), checkDay5.isChecked(), checkDay6.isChecked(), checkDay7.isChecked()};
        currentAlarm.setDays(days);
        currentAlarm.cancelAlarm(this);

        if (currentAlarm.isEnabled()) currentAlarm.setAlarm(this);
        preferences.writeAlarm(currentAlarm);
        //Toast.makeText(this, "Будильник!!!", Toast.LENGTH_SHORT).show();
    }

    private void chooseLocation() {
        Intent mapIntent = new Intent("android.intent.action.SET_LOCATION");
        this.startActivityForResult(mapIntent, 1);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_LOCATION_CHOSEN) {
            //alarmSwitch.setChecked(false);
            lat = data.getDoubleExtra(LATITUDE, 0);
            lon = data.getDoubleExtra(LONGITUDE, 0);
            Calendar calendar = Calendar.getInstance();
            calendar.setTimeInMillis(System.currentTimeMillis());
            int day = calendar.get(Calendar.DAY_OF_MONTH);
            int month = calendar.get(Calendar.MONTH) + 1;
            int year = calendar.get(Calendar.YEAR);
            SunInfo sunInfo = new SunInfo(day, month, year, lat, lon);
            // timeView.setText(alarm.toString());
            timePicker.setCurrentHour((int) sunInfo.getSunriseLocalTime());
            timePicker.setCurrentMinute((int) (60 * (sunInfo.getSunriseLocalTime() % 1)));
            Toast.makeText(getApplicationContext(), lat + " " + lon, Toast.LENGTH_LONG).show();

        } else {
            //timeView.setText(resultCode+"");
        }

    }

    @Override
    public void onResume() {

        super.onResume();
        //updateView();
    }

    private void updateView(int type) {
        if (type == AlarmPreferences.SIMPLE_TYPE) {
        } else if (type == AlarmPreferences.SUN_TYPE) {
            SunAlarm sunAlarm = (SunAlarm) currentAlarm;
            radioSunMode.setVisibility(View.VISIBLE);
            locationView.setVisibility(View.VISIBLE);
            radioSunrise.setChecked(sunAlarm.getSunMode() == SunAlarm.MODE_SUNRISE);
            radioSunset.setChecked(sunAlarm.getSunMode() == SunAlarm.MODE_SUNSET);
            timePicker.setVisibility(View.GONE);
            delayPicker.setVisibility(View.VISIBLE);

        }

        alarmSwitch.setChecked(currentAlarm.isEnabled());
        repeatChk.setChecked(currentAlarm.isRepeat());
        setDays();
        findViewById(R.id.view_week).setVisibility(currentAlarm.isRepeat() ? View.VISIBLE : View.GONE);
        timePicker.setCurrentHour(currentAlarm.getHour());
        timePicker.setCurrentMinute(currentAlarm.getMinute());
        timeView.setText(currentAlarm.toString());
    }

    private void setDays() {
        checkDay1.setChecked(currentAlarm.getDays()[0]);
        checkDay2.setChecked(currentAlarm.getDays()[1]);
        checkDay3.setChecked(currentAlarm.getDays()[2]);
        checkDay4.setChecked(currentAlarm.getDays()[3]);
        checkDay5.setChecked(currentAlarm.getDays()[4]);
        checkDay6.setChecked(currentAlarm.getDays()[5]);
        checkDay7.setChecked(currentAlarm.getDays()[6]);
    }

}