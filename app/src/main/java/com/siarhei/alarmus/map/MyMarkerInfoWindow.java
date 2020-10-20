package com.siarhei.alarmus.map;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.siarhei.alarmus.R;
import com.siarhei.alarmus.sun.SunInfo;

import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.Marker;
import org.osmdroid.views.overlay.infowindow.MarkerInfoWindow;

public class MyMarkerInfoWindow extends MarkerInfoWindow implements View.OnClickListener {
    private final TextView description;
    /**
     * @param layoutResId layout that must contain these ids: bubble_title,bubble_description,
     * bubble_subdescription, bubble_image
     * @param mapView
     */

    private Button dateButton;
    private Button okButton;
    private Marker relatedMarker;
    private final TextView subDescription;
    private final MapView mapView;
    private final Activity context;

    public MyMarkerInfoWindow(int layoutResId, MapView mapView, Activity context) {

        super(layoutResId, mapView);
        this.context = context;
        this.mapView = mapView;
        dateButton = (mView.findViewById(R.id.date_button));
        dateButton.setOnClickListener(this);
        description = (TextView) (mView.findViewById(R.id.bubble_description));
        subDescription = (TextView) (mView.findViewById(R.id.bubble_subdescription));
    }

    @Override
    public void onOpen(Object item) {
        super.onOpen(item);
        relatedMarker = (SunInfoMarker) item;

        description.setText(relatedMarker.getSnippet());
        subDescription.setText(relatedMarker.getSubDescription());
    }

    @Override
    public void onClick(View view) {
        SunInfo info = (SunInfo) (relatedMarker.getRelatedObject());
        if (view.getId() == R.id.date_button) {
            DatePickerDialog datePickerDialog = new DatePickerDialog(mMapView.getContext(),
                    (view1, year, monthOfYear, dayOfMonth) -> {
                        info.setNewDate(dayOfMonth, monthOfYear + 1, year);
                        relatedMarker.setRelatedObject(info);
                        updateView();
                    }, info.getYear(), info.getMonth() - 1, info.getDay());
            datePickerDialog.show();
        }
    }

    public void updateView() {
        description.setText(relatedMarker.getSnippet());
        subDescription.setText(relatedMarker.getSubDescription());
    }
}
