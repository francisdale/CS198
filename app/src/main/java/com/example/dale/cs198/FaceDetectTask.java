package com.example.dale.cs198;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.TextView;

import org.bytedeco.javacpp.opencv_core;
import org.bytedeco.javacpp.opencv_ml;
import org.bytedeco.javacpp.opencv_objdetect.CascadeClassifier;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.FilenameFilter;
import java.io.InputStream;
import java.util.HashMap;

import static org.bytedeco.javacpp.opencv_core.CV_32FC1;
import static org.bytedeco.javacpp.opencv_core.CvScalar;
import static org.bytedeco.javacpp.opencv_core.IplImage;
import static org.bytedeco.javacpp.opencv_core.Mat;
import static org.bytedeco.javacpp.opencv_core.Rect;
import static org.bytedeco.javacpp.opencv_core.RectVector;
import static org.bytedeco.javacpp.opencv_core.Size;
import static org.bytedeco.javacpp.opencv_core.cvPoint;
import static org.bytedeco.javacpp.opencv_imgcodecs.cvSaveImage;
import static org.bytedeco.javacpp.opencv_imgcodecs.imread;
import static org.bytedeco.javacpp.opencv_imgcodecs.imwrite;
import static org.bytedeco.javacpp.opencv_imgproc.CV_AA;
import static org.bytedeco.javacpp.opencv_imgproc.CV_BGR2GRAY;
import static org.bytedeco.javacpp.opencv_imgproc.cvRectangle;
import static org.bytedeco.javacpp.opencv_imgproc.cvtColor;
import static org.bytedeco.javacpp.opencv_imgproc.equalizeHist;
import static org.bytedeco.javacpp.opencv_imgproc.resize;

/**
 * Created by jedpatrickdatu on 2/10/2016.
 */
public class FaceDetectTask extends AsyncTask<Void, Void, Void> {

    private static final String TAG = "testMessage";

    private static final String untrainedCropsDir = "sdcard/PresentData/faceDatabase/untrainedCrops";
    private static final String haarCascadeXML = "haarcascade_frontalface_default.xml";
    //private static final String testResultsDir = "sdcard/PresentData/researchMode/faceDetectTestResults";

    static final int ATTENDANCE_USAGE = 0;
    static final int TRAIN_USAGE = 1;
    static final int TEST_USAGE = 2;
    static final int TESTTIME_USAGE = 3;
    static final int CREATEDATASET_USAGE = 4;

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


    private ProgressDialog dialog;

    public FaceDetectTask(TaskData td, Context c, int usageType){
        this.td = td;
        this.c = c;
        this.usageType = usageType;
    }

    protected void onPreExecute() {
        if(usageType == TEST_USAGE) {
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
    protected void onPostExecute(Void v) {
        if(usageType == TEST_USAGE) {
            if (dialog.isShowing()) {
                dialog.dismiss();
            }
        }
    }

    @Override
    protected Void doInBackground(Void... params) {
        Log.i(TAG, "FaceDetectTask doInBackground start");
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
            int numFaces;
            int secondaryID = 0;
            File f;


            if (usageType == ATTENDANCE_USAGE){
                Log.i(TAG, "Now in Attendance Usage ");
                tv = (TextView) ((CustomCamera) c).findViewById(R.id.detectionCounter);

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
                    equalizeHist(mGray, mGray);
                    //fastNlMeansDenoising(mGray, mGray);

                    faces = new RectVector();

                    //Detect faces:
                    Log.i(TAG, "Detecting...");
                    timeStart = System.currentTimeMillis();
                    faceDetector.detectMultiScale(mGray, faces, scaleFactor, minNeighbors, flags, minSize, maxSize);
                    timeEnd = System.currentTimeMillis();
                    timeElapsed = timeEnd - timeStart;

                    mGray.deallocate();

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

                tv = (TextView) ((CustomCamera) c).findViewById(R.id.detectionCounter);

                File folder = new File(untrainedCropsDir);
                if(folder.exists()==false){
                    folder.mkdirs();
                }


                while (null != (mColor = td.detectQueue.poll())) { //This condition ends this thread and will happen when the queue returns null, meaning there are no more images coming for detecting.
                    imgCount++;
                    mGray = new Mat();
                    cvtColor(mColor, mGray, CV_BGR2GRAY);
                    equalizeHist(mGray, mGray);

                    faces = new RectVector();

                    //Detect faces:
                    Log.i(TAG, "Detecting...");
                    timeStart = System.currentTimeMillis();
                    faceDetector.detectMultiScale(mGray, faces, scaleFactor, minNeighbors, flags, minSize, maxSize);
                    timeEnd = System.currentTimeMillis();
                    timeElapsed = timeEnd - timeStart;

                    mGray.deallocate();

                    numFaces = (int)faces.size();

                    if(numFaces > 0) {//check if faces is not empty; an empty r means no face was really detected
                        //Crop faces:

                        Log.i(TAG, "Detection complete. Found " + numFaces + " faces. Cropping...");

                        for (int i = 0; i < numFaces; i++) {
                            r = faces.get(i);

                            //roi = new Rect(r.x(), r.y(), r.width(), r.height());

                            crop = new Mat(mColor, r);

                            //Before moving the crop to untrainedCrops, find a new filename for the crop that does not conflict with a crop already in untrainedCrops.
                            secondaryID = 0;
                            do {
                                f = new File(untrainedCropsDir + "/unlabeled_" + secondaryID + ".jpg");
                                secondaryID++;
                            } while(f.exists());
                            imwrite(f.getAbsolutePath(), crop);
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

            } else if(usageType == TEST_USAGE) {
                Log.i(TAG, "Now in face detect task Test Usage ");

                String[] testClassNamesAndDataSplits = {"CS 197", "CS 133"};
                final String testClassDataDir = "sdcard/PresentData/researchMode";

                FilenameFilter imgFilter = new FilenameFilter() {
                    public boolean accept(File dir, String name) {
                        name = name.toLowerCase();
                        return name.endsWith(".jpg");
                    }
                };

                String dataFolderDir;
                String resultsFolderDir;
                File dataFolder;
                File resultsFolder;

                int numImagesInClass;
                int numFacesInClass;
                String newImgName;

                IplImage imgColor;
                int x;
                int y;

                float avgTime;
                float avgDenoiseTime;
                float avgHETime;


                for (int i = 0; i < testClassNamesAndDataSplits.length; i++) {
                    Log.i(TAG, "Running test on class " + testClassNamesAndDataSplits[i] + "...");
                    dataFolderDir = testClassDataDir + "/" + testClassNamesAndDataSplits[i] + " Classroom Data";
                    resultsFolderDir = dataFolderDir + "/greenBoxesHaar";
                    dataFolder = new File(dataFolderDir);
                    resultsFolder = new File(resultsFolderDir);

                    if (resultsFolder.exists()) {
                        DirectoryDeleter.deleteDir(resultsFolder);
                    }
                    resultsFolder.mkdirs();

                    numImagesInClass = 0;
                    numFacesInClass = 0;

                    avgTime = 0;
                    avgDenoiseTime = 0;
                    avgHETime = 0;

                    for (File imgFile : dataFolder.listFiles(imgFilter)) {
                        numImagesInClass++;
                        mColor = imread(imgFile.getAbsolutePath());
                        imgCount++;
                        mGray = new Mat();
                        cvtColor(mColor, mGray, CV_BGR2GRAY);
                        //equalizeHist(mGray, mGray);

                        /*timeStart = System.currentTimeMillis();
                        equalizeHist(mGray, mGray);
                        timeEnd = System.currentTimeMillis();
                        timeElapsed = timeEnd - timeStart;
                        avgHETime += timeElapsed;*/

                        /*timeStart = System.currentTimeMillis();
                        fastNlMeansDenoising(mGray, mGray);
                        timeEnd = System.currentTimeMillis();
                        timeElapsed = timeEnd - timeStart;
                        avgDenoiseTime += timeElapsed;*/


                        faces = new RectVector();

                        //Detect faces:
                        Log.i(TAG, "Detecting...");
                        timeStart = System.currentTimeMillis();
                        faceDetector.detectMultiScale(mGray, faces, scaleFactor, minNeighbors, flags, minSize, maxSize);
                        timeEnd = System.currentTimeMillis();
                        timeElapsed = timeEnd - timeStart;
                        avgTime += timeElapsed;

                        mGray.deallocate();

                        imgColor = new IplImage(mColor);

                        numFaces = (int) faces.size();

                        if (numFaces > 0) {//check if faces is not empty; an empty r means no face was really detected
                            //Crop faces:

                            Log.i(TAG, "Detection complete. Found " + numFaces + " faces. Boxing...");

                            for (int j = 0; j < numFaces; j++) {
                                r = faces.get(j);
                                x = r.x();
                                y = r.y();

                                cvRectangle(imgColor, cvPoint(x, y), cvPoint((x + r.width()), (y + r.height())), CvScalar.GREEN, 6, CV_AA, 0);

                                //crop = new Mat(mColor, r);
                            }
                        } else {
                            numFaces = 0;
                        }
                        newImgName = imgFile.getName() + "_" + numFaces + "facesDetected_" + (float) timeElapsed / 1000 + "sec.jpg";
                        cvSaveImage(resultsFolderDir + "/" + newImgName, imgColor);

                        numFacesInClass += numFaces;

                        mColor.deallocate();
                        mGray.deallocate();

                    }
                    avgTime = (avgTime / numImagesInClass) / 1000;
                    /*avgHETime = (avgHETime / numImagesInClass) / 1000;*/
                    //avgDenoiseTime = (avgDenoiseTime / numImagesInClass) / 1000;

                    //Write down number of faces detected:
                    BufferedWriter bw = new BufferedWriter(new FileWriter(resultsFolderDir + "/totalResults.txt"));
                    bw.write(numFacesInClass + " total faces detected in an average of " + avgTime + " seconds for each image.\n" + numImagesInClass + " images were used.");//\nAverage histogram equalization time is " + avgHETime + " seconds.");//\nAverage denoise time is " + avgDenoiseTime + " seconds.");
                    bw.flush();
                    bw.close();
                }


            } else if (usageType == TESTTIME_USAGE) {
                Log.i(TAG, "Now in TESTTIME Usage ");

                //Time Test:
                //Write down number of faces detected:

                BufferedWriter bw = new BufferedWriter(new FileWriter("sdcard/PresentData/researchMode/timeTest.txt"));
                File[] testingFolder = new File("sdcard/PresentData/researchMode/testingImages").listFiles();
                bw.write(System.currentTimeMillis() + "_" + testingFolder.length);
                bw.flush();
                bw.close();


                for (File im : testingFolder) { //This condition ends this thread and will happen when the queue returns null, meaning there are no more images coming for detecting.
                    imgCount++;
                    mColor = imread(im.getAbsolutePath());
                    mGray = new Mat();
                    cvtColor(mColor, mGray, CV_BGR2GRAY);
                    equalizeHist(mGray, mGray);

                    faces = new RectVector();

                    //Detect faces:
                    Log.i(TAG, "Detecting img " + imgCount);
                    timeStart = System.currentTimeMillis();
                    faceDetector.detectMultiScale(mGray, faces, scaleFactor, minNeighbors, flags, minSize, maxSize);
                    timeEnd = System.currentTimeMillis();
                    timeElapsed = timeEnd - timeStart;

                    mGray.deallocate();

                    numFaces = (int) faces.size();

                    if (numFaces > 0) {//check if faces is not empty; an empty r means no face was really detected

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
                    mColor.deallocate();

                    faceCount += numFaces;

                    Log.i(TAG, "Cropping complete. Publishing progress.");
                    Log.i(TAG, imgCount + " images detected.");
                }
                td.setThreadsToDie();
                Log.i(TAG, "FaceDetect Test successful.");


            } else if (usageType == CREATEDATASET_USAGE) {

                Log.i(TAG, "Now in CREATEDATASET Usage ");
                String[] classNamesAndDateParsing = {"CS 197-_-1", "CS 133- -0"};
                String researchModeDir = "sdcard/PresentData/researchMode";

                File[] images;
                FilenameFilter imgFilter = new FilenameFilter() {
                    public boolean accept(File dir, String name) {
                        name = name.toLowerCase();
                        return name.endsWith(".jpg");
                    }
                };

                //Initializing Face Recognition:
                String modelDir = "sdcard/PresentData/recognizerModels";
                String classesDir = "sdcard/PresentData/Classes";
                Size dSize = new Size(64, 64);

                //For PCA+SVM recognition:
                Log.i(TAG, "Loading SVM...");
                opencv_core.FileStorage fs = new opencv_core.FileStorage(modelDir + "/svmModel.xml", opencv_core.FileStorage.READ);
                opencv_ml.SVM sfr = opencv_ml.SVM.create();
                sfr.read(fs.root());
                fs.release();

                fs = new opencv_core.FileStorage(modelDir + "/pca.xml", opencv_core.FileStorage.READ);
                opencv_core.PCA pca = new opencv_core.PCA();
                pca.read(fs.root());
                fs.release();

                Log.i(TAG, "orig mean rows = " + pca.mean().rows() + ", cols = " + pca.mean().cols());
                Log.i(TAG, "orig eigenvectors rows = " + pca.eigenvectors().rows() + ", cols = " + pca.eigenvectors().cols());
                Log.i(TAG, "orig eigenvalues rows = " + pca.eigenvalues().rows() + ", cols = " + pca.eigenvalues().cols());


                Log.i(TAG, "SVM loaded.");

                int predictedLabel;
                String studentName;
                String[] temp;

                Log.i(TAG, "CreateDataSet: Face Recog Initialization complete.");


                String sourceClassDataDir;
                String resultDataDir;
                String dateFolderDir;

                File tempF;
                String date;
                String className;
                String[] details;
                String dateDelimiter;
                int dateIndex;

                for(int i = 0; i < classNamesAndDateParsing.length; i++) {

                    details = classNamesAndDateParsing[i].split("-");
                    className = details[0];
                    sourceClassDataDir = researchModeDir +"/" + className + " Classroom Data";
                    resultDataDir = sourceClassDataDir + " Result";
                    images = new File(sourceClassDataDir).listFiles(imgFilter);

                    dateDelimiter = details[1];
                    dateIndex = Integer.parseInt(details[2]);


                    tempF = new File(resultDataDir);
                    if(tempF.exists()){
                       DirectoryDeleter.deleteDir(tempF);
                    }
                    tempF.mkdirs();

                    //Read class list:
                    BufferedReader br;
                    HashMap<Integer, Integer> attendanceRecord = new HashMap<Integer, Integer>(); //This ArrayList is parallel with the attendance ArrayList
                    HashMap<Integer, String> studentNumsAndNames = new HashMap<Integer, String>(); //Also parallel with the two ArrayLists above
                    String line;

                    String classDir = classesDir + "/" + className;

                    try {
                        br = new BufferedReader(new FileReader(classDir + "/" + className + "_studentList.txt"));
                        while ((line = br.readLine()) != null) {
                            details = line.split(",");
                            //a line in the studentList has the syntax: <id>,<student number>,<lastname>,<firstname>
                            attendanceRecord.put(Integer.parseInt(details[0]), 0); //(id, attendance)
                            studentNumsAndNames.put(Integer.parseInt(details[0]), details[1] + "," + details[2] + "," + details[3]); //(id, studentnum+lastname+firstname)
                        }
                        br.close();
                    }catch(Exception e){
                        e.printStackTrace();
                    }


                    for (File im : images) {
                        imgCount++;

                        date = im.getName().split(dateDelimiter)[dateIndex];
                        dateFolderDir = sourceClassDataDir + "/" + date;
                        tempF = new File(dateFolderDir);
                        if(!tempF.exists()){
                            tempF.mkdirs();
                        }

                        mColor = imread(im.getAbsolutePath());
                        mGray = new Mat();
                        cvtColor(mColor, mGray, CV_BGR2GRAY);
                        equalizeHist(mGray, mGray);

                        faces = new RectVector();

                        //Detect faces:
                        Log.i(TAG, "Detecting img " + imgCount);
                        timeStart = System.currentTimeMillis();
                        faceDetector.detectMultiScale(mGray, faces, scaleFactor, minNeighbors, flags, minSize, maxSize);
                        timeEnd = System.currentTimeMillis();
                        timeElapsed = timeEnd - timeStart;

                        mGray.deallocate();

                        numFaces = (int) faces.size();

                        if (numFaces > 0) {//check if faces is not empty; an empty r means no face was really detected

                            Log.i(TAG, "Detection complete. Cropping...");
                            //Crop faces:

                            for (int j = 0; j < numFaces; j++) {
                                r = faces.get(j);

                                Log.i(TAG, "Recognizing j " + j);

                                mGray = new Mat();
                                cvtColor(new Mat(mColor, r), mGray, CV_BGR2GRAY);

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

                                    imwrite(f.getAbsolutePath(), mColor);

                                    Log.i(TAG, "Crop saved.");
                                } else if (0 == predictedLabel) {
                                    Log.i(TAG, "Non face found.");

                                    //Before saving the crop, check which secondaryID is still available:
                                    secondaryID = 0;
                                    do {
                                        f = new File(dateFolderDir + "/0_nonFace_" + secondaryID + ".jpg");
                                        secondaryID++;
                                    } while (f.exists());

                                    imwrite(f.getAbsolutePath(), mColor);

                                }

                                mGray.deallocate();
                            }
                        } else {
                            numFaces = 0;
                        }
                        mColor.deallocate();

                        faceCount += numFaces;

                        Log.i(TAG, "Cropping complete. Publishing progress.");
                        Log.i(TAG, imgCount + " images detected.");
                    }
                }
                td.setThreadsToDie();
                Log.i(TAG, "FaceDetect Test successful.");
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
