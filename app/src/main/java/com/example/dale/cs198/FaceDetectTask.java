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

    private static final String untrainedCropsDir = "sdcard/PresentData/faceDatabase/untrainedCrops";
    private static final String haarCascadeXML = "haarcascade_frontalface_default.xml";
    private int mAbsoluteFaceSize = 30;

    static final int ATTENDANCE_USAGE = 0;
    static final int TRAIN_USAGE = 1;

    int faceCount = 0;
    int imgCount = 0;
    long timeStart;
    long timeEnd;
    long timeElapsed;

    TaskData td;
    Context c;
    int usageType;

    TextView tv;

    public FaceDetectTask(TaskData td, Context c, int usageType){
        this.td = td;
        this.c = c;
        this.usageType = usageType;
        tv = (TextView) ((CustomCamera) c).findViewById(R.id.detectionCounter);
    }


    @Override
    protected Void doInBackground(Void... params) {
        Log.i(TAG, "FaceDetectTask doInBackground start with context " + c.toString());
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

            Mat mColor;
            Mat mGray;
            Mat crop;
            Rect faces;
            Rect r;
            Rect roi;
            int numFaces;


            if (usageType == ATTENDANCE_USAGE){
                Log.i(TAG, "Now in Attendance Usage ");
                while (null != (mColor = td.detectQueue.poll())) { //This condition ends this thread and will happen when the queue returns null, meaning there are no more images coming for detecting.
                    imgCount++;
                    mGray = null;
                    cvtColor(mColor, mGray, CV_BGR2GRAY);
                    faces = new Rect();

                    //Detect faces:
                    Log.i(TAG, "Detecting...");
                    timeStart = System.currentTimeMillis();
                    faceDetector.detectMultiScale(mGray, faces, 1.2, 4, 0, new Size(mAbsoluteFaceSize, mAbsoluteFaceSize), new Size());
                    timeEnd = System.currentTimeMillis();
                    timeElapsed = timeEnd - timeStart;


                    Log.i(TAG, "Detection complete. Cropping...");
                    //Crop faces:
                    numFaces = faces.capacity();
                    faceCount += numFaces;
                    for (int i = 0; i < numFaces; i++) {
                        r = faces.position(i);

                        roi = new Rect(r.x(), r.y(), r.width(), r.height());
                        td.recogQueue.add(new Mat(mColor, roi));
                    }
                    Log.i(TAG, "Cropping complete. Publishing progress.");
                    publishProgress();
                }
                Log.i(TAG, "Attendance camera UI closed. Goodbye!");
            } else if(usageType == TRAIN_USAGE){
                Log.i(TAG, "Now in Train Usage ");
                File folder = new File(untrainedCropsDir);
                if(folder.exists()==false){
                    folder.mkdir();
                }

                while (null != (mColor = td.detectQueue.poll())) { //This condition ends this thread and will happen when the queue returns null, meaning there are no more images coming for detecting.
                    imgCount++;
                    mGray = null;
                    cvtColor(mColor, mGray, CV_BGR2GRAY);
                    faces = new Rect();

                    //Detect faces:
                    Log.i(TAG, "Detecting...");
                    timeStart = System.currentTimeMillis();
                    faceDetector.detectMultiScale(mGray, faces, 1.2, 4, 0, new Size(mAbsoluteFaceSize, mAbsoluteFaceSize), new Size());
                    timeEnd = System.currentTimeMillis();
                    timeElapsed = timeEnd - timeStart;

                    Log.i(TAG, "Detection complete. Cropping...");
                    //Crop faces:
                    numFaces = faces.capacity();
                    faceCount += numFaces;
                    for (int i = 0; i < numFaces; i++) {
                        r = faces.position(i);
                        roi = new Rect(r.x(), r.y(), r.width(), r.height());

                        crop = new Mat(mColor, roi);
                        imwrite(untrainedCropsDir + "/" + "unlabeled_" + System.currentTimeMillis() + ".jpg", crop);
                    }

                    Log.i(TAG, "Faces cropped. Publishing progress...");
                    publishProgress();
                }
                Log.i(TAG, "Train camera UI closed. Goodbye!");
            }
        } catch (Exception e) {
            e.printStackTrace();
            Log.e(TAG, "Exception thrown at FaceDetectTask: " + e);
        }
        Log.i(TAG, "Closing FaceDetectTask thread.");
        return null;
    }

    @Override
    protected void onProgressUpdate(Void... progress){
        if(td.isUIOpen()) {
            tv.setText("f" + faceCount + "i" + imgCount);

            //tv.setText("Time elapsed: " + (float) timeElapsed/1000 + "s. Detected a total of " + faceCount + " faces from " + imgCount + " photos.");
        }

    }
}
