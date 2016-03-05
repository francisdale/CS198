package com.example.dale.cs198;

import android.content.Intent;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.GridView;

import java.io.File;
import java.util.ArrayList;

public class TrainListActivity extends AppCompatActivity {

    private static final String TAG = "testMessage";

    GridView gridView;
    TrainListAdapter customGridAdapter;
    ArrayList<CropImageItem> pathList = new ArrayList<CropImageItem>();
    Button openCamera;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_train_list);

        //POPULATE pathList from the cropped faces files from sdCard/PresentData/faceCrops folder
        File sdCardRoot = Environment.getExternalStorageDirectory();
        File faceCropsDir = new File(sdCardRoot, "PresentData/faceDatabase/untrainedCrops");
        //File faceCropsDir = new File(sdCardRoot, "PresentData/faceCrops");

        //change the directory to /sdcard/presentdata/facedatabase/untrainedcrops

        for (File f : faceCropsDir.listFiles()) {
            if (f.isFile()) {
                String name = f.getName();
                CropImageItem c = new CropImageItem("sdcard/PresentData/faceDatabase/untrainedCrops/"+name,name);
                Log.i(TAG, c.getPath());
                //CropImageItem c = new CropImageItem("sdcard/PresentData/faceCrops/"+name,name);
                pathList.add(c);
            }
        }

        openCamera = (Button)findViewById(R.id.camera_train);
        openCamera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent cam = new Intent(TrainListActivity.this, CustomCamera.class);
                cam.putExtra("detectUsage", FaceDetectTask.TRAIN_USAGE);
                startActivity(cam);
            }
        });


        gridView = (GridView) findViewById(R.id.grid_face_crops);
        customGridAdapter = new TrainListAdapter(this, R.layout.crop_image_layout_secondary, pathList);
        gridView.setAdapter(customGridAdapter);
        runOnUiThread(new Runnable() {
            public void run() {
                customGridAdapter.notifyDataSetChanged();
            }
        });



    }
}
