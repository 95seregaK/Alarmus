package com.siarhei.alarmus.data;

import android.content.Context;
import android.content.SharedPreferences;
import android.widget.Toast;

import com.siarhei.alarmus.R;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

public class AlarmPreferences {


    private SharedPreferences pref;
    private Context context;
    private Set<String> idSet;
    public static final String APP_PREFERENCES = "alarmsList";
    public static final String KEY_ID_SET = "idSet";
    public static final String KEY_NAME = "name";
    public static final String KEY_ENABLE = "enable";
    public static final String KEY_SUN_MODE = "sun";
    public static final String KEY_ONCE = "once";
    public static final String KEY_TIME = "time";
    public static final String KEY_LATITUDE = "latitude";
    public static final String KEY_LONGITUDE = "longitude";

    private AlarmPreferences(Context context) {
        this.context = context;
        pref = context.getSharedPreferences(APP_PREFERENCES, Context.MODE_PRIVATE);
        idSet = pref.getStringSet(KEY_ID_SET, new HashSet<>());
    }

    public static AlarmPreferences getInstance(Context context) {
        return new AlarmPreferences(context);
    }

    public void writeAlarm(AlarmData alarm) {
        SharedPreferences.Editor ed = pref.edit();
        int id = alarm.getId();
        idSet.add(String.valueOf(id));
        ed.putStringSet(KEY_ID_SET, idSet);
        ed.putString(id + KEY_NAME, alarm.getName());
        ed.putBoolean(id + KEY_ENABLE, alarm.isEnabled());
        ed.putInt(id + KEY_SUN_MODE, alarm.getSunMode());
        ed.putBoolean(id + KEY_ONCE, alarm.isOnce());
        ed.putLong(id + KEY_TIME, alarm.getTimeInMillis());
        ed.putLong(id + KEY_LATITUDE, Double.doubleToLongBits(alarm.getLatitude()));
        ed.putLong(id + KEY_LONGITUDE, Double.doubleToLongBits(alarm.getLongitude()));
        //  ed.apply();
        ed.commit();
        Toast.makeText(context, "Будильник записан!!!", Toast.LENGTH_SHORT).show();
    }

    public AlarmData readAlarm(int id) {
        AlarmData alarm = new SimpleAlarm(id);
        String name = pref.getString(id + KEY_NAME, null);
        boolean enable = pref.getBoolean(id + KEY_ENABLE, false);
        boolean once = pref.getBoolean(id + KEY_ONCE, true);
        int sunMode = pref.getInt(id + KEY_SUN_MODE, 0);
        long time = pref.getLong(id + KEY_TIME, System.currentTimeMillis());
        double lat = Double.longBitsToDouble(pref.getLong(id + KEY_LATITUDE, 0));
        double lon = Double.longBitsToDouble(pref.getLong(id + KEY_LONGITUDE, 0));
        alarm.setId(id);
        alarm.setName(name);
        alarm.setEnable(enable);
        alarm.setTime(time);
        alarm.setSunMode(sunMode);
        alarm.setOnce(once);
        alarm.setPosition(lat, lon);
        return alarm;
    }

    public List<AlarmData> readAllAlarms() {
        List alarms = new ArrayList();
        Iterator<String> i = idSet.iterator();
        while (i.hasNext()) {
            alarms.add(readAlarm(Integer.parseInt(i.next())));
        }
        return alarms;
    }

    public Set<String> getIdes() {
        return idSet;
    }

    public int getAlarmsCount() {
        return idSet.size();
    }

    public void remove(int id) {
        SharedPreferences.Editor ed = pref.edit();
        idSet.remove(String.valueOf(id));
        ed.putStringSet(KEY_ID_SET, idSet);
        ed.remove(id + KEY_NAME);
        ed.remove(id + KEY_ENABLE);
        ed.remove(id + KEY_ONCE);
        ed.remove(id + KEY_SUN_MODE);
        ed.remove(id + KEY_TIME);
        ed.remove(id + KEY_LATITUDE);
        ed.remove(id + KEY_LONGITUDE);
        ed.commit();
        Toast.makeText(context, R.string.alarm_deleted, Toast.LENGTH_SHORT).show();
    }
}
