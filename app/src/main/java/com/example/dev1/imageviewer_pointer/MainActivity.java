package com.example.dev1.imageviewer_pointer;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

public class MainActivity extends AppCompatActivity {
    private ImageViewerPointer imageViewerPointer;
    private float w,h;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        imageViewerPointer = findViewById(R.id.imageViewerPointer);
    }
}
