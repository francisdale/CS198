package com.example.dale.cs198;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.TextView;

import org.bytedeco.javacpp.opencv_core.Mat;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map.Entry;
import java.util.Set;

import static org.bytedeco.javacpp.opencv_contrib.FaceRecognizer;
import static org.bytedeco.javacpp.opencv_contrib.createEigenFaceRecognizer;
import static org.bytedeco.javacpp.opencv_imgproc.CV_BGR2GRAY;
import static org.bytedeco.javacpp.opencv_imgproc.cvtColor;

/**
 * Created by jedpatrickdatu on 2/15/2016.
 */
public class FaceRecogTask extends AsyncTask<Void, Void, Void> {

    private static final String TAG = "testMessage";

    private static final String modelFileDir = "sdcard/PresentData/eigenModel.xml";
    private static final String classesDir = "sdcard/PresentData/Classes";

    private int numPrincipalComponents;
    private final double threshold = 10.0;

    int faceCount = 0;
    int imgCount = 0;
    int numStudents;
    int numStudentsPresent = 0;
    int numUnrecognizedFaces = 0;
    long timeStart;
    long timeEnd;
    long timeElapsed;

    TaskData td;
    Context c;
    String className;

    TextView tv;

    public FaceRecogTask(TaskData td, Context c, String className){
        this.td = td;
        this.c = c;
        this.className = className;
        tv = (TextView)((CustomCamera)c).findViewById(R.id.attendanceCounter);
    }

    @Override
    protected Void doInBackground(Void... params) {
        try {

            //Read class list:
            BufferedReader br;
            HashMap<Integer, Integer> attendanceRecord = new HashMap<Integer, Integer>(); //This ArrayList is parallel with the attendance ArrayList
            HashMap<Integer, String> studentNumsAndNames = new HashMap<Integer, String>(); //Also parallel with the two ArrayLists above
            String line;
            String[] details;
            String timeStamp = new SimpleDateFormat("yyyy-MM-dd").format(new Date());

            String classDir = classesDir + "/" + className;
            String recordsDir = classDir + "/attendanceReports";
            //Filename of record is <className>_<date>.txt
            String recordFilePath = recordsDir + "/" + className + "_" + timeStamp + ".txt";
            File recordFile = new File(recordFilePath);
            if(!recordFile.exists()){
                //f.createNewFile();
                br = new BufferedReader(new FileReader(classDir + "/" + className + "_studentList.txt"));
                while((line = br.readLine()) != null){
                    details = line.split(",");
                    //a line in the studentList has the syntax: <id>,<student number>,<lastname>,<firstname>
                    attendanceRecord.put(Integer.parseInt(details[0]), 0); //(id, attendance)
                    studentNumsAndNames.put(Integer.parseInt(details[0]), details[1] + "," + details[2] + "," + details[3]); //(id, studentnum+lastname+firstname)
                }
            } else {
                br = new BufferedReader(new FileReader(recordFile));
                while((line = br.readLine()) != null){
                    details = line.split(",");
                    //a line in an attendance record text file has the syntax: <id>,<studentNumber>,<lastname>,<firstname>,<attendance>
                    attendanceRecord.put(Integer.parseInt(details[0]), Integer.parseInt(details[4])); //(id, attendance)
                    studentNumsAndNames.put(Integer.parseInt(details[0]), details[1] + "," + details[2] + "," + details[3]); //(id, studentnum+lastname+firstname)
                    if(details[4] == "1"){
                        numStudentsPresent++;
                    }
                }
            }

            numStudents = attendanceRecord.size();


            FaceRecognizer efr = createEigenFaceRecognizer();
            efr.load(modelFileDir);

            Mat mColor;
            Mat mGray;
            int predictedLabel;

            while((mColor = td.recogQueue.poll()) != null) {//This condition ends this thread and will happen when the queue returns null, meaning there are no more images coming for recognition.
                Log.i(TAG, "FaceRecogTask: Now in recognition loop.");
                mGray = new Mat();
                cvtColor(mColor, mGray, CV_BGR2GRAY);
                Log.i(TAG, "Train usage: image converted to grayscale");
                //Recognize faces:


                timeStart = System.currentTimeMillis();
                predictedLabel = efr.predict(mGray);
                timeEnd = System.currentTimeMillis();
                timeElapsed = timeEnd - timeStart;

                mColor.deallocate();
                mGray.deallocate();

                Log.i(TAG, "Recognition complete. predictedLabel = " + predictedLabel);


                if(attendanceRecord.containsKey(predictedLabel)){
                    Log.i(TAG, "predictedLabel was found in the classlist.");
                    attendanceRecord.put(predictedLabel, 1);
                } else {
                    Log.i(TAG, "Unrecognized face found.");
                    numUnrecognizedFaces++;
                }

                publishProgress();
                Log.i(TAG, "FaceRecogTask: Progress published.");
                //}

            }

            //Thread has been signalled to die at this point; time to write the attendance report .txt file.
            BufferedWriter bw = new BufferedWriter(new FileWriter(recordFile));
            Set attendanceSet = attendanceRecord.entrySet();
            Set nameSet = studentNumsAndNames.entrySet();
            Iterator ai = attendanceSet.iterator();
            Iterator ni = nameSet.iterator();
            Entry ae;
            Entry ne;
            Log.i(TAG, "Recog: Writing and saving attendance record...");
            // Display elements
            while(ai.hasNext()) {
                ae = (Entry)ai.next();
                ne = (Entry)ni.next();
                bw.write(ae.getKey() + "," + ne.getValue() + "," + ae.getValue() + "\n");
            }

            bw.flush();
            bw.close();

        } catch (Exception e) {
            e.printStackTrace();
        }
        Log.i(TAG, "Closing FaceRecogTask thread.");

        return null;
    }

    @Override
    protected void onProgressUpdate(Void... progress){
        if(td.isUIOpen()) {
            tv.setText(numStudentsPresent + "/" + numStudents + ", u" + numUnrecognizedFaces);
        }
    }
}
