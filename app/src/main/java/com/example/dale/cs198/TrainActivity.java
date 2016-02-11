package com.example.dale.cs198;

import android.content.Intent;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;

public class TrainActivity extends AppCompatActivity {

    private static final String TAG = "testMessage";

    RecyclerView recyclerView;
    CropImageAdapter adapter;
    RecyclerView.LayoutManager layoutManager;
    ArrayList<CropImageItem> pathList = new ArrayList<CropImageItem>();

    Button openCamera;




    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_train);

        //POPULATE pathList from the cropped faces files from sdCard/PresentData/faceCrops folder

        File sdCardRoot = Environment.getExternalStorageDirectory();
        File faceCropsDir = new File(sdCardRoot, "PresentData/faceCrops");
        for (File f : faceCropsDir.listFiles()) {
            if (f.isFile()) {
                String name = f.getName();
                CropImageItem c = new CropImageItem("sdcard/PresentData/faceCrops/"+name,name);
                pathList.add(c);
            }
        }

        openCamera = (Button)findViewById(R.id.camera_train);
        openCamera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent cam = new Intent(TrainActivity.this, CustomCamera.class);
                startActivity(cam);
            }
        });






        recyclerView = (RecyclerView)findViewById(R.id.recycler_view_crop_images);
        //layoutManager = new LinearLayoutManager(this);
        //recyclerView.setLayoutManager(layoutManager);
        recyclerView.setLayoutManager(new GridLayoutManager(this, 2));
        recyclerView.setHasFixedSize(true);
        recyclerView.setItemAnimator(null);
        adapter=new CropImageAdapter(this,pathList);
        recyclerView.setAdapter(adapter);

        // Setup ItemTouchHelper
        ItemTouchHelper.Callback callback = new CropImageTouchHelper(adapter);
        ItemTouchHelper helper = new ItemTouchHelper(callback);
        helper.attachToRecyclerView(recyclerView);


    }





}
