package com.siarhei.alarmus.map;

import android.content.Context;

import com.siarhei.alarmus.R;
import com.siarhei.alarmus.sun.SunInfo;

import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.Marker;

import java.util.Calendar;

public class SunInfoMarker extends Marker {

    public SunInfoMarker(MapView mapView, Context resourceProxy) {
        super(mapView, resourceProxy);
        setIcon(resourceProxy.getResources().getDrawable(R.drawable.ic_default_marker));
    }

    @Override
    public void setRelatedObject(Object relatedObject) {
        super.setRelatedObject(relatedObject);
        if (relatedObject != null) {
            final SunInfo info = (SunInfo) relatedObject;
            setSnippet(info.toString(SunInfo.HH_MM));
            setSubDescription(info.subDescription());
            setTitle("INFO:");
        }

    }

    @Override
    public void setPosition(GeoPoint position) {
        super.setPosition(position);
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(System.currentTimeMillis());
        int day = calendar.get(Calendar.DAY_OF_MONTH);
        int month = calendar.get(Calendar.MONTH) + 1;
        int year = calendar.get(Calendar.YEAR);
        setRelatedObject(new SunInfo(day, month, year, position.getLatitude(), position.getLongitude()));
    }
}
