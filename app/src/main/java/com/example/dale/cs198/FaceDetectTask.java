package com.example.dale.cs198;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.TextView;

import org.bytedeco.javacpp.opencv_objdetect.CascadeClassifier;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Date;

import static org.bytedeco.javacpp.opencv_core.Mat;
import static org.bytedeco.javacpp.opencv_core.Rect;
import static org.bytedeco.javacpp.opencv_core.RectVector;
import static org.bytedeco.javacpp.opencv_core.Size;
import static org.bytedeco.javacpp.opencv_imgcodecs.imwrite;
import static org.bytedeco.javacpp.opencv_imgproc.CV_BGR2GRAY;
import static org.bytedeco.javacpp.opencv_imgproc.cvtColor;

/**
 * Created by jedpatrickdatu on 2/10/2016.
 */
public class FaceDetectTask extends AsyncTask<Void, Void, Void> {

    private static final String TAG = "testMessage";

    private static final String untrainedCropsDir = "sdcard/PresentData/faceDatabase/untrainedCrops";
    private static final String haarCascadeXML = "haarcascade_frontalface_default.xml";

    static final int ATTENDANCE_USAGE = 0;
    static final int TRAIN_USAGE = 1;

    //Face detection parameters:
    double scaleFactor = 1.1;
    int minNeighbors = 3;
    int flags = 0;
    Size minSize = new Size(30, 30);
    Size maxSize = new Size();

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
            RectVector faces;
            Rect r;
            Rect roi;
            int numFaces;


            if (usageType == ATTENDANCE_USAGE){
                Log.i(TAG, "Now in Attendance Usage ");


                /*
                //For testing with AT&T database:
                File[] testCrops = new File("sdcard/PresentData/att/att_faces_labeled_testing_jpg").listFiles();
                int count = 0;
                for(File f : testCrops){
                    td.detectQueue.add(imread(f.getAbsolutePath()));
                    count++;
                    Log.i(TAG, count + " images added.");
                }
                */


                while (null != (mColor = td.detectQueue.poll())) { //This condition ends this thread and will happen when the queue returns null, meaning there are no more images coming for detecting.
                    imgCount++;
                    mGray = new Mat();
                    cvtColor(mColor, mGray, CV_BGR2GRAY);
                    faces = new RectVector();

                    //Detect faces:
                    Log.i(TAG, "Detecting...");
                    timeStart = System.currentTimeMillis();
                    faceDetector.detectMultiScale(mGray, faces, scaleFactor, minNeighbors, flags, minSize, maxSize);
                    timeEnd = System.currentTimeMillis();
                    timeElapsed = timeEnd - timeStart;

                    mGray.release();

                    numFaces = (int)faces.size();

                    if(numFaces > 0) {//check if faces is not empty; an empty r means no face was really detected

                        Log.i(TAG, "Detection complete. Cropping...");
                        //Crop faces:

                        for (int i = 0; i < numFaces; i++) {
                            r = faces.get(i);

                            //roi = new Rect(r.x(), r.y(), r.width(), r.height());
                            td.recogQueue.add(new Mat(mColor, r));
                        }
                    } else {
                        numFaces = 0;
                    }

                    faceCount += numFaces;

                    Log.i(TAG, "Cropping complete. Publishing progress.");
                    Log.i(TAG, imgCount + " images detected.");
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
                    mGray = new Mat();
                    cvtColor(mColor, mGray, CV_BGR2GRAY);
                    faces = new RectVector();

                    //Detect faces:
                    Log.i(TAG, "Detecting...");
                    timeStart = System.currentTimeMillis();
                    faceDetector.detectMultiScale(mGray, faces, scaleFactor, minNeighbors, flags, minSize, maxSize);
                    timeEnd = System.currentTimeMillis();
                    timeElapsed = timeEnd - timeStart;

                    mGray.release();

                    numFaces = (int)faces.size();

                    if(numFaces > 0) {//check if faces is not empty; an empty r means no face was really detected
                        //Crop faces:

                        Log.i(TAG, "Detection complete. Found " + numFaces + " faces. Cropping...");

                        for (int i = 0; i < numFaces; i++) {
                            r = faces.get(i);

                            //roi = new Rect(r.x(), r.y(), r.width(), r.height());

                            crop = new Mat(mColor, r);

                            imwrite(untrainedCropsDir + "/" + "unlabeled_" + new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date()) + ".jpg", crop);
                        }
                    } else {
                        numFaces = 0;
                    }

                    faceCount += numFaces;


                    /*
                    //For cropping the AT&T faces:
                    File fd = new File("sdcard/PresentData/att_faces_labeled_jpg");
                    for (File f : fd.listFiles()) {
                        td.pathQueue.add(f.getAbsolutePath());
                        Log.i(TAG, "Added " + f.getName() + " to detectQueue for cropping");
                    }
                    (new File("sdcard/PresentData/att_faces_labeled_cropped_jpg")).mkdirs();

                    while(!td.pathQueue.isQueueEmpty()) {
                        String path = td.pathQueue.poll();
                        String name = (new File(path)).getName();
                        Mat m = imread(path);
                        faces = new Rect();
                        faceDetector.detectMultiScale(m, faces, scaleFactor, minNeighbors, flags, minSize, maxSize);

                        if(faces.width() > 0) {//check if faces is not empty; an empty r means no face was really detected
                            int sizeF = faces.capacity();
                            Log.i(TAG, "AT&T numOfFacesDetected: " + sizeF);
                            for (int i = 0; i < sizeF; i++) {
                                r = faces.position(i);

                                roi = new Rect(r.x(), r.y(), r.width(), r.height());

                                Log.i(TAG, "AT&T cropping...");
                                crop = new Mat(m, r);
                                Log.i(TAG, "AT&T cropped");
                                imwrite("sdcard/PresentData/att_faces_labeled_cropped_jpg/" + name.replace(".jpg", "_" + i + ".jpg"), crop);
                                Log.i(TAG, "AT&T cropped " + name.replace(".jpg", "_" + i + ".jpg") + ". x = " + r.x() + ", y = " + r.y() + ", x2 = " + (r.x() + r.width()) + ", y2 = " + (r.y() + r.height()));
                            }
                        }
                        m.release();
                    }
                    //
                    */



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
