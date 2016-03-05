package com.example.dale.cs198;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;

public class TrainActivity extends AppCompatActivity {

    private static final String TAG = "testMessage";

    RecyclerView recyclerView;
    CropImageAdapter adapter;
    RecyclerView.LayoutManager layoutManager;
    ArrayList<CropImageItem> pathList = new ArrayList<CropImageItem>();

    Button openCamera;
    Button train;
    String dataPath = "sdcard/PresentData/faceDatabase/untrainedCrops/";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_train);

        //POPULATE pathList from the cropped faces files from sdCard/PresentData/faceCrops folder
        File sdCardRoot = Environment.getExternalStorageDirectory();
        File faceCropsDir = new File(sdCardRoot, "PresentData/faceDatabase/untrainedCrops");

        try{
            File trainFile = new File(dataPath, "train.txt");
            trainFile.createNewFile();
        }catch (IOException e){
            e.printStackTrace();
        }


        //File faceCropsDir = new File(sdCardRoot, "PresentData/faceCrops");

        //change the directory to /sdcard/presentdata/facedatabase/untrainedcrops
        int i=0;
        for (File f : faceCropsDir.listFiles()) {
            if (f.isFile()) {
                String name = f.getName();
                CropImageItem c = new CropImageItem("sdcard/PresentData/faceDatabase/untrainedCrops/"+name,name);
                c.setPos(i);
                //CropImageItem c = new CropImageItem("sdcard/PresentData/faceCrops/"+name,name);
                pathList.add(c);
                i++;
            }
        }

        openCamera = (Button)findViewById(R.id.camera_train);
        openCamera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent cam = new Intent(TrainActivity.this, CustomCamera.class);
                cam.putExtra("detectUsage", FaceDetectTask.TRAIN_USAGE);
                startActivity(cam);
            }
        });

<<<<<<< HEAD
        train = (Button)findViewById(R.id.train);
        train.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
               //
            }
        });


=======
>>>>>>> 5d287cfb474bf469fdfd7114fa460082dd796ff0
        recyclerView = (RecyclerView)findViewById(R.id.recycler_view_crop_images);
        //layoutManager = new LinearLayoutManager(this);
        //recyclerView.setLayoutManager(layoutManager);
        recyclerView.setLayoutManager(new GridLayoutManager(this, 2));
        recyclerView.setHasFixedSize(true);
        recyclerView.setItemAnimator(null);
        adapter=new CropImageAdapter(this,pathList);

        recyclerView.setAdapter(adapter);
        runOnUiThread(new Runnable() {
            public void run() {
                adapter.notifyDataSetChanged();
                Log.i(TAG,"running notify data set change");
            }
        });

        // Setup ItemTouchHelper
        ItemTouchHelper.Callback callback = new CropImageTouchHelper(recyclerView,adapter);
        ItemTouchHelper helper = new ItemTouchHelper(callback);
        helper.attachToRecyclerView(recyclerView);

        Toast toast = Toast.makeText(this, "Face Crop Number = " + pathList.size(), Toast.LENGTH_SHORT);
        toast.show();
    }

    public void trainRecognizer(View view){
        FaceRecogTrainTask tt = new FaceRecogTrainTask(this);
        tt.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }





}
