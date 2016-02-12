package com.example.dale.cs198;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.TextView;

import org.bytedeco.javacpp.opencv_core;
import org.bytedeco.javacpp.opencv_objdetect.CascadeClassifier;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;

import static org.bytedeco.javacpp.opencv_core.Mat;
import static org.bytedeco.javacpp.opencv_core.Rect;
import static org.bytedeco.javacpp.opencv_highgui.imwrite;
import static org.bytedeco.javacpp.opencv_imgproc.CV_BGR2GRAY;
import static org.bytedeco.javacpp.opencv_imgproc.cvtColor;

/**
 * Created by jedpatrickdatu on 2/10/2016.
 */
public class FaceDetectTask extends AsyncTask<Void, Void, Void> {

    private static final String TAG = "testMessage";

    private static final String trainImgColorDir = "sdcard/cs198/unlabeledCrops";
    private static final String trainImgGrayDir = "sdcard/cs198/unlabeledCrops/grayscale";
    private static final String haarCascadeXML = "haarcascade_frontalface_default.xml";
    private int mAbsoluteFaceSize = 30;
    private static final int SCALE = 2; // scaling factor to reduce size of input image
    static final int ATTENDANCE_USAGE = 0;
    static final int TRAIN_USAGE = 1;

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
            Mat mGray = null;
            Rect faces;
            Rect r;
            Mat cropColor;
            Mat cropGray;
            Rect roi;
            int faceCount;
            int x;
            int y;


            if (usageType == ATTENDANCE_USAGE){

                GenQueue<Mat> recogQueue = td.recogQueue;
                while(td.isUIOpened()){
                    //wait();
                    while(detectQueue.hasItems()){
                        mColor = detectQueue.poll();
                        cvtColor(mColor, mGray, CV_BGR2GRAY);

                        faces = new Rect();

                        //Detect faces:
                        faceDetector.detectMultiScale(mGray, faces, 1.1, 2, 2, new opencv_core.Size(mAbsoluteFaceSize, mAbsoluteFaceSize), new opencv_core.Size());

                        //Crop faces:
                        faceCount = faces.capacity();
                        for (int i = 0; i < faceCount; i++) {
                            r = faces.position(i);
                            x = r.x();
                            y = r.y();

                            roi = new Rect(x, y, r.width(), r.height());
                            recogQueue.add(new Mat(mColor, roi));
                        }
                    }
                }
            } else if(usageType == TRAIN_USAGE){

                File folder = new File(trainImgColorDir);
                if(folder.exists()==false){
                    folder.mkdir();
                    (new File(trainImgGrayDir)).mkdir();
                }

                int numTrainImgInFolder = folder.listFiles().length;
                Log.i(TAG, "numFiles: " + numTrainImgInFolder);

                while(td.isUIOpened()){
                    //wait();
                    while(detectQueue.hasItems()){

                        mColor = detectQueue.poll();
                        cvtColor(mColor, mGray, CV_BGR2GRAY);

                        faces = new Rect();

                        //Detect faces:
                        faceDetector.detectMultiScale(mGray, faces, 1.1, 2, 2, new opencv_core.Size(mAbsoluteFaceSize, mAbsoluteFaceSize), new opencv_core.Size());

                        //Crop faces:
                        faceCount = faces.capacity();
                        for (int i = 0; i < faceCount; i++, numTrainImgInFolder++) {
                            r = faces.position(i);
                            x = r.x();
                            y = r.y();

                            roi = new Rect(x, y, r.width(), r.height());
                            cropColor = new Mat(mColor, roi);
                            cropGray = new Mat(mGray, roi);
                            imwrite(trainImgColorDir + "/" + numTrainImgInFolder + ".jpg", cropColor);
                            imwrite(trainImgGrayDir + "/" + numTrainImgInFolder + ".jpg", cropColor);
                        }
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            Log.e(TAG, "Failed to load cascade. Exception thrown: " + e);
        }
        return null;
    }

    @Override
    protected void onProgressUpdate(Void... progress){
        TextView tv;
        tv = (TextView) findViewById(R.id.detectNotification);
        tv.setText("Detection method: " + detectTypeString);

    }
}
