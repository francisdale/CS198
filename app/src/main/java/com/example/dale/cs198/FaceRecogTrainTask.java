package com.example.dale.cs198;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.TextView;

import org.bytedeco.javacpp.opencv_contrib.FaceRecognizer;
import org.bytedeco.javacpp.opencv_core.Mat;
import org.bytedeco.javacpp.opencv_core.MatVector;

import java.io.File;
import java.io.FilenameFilter;
import java.nio.IntBuffer;

import static org.bytedeco.javacpp.opencv_contrib.createEigenFaceRecognizer;
import static org.bytedeco.javacpp.opencv_core.CV_32SC1;
import static org.bytedeco.javacpp.opencv_highgui.CV_LOAD_IMAGE_GRAYSCALE;
import static org.bytedeco.javacpp.opencv_highgui.imread;

/**
 * Created by jedpatrickdatu on 2/12/2016.
 */
public class FaceRecogTrainTask extends AsyncTask<Void, Void, Void> {

    private static final String TAG = "testMessage";
    private static final String untrainedCropsDir = "sdcard/PresentData/faceDatabase/untrainedCrops";
    private static final String trainedCropsDir = "sdcard/PresentData/faceDatabase/trainedCrops";
    private static final String modelDir = "sdcard/PresentData/eigenModel.xml";

    long timeStart;
    long timeEnd;
    long timeElapsed;

    Context c;

    public FaceRecogTrainTask(Context c){
        this.c = c;
    }

    @Override
    protected Void doInBackground(Void... params) {

        //Load training images from trainedCropsDir and untrainedCropsDir
        File trainedCropsFolder = new File(trainedCropsDir);
        File untrainedCropsFolder = new File(untrainedCropsDir);

        FilenameFilter trainedCropsImgFilter = new FilenameFilter() {
            public boolean accept(File dir, String name) {
                name = name.toLowerCase();
                return name.endsWith(".jpg");
            }
        };

        FilenameFilter untrainedCropsImgFilter = new FilenameFilter() {
            public boolean accept(File dir, String name) {
                name = name.toLowerCase();
                return name.endsWith(".jpg") && !name.startsWith("unlabeled") && !name.startsWith("delete");
            }
        };

        File[] trainedCrops = trainedCropsFolder.listFiles(trainedCropsImgFilter);
        File[] untrainedCrops = untrainedCropsFolder.listFiles(untrainedCropsImgFilter);


        if(trainedCrops.length == 0 && untrainedCrops.length == 0){
            Log.i(TAG, "No training images found.");
            return null;
        }

        int numTotalCrops = trainedCrops.length + untrainedCrops.length;
        String currSPath;

        MatVector images = new MatVector(numTotalCrops);
        Mat labels = new Mat(numTotalCrops, 1, CV_32SC1);
        Mat img;
        IntBuffer labelsBuf = labels.getIntBuffer();
        int label;


        for(int i = 0; i <= trainedCrops.length; i++){
            img = imread(trainedCrops[i].getAbsolutePath(), CV_LOAD_IMAGE_GRAYSCALE);

            images.put(i, img);
            //Image name syntax: <id number>_<img num>.<format>
            label = Integer.parseInt(trainedCrops[i].getName().split("_")[0]);
            images.put(i, img);
            labelsBuf.put(i, label);
            Log.i(TAG, "trainedCrops i" + i);
        }

        for(int i = 0; i <= untrainedCrops.length; i++){
            img = imread(untrainedCrops[i].getAbsolutePath(), CV_LOAD_IMAGE_GRAYSCALE);

            images.put(i, img);
            //Image name syntax: <id number>_<img num>.<format>
            label = Integer.parseInt(untrainedCrops[i].getName().split("_")[0]);
            images.put(i, img);
            labelsBuf.put(i, label);
            Log.i(TAG, "trainedCrops i" + i);
            untrainedCrops[i].renameTo(new File(trainedCropsDir + "/" + untrainedCrops[i].getName()));
        }

        Log.i(TAG, "Number of images loaded: " + images.size());

        FaceRecognizer faceRecognizer = createEigenFaceRecognizer();

        File folder = new File(modelDir);
        if(!folder.exists()){
            Log.i(TAG, modelDir + " does not exist. Creating...");
            folder.mkdir();
        }


        Log.i(TAG, "Training Eigenface...");
        timeStart = System.currentTimeMillis();
        faceRecognizer.train(images, labels);
        timeEnd = System.currentTimeMillis();
        timeElapsed = timeEnd - timeStart;
        Log.i(TAG, "Training complete.");
        faceRecognizer.save(modelDir);

        return null;
    }

    @Override
    protected void onProgressUpdate(Void... progress){
       TextView tv = (TextView)((MainActivity)c).findViewById(R.id.detectNotification);
        //tv.setText("Detected " + faceCount + " faces in " + (float) timeElapsed/1000 + "s." );
    }
}
