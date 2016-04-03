package com.example.dale.cs198;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import org.bytedeco.javacpp.opencv_core;
import org.bytedeco.javacpp.opencv_core.FileStorage;
import org.bytedeco.javacpp.opencv_core.Mat;
import org.bytedeco.javacpp.opencv_core.MatVector;
import org.bytedeco.javacpp.opencv_ml.SVM;

import java.io.File;
import java.io.FilenameFilter;
import java.nio.IntBuffer;
import java.text.SimpleDateFormat;
import java.util.Date;

import static org.bytedeco.javacpp.opencv_core.CV_32FC1;
import static org.bytedeco.javacpp.opencv_core.CV_32SC1;
import static org.bytedeco.javacpp.opencv_core.CV_PCA_DATA_AS_ROW;
import static org.bytedeco.javacpp.opencv_core.PCA;
import static org.bytedeco.javacpp.opencv_core.Size;
import static org.bytedeco.javacpp.opencv_imgcodecs.CV_LOAD_IMAGE_GRAYSCALE;
import static org.bytedeco.javacpp.opencv_imgcodecs.imread;
import static org.bytedeco.javacpp.opencv_imgproc.resize;
import static org.bytedeco.javacpp.opencv_ml.ROW_SAMPLE;

;

/**
 * Created by jedpatrickdatu on 2/12/2016.
 */
public class FaceRecogTrainTask extends AsyncTask<Void, Void, Boolean> {

    private static final String TAG = "testMessage";
    private static final String untrainedCropsDir = "sdcard/PresentData/faceDatabase/untrainedCrops";
    private static final String trainedCropsDir = "sdcard/PresentData/faceDatabase/trainedCrops";
    private static final String modelDir = "sdcard/PresentData";

    private static final Size dSize = new Size(160, 160);
    int numPrincipalComponents;
    double threshold = 0.01;

    long timeStart;
    long timeEnd;
    long timeElapsed;

    Context c;

    private ProgressDialog dialog;
    Boolean isTrainingSuccess = false;



    protected void onPreExecute() {
        dialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
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

        //For SVM:
        Mat trainingMat = new Mat();

        for (File c : trainedCrops) {
            img = imread(c.getAbsolutePath(), CV_LOAD_IMAGE_GRAYSCALE);
            label = Integer.parseInt(c.getName().split("_")[0]);

            resize(img, img, dSize);

            labelsBuf.put(counter, label);
            images.put(counter, img);

            //For SVM:
            img.reshape(1, 1).convertTo(img, CV_32FC1);
            trainingMat.push_back(img);

            img.deallocate();
            counter++;
            Log.i(TAG, "trainedCrops " + counter);
        }

        for(File c : untrainedCrops) {
            img = imread(c.getAbsolutePath(), CV_LOAD_IMAGE_GRAYSCALE);
            label = Integer.parseInt(c.getName().split("_")[0]);

            resize(img, img, dSize);

            labelsBuf.put(counter, label);
            images.put(counter, img);

            //For SVM:
            img.reshape(1, 1).convertTo(img, CV_32FC1);
            trainingMat.push_back(img);

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

        int numTrainingImages = (int)images.size();
        numPrincipalComponents = numTrainingImages - 1;

        //For PCA+SVM recognition:
        trainingMat.convertTo(trainingMat, CV_32FC1);
        Mat data = new Mat();
        Mat projectedMat;
        Mat temp;
        PCA pca = new PCA(trainingMat, new Mat(), CV_PCA_DATA_AS_ROW, numPrincipalComponents);

        for(int i = 0; i < numTrainingImages; i++) {
            projectedMat = new Mat(1, numPrincipalComponents, CV_32FC1);
            Log.i(TAG, "Loop " + i + " - data now has num of rows, cols :" + data.rows() + ", " + data.cols());
            pca.project(trainingMat.row(i), projectedMat);
            temp = pca.project(trainingMat.row(i));

            Log.i(TAG, "Num of rows and cols of projection: " + temp.rows() + ", " + temp.cols());
            Log.i(TAG, "Num of rows and cols of projectedMat: " + projectedMat.rows() + ", " + projectedMat.cols());
            data.push_back(projectedMat);
        }

        Log.i(TAG, "orig mean rows = " + pca.mean().rows() + ", cols = " + pca.mean().cols());
        Log.i(TAG, "orig eigenvectors rows = " + pca.eigenvectors().rows() + ", cols = " + pca.eigenvectors().cols());
        Log.i(TAG, "orig eigenvalues rows = " + pca.eigenvalues().rows() + ", cols = " + pca.eigenvalues().cols());

        data.convertTo(data, CV_32FC1);
        SVM svm = SVM.create();
        svm.setType(SVM.C_SVC);
        svm.setKernel(SVM.LINEAR);
        //svm.setDegree(2);
        svm.setGamma(3);
        Log.i(TAG, "Training SVM...");
        Log.i(TAG, "Num of rows and cols in data: " + data.rows() + ", " + data.cols());
        Log.i(TAG, "Num of rows and cols in labels: " + labels.rows() + ", " + labels.cols());
        svm.train(data, ROW_SAMPLE, labels);
        Log.i(TAG, "SVM trained. Saving to svmModel.xml...");

        FileStorage fs = new FileStorage(modelDir + "/svmModel.xml", FileStorage.WRITE);
        svm.write(fs);
        fs.release();

        Log.i(TAG, "Saving pca to pca.xml...");
        fs = new FileStorage(modelDir + "/pca.xml", opencv_core.FileStorage.WRITE);
        pca.write(fs);
        fs.release();

        /*
        //For PCA+KNN recognition:
        FaceRecognizer faceRecognizer = createEigenFaceRecognizer(numPrincipalComponents, threshold);
        //FaceRecognizer faceRecognizer = createEigenFaceRecognizer();

        //Delete the old eigenModel xml model if there is any
        FilenameFilter eigenModelFilter = new FilenameFilter() {
            public boolean accept(File dir, String name) {
                name = name.toLowerCase();
                return name.startsWith("eigenModel") && name.endsWith(".xml");
            }
        };

        File[] modelFileDirFiles = (new File(modelDir)).listFiles(eigenModelFilter);
        if(modelFileDirFiles.length > 0) {
            modelFileDirFiles[0].delete();
        }
        */

        /*
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
        */

        isTrainingSuccess = true;
        return isTrainingSuccess;
    }

    @Override
    protected void onProgressUpdate(Void... progress){
       TextView tv = (TextView)((MainActivity)c).findViewById(R.id.detectNotification);
        //tv.setText("Detected " + faceCount + " faces in " + (float) timeElapsed/1000 + "s." );
    }
}
