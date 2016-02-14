package com.example.dale.cs198;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.TextView;

import org.bytedeco.javacpp.opencv_contrib;
import org.bytedeco.javacpp.opencv_core.Mat;
import org.bytedeco.javacpp.opencv_core.MatVector;

import java.io.File;
import java.io.FilenameFilter;
import java.nio.IntBuffer;

import static org.bytedeco.javacpp.opencv_core.CV_32FC1;
import static org.bytedeco.javacpp.opencv_core.CV_32SC1;
import static org.bytedeco.javacpp.opencv_highgui.CV_LOAD_IMAGE_GRAYSCALE;
import static org.bytedeco.javacpp.opencv_highgui.imread;

/**
 * Created by jedpatrickdatu on 2/12/2016.
 */
public class FaceRecogTrainTask extends AsyncTask<Void, Void, Void> {

    private static final String TAG = "testMessage";
    private static final String untrainedLabeledCropsDir = "sdcard/CS198/faceDatabase/untrainedLabeledCrops";
    private static final String trainedCropsDir = "sdcard/CS198/faceDatabase/trainedCrops";
    private static final String modelDir = "sdcard/CS198/recognizerModels";

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
        File trainingFolder = new File(trainingDir);

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

                label = Integer.parseInt(image.getName().split("\\-")[0]);

                images.put(i, img);
                labelsBuf.put(i, s);
                Log.i(TAG, "i" + i);
            }
        }
        trainingMat.convertTo(trainingMat, CV_32FC1);
        Log.i(TAG, "Number of images loaded: " + images.size());

        opencv_contrib.FaceRecognizer faceRecognizer;

        File folder = new File(modelDir);
        if(!folder.exists()){
            Log.i(TAG, modelDir + " does not exist. Creating...");
            folder.mkdir();
        }

        TextView eigenTime = (TextView) findViewById(R.id.eigenTime);

        //Eigen Recog:
        faceRecognizer = createEigenFaceRecognizer();
        Log.i(TAG, "okz4.1");
        notification.setText("Training Eigenface...");
        timeStart = System.currentTimeMillis();
        Log.i(TAG, "Training Eigenface...");
        faceRecognizer.train(images, labels);
        Log.i(TAG, "okz4.2");
        timeEnd = System.currentTimeMillis();
        timeElapsed = timeEnd - timeStart;
        eigenTime.setText("Eigen Time = " + timeElapsed + "ms");
        faceRecognizer.save(modelDir + "/eigenModel.xml");
        Log.i(TAG, "okz4.3");

        notification.setText("Training complete.");
        Log.i(TAG, "okz5");
        return null;
    }

    @Override
    protected void onProgressUpdate(Void... progress){
        TextView tv = (TextView)((MainActivity)c).findViewById(R.id.detectNotification);
        //tv.setText("Detected " + faceCount + " faces in " + (float) timeElapsed/1000 + "s." );
    }
}
