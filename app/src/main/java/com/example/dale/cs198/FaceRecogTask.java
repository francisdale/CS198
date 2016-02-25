package com.example.dale.cs198;

import android.content.Context;
import android.os.AsyncTask;
import android.widget.TextView;

import org.bytedeco.javacpp.opencv_core.Mat;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.text.SimpleDateFormat;
import java.util.Date;

import static org.bytedeco.javacpp.opencv_contrib.FaceRecognizer;
import static org.bytedeco.javacpp.opencv_contrib.createEigenFaceRecognizer;

/**
 * Created by jedpatrickdatu on 2/15/2016.
 */
public class FaceRecogTask extends AsyncTask<Void, Void, Void> {

    private static final String modelFileDir = "sdcard/CS198/recognizerModels/eigenModel.xml";
    private static final String classesDir = "sdcard/CS198/Classes";
    private static final String recordDir = "sdcard/CS198/Classes/CS 32/attendanceRecords";


    TaskData td;
    Context c;
    String className;


    public FaceRecogTask(TaskData td, Context c, String className){
        this.td = td;
        this.c = c;
        this.className = className;
    }

    @Override
    protected Void doInBackground(Void... params) {

        try {

            BufferedWriter bw;
            String timeStamp = new SimpleDateFormat("yyyy-MM-dd").format(new Date());

            //Prepare the record text file:
            File f = new File(recordDir + "/" + className);
            if(f.exists()==false){
                f.mkdir();
            }

            //Filename of record is <className>_<date>.txt
            f = new File(f.getAbsolutePath() + "/" + className + "_" + timeStamp + ".txt");
            if(f.exists()==false){
                f.createNewFile();
                bw = new BufferedWriter(new FileWriter(f.getAbsoluteFile()));
                bw.write(className + "," + timeStamp);
            }


            //bw.close();
        } catch (Exception e) {
            e.printStackTrace();
        }

        FaceRecognizer efr = createEigenFaceRecognizer();
        efr.load(modelFileDir);


        Mat mColor;

        while(td.isUIOpened()){

            mColor = td.recogQueue.poll();
            if(mColor == null){
                continue; //This if statement ends this thread and is triggered when the UI thread is dead and there ar eno more images waiting to be processed.
            }
            /*
            Log.i(TAG, "Train usage: Size of detectQueue is now " + detectQueue.size() + ". Detecting faces...");
            mGray = mColor;
            Log.i(TAG, "Train usage: image polled");
            cvtColor(mColor, mGray, CV_BGR2GRAY);
            Log.i(TAG, "Train usage: image converted to grayscale");
            faces = new opencv_core.Rect();
            Log.i(TAG, "Image Mats loaded");
            //Detect faces:

            timeStart = System.currentTimeMillis();
            faceDetector.detectMultiScale(mGray, faces, 1.2, 3, 0, new opencv_core.Size(mAbsoluteFaceSize, mAbsoluteFaceSize), new opencv_core.Size());
            timeEnd = System.currentTimeMillis();
            timeElapsed = timeEnd - timeStart;


            Log.i(TAG, "Detection complete. Cropping...");
            //Crop faces:
            faceCount = faces.capacity();
            for (int i = 0; i < faceCount; i++) {
                r = faces.position(i);
                x = r.x();
                y = r.y();

                roi = new opencv_core.Rect(x, y, r.width(), r.height());
                crop = new opencv_core.Mat(mColor, roi);
                imwrite(untrainedUnlabeledCropsDir + "/" + System.currentTimeMillis() + ".jpg", crop);
            }
            publishProgress();
            Log.i(TAG, "Progress published.");
            //}
            */
        }

        return null;
    }

    @Override
    protected void onProgressUpdate(Void... progress){
        TextView tv = (TextView)((MainActivity)c).findViewById(R.id.detectNotification);
        //tv.setText("Detected " + faceCount + " faces in " + (float) timeElapsed/1000 + "s." );
    }
}
