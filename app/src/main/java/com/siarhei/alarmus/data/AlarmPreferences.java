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


    public static final int SUN_TYPE = 1;
    public static final int SIMPLE_TYPE = 2;

    private final SharedPreferences pref;
    private final Context context;
    private final Set<String> idSet;
    public static final String APP_PREFERENCES = "alarmsList";
    public static final String KEY_ID_SET = "idSet";
    public static final String KEY_TYPE = "type";
    public static final String KEY_LABEL = "name";
    public static final String KEY_ENABLE = "enable";
    public static final String KEY_SUN_MODE = "sun";
    public static final String KEY_REPEAT = "repeat";
    public static final String KEY_DAYS = "days";
    public static final String KEY_DELAY = "delay";
    public static final String KEY_TIME = "time";
    public static final String KEY_LATITUDE = "latitude";
    public static final String KEY_LONGITUDE = "longitude";
    public static final String KEY_UPDATE = "update";
    public static final String KEY_CITY = "city";

    private AlarmPreferences(Context context) {
        this.context = context;
        pref = context.getSharedPreferences(APP_PREFERENCES, Context.MODE_PRIVATE);
        idSet = pref.getStringSet(KEY_ID_SET, new HashSet<>());
    }

    public static AlarmPreferences getInstance(Context context) {
        return new AlarmPreferences(context);
    }

    public void writeAlarm(Alarm alarm) {
        SharedPreferences.Editor ed = pref.edit();
        int id = alarm.getId();
        idSet.add(String.valueOf(id));
        ed.putStringSet(KEY_ID_SET, idSet);
        ed.putString(id + KEY_LABEL, alarm.getLabel());
        ed.putBoolean(id + KEY_ENABLE, alarm.isEnabled());
        ed.putInt(id + KEY_DAYS, toInteger(alarm.getDays()));
        ed.putBoolean(id + KEY_REPEAT, alarm.isRepeat());
        ed.putLong(id + KEY_TIME, alarm.getTimeInMillis());

        if (alarm instanceof SunAlarm) {
            ed.putInt(id + KEY_TYPE, SUN_TYPE);
            SunAlarm sunAlarm = (SunAlarm) alarm;
            ed.putInt(id + KEY_SUN_MODE, sunAlarm.getSunMode());
            ed.putLong(id + KEY_LATITUDE, Double.doubleToLongBits(sunAlarm.getLatitude()));
            ed.putLong(id + KEY_LONGITUDE, Double.doubleToLongBits(sunAlarm.getLongitude()));
            ed.putInt(id + KEY_DELAY, sunAlarm.getDelay());
            ed.putBoolean(id + KEY_UPDATE, sunAlarm.isUpdate());
            ed.putString(id+KEY_CITY,sunAlarm.getCity());
        } else {
            ed.putInt(id + KEY_TYPE, SIMPLE_TYPE);
        }
        ed.apply();
        //ed.commit();
       // Toast.makeText(context, "Будильник записан!!!", Toast.LENGTH_SHORT).show();
    }

    public Alarm readAlarm(int id) {
        Alarm alarm;
        int type = pref.getInt(id + KEY_TYPE, 0);
        String name = pref.getString(id + KEY_LABEL, null);
        boolean enable = pref.getBoolean(id + KEY_ENABLE, false);
        boolean once = pref.getBoolean(id + KEY_REPEAT, true);
        long time = pref.getLong(id + KEY_TIME, System.currentTimeMillis());
        boolean[] days = toBooleanArray(pref.getInt(id + KEY_DAYS, 0));
        if (type == SUN_TYPE) {

            int sunMode = pref.getInt(id + KEY_SUN_MODE, 0);
            double lat = Double.longBitsToDouble(pref.getLong(id + KEY_LATITUDE, 0));
            double lon = Double.longBitsToDouble(pref.getLong(id + KEY_LONGITUDE, 0));
            int delay = pref.getInt(id + KEY_DELAY, 0);
            boolean update = pref.getBoolean(id + KEY_UPDATE, true);
            String city=pref.getString(id + KEY_CITY, "");
            alarm = new SunAlarm(id);
            SunAlarm sunAlarm = (SunAlarm) alarm;
            sunAlarm.sunMode=sunMode;
            sunAlarm.latitude=lat;
            sunAlarm.longitude=lon;
            sunAlarm.delay=delay;
            sunAlarm.update=update;
            sunAlarm.city=city;
        } else {
            alarm = new Alarm(id);
        }

        alarm.label=name;
        alarm.enabled=enable;
        alarm.setTime(time);
        alarm.repeat=once;
        alarm.days=days;

        return alarm;
    }

    public static boolean[] toBooleanArray(int in) {
        boolean[] res = new boolean[7];
        for (int i = 0; i < 7; i++) {
            res[i] = (in & (1 << i)) == (1 << i);
        }
        return res;
    }

    public static int toInteger(boolean[] in) {
        int res = 0;
        for (int i = 0; i < 7; i++) {
            res = res | (in[i] ? 1 << i : 0);
        }
        return res;
    }

    public List<Alarm> readAllAlarms() {
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
        ed.remove(id + KEY_TYPE);
        ed.remove(id + KEY_TIME);
        ed.remove(id + KEY_LABEL);
        ed.remove(id + KEY_ENABLE);
        ed.remove(id + KEY_REPEAT);
        ed.remove(id + KEY_SUN_MODE);
        ed.remove(id + KEY_LATITUDE);
        ed.remove(id + KEY_LONGITUDE);
        ed.remove(id + KEY_DELAY);
        ed.remove(id + KEY_DAYS);
        ed.remove(id + KEY_UPDATE);
        ed.remove(id + KEY_CITY);
        ed.apply();
        Toast.makeText(context, R.string.alarm_deleted, Toast.LENGTH_SHORT).show();
    }
}
