package com.siarhei.alarmus.data;

import android.os.Parcel;

public class SimpleAlarm extends AlarmData {

    protected SimpleAlarm(Parcel in) {
        super(in);
    }

    public SimpleAlarm(int id) {
        super(id);
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

}
