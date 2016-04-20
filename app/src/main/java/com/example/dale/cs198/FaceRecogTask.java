package com.example.dale.cs198;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.TextView;

import org.bytedeco.javacpp.opencv_core;
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

import static org.bytedeco.javacpp.opencv_core.CV_32FC1;
import static org.bytedeco.javacpp.opencv_core.FileStorage;
import static org.bytedeco.javacpp.opencv_core.PCA;
import static org.bytedeco.javacpp.opencv_core.Size;
import static org.bytedeco.javacpp.opencv_imgcodecs.imwrite;
import static org.bytedeco.javacpp.opencv_imgproc.CV_BGR2GRAY;
import static org.bytedeco.javacpp.opencv_imgproc.cvtColor;
import static org.bytedeco.javacpp.opencv_imgproc.equalizeHist;
import static org.bytedeco.javacpp.opencv_imgproc.resize;
import static org.bytedeco.javacpp.opencv_ml.SVM;

/**
 * Created by jedpatrickdatu on 2/15/2016.
 */
public class FaceRecogTask extends AsyncTask<Void, Void, Void> {

    private static final String TAG = "testMessage";


    private static final String modelDir = "sdcard/PresentData/recognizerModels";
    private static final String classesDir = "sdcard/PresentData/Classes";
    private static final String testResultsDir = "sdcard/PresentData/researchMode/faceRecogTestResults";
    private static final String testModelsDir = "sdcard/PresentData/researchMode/testRecogModels";
    private static final String masterListPath = "sdcard/PresentData/Master List.txt";

    static final int ATTENDANCE_USAGE = 0;
    private static final int TEST_USAGE = 2;
    int usageType;

    private static final Size dSize = new Size(160, 160);
    int numPrincipalComponents = 250;
    double threshold = 10000.0;

    int numStudents;
    int numStudentsPresent = 0;
    int numUnrecognizedFaces = 0;
    long timeStart;
    long timeEnd;
    long timeElapsed;

    TaskData td;
    Context c;
    String className;
    String testAlgo;

    TextView tv;

    public FaceRecogTask(TaskData td, Context c, String className, int usageType){
        this.td = td;
        this.c = c;
        this.className = className;
        this.usageType = usageType;
        tv = (TextView)((CustomCamera)c).findViewById(R.id.attendanceCounter);
    }

    public FaceRecogTask(TaskData td, int usageType, String testAlgos){
        this.td = td;
        this.testAlgo = testAlgo;
        this.usageType = TEST_USAGE;
    }

    @Override
    protected Void doInBackground(Void... params) {

        Log.i(TAG, "FaceRecogTask started.");
        try {


            if (usageType == ATTENDANCE_USAGE) {
                //Read class list:
                BufferedReader br;
                HashMap<Integer, Integer> attendanceRecord = new HashMap<Integer, Integer>(); //This ArrayList is parallel with the attendance ArrayList
                HashMap<Integer, String> studentNumsAndNames = new HashMap<Integer, String>(); //Also parallel with the two ArrayLists above
                String line;
                String[] details;
                String timeStamp = new SimpleDateFormat("MMddyyyy").format(new Date());
                //String timeStamp = new SimpleDateFormat("yyyy-MM-dd").format(new Date());

                String classDir = classesDir + "/" + className;
                String recordsDir = classDir + "/attendanceReports";
                //Filename of record is <className>_<date>.txt
                String recordCropsDirPath = recordsDir + "/" + className + "_" + timeStamp;
                String recordFilePath = recordCropsDirPath + ".txt";
                File recordCropsDirFile = new File(recordCropsDirPath);
                File recordFile = new File(recordFilePath);

                if (!recordFile.exists()) {
                    Log.i(TAG, "Record doesn't exist yet. Creating...");
                    recordCropsDirFile.mkdirs();
                    br = new BufferedReader(new FileReader(classDir + "/" + className + "_studentList.txt"));
                    while ((line = br.readLine()) != null) {
                        details = line.split(",");
                        //a line in the studentList has the syntax: <id>,<student number>,<lastname>,<firstname>
                        attendanceRecord.put(Integer.parseInt(details[0]), 0); //(id, attendance)
                        studentNumsAndNames.put(Integer.parseInt(details[0]), details[1] + "," + details[2] + "," + details[3]); //(id, studentnum+lastname+firstname)
                    }
                } else {
                    Log.i(TAG, "Record exists. Loading...");
                    br = new BufferedReader(new FileReader(recordFile));
                    while ((line = br.readLine()) != null) {
                        details = line.split(",");
                        //a line in an attendance record text file has the syntax: <id>,<studentNumber>,<lastname>,<firstname>,<attendance>
                        attendanceRecord.put(Integer.parseInt(details[0]), Integer.parseInt(details[4])); //(id, attendance)
                        studentNumsAndNames.put(Integer.parseInt(details[0]), details[1] + "," + details[2] + "," + details[3]); //(id, studentnum+lastname+firstname)
                        if (details[4].equals("1")) {
                            numStudentsPresent++;
                        }
                    }
                }


                numStudents = attendanceRecord.size();
                publishProgress();

                /*Log.i(TAG, "Loading Eigen...");
                FaceRecognizer efr = createEigenFaceRecognizer();

                FilenameFilter eigenModelFilter = new FilenameFilter() {
                    public boolean accept(File dir, String name) {
                        name = name.toLowerCase();
                        return name.startsWith("eigenModel") || name.endsWith(".xml");
                    }
                };

                String modelFilePath = ((new File(modelDir)).listFiles(eigenModelFilter))[0].getAbsolutePath();

                timeStart = System.currentTimeMillis();
                efr.load(modelDir + "/eigenModel.xml");
                timeEnd = System.currentTimeMillis();
                timeElapsed = timeEnd - timeStart;
                Log.i(TAG, "Recognizer model loaded in " + (float) timeElapsed / 1000 + "s.");
                Log.i(TAG, "Eigen loaded.");*/

                //For PCA+SVM recognition:
                Log.i(TAG, "Loading SVM...");
                FileStorage fs = new FileStorage(modelDir + "/svmModel.xml", opencv_core.FileStorage.READ);
                SVM sfr = SVM.create();
                sfr.read(fs.root());
                fs.release();

                fs = new FileStorage(modelDir + "/pca.xml", opencv_core.FileStorage.READ);
                PCA pca = new PCA();
                pca.read(fs.root());
                fs.release();

                Log.i(TAG, "orig mean rows = " + pca.mean().rows() + ", cols = " + pca.mean().cols());
                Log.i(TAG, "orig eigenvectors rows = " + pca.eigenvectors().rows() + ", cols = " + pca.eigenvectors().cols());
                Log.i(TAG, "orig eigenvalues rows = " + pca.eigenvalues().rows() + ", cols = " + pca.eigenvalues().cols());

                Mat cw = sfr.getClassWeights();
                Log.i(TAG, "cw rows: " + cw.rows() + ", cw cols: " + cw.cols());


                /*//One class svm:
                fs = new FileStorage(modelDir + "/svmModelOneClass.xml", opencv_core.FileStorage.READ);
                SVM sfrOneClass = SVM.create();
                sfrOneClass.read(fs.root());
                fs.release();*/


                Log.i(TAG, "SVM loaded.");

                Mat mColor;
                Mat mGray;
                int predictedLabel;
                int secondaryID;
                File f;
                String studentName;
                String[] temp;

                Log.i(TAG, "FaceRecogTask: Initialization complete.");
            /*
            //For testing with AT&T database:
            File[] testCrops = new File("sdcard/PresentData/att/att_faces_labeled_cropped_testing_jpg").listFiles();
            int count = 0;
            for(File c : testCrops){
                td.detectQueue.add(imread(c.getAbsolutePath()));
                count++;
                Log.i(TAG, count + " images added.");
            }
            */

                /*//For testing with CS 197 april 08:

                FilenameFilter imgFilter = new FilenameFilter() {
                    public boolean accept(File dir, String name) {
                        name = name.toLowerCase();
                        return !name.startsWith("0_") && name.endsWith(".jpg");
                    }
                };

                File[] testCrops = new File("sdcard/PresentData/researchMode/CS 197 Classroom Data/15_20160330_6").listFiles(imgFilter);
                for (File c : testCrops) {
                    td.recogQueue.add(imread(c.getAbsolutePath()));
                }*/

                //Resize input mats to same size as mats in train.
                while (null != (mColor = td.recogQueue.poll())) {//This condition ends this thread and will happen when the queue returns null, meaning there are no more images coming for recognition.
                    Log.i(TAG, "FaceRecogTask: Now in recognition loop.");

                    mGray = new Mat();
                    cvtColor(mColor, mGray, CV_BGR2GRAY);

                    Log.i(TAG, "Train usage: image converted to grayscale");

                    equalizeHist(mGray, mGray);
                    //fastNlMeansDenoising(mGray,mGray);
                    resize(mGray, mGray, dSize);
                    mGray.reshape(1, 1).convertTo(mGray, CV_32FC1);

                    predictedLabel = (int) sfr.predict(pca.project(mGray));

                    /*//Recognize faces:
                    if(1 == (predictedLabel = (int)sfrOneClass.predict(pca.project(mGray)))) {
                        Log.i(TAG, "One class svm: " + predictedLabel);
                        timeStart = System.currentTimeMillis();

                        predictedLabel = (int) sfr.predict(pca.project(mGray));

                        //predictedLabel = efr.predict(mGray);
                        timeEnd = System.currentTimeMillis();
                        timeElapsed = timeEnd - timeStart;
                    } else {
                        Log.i(TAG, "One class svm: " + predictedLabel);
                        predictedLabel = 0;
                    }*/

                    Log.i(TAG, "Recognition complete. predictedLabel = " + predictedLabel);

                    if (attendanceRecord.containsKey(predictedLabel)) {
                        Log.i(TAG, "predictedLabel was found in the classlist.");
                        if (0 == attendanceRecord.get(predictedLabel)) {
                            attendanceRecord.put(predictedLabel, 1);
                            numStudentsPresent++;
                            Log.i(TAG, "predictedLabel attendance was marked.");
                        }

                        temp = studentNumsAndNames.get(predictedLabel).split(",");
                        studentName = temp[1] + "," + temp[2];
                        //Before saving the crop, check which secondaryID is still available:
                        secondaryID = 0;
                        do {
                            temp = studentNumsAndNames.get(predictedLabel).split(",");
                            studentName = temp[1] + "," + temp[2];
                            f = new File(recordCropsDirPath + "/" + predictedLabel + "_" + studentName + "_" + secondaryID + ".jpg");
                            secondaryID++;
                        } while (f.exists());

                        imwrite(f.getAbsolutePath(), mColor);

                        Log.i(TAG, "Crop saved.");
                    } else if (0 == predictedLabel) {
                        Log.i(TAG, "Non face found.");

                        //Before saving the crop, check which secondaryID is still available:
                        secondaryID = 0;
                        do {
                            f = new File(recordCropsDirPath + "/0_nonFace_" + secondaryID + ".jpg");
                            secondaryID++;
                        } while (f.exists());

                        imwrite(f.getAbsolutePath(), mColor);
                        numUnrecognizedFaces++;

                    } else {
                        Log.i(TAG, "Urecognized face found.");

                        //Before saving the crop, check which secondaryID is still available:
                        secondaryID = 0;
                        do {
                            f = new File(recordCropsDirPath + "/unlabeled_" + secondaryID + ".jpg");
                            secondaryID++;
                        } while (f.exists());

                        imwrite(f.getAbsolutePath(), mColor);
                        numUnrecognizedFaces++;

                    }

                    mColor.deallocate();
                    mGray.deallocate();

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
                while (ai.hasNext()) {
                    ae = (Entry) ai.next();
                    ne = (Entry) ni.next();
                    bw.write(ae.getKey() + "," + ne.getValue() + "," + ae.getValue() + "\n");
                }

                bw.flush();
                bw.close();

            } else if(usageType == TEST_USAGE){

                HashMap<Integer, Integer> attendanceRecord = new HashMap<Integer, Integer>(); //This ArrayList is parallel with the attendance ArrayList
                HashMap<Integer, String> studentNumsAndNames = new HashMap<Integer, String>(); //Also parallel with the two ArrayLists above
                String line;
                String[] details;
                String[] testAlgoDetails = testAlgo.split("_");
                String testResultsForAlgoDir = testResultsDir + "/" + testAlgo;

                File folder = new File(testResultsForAlgoDir);
                if (folder.exists()) {
                    DirectoryDeleter.deleteDir(folder);
                }
                folder.mkdirs();



                File testRecordFilesDir = new File(testResultsForAlgoDir + "/attendanceRecordsOutput");
                //File trueRecordFile = new File(recordFilePath);

                BufferedReader br = new BufferedReader(new FileReader(masterListPath));
                while ((line = br.readLine()) != null) {
                    details = line.split(",");
                    //a line in the studentList has the syntax: <id>,<student number>,<lastname>,<firstname>
                    attendanceRecord.put(Integer.parseInt(details[0]), 0); //(id, attendance)
                    studentNumsAndNames.put(Integer.parseInt(details[0]), details[1] + "," + details[2] + "," + details[3]); //(id, studentnum+lastname+firstname)
                }

                numStudents = attendanceRecord.size();

            }

            }catch(Exception e){
                e.printStackTrace();
                Log.e(TAG, "Exception thrown at FaceRecogTask: " + e);
            }
            Log.i(TAG, "Closing FaceRecogTask thread.");

            return null;
        }




    @Override
    protected void onProgressUpdate(Void... progress){
        if(td.isUIOpen()) {
            tv.setText(numStudentsPresent + "/" + numStudents + ", n" + numUnrecognizedFaces);
        }
    }
}
