package com.example.dale.cs198;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.TextView;

import org.bytedeco.javacpp.opencv_objdetect.CascadeClassifier;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;

import static org.bytedeco.javacpp.opencv_core.Mat;
import static org.bytedeco.javacpp.opencv_core.Rect;
import static org.bytedeco.javacpp.opencv_core.Size;
import static org.bytedeco.javacpp.opencv_highgui.imwrite;
import static org.bytedeco.javacpp.opencv_imgproc.CV_BGR2GRAY;
import static org.bytedeco.javacpp.opencv_imgproc.cvtColor;

/**
 * Created by jedpatrickdatu on 2/10/2016.
 */
public class FaceDetectTask extends AsyncTask<Void, Void, Void> {

    private static final String TAG = "testMessage";

    private static final String untrainedUnlabeledCropsDir = "sdcard/cs198/faceDatabase/untrainedCrops/unlabeledCrops";
    private static final String haarCascadeXML = "haarcascade_frontalface_default.xml";
    private int mAbsoluteFaceSize = 30;

    static final int ATTENDANCE_USAGE = 0;
    static final int TRAIN_USAGE = 1;

    int faceCount;
    long timeStart;
    long timeEnd;
    long timeElapsed;

    TaskData td;
    Context c;
    int usageType;

    public FaceDetectTask(TaskData td, Context c, int usageType){
        this.td = td;
        this.c = c;
        this.usageType = usageType;
    }


    @Override
    protected Void doInBackground(Void... params) {

        try {
            //Initialize face detector:
            File cascadeDir = c.getDir("cascade", Context.MODE_PRIVATE);
            File cascadeFile = new File(cascadeDir, haarCascadeXML);
            FileOutputStream os = new FileOutputStream(cascadeFile);
            InputStream is = c.getResources().openRawResource(R.raw.haarcascade_frontalface_default);

            byte[] buffer = new byte[4096];
            int bytesRead;
            while ((bytesRead = is.read(buffer)) != -1) {
                os.write(buffer, 0, bytesRead);
            }
            is.close();
            os.close();
            Log.i(TAG, "cascadeFile.getAbsolutePath: " + cascadeFile.getAbsolutePath());
            Log.i(TAG, "Does the file above exist: " + (new File(cascadeFile.getAbsolutePath())).exists());

            CascadeClassifier faceDetector = new CascadeClassifier(cascadeFile.getAbsolutePath());
            cascadeDir.delete();

            GenQueue<Mat> detectQueue = td.detectQueue;
            Mat mColor;
            Mat mGray;
            Mat crop;
            Rect faces;
            Rect r;
            Rect roi;
            int x;
            int y;


            if (usageType == ATTENDANCE_USAGE){
                Log.i(TAG, "Now in Attendance Usage ");
                GenQueue<Mat> recogQueue = td.recogQueue;
                while(td.isUIOpened()){
                    mColor = detectQueue.poll();
                    if(mColor == null){
                        continue; //This if statement ends this thread and is triggered when the UI thread is dead and there ar eno more images queued up for processing.
                    }
                    mGray = mColor;
                    cvtColor(mColor, mGray, CV_BGR2GRAY);
                    faces = new Rect();

                    //Detect faces:
                    timeStart = System.currentTimeMillis();
                    faceDetector.detectMultiScale(mGray, faces, 1.2, 3, 0, new Size(mAbsoluteFaceSize, mAbsoluteFaceSize), new Size());
                    timeEnd = System.currentTimeMillis();
                    timeElapsed = timeEnd - timeStart;


                    Log.i(TAG, "Detection complete. Cropping...");
                    //Crop faces:
                    faceCount = faces.capacity();
                    for (int i = 0; i < faceCount; i++) {
                        r = faces.position(i);
                        x = r.x();
                        y = r.y();

                        roi = new Rect(x, y, r.width(), r.height());
                        recogQueue.add(new Mat(mColor, roi));
                    }
                    publishProgress();
                }

            } else if(usageType == TRAIN_USAGE){
                Log.i(TAG, "Now in Train Usage ");
                File folder = new File(untrainedUnlabeledCropsDir);
                if(folder.exists()==false){
                    folder.mkdir();
                }

                //Log.i(TAG, "numFiles: " + folder.listFiles().length);

                while(td.isUIOpened()){

                    mColor = detectQueue.poll();
                    if(mColor == null){
                        continue; //This if statement ends this thread and is triggered when the UI thread is dead and there ar eno more images queued up for processing.
                    }
                    Log.i(TAG, "Train usage: Size of detectQueue is now " + detectQueue.size() + ". Detecting faces...");
                    mGray = mColor;
                    Log.i(TAG, "Train usage: image polled");
                    cvtColor(mColor, mGray, CV_BGR2GRAY);
                    Log.i(TAG, "Train usage: image converted to grayscale");
                    faces = new Rect();
                    Log.i(TAG, "Image Mats loaded");

                    //Detect faces:
                    timeStart = System.currentTimeMillis();
                    faceDetector.detectMultiScale(mGray, faces, 1.2, 3, 0, new Size(mAbsoluteFaceSize, mAbsoluteFaceSize), new Size());
                    timeEnd = System.currentTimeMillis();
                    timeElapsed = timeEnd - timeStart;


                    Log.i(TAG, "Detection complete. Cropping...");
                    //Crop faces:
                    faceCount = faces.capacity();
                    for (int i = 0; i < faceCount; i++) {
                        r = faces.position(i);
                        x = r.x();
                        y = r.y();

                        roi = new Rect(x, y, r.width(), r.height());
                        crop = new Mat(mColor, roi);
                        imwrite(untrainedUnlabeledCropsDir + "/" + System.currentTimeMillis() + ".jpg", crop);
                    }
                    publishProgress();
                    Log.i(TAG, "Progress published.");

                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            Log.e(TAG, "Exception thrown: " + e);
        }
        return null;
    }

    @Override
    protected void onProgressUpdate(Void... progress){
        TextView tv = (TextView)((MainActivity)c).findViewById(R.id.detectNotification);
        tv.setText("Detected " + faceCount + " faces in " + (float) timeElapsed/1000 + "s." );
    }
}
