package com.siarhei.alarmus.data;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.Calendar;

public class SunAlarm extends AlarmData implements Parcelable {
    public SunAlarm(Parcel in) {
        id = in.readInt();
        name = in.readString();
        time = Calendar.getInstance();
        time.setTimeInMillis(in.readLong());
        enabled = in.readByte() == 1;
        sunMode = in.readInt();
        once = in.readByte() == 1;
        latitude = in.readDouble();
        longitude = in.readDouble();
        //days = in.createBooleanArray();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(id);
        dest.writeString(name);
        dest.writeLong(time.getTimeInMillis());
        dest.writeByte((byte) (enabled ? 1 : 0));
        dest.writeInt(sunMode);
        dest.writeByte((byte) (once ? 1 : 0));
        dest.writeDouble(latitude);
        dest.writeDouble(longitude);
        //dest.writeBooleanArray(days);
    }

    public SunAlarm(int id) {
        this.id = id;
        this.time = Calendar.getInstance();
        setTime(time.getTimeInMillis());
        enabled = false;
        once = true;
        // days = new boolean[]{true, true, true, true, true, true, true};

    }

    public static final Creator<SimpleAlarm> CREATOR = new Creator<SimpleAlarm>() {
        @Override
        public SimpleAlarm createFromParcel(Parcel in) {
            return new SimpleAlarm(in);
        }

        @Override
        public SimpleAlarm[] newArray(int size) {
            return new SimpleAlarm[size];
        }
    };


    @Override
    public int describeContents() {
        return 0;
    }
}
