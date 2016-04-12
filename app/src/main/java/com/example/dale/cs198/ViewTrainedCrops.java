package com.example.dale.cs198;

import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.widget.Button;

import java.io.File;
import java.io.FilenameFilter;
import java.util.ArrayList;

public class ViewTrainedCrops extends AppCompatActivity {

    private static final String TAG = "testMessage";

    RecyclerView recyclerView;
    ViewTrainedCropsAdapter adapter;
    RecyclerView.LayoutManager layoutManager;
    ArrayList<CropImageItem> pathList = new ArrayList<CropImageItem>();

    Button openCamera;
    String modelFileDir = "sdcard/PresentData";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_trained_crops);

        File sdCardRoot = Environment.getExternalStorageDirectory();
        File faceCropsDir = new File(sdCardRoot, "PresentData/faceDatabase/trainedCrops");

        FilenameFilter trainedCropsImgFilter = new FilenameFilter() {
            public boolean accept(File dir, String name) {
                name = name.toLowerCase();
                return !name.startsWith("unlabeled") || !name.startsWith("delete");
            }
        };

        int i=0;
        for (File f : faceCropsDir.listFiles(trainedCropsImgFilter)) {
            if (f.isFile()) {
                String nameArr[] = f.getName().split("_");
                String name = f.getName();
                CropImageItem c = new CropImageItem("sdcard/PresentData/faceDatabase/trainedCrops/"+name,name);
                c.setPos(i);
                pathList.add(c);
                i++;
            }
        }
        recyclerView = (RecyclerView)findViewById(R.id.recycler_view_trained_crops);
        //layoutManager = new LinearLayoutManager(this);
        //recyclerView.setLayoutManager(layoutManager);
        recyclerView.setLayoutManager(new GridLayoutManager(this, 2));
        recyclerView.setHasFixedSize(true);
        recyclerView.setItemAnimator(null);
        adapter=new ViewTrainedCropsAdapter(this,pathList);

        recyclerView.setAdapter(adapter);
        runOnUiThread(new Runnable() {
            public void run() {
                adapter.notifyDataSetChanged();
                Log.i(TAG, "running notify data set change");
            }
        });








    }
}
