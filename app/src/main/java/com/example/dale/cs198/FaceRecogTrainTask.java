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
    private static final String untrainedLabeledCropsDir = "sdcard/CS198/faceDatabase/untrainedCrops/labeledCrops";
    private static final String trainedCropsDir = "sdcard/CS198/faceDatabase/trainedCrops";
    private static final String modelDir = "sdcard/CS198/recognizerModels";
    private static final String modelFileName = "eigenModel.xml";

    long timeStart;
    long timeEnd;
    long timeElapsed;

    TaskData td;
    Context c;

    public FaceRecogTrainTask(TaskData td, Context c){
        this.td = td;
        this.c = c;
    }

    @Override
    protected Void doInBackground(Void... params) {

        //Load training images from trainingDir and
        File trainingFolder = new File(untrainedLabeledCropsDir);

        FilenameFilter imgFilter = new FilenameFilter() {
            public boolean accept(File dir, String name) {
                name = name.toLowerCase();
                return name.endsWith(".jpg") || name.endsWith(".pgm") || name.endsWith(".png");
            }
        };

        File[] imageFiles = trainingFolder.listFiles(imgFilter);


        if(imageFiles.length == 0){
            Log.i(TAG, "No training images found.");
            return null;
        }

        String currSPath;

        MatVector images = new MatVector(imageFiles.length);
        Mat labels = new Mat(imageFiles.length, 1, CV_32SC1);
        Mat img;
        IntBuffer labelsBuf = labels.getIntBuffer();
        int label;


        for(int i = 0; i <= imageFiles.length; i++){
            img = imread(imageFiles[i].getAbsolutePath(), CV_LOAD_IMAGE_GRAYSCALE);

            images.put(i, img);
            //Image name syntax: <id number>.<format>
            label = Integer.parseInt(imageFiles[i].getName().split(".")[0]);
            images.put(i, img);
            labelsBuf.put(i, label);
            Log.i(TAG, "i" + i);
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
        faceRecognizer.save(modelDir + "/" + modelFileName);

        return null;
    }

    @Override
    protected void onProgressUpdate(Void... progress){
        TextView tv = (TextView)((MainActivity)c).findViewById(R.id.detectNotification);
        //tv.setText("Detected " + faceCount + " faces in " + (float) timeElapsed/1000 + "s." );
    }
}
