package com.example.dale.cs198;

import android.app.ProgressDialog;
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
import java.io.FilenameFilter;
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
    static final int TEST_USAGE = 2;
    static final int TESTTIME_USAGE = 3;
    static final int CREATEDATASET_USAGE = 4;
    int usageType;

    private static final Size dSize = new Size(64, 64);
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

    public FaceRecogTask(TaskData td, Context c, int usageType){
        this.td = td;
        this.c = c;
        this.usageType = usageType;
    }

    public FaceRecogTask(TaskData td, String className, int usageType){
        this.td = td;
        this.c = c;
        this.className = className;
        this.usageType = usageType;
    }


    private ProgressDialog dialog;


    protected void onPreExecute() {
        if(usageType == TEST_USAGE || usageType == TESTTIME_USAGE || usageType == CREATEDATASET_USAGE) {
            dialog = new ProgressDialog(c);
            dialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            dialog.setIndeterminate(true);
            dialog.setCancelable(false);
            dialog.setCanceledOnTouchOutside(false);
            dialog.setMessage("Testing face detection...");
            dialog.show();
            Log.i(TAG, "Progress dialog shown.");
        }
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

                *//*FilenameFilter eigenModelFilter = new FilenameFilter() {
                    public boolean accept(File dir, String name) {
                        name = name.toLowerCase();
                        return name.startsWith("eigenModel") || name.endsWith(".xml");
                    }
                };

                String modelFilePath = ((new File(modelDir)).listFiles(eigenModelFilter))[0].getAbsolutePath();*//*

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
                    //predictedLabel = efr.predict(mGray);

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

                String[] testClassNamesAndTestingDataSplits = {"CS 197 Classroom Data Haar20HE,11,19", "CS 133 Classroom Data Haar20HE,7,10", "CS 197 Classroom Data Haar20,11,19", "CS 133 Classroom Data Haar20,7,10", "CS 197 Classroom Data HaarHE,11,19", "CS 133 Classroom Data HaarHE,7,10", "CS 197 Classroom Data Haar,11,19", "CS 133 Classroom Data Haar,7,10"};
                String researchDir = "sdcard/PresentData/researchMode";
                String[] testClassDetails;
                String[] details;
                String className;
                int testStart;
                int testEnd;

                File tempF;
                String date;
                Mat mColorCrop;
                Mat mGrayCrop;

                for(int i = 0; i < testClassNamesAndTestingDataSplits.length; i++) {
                    testClassDetails = testClassNamesAndTestingDataSplits[i].split(",");
                    details = testClassDetails[0].split(" ");
                    className = details[0] + " " + details[1];
                    testStart = Integer.parseInt(testClassDetails[1]);
                    testEnd = Integer.parseInt(testClassDetails[2]);

                    HashMap<Integer, Integer> attendanceRecord = new HashMap<Integer, Integer>(); //This ArrayList is parallel with the attendance ArrayList
                    HashMap<Integer, String> studentNumsAndNames = new HashMap<Integer, String>(); //Also parallel with the two ArrayLists above
                    String line;
                    String classroomDataDir = researchDir + "/" + testClassDetails[0];
                    String testResultsDir =  researchDir + "/Recognition Test Results/" + testClassDetails[0];

                    File folder = new File(testResultsDir);
                    if (folder.exists()) {
                        DirectoryDeleter.deleteDir(folder);
                    }
                    folder.mkdirs();


                    File testRecordFilesDir = new File(researchDir + "/" + className + "_manualAttendanceReports");

                    BufferedReader br = new BufferedReader(new FileReader(masterListPath));
                    while ((line = br.readLine()) != null) {
                        details = line.split(",");
                        //a line in the studentList has the syntax: <id>,<student number>,<lastname>,<firstname>
                        attendanceRecord.put(Integer.parseInt(details[0]), 0); //(id, attendance)
                        studentNumsAndNames.put(Integer.parseInt(details[0]), details[1] + "," + details[2] + "," + details[3]); //(id, studentnum+lastname+firstname)
                    }

                    numStudents = attendanceRecord.size();


                    //Initializing Face Recognition:
                    String researchModelDir = "sdcard/PresentData/researchMode/recognizerModels";

                    FilenameFilter svmModelFilter = new FilenameFilter() {
                        public boolean accept(File dir, String name) {
                            name = name.toLowerCase();
                            return name.startsWith("svmModel_") && name.endsWith(".xml");
                        }
                    };

                    File[] svmModels = new File(researchModelDir).listFiles(svmModelFilter);

                    SVM[] sfrs = new SVM[svmModels.length];

                    FileStorage fs = new FileStorage(researchModelDir + "/pca_" + className + ".xml", FileStorage.READ);
                    PCA pca = new PCA();
                    pca.read(fs.root());
                    fs.release();

                    for(int s = 0; s < svmModels.length; s++) {
                        Log.i(TAG, "Loading SVM...");
                        fs = new FileStorage(researchModelDir + "/svmModel_" + className + "_allHE.xml", opencv_core.FileStorage.READ);
                        sfrs[s] = SVM.create();
                        sfrs[s].read(fs.root());
                        fs.release();
                    }


                    FilenameFilter dateFolderFilter = new FilenameFilter() {
                        public boolean accept(File dir, String name) {
                            name = name.toLowerCase();
                            return dir.isDirectory();
                        }
                    };
                    File[] dateFolders;


                    FilenameFilter imgFilter = new FilenameFilter() {
                        public boolean accept(File dir, String name) {
                            name = name.toLowerCase();
                            return name.endsWith(".jpg");
                        }
                    };
                    File[] crops;

                    crops = new File(sourceClassDataDir).listFiles(imgFilter);

                    for (int j = 0; j < numFaces; j++) {
                        Log.i(TAG, "Recognizing j " + j);

                        mGrayCrop = new Mat();
                        cvtColor(mColorCrop, mGrayCrop, CV_BGR2GRAY);

                        equalizeHist(mGrayCrop, mGrayCrop);
                        //fastNlMeansDenoising(mGray,mGray);
                        resize(mGrayCrop, mGrayCrop, dSize);
                        mGrayCrop.reshape(1, 1).convertTo(mGrayCrop, CV_32FC1);

                        predictedLabel = (int) sfr.predict(pca.project(mGrayCrop));

                        Log.i(TAG, "Recognition complete. predictedLabel = " + predictedLabel);

                        if (attendanceRecord.containsKey(predictedLabel)) {
                            Log.i(TAG, "predictedLabel was found in the classlist.");
                            if (0 == attendanceRecord.get(predictedLabel)) {
                                attendanceRecord.put(predictedLabel, 1);
                                Log.i(TAG, "predictedLabel attendance was marked.");
                            }

                            //Before saving the crop, check which secondaryID is still available:
                            secondaryID = 0;
                            do {
                                temp = studentNumsAndNames.get(predictedLabel).split(",");
                                studentName = temp[1] + "," + temp[2];
                                f = new File(dateFolderDir + "/" + predictedLabel + "_" + studentName + "_" + secondaryID + ".jpg");
                                secondaryID++;
                            } while (f.exists());

                            imwrite(f.getAbsolutePath(), mColorCrop);

                            Log.i(TAG, "Crop saved.");
                        } else if (0 == predictedLabel) {
                            Log.i(TAG, "Non face found.");

                            //Before saving the crop, check which secondaryID is still available:
                            secondaryID = 0;
                            do {
                                f = new File(dateFolderDir + "/0_nonFace_" + secondaryID + ".jpg");
                                secondaryID++;
                            } while (f.exists());

                            imwrite(f.getAbsolutePath(), mColorCrop);

                        }

                        mColorCrop.deallocate();
                        mGrayCrop.deallocate();
                    }
                    Log.i(TAG, "CreateDataSet: Face Recog Initialization complete.");


                }

            } else if (usageType == TESTTIME_USAGE) {

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


                Log.i(TAG, "SVM loaded.");

                Mat mColor;
                Mat mGray;
                int predictedLabel;
                int secondaryID;
                File f;
                String studentName;
                String[] temp;

                Log.i(TAG, "FaceRecogTask: Initialization complete.");


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

                    mColor.release();
                    mGray.release();


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

                Log.i(TAG, "Time test reading and writing:");
                //Time Test:
                timeEnd = System.currentTimeMillis();
                BufferedReader brr = new BufferedReader(new FileReader("sdcard/PresentData/researchMode/timeTest.txt"));
                String[] timeDetails = brr.readLine().split("_");
                Log.i(TAG, "Read line " + timeDetails[0] + "_" + timeDetails[1]);
                timeStart = Long.parseLong(timeDetails[0]);
                timeElapsed = timeEnd - timeStart;
                int numTestImgs = Integer.parseInt(timeDetails[1]);
                brr.close();

                BufferedWriter bww = new BufferedWriter(new FileWriter("sdcard/PresentData/researchMode/timeTest.txt"));
                bww.write("Total time for detecting and recognizing " + numTestImgs + " images: " + (float) timeElapsed / 1000 + "seconds\nAvg time = " + (float) timeElapsed / (1000 * numTestImgs) + " seconds.");
                bww.flush();
                bww.close();
            }

            Log.i(TAG, "Test recog done.");


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
