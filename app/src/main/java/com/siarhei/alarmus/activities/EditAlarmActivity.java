package com.siarhei.alarmus.activities;

import android.app.TimePickerDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.TimePicker;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.siarhei.alarmus.R;
import com.siarhei.alarmus.data.Alarm;
import com.siarhei.alarmus.data.AlarmPreferences;
import com.siarhei.alarmus.data.SunAlarm;
import com.siarhei.alarmus.sun.SunInfo;
import com.siarhei.alarmus.sun.SunMath;
import com.siarhei.alarmus.views.CircleCheckBox;
import com.siarhei.alarmus.views.DelayPicker;
import com.siarhei.alarmus.views.ImageRadioButton;
import com.siarhei.alarmus.views.ImageRadioGroup;
import com.siarhei.alarmus.views.SunInfoScrollView;

import java.util.Calendar;

public class EditAlarmActivity extends AppCompatActivity implements CompoundButton.OnCheckedChangeListener, View.OnClickListener {
    public static final int RESULT_LOCATION_CHOSEN = 2;
    public static final int RESULT_NEW_ALARM = 1;
    public static final int RESULT_EDIT_ALARM = 4;
    public static final String LONGITUDE = "longitude";
    public static final String LATITUDE = "latitude";
    public static final String ALARM_NAME = "alarm";
    public static final String ID = "id";

    private TextView timeView, dateView, locationView;
    private EditText labelEdit;
    private Switch alarmSwitch;
    private TimePicker timePicker;
    private ImageButton saveBtn;
    private CheckBox repeatCheck;
    private ImageRadioGroup radioGroupSunMode;
    private ImageRadioButton radioSunrise, radioSunset, radioNoon;
    private DelayPicker delayPicker;
    private CircleCheckBox[] checkDays;
    private SunInfoScrollView infoView;
    private TextView delayView;
    private View weekView;
    private Alarm currentAlarm;
    private AlarmPreferences preferences;
    private int alarmType;
    private double latitude;
    private double longitude;
    private Toolbar toolbar;
    private AlertDialog editLabeldialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_alarm);

        initContentView();
        preferences = AlarmPreferences.getInstance(this);
        currentAlarm = getIntent().getParcelableExtra(MainActivity.ALARM_EDITING);
        alarmType = (currentAlarm instanceof SunAlarm) ?
                AlarmPreferences.SUN_TYPE : AlarmPreferences.SIMPLE_TYPE;
        setVisibility();
        updateContentView();
        setListeners();
        updateTimeView();
    }

    private void initContentView() {
        toolbar = findViewById(R.id.add_alarm_toolbar);
        setSupportActionBar(toolbar);
        saveBtn = findViewById(R.id.save_button);
        timeView = findViewById(R.id.timeView);
        dateView = findViewById(R.id.dateView);
        locationView = findViewById(R.id.btn_location);
        timePicker = findViewById(R.id.timePicker);
        timePicker.setIs24HourView(true);
        labelEdit = findViewById(R.id.label_edit);
        infoView = findViewById(R.id.sun_info_view);
        radioGroupSunMode = findViewById(R.id.radio_group_sun_mode);
        radioSunrise = findViewById(R.id.radioSunrise);
        radioNoon = findViewById(R.id.radioNoon);
        radioSunset = findViewById(R.id.radioSunset);
        radioSunrise.setImage(R.drawable.ic_sunrise);
        radioNoon.setImage(R.drawable.ic_noon);
        radioSunset.setImage(R.drawable.ic_sunset);
        delayPicker = findViewById(R.id.delay_picker);
        delayView = findViewById(R.id.delay_view);
        repeatCheck = findViewById(R.id.repeatCheck);
        weekView = findViewById(R.id.view_week);
        checkDays = new CircleCheckBox[]{findViewById(R.id.check_day1),
                findViewById(R.id.check_day2), findViewById(R.id.check_day3),
                findViewById(R.id.check_day4), findViewById(R.id.check_day5),
                findViewById(R.id.check_day6), findViewById(R.id.check_day7)};
        alarmSwitch = findViewById(R.id.alarmSwitch);
    }

    private void setVisibility() {
        if (alarmType == AlarmPreferences.SUN_TYPE) {
            radioGroupSunMode.setVisibility(View.VISIBLE);
            locationView.setVisibility(View.VISIBLE);
            timePicker.setVisibility(View.GONE);
            delayPicker.setVisibility(View.VISIBLE);
            delayView.setVisibility(View.VISIBLE);
        }
    }

    private void updateContentView() {
        alarmSwitch.setChecked(currentAlarm.isEnabled());
        repeatCheck.setChecked(currentAlarm.isRepeat());
        weekView.setVisibility(currentAlarm.isRepeat() ? View.VISIBLE : View.GONE);
        for (int i = 0; i < 7; i++) {
            checkDays[i].setChecked(currentAlarm.getDays()[i]);
        }
        if (alarmType == AlarmPreferences.SUN_TYPE) {
            SunAlarm sunAlarm = (SunAlarm) currentAlarm;
            radioSunrise.setChecked(sunAlarm.getSunMode() == SunAlarm.MODE_SUNRISE);
            radioNoon.setChecked(sunAlarm.getSunMode() == SunAlarm.MODE_NOON);
            radioSunset.setChecked(sunAlarm.getSunMode() == SunAlarm.MODE_SUNSET);
            latitude = sunAlarm.getLatitude();
            longitude = sunAlarm.getLongitude();
            updateLocationViews();
        } else {
            timePicker.setCurrentHour(currentAlarm.getHour());
            timePicker.setCurrentMinute(currentAlarm.getMinute());
        }
        labelEdit.setText(currentAlarm.getLabel());
    }

    public AlertDialog createLabelEditDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        LayoutInflater inflater = getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.dialog_edit_label, null);
        builder.setView(dialogView);
        EditText labelEditD = dialogView.findViewById(R.id.label_edit);
        labelEditD.setText(labelEdit.getText().toString());
        labelEditD.setSelection(labelEditD.getText().length());
        labelEditD.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == 6) {
                labelEdit.setText(labelEditD.getText().toString());
                editLabeldialog.cancel();
            }
            return false;
        });
        builder.setOnCancelListener(new DialogInterface.OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialog) {
                InputMethodManager imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
                if (imm != null) {
                    imm.toggleSoftInput(0, 0);
                }
            }
        });
        AlertDialog dialog = builder.create();
        dialogView.findViewById(R.id.button_cancel).setOnClickListener((v) -> {
            dialog.cancel();
        });
        dialogView.findViewById(R.id.button_ok).setOnClickListener((v) -> {
            labelEdit.setText(labelEditD.getText().toString());
            dialog.cancel();
        });
        return dialog;
    }

    private void setListeners() {
        saveBtn.setOnClickListener(this);
        repeatCheck.setOnCheckedChangeListener(this);
        locationView.setOnClickListener(this);
        timePicker.setOnTimeChangedListener((view, hourOfDay, minute) -> {
            updateAlarm();
            updateTimeView();
        });
        labelEdit.setOnClickListener(v -> {
            editLabeldialog = createLabelEditDialog();
            editLabeldialog.show();
            InputMethodManager imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
            imm.toggleSoftInput(InputMethodManager.SHOW_FORCED, 0);
        });
        radioGroupSunMode.setOnCheckedChangeListener((id) -> {
            //Log.d("update", "" + id);
            updateAlarm();
            updateTimeView();
        });
        delayPicker.setOnItemSelectedListener(index -> {
            updateAlarm();
            updateTimeView();
        });
        for (int i = 0; i < 7; i++) {
            checkDays[i].setOnCheckedChangeListener((view, checked) -> {
                updateAlarm();
                updateTimeView();
                updateLocationViews();
            });
        }
    }

    @Override
    public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
        if (compoundButton.getId() == R.id.repeatCheck) {
            findViewById(R.id.view_week).setVisibility(b ? View.VISIBLE : View.GONE);
            updateAlarm();
            updateTimeView();
            updateLocationViews();
        }
    }

    @Override
    public void onClick(View view) {
        if (view.getId() == R.id.timeView) {
            TimePickerDialog timePickerDialog = new TimePickerDialog(this,
                    (view1, hourOfDay, minute) ->
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
        updateAlarm();
        currentAlarm.cancelAlarm(this);
        // if (currentAlarm.isEnabled())
        currentAlarm.setEnable(true);
        currentAlarm.setAlarm(this);
        //MainActivity.addAlarm(currentAlarm);
        preferences.writeAlarm(currentAlarm);
        //Toast.makeText(this, "Будильник!!!", Toast.LENGTH_SHORT).show();
    }

    private void chooseLocation() {
        Intent mapIntent = new Intent("android.intent.action.SET_LOCATION");
        mapIntent.putExtra(LATITUDE, ((SunAlarm) currentAlarm).getLatitude());
        mapIntent.putExtra(LONGITUDE, ((SunAlarm) currentAlarm).getLongitude());
        this.startActivityForResult(mapIntent, 1);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_LOCATION_CHOSEN) {
            latitude = data.getDoubleExtra(LATITUDE, 0);
            longitude = data.getDoubleExtra(LONGITUDE, 0);
            updateAlarm();
            updateLocationViews();
            updateTimeView();
            //Toast.makeText(getApplicationContext(), lat + " " + lon, Toast.LENGTH_LONG).show();
        }
    }

    private void updateLocationViews() {
        SunAlarm sunAlarm = (SunAlarm) currentAlarm;
        locationView.setText("Location: " + SunMath.round(latitude, 4) + " " + SunMath.round(longitude, 4));
        SunInfo sunInfo = new SunInfo(currentAlarm.getTime(), latitude, longitude);
        if (!SunInfo.afterNow(sunInfo, SunInfo.SUNRISE_MODE))
            sunInfo = SunInfo.nextDaySunInfo(sunInfo, 1);
        radioSunrise.setText(SunInfo.timeToString(sunInfo.getSunriseLocalTime(), SunInfo.HH_MM));
        radioSunrise.setSubText(sunInfo.getDateString());
        if (!SunInfo.afterNow(sunInfo, SunInfo.NOON_MODE))
            sunInfo = SunInfo.nextDaySunInfo(sunInfo, 1);
        radioNoon.setSubText(sunInfo.getDateString());
        radioNoon.setText(SunInfo.timeToString(sunInfo.getNoonLocalTime(), SunInfo.HH_MM));
        if (!SunInfo.afterNow(sunInfo, SunInfo.SUNSET_MODE))
            sunInfo = SunInfo.nextDaySunInfo(sunInfo, 1);
        radioSunset.setSubText(sunInfo.getDateString());
        radioSunset.setText(SunInfo.timeToString(sunInfo.getSunsetLocalTime(), SunInfo.HH_MM));

        delayPicker.setValue(sunAlarm.getDelay());
    }


    private void updateAlarm() {
        currentAlarm.setEnable(alarmSwitch.isChecked());
        if (alarmType == AlarmPreferences.SUN_TYPE) {
            SunAlarm sunAlarm = (SunAlarm) currentAlarm;
            if (radioSunrise.isChecked()) sunAlarm.setSunMode(SunAlarm.MODE_SUNRISE);
            else if (radioNoon.isChecked()) sunAlarm.setSunMode(SunAlarm.MODE_NOON);
            else if (radioSunset.isChecked()) sunAlarm.setSunMode(SunAlarm.MODE_SUNSET);
            sunAlarm.setPosition(latitude, longitude);
            sunAlarm.setDelay(delayPicker.getValue());
        } else {
            currentAlarm.setTime(timePicker.getCurrentHour(), timePicker.getCurrentMinute());
        }
        currentAlarm.setRepeat(repeatCheck.isChecked());
        boolean[] days = new boolean[7];
        for (int i = 0; i < 7; i++) {
            days[i] = checkDays[i].isChecked();
        }
        currentAlarm.setDays(days);
        currentAlarm.setTimeNext();
        currentAlarm.setLabel(labelEdit.getText().toString());
        //Log.d("update", "update");
    }

    private void updateTimeView() {
        timeView.setText(currentAlarm.toTime());
        dateView.setText(currentAlarm.toDate());
    }
}