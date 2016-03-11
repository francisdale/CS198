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
import static org.bytedeco.javacpp.opencv_core.Size;
import static org.bytedeco.javacpp.opencv_highgui.CV_LOAD_IMAGE_GRAYSCALE;
import static org.bytedeco.javacpp.opencv_highgui.imread;
import static org.bytedeco.javacpp.opencv_imgproc.resize;

/**
 * Created by jedpatrickdatu on 2/12/2016.
 */
public class FaceRecogTrainTask extends AsyncTask<Void, Void, Void> {

    private static final String TAG = "testMessage";
    private static final String untrainedCropsDir = "sdcard/PresentData/faceDatabase/untrainedCrops";
    private static final String trainedCropsDir = "sdcard/PresentData/faceDatabase/trainedCrops";
    private static final String modelDir = "sdcard/PresentData/eigenModel.xml";

    private static final int dSize = 200;
    int numPrincipalComponents;

    long timeStart;
    long timeEnd;
    long timeElapsed;

    Context c;

    public FaceRecogTrainTask(Context c){
        this.c = c;
    }

    @Override
    protected Void doInBackground(Void... params) {

        //Toast toast = Toast.makeText(c, "Training...", Toast.LENGTH_SHORT);
        //toast.show();

        //Load training images from trainedCropsDir and untrainedCropsDir
        File trainedCropsFolder = new File(trainedCropsDir);
        File untrainedCropsFolder = new File(untrainedCropsDir);

        FilenameFilter untrainedCropsImgFilter = new FilenameFilter() {
            public boolean accept(File dir, String name) {
                name = name.toLowerCase();
                return !name.startsWith("unlabeled");
            }
        };

        File[] trainedCrops = trainedCropsFolder.listFiles();
        File[] untrainedCrops = untrainedCropsFolder.listFiles(untrainedCropsImgFilter);


        if(untrainedCrops.length == 0){
            //toast = Toast.makeText(c, "No training images found.", Toast.LENGTH_SHORT);
            //toast.show();
            return null;
        }

        int numTotalCrops = trainedCrops.length + untrainedCrops.length;

        MatVector images = new MatVector(numTotalCrops);
        Mat labels = new Mat(numTotalCrops, 1, CV_32SC1);
        Mat img;
        IntBuffer labelsBuf = labels.getIntBuffer();
        int label;


        if(trainedCrops.length > 0) {
            for (int i = 0; i < trainedCrops.length; i++) {
                label = Integer.parseInt(trainedCrops[i].getName().split("_")[0]);
                img = imread(trainedCrops[i].getAbsolutePath(), CV_LOAD_IMAGE_GRAYSCALE);
                resize(img, img, new Size(dSize, dSize));

                labelsBuf.put(i, label);
                images.put(i, img);
                img.deallocate();
                Log.i(TAG, "trainedCrops i" + i);
            }
        }

        String firstWord;
        for(int i = 0; i < untrainedCrops.length; i++) {
            //Image name syntax: <id number>_<img num>.<format>
            firstWord = untrainedCrops[i].getName().split("_")[0];

            if (firstWord.equals("delete")) {
                untrainedCrops[i].delete();
            } else {
                label = Integer.parseInt(firstWord);
                img = imread(untrainedCrops[i].getAbsolutePath(), CV_LOAD_IMAGE_GRAYSCALE);
                resize(img, img, new Size(dSize, dSize));

                labelsBuf.put(i, label);
                images.put(i, img);
                untrainedCrops[i].renameTo(new File(trainedCropsDir + "/" + untrainedCrops[i].getName()));
                img.deallocate();
                Log.i(TAG, "untrainedCrops i" + i);
            }
        }

        Log.i(TAG, "Number of images loaded: " + images.size());

        FaceRecognizer faceRecognizer = createEigenFaceRecognizer();


        Log.i(TAG, "Training Eigenface...");
        timeStart = System.currentTimeMillis();
        faceRecognizer.train(images, labels);
        timeEnd = System.currentTimeMillis();
        timeElapsed = timeEnd - timeStart;
        Log.i(TAG, "Training complete.");
        faceRecognizer.save(modelDir);
        Log.i(TAG, "Training model saved.");
        //toast = Toast.makeText(c, "Training complete.", Toast.LENGTH_SHORT);
        //toast.show();

        return null;
    }

    @Override
    protected void onProgressUpdate(Void... progress){
       TextView tv = (TextView)((MainActivity)c).findViewById(R.id.detectNotification);
        //tv.setText("Detected " + faceCount + " faces in " + (float) timeElapsed/1000 + "s." );
    }
}
