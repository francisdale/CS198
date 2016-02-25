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
import static org.bytedeco.javacpp.opencv_imgproc.CV_BGR2GRAY;
import static org.bytedeco.javacpp.opencv_imgproc.cvtColor;
import static org.bytedeco.javacpp.opencv_highgui.imwrite;

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


            Mat mColor;
            Mat mGray;
            Mat crop;
            Rect faces;
            Rect r;
            Rect roi;
            int x;
            int y;
            int numFaces;


            if (usageType == ATTENDANCE_USAGE){
                Log.i(TAG, "Now in Attendance Usage ");
                while(td.isUIOpened()) {
                    while (null != (mColor = td.detectQueue.poll())) { //This condition ends this thread and is triggered when the UI thread is dead and there are no more images queued up for detecting.
                        imgCount++;
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
                        faceCount += faces.capacity();
                        for (int i = 0; i < faceCount; i++) {
                            r = faces.position(i);
                            x = r.x();
                            y = r.y();

                            roi = new Rect(x, y, r.width(), r.height());
                            td.recogQueue.add(new Mat(mColor, roi));
                        }
                        publishProgress();
                    }
                }
            } else if(usageType == TRAIN_USAGE){
                Log.i(TAG, "Now in Train Usage ");
                File folder = new File(untrainedCropsDir);
                if(folder.exists()==false){
                    folder.mkdir();
                }

                //Log.i(TAG, "numFiles: " + folder.listFiles().length);

                while(td.isUIOpened()){
                    while (null != (mColor = td.detectQueue.poll())) { //This condition ends this thread and is triggered when the UI thread is dead and there are no more images queued up for detecting.
                        imgCount++;
                        Log.i(TAG, "Train usage: Size of detectQueue is now " + td.detectQueue.size() + ". Detecting faces...");
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
                        numFaces = faces.capacity();
                        faceCount += numFaces;
                        for (int i = 0; i < numFaces; i++) {
                            Log.i(TAG, "Start cropping face " + i);
                            r = faces.position(i);
                            x = r.x();
                            y = r.y();

                            Log.i(TAG, "Mid cropping face " + i);
                            roi = new Rect(x, y, r.width(), r.height());
                            crop = new Mat(mColor, roi);
                            imwrite(untrainedCropsDir + "/" + "unlabeled_" + System.currentTimeMillis() + ".jpg", crop);
                            Log.i(TAG, "End cropping face " + i);
                        }
                        Log.i(TAG, "Publishing progress...");
                        publishProgress();
                        Log.i(TAG, "Progress published.");
                    }
                }
                Log.i(TAG, "UI closed. Goodbye!");
            }
        } catch (Exception e) {
            e.printStackTrace();
            Log.e(TAG, "Exception thrown: " + e);
        }
        return null;
    }

    @Override
    protected void onProgressUpdate(Void... progress){
        if(td.isUIOpened()) {
            TextView tv = (TextView) ((CustomCamera) c).findViewById(R.id.custom_camera_status);
            tv.setText("Detected " + faceCount + " faces from " + imgCount + " photos.");
            //tv.setText("Time elapsed: " + (float) timeElapsed/1000 + "s. Detected a total of " + faceCount + " faces from " + imgCount + " photos.");
        }

    }
}
