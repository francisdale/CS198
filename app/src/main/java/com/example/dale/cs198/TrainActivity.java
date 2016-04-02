package com.example.dale.cs198;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.Toast;

import java.io.File;
import java.io.FilenameFilter;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

public class TrainActivity extends AppCompatActivity {

    private static final String TAG = "testMessage";

    RecyclerView recyclerView;
    CropImageAdapter adapter;
    RecyclerView.LayoutManager layoutManager;
    ArrayList<CropImageItem> pathList = new ArrayList<CropImageItem>();

    Button openCamera;
    String modelFileDir = "sdcard/PresentData";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_train);

        openCamera = (Button)findViewById(R.id.camera_train);
        openCamera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent cam = new Intent(TrainActivity.this, CustomCamera.class);
                cam.putExtra("detectUsage", FaceDetectTask.TRAIN_USAGE);
                startActivity(cam);
            }
        });

        FilenameFilter eigenModelFilter = new FilenameFilter() {
            public boolean accept(File dir, String name) {
                name = name.toLowerCase();
                return name.startsWith("eigenModel") && name.endsWith(".xml");
            }
        };

        File[] modelFileDirFiles = (new File(modelFileDir)).listFiles(eigenModelFilter);
        if(modelFileDirFiles.length > 0) {

            String modelFileDateString = modelFileDirFiles[0].getName().split("_")[1];
            SimpleDateFormat modelFileDate = new SimpleDateFormat("MM/dd/yyyy - HH:mm");

            try {
                Date dt = modelFileDate.parse(modelFileDateString);
                getSupportActionBar().setTitle("Last trained at " + dt);
            } catch (Exception e) {
                Log.e(TAG, "Exception thrown at TrainActivity onCreate.");
            }
        }

    }

    @Override
    protected void onStart() {
        super.onStart();
        populateWithFaces();
    }

    public void populateWithFaces(){
        //POPULATE pathList from the cropped faces files from sdCard/PresentData/faceCrops folder
        File sdCardRoot = Environment.getExternalStorageDirectory();
        File faceCropsDir = new File(sdCardRoot, "PresentData/faceDatabase/untrainedCrops");

        FilenameFilter untrainedCropsImgFilter = new FilenameFilter() {
            public boolean accept(File dir, String name) {
                name = name.toLowerCase();
                return !name.startsWith("unlabeled") || !name.startsWith("delete");
            }
        };

        int i=0;
        for (File f : faceCropsDir.listFiles(untrainedCropsImgFilter)) {
            if (f.isFile()) {
                String nameArr[] = f.getName().split("_");
                String name = f.getName();
                CropImageItem c = new CropImageItem("sdcard/PresentData/faceDatabase/untrainedCrops/"+name,name);
                c.setPos(i);
                pathList.add(c);
                i++;

            }
        }

//        for (File f : faceCropsDir.listFiles(untrainedCropsImgFilter)) {
//            if (f.isFile()) {
//                String nameArr[] = f.getName().split("_");
//                String name = f.getName();
//                if(nameArr[0].equals("delete")){
//                    //delete file
//                }
//                if(nameArr[0].equals("unlabeled")){
//                    //do nothing
//                }
//                else{
//                    //may id sama sa queue
//                }
//            }
//        }

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
        //ItemTouchHelper.Callback callback = new CropImageTouchHelper(recyclerView,adapter);
        //ItemTouchHelper helper = new ItemTouchHelper(callback);
        //helper.attachToRecyclerView(recyclerView);

        Toast toast = Toast.makeText(this, "Face Crop Number = " + pathList.size(), Toast.LENGTH_SHORT);
        toast.show();

    }


    public void trainRecognizer(View view){
        FaceRecogTrainTask tt = new FaceRecogTrainTask(this);

        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        tt.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
    }




}
