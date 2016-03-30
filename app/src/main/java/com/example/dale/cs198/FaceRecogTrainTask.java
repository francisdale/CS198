package com.example.dale.cs198;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import org.bytedeco.javacpp.opencv_contrib.FaceRecognizer;
import org.bytedeco.javacpp.opencv_core.Mat;
import org.bytedeco.javacpp.opencv_core.MatVector;

import java.io.File;
import java.io.FilenameFilter;
import java.nio.IntBuffer;
import java.text.SimpleDateFormat;
import java.util.Date;

import static org.bytedeco.javacpp.opencv_contrib.createEigenFaceRecognizer;
import static org.bytedeco.javacpp.opencv_core.CV_32SC1;
import static org.bytedeco.javacpp.opencv_core.Size;
import static org.bytedeco.javacpp.opencv_highgui.CV_LOAD_IMAGE_GRAYSCALE;
import static org.bytedeco.javacpp.opencv_highgui.imread;
import static org.bytedeco.javacpp.opencv_imgproc.resize;

;

/**
 * Created by jedpatrickdatu on 2/12/2016.
 */
public class FaceRecogTrainTask extends AsyncTask<Void, Void, Boolean> {

    private static final String TAG = "testMessage";
    private static final String untrainedCropsDir = "sdcard/PresentData/faceDatabase/untrainedCrops";
    private static final String trainedCropsDir = "sdcard/PresentData/faceDatabase/trainedCrops";
    private static final String modelDir = "sdcard/PresentData";

    private static final int dSize = 160;
    int numPrincipalComponents;
    double threshold = 0.01;

    long timeStart;
    long timeEnd;
    long timeElapsed;

    Context c;

    private ProgressDialog dialog;
    Boolean isTrainingSuccess = false;



    protected void onPreExecute() {


        dialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        dialog.setIndeterminate(true);
        dialog.setCancelable(false);
        dialog.setCanceledOnTouchOutside(false);
        dialog.setMessage("Training recognizer...");
        dialog.show();
    }

    @Override
    protected void onPostExecute(Boolean isSuccess) {
        if (dialog.isShowing()) {
            dialog.dismiss();
        }

        if (isSuccess) {
            Toast.makeText(c, "Training successful.", Toast.LENGTH_LONG).show();
        } else {
            Toast.makeText(c, "Training failed. Error encountered.", Toast.LENGTH_LONG).show();
        }

    }

    public FaceRecogTrainTask(Context c){
        dialog = new ProgressDialog(c);
        this.c = c;
    }

    @Override
    protected Boolean doInBackground(Void... params) {
        String timeStamp = new SimpleDateFormat("MMddyyy-HHmmss").format(new Date());
        String modelFileName = "eigenModel_" + timeStamp + ".xml";

        //Load training images from trainedCropsDir and untrainedCropsDir
        File trainedCropsFolder = new File(trainedCropsDir);
        File untrainedCropsFolder = new File(untrainedCropsDir);

        File[] trainedCrops = trainedCropsFolder.listFiles();

        //For untrainedCrops, first delete the photos named with "delete" or "unlabeled"
        FilenameFilter untrainedCropsDeleteImgFilter = new FilenameFilter() {
            public boolean accept(File dir, String name) {
                name = name.toLowerCase();
                return name.startsWith("delete") || name.startsWith("unlabeled");
            }
        };

        File[] untrainedCrops = untrainedCropsFolder.listFiles(untrainedCropsDeleteImgFilter);
        for(File d : untrainedCrops){
            d.delete();
        }

        //Now that the deletable crops have been deleted, get the remaining untrainedCrops for training:
        untrainedCrops = untrainedCropsFolder.listFiles();

        if(untrainedCrops.length == 0 && trainedCrops.length == 0){
            //toast = Toast.makeText(c, "No training images found.", Toast.LENGTH_SHORT);
            //toast.show();
            return null;
        }

        int numTotalCrops = trainedCrops.length + untrainedCrops.length;

        Log.i(TAG, "numTotalCrops: " + numTotalCrops);

        MatVector images = new MatVector(numTotalCrops);
        Mat labels = new Mat(numTotalCrops, 1, CV_32SC1);
        Mat img;
        IntBuffer labelsBuf = labels.getIntBuffer();
        int label;
        File f;
        int counter = 0;
        int secondaryID;


        for (File c : trainedCrops) {
            img = imread(c.getAbsolutePath(), CV_LOAD_IMAGE_GRAYSCALE);
            label = Integer.parseInt(c.getName().split("_")[0]);

            resize(img, img, new Size(dSize, dSize));

            labelsBuf.put(counter, label);
            images.put(counter, img);
            img.deallocate();
            counter++;
            Log.i(TAG, "trainedCrops " + counter);
        }

        for(File c : untrainedCrops) {
            img = imread(c.getAbsolutePath(), CV_LOAD_IMAGE_GRAYSCALE);
            label = Integer.parseInt(c.getName().split("_")[0]);

            resize(img, img, new Size(dSize, dSize));

            labelsBuf.put(counter, label);
            images.put(counter, img);

            //Before moving the crop to trainedCrops, find a new filename for the crop that does not conflict with a crop already in trainedCrops.
            secondaryID = 0;
            do {
                f = new File(trainedCropsDir + "/" + label + "_" + secondaryID + ".jpg");
                secondaryID++;
            } while(f.exists());
            c.renameTo(f);

            img.deallocate();
            counter++;
            Log.i(TAG, "untrainedCrops " + counter);

        }

        Log.i(TAG, "Number of images loaded: " + images.size());

        //dialog.setMessage("Training recognizer...");

        numPrincipalComponents = (int)images.size() - 1;
        FaceRecognizer faceRecognizer = createEigenFaceRecognizer(numPrincipalComponents, threshold);
        //FaceRecognizer faceRecognizer = createEigenFaceRecognizer();

        //Delete the old eigenModel xml model if there is any
        FilenameFilter eigenModelFilter = new FilenameFilter() {
            public boolean accept(File dir, String name) {
                name = name.toLowerCase();
                return name.startsWith("eigenModel") || name.endsWith(".xml");
            }
        };

        File[] modelFileDirFiles = (new File(modelDir)).listFiles(eigenModelFilter);
        if(modelFileDirFiles.length > 0) {
            modelFileDirFiles[0].delete();
        }


        Log.i(TAG, "Training Eigenface...");
        timeStart = System.currentTimeMillis();
        faceRecognizer.train(images, labels);
        timeEnd = System.currentTimeMillis();
        timeElapsed = timeEnd - timeStart;
        Log.i(TAG, "Training completed in " + (float) timeElapsed / 1000 + "s.");

        //dialog.setMessage("Saving recognizer...");
        faceRecognizer.save(modelDir + "/" + modelFileName);
        Log.i(TAG, "Training model saved.");
        //toast = Toast.makeText(c, "Training complete.", Toast.LENGTH_SHORT);
        //toast.show();

        isTrainingSuccess = true;
        return isTrainingSuccess;
    }

    @Override
    protected void onProgressUpdate(Void... progress){
       TextView tv = (TextView)((MainActivity)c).findViewById(R.id.detectNotification);
        //tv.setText("Detected " + faceCount + " faces in " + (float) timeElapsed/1000 + "s." );
    }
}
