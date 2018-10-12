package com.example.dev1.imageviewer_pointer;

import android.content.Context;
import android.graphics.PointF;
import android.support.annotation.Nullable;
import android.util.AttributeSet;

import java.util.List;
import java.util.Vector;

public class ImageMarker extends ImageViewerPointer {
    private List<PointF> markers;

    public boolean addMarker(float x, float y){
        PointF newPoint = new PointF(x,y);
        markers.add(newPoint);
        return true;
    }

    public boolean deleteMarker(float x, float y){

        return true;
    }



    public ImageMarker(Context context) {
        super(context);
    }
    public ImageMarker(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }
    public ImageMarker(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }
}
