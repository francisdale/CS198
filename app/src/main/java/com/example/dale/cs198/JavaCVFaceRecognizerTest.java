package com.example.dale.cs198;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.TextView;

import org.bytedeco.javacpp.opencv_core;
import org.bytedeco.javacpp.opencv_core.FileStorage;
import org.bytedeco.javacpp.opencv_core.Mat;

import java.io.File;

import static org.bytedeco.javacpp.opencv_core.CV_32FC1;
import static org.bytedeco.javacpp.opencv_core.PCA;
import static org.bytedeco.javacpp.opencv_face.FaceRecognizer;
import static org.bytedeco.javacpp.opencv_face.createEigenFaceRecognizer;
import static org.bytedeco.javacpp.opencv_imgcodecs.CV_LOAD_IMAGE_GRAYSCALE;
import static org.bytedeco.javacpp.opencv_imgcodecs.imread;
import static org.bytedeco.javacpp.opencv_imgcodecs.imwrite;
import static org.bytedeco.javacpp.opencv_imgproc.equalizeHist;
import static org.bytedeco.javacpp.opencv_ml.SVM;


public class JavaCVFaceRecognizerTest extends AppCompatActivity {

    private static final String TAG = "testMessage";

    private static final String trainingSetDir = "sdcard/PresentData/att/att_faces";

    String modelDir = "sdcard/PresentData/researchMode/recognizerModels";
    String targetDir = "sdcard/PresentData/researchMode/recognitionResults";


    public static final String eigenModelYML = "eigenModel.xml";
    public static final String fisherModelYML = "fisherModel.xml";
    public static final String lbphModelYML = "lbphModel.xml";
    public static final String svmModelXML = "svmModel.xml";

    String eigenOutputDirRight = targetDir + "/eigenRecog/right";
    String eigenOutputDirWrong = targetDir + "/eigenRecog/wrong";
    String fisherOutputDirRight = targetDir + "/fisherRecog/right";
    String fisherOutputDirWrong = targetDir + "/fisherRecog/wrong";
    String lbphOutputDirRight = targetDir + "/lbphRecog/right";
    String lbphOutputDirWrong = targetDir + "/lbphRecog/wrong";
    String svmOutputDirRight = targetDir + "/svmRecog/right";
    String svmOutputDirWrong = targetDir + "/svmRecog/wrong";

    int numTrainingImages = 160;

    String filepath;

    long timeStart;
    long timeEnd;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_java_cvface_recognizer_test);

        /*
        ProgressDialog dialog = new ProgressDialog(this);
        dialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        dialog.setIndeterminate(true);
        dialog.setCancelable(false);
        dialog.setCanceledOnTouchOutside(false);
        dialog.setMessage("Testing recognizer...");
        dialog.show();
        */

        Log.i(TAG, "onCreate na ng recognizer");

        Intent intent = getIntent();
        filepath = intent.getStringExtra("filepath");

        /*
        String[] stringArray = filepath.split("/");
        imgName = stringArray[stringArray.length - 1];
        imgName = imgName.substring(0, imgName.length() - 4);
        Log.i(TAG, "filepath: " + filepath);
        Log.i(TAG, "imgName: " + imgName);
        */
        Log.i(TAG, "Creating folders:");

        //mkdir does not overwrite the directory if it already exists.


        File folder = new File(eigenOutputDirRight);
        if(!folder.exists()){
            folder.mkdirs();
        }

        folder = new File(eigenOutputDirWrong);
        if(!folder.exists()){
            folder.mkdirs();
        }

        folder = new File(fisherOutputDirRight);
        if(!folder.exists()){
            folder.mkdirs();
        }

        folder = new File(fisherOutputDirWrong);
        if(!folder.exists()){
            folder.mkdirs();
        }

        folder = new File(lbphOutputDirRight);
        if(!folder.exists()){
            folder.mkdirs();
        }

        folder = new File(lbphOutputDirWrong);
        if(!folder.exists()){
            folder.mkdirs();
        }

        folder = new File(svmOutputDirRight);
        if(!folder.exists()){
            folder.mkdirs();
        }

        folder = new File(svmOutputDirWrong);
        if(!folder.exists()){
            folder.mkdirs();
        }

        Log.i(TAG, "onCreate initialization complete");
        recog();

        //dialog.dismiss();
    }

    public void recog(){
        Log.i(TAG, "Now in recog(). Loading eigenModel...");
        FaceRecognizer efr = createEigenFaceRecognizer();
        efr.load(modelDir + "/eigenModel.xml");

        Log.i(TAG, "eigenModel loaded. Loading SVM...");

        FileStorage fs = new FileStorage(modelDir + "/svmModel.xml", opencv_core.FileStorage.READ);
        SVM sfr = SVM.create();
        sfr.read(fs.root());
        fs.release();

        fs = new FileStorage(modelDir + "/pca.xml", opencv_core.FileStorage.READ);
        PCA pca = new PCA();
        pca.read(fs.root());
        fs.release();

        Log.i(TAG, "SVM and PCA loaded.");

        /*
        Log.i(TAG, "Setting SVM params...");
        CvSVMParams params = new CvSVMParams();
        params = params.svm_type(CvSVM.C_SVC);
        params = params.kernel_type(CvSVM.LINEAR);
        params = params.gamma(3);
        Log.i(TAG, "SVM params set.");
        //params = params.C(1);
        //params = params.gamma(0.001);
        //params = params.degree(3);

        /*
        FaceRecognizer ffr = createFisherFaceRecognizer();
        FaceRecognizer lfr = createLBPHFaceRecognizer();
        CvSVM sfr = new CvSVM();

        Log.i(TAG, "Does " + modelDir + "/" + eigenModelYML + " exist = " + (new File(modelDir + "/" + eigenModelYML)).exists());

        efr.load(modelDir + "/" + eigenModelYML);
        ffr.load(modelDir + "/" + fisherModelYML);
        lfr.load(modelDir + "/" + lbphModelYML);

        Log.i(TAG, "Three KNN recog loading complete");
        */

        /*
        Log.i(TAG, "Training SVM...");
        Mat trainingMat = new Mat();
        opencv_core.MatVector images = new opencv_core.MatVector(numTrainingImages);
        Mat labels = new Mat(numTrainingImages, 1, CV_32SC1);
        IntBuffer labelsBuf = labels.getIntBuffer();
        int counter = 0;

        Log.i(TAG, "Reshaping images to 1,1...");
        for(int s = 1; s <= 40; s++){
            for(int i = 1; i <= 4; i++, counter++){
                Mat img = imread(trainingSetDir + "/s" + s + "/" + i + ".pgm", CV_LOAD_IMAGE_GRAYSCALE);

                images.put(counter, img);
                labelsBuf.put(counter, s);
                img.reshape(1, 1).convertTo(img, CV_32FC1);
                trainingMat.push_back(img);
                Log.i(TAG, "image s" + s + "i" + i + " reshaped.");
            }
        }
        trainingMat.convertTo(trainingMat, CV_32FC1);
        Log.i(TAG, "Number of images loaded for SVM: " + trainingMat.rows());
        Log.i(TAG, "Images reshaped to 1,1.");

        int nEigens = trainingMat.rows() - 1; //Number of Eigen Vectors.

        PCA pca = new PCA(trainingMat, new Mat(), CV_PCA_DATA_AS_ROW, nEigens);

        Mat data = new Mat();
        for(int i = 0; i < numTrainingImages; i++) {
            Mat projectedMat = new Mat(1, nEigens, CV_32FC1);
            Log.i(TAG, "Loop " + i + " - data now has num of rows, cols :" + data.rows() + ", " + data.cols());
            pca.project(trainingMat.row(i), projectedMat);
            Mat temp = pca.project(trainingMat.row(i));

            Log.i(TAG, "Num of rows and cols of projection: " + temp.rows() + ", " + temp.cols());

            Log.i(TAG, "Num of rows and cols of projectedMat: " + projectedMat.rows() + ", " + projectedMat.cols());

            data.push_back(projectedMat);

        }
        Log.i(TAG, "PCA data set.");

        data.convertTo(data, CV_32FC1);
        */

        //sfr.train(data, labels, new Mat(), new Mat(), params);

        /*
        Log.i(TAG, "SVM model loaded");
        FileStorage fs = new FileStorage(modelDir + "/pca.xml", FileStorage.READ);
        Log.i(TAG, "File storage loaded");
        PCA pca = new PCA();
        Log.i(TAG, "PCA initialized");
        //pca.mean(new Mat(new CvMat(fs.get("mean"))));
        pca.mean(imread("sdcard/CS198/MeanInJTrain.jpg"));
        Log.i(TAG, "mean loaded");
        //pca.eigenvectors(new Mat(new CvMat(fs.get("eigenvectors"))));
        pca.eigenvectors(imread("sdcard/CS198/EVectorsInJTrain.jpg"));
        Log.i(TAG, "eigenvectors loaded");
        //pca.eigenvalues(new Mat(new CvMat(fs.get("eigenvalues"))));
        pca.eigenvalues(imread("sdcard/CS198/EValuesInJTrain.jpg"));
        Log.i(TAG, "eigenvalues loaded");
        fs.release();
        Log.i(TAG, "fs released");

        Log.i(TAG, "recog mean row col: " + pca.mean().rows() + ", " + pca.mean().cols());
        Log.i(TAG, "recog eigenvectors row col: " + pca.eigenvectors().rows() + ", " + pca.eigenvectors().cols());
        Log.i(TAG, "recog eigenvalues row col: " + pca.eigenvalues().rows() + ", " + pca.eigenvalues().cols());

        imwrite("sdcard/CS198/MeanInJRecog.jpg", pca.mean());
        imwrite("sdcard/CS198/EVectorsInJRecog.jpg", pca.eigenvectors());
        imwrite("sdcard/CS198/EValuesInJRecog.jpg", pca.eigenvalues());
        */
        Log.i(TAG, "SVM loading complete");

        float eigenAvgTime = 0;
        float fisherAvgTime = 0;
        float lbphAvgTime = 0;
        float svmAvgTime = 0;

        float eigenNumRight = 0;
        float fisherNumRight = 0;
        float lbphNumRight = 0;
        float svmNumRight = 0;

        float eigenPercentAcc;
        float fisherPercentAcc;
        float lbphPercentAcc;
        float svmPercentAcc;

        float numImg = 0;


        //Load and recognize AT&T faces
        int predictedLabel;
        float confidence;
        String currSPath;
        Mat img;
        Mat projectedImg;

        Log.i(TAG, "recog initialization complete");


        for(int s = 1; s <= 40; s++) {
            for (int i = 5; i <= 10; i ++, numImg++) {

                Log.i(TAG, "s" + s + " i" + i);

                img = imread(trainingSetDir + "/s" + s + "/" + i + ".pgm", CV_LOAD_IMAGE_GRAYSCALE);
                int intNumImg = (int) numImg;

                equalizeHist(img, img);


                timeStart = System.currentTimeMillis();
                predictedLabel = efr.predict(img);
                timeEnd = System.currentTimeMillis();
                eigenAvgTime += timeEnd - timeStart;
                if (s == predictedLabel) {
                    imwrite(eigenOutputDirRight + "/" + intNumImg + "_" + s + "_" + predictedLabel + ".jpg", img);
                    eigenNumRight++;
                } else {
                    imwrite(eigenOutputDirWrong + "/" + intNumImg + "_" + s + "_" + predictedLabel + ".jpg", img);
                }
                Log.i(TAG, "Eigen done");

                /*
                timeStart = System.currentTimeMillis();
                predictedLabel = ffr.predict(img);
                timeEnd = System.currentTimeMillis();
                fisherAvgTime += timeEnd - timeStart;
                if (s == predictedLabel) {
                    imwrite(fisherOutputDirRight + "/" + intNumImg + "_" + s + "_" + predictedLabel + ".jpg", img);
                    fisherNumRight++;
                } else {
                    imwrite(fisherOutputDirWrong + "/" + intNumImg + "_" + s + "_" + predictedLabel + ".jpg", img);
                }
                Log.i(TAG, "Fisher done");


                timeStart = System.currentTimeMillis();
                predictedLabel = lfr.predict(img);
                timeEnd = System.currentTimeMillis();
                lbphAvgTime += timeEnd - timeStart;
                if (s == predictedLabel) {
                    imwrite(lbphOutputDirRight + "/" + intNumImg + "_" + s + "_" + predictedLabel + ".jpg", img);
                    lbphNumRight++;
                } else {
                    imwrite(lbphOutputDirWrong + "/" + intNumImg + "_" + s + "_" + predictedLabel + ".jpg", img);
                }
                Log.i(TAG, "lbph done");
                */

                timeStart = System.currentTimeMillis();
                img.reshape(1, 1).convertTo(img, CV_32FC1);
                projectedImg = pca.project(img);
                predictedLabel = (int) sfr.predict(projectedImg);
                timeEnd = System.currentTimeMillis();
                svmAvgTime += timeEnd - timeStart;
                if (s == predictedLabel) {
                    imwrite(svmOutputDirRight + "/" + intNumImg + "_" + s + "_" + predictedLabel + ".jpg", img);
                    svmNumRight++;
                } else {
                    imwrite(svmOutputDirWrong + "/" + intNumImg + "_" + s + "_" + predictedLabel + ".jpg", img);
                }
                Log.i(TAG, "SVM prediction: Correct label = " + s + ", predictedLabel = " + predictedLabel);
                Log.i(TAG, "SVM done");
            }
        }


        /*
        //For testing different numbers of PCs:
        float[] eigenTimes = new float[21];
        float[] eigenAccuracies = new float[21];
        int[] numsRight = new int[21];
        int[] numsFalseNegatives = new int[21];
        int index = 0;
        int numImages = 0;

        for(int j = 0; j <= 200; j += 10, index++) {
            efr = createEigenFaceRecognizer();
            efr.load(modelDir + "/eigenModel_" + j + ".xml");
            eigenTimes[index] = 0;
            numsRight[index] = 0;
            numsFalseNegatives[index] = 0;
            numImages = 0;
            for (int s = 1; s <= 40; s++) {
                for (int i = 5; i <= 10; i++, numImages++) {

                    Log.i(TAG, "PCs: " + j + ", s" + s + " i" + i);

                    img = imread(trainingSetDir + "/" + s + "_" + i + ".jpg", CV_LOAD_IMAGE_GRAYSCALE);

                    timeStart = System.currentTimeMillis();
                    predictedLabel = efr.predict(img);
                    timeEnd = System.currentTimeMillis();
                    eigenTimes[index] += timeEnd - timeStart;
                    if (s == predictedLabel) {
                        numsRight[index]++;
                    } else if (s == -1){
                        numsFalseNegatives[index]++;
                    }
                }
            }
            eigenTimes[index] =  (eigenTimes[index]/(numImages))/1000;
            eigenAccuracies[index] = ((float) numsRight[index]/numImages) * 100;
        }

        TextView numImgViewz = (TextView) findViewById(R.id.numImg);
        TextView notificationz = (TextView) findViewById(R.id.recogNotification);
        TextView timesAndAccuraciesTextViewz = (TextView) findViewById(R.id.recogTimesAndAccuraciesTextView);

        try {
            BufferedWriter bw = new BufferedWriter(new FileWriter(targetDir + "/eigenTimesAndAcccuracies.txt"));
            Log.i(TAG, "Setting up the text fields");
            notificationz.setText("Recognition complete.");
            numImgViewz.setText("Number of Testing Images = " + numImages);
            bw.write("Recognition complete.\nNumber of Testing Images = " + numImages + "\n\n");

            for(int i = 0; i < 21; i++) {
                Log.i(TAG, "Writing eigen" + i);
                timesAndAccuraciesTextViewz.setText(timesAndAccuraciesTextViewz.getText() + "PCs= " + (i*10) + ", avgTime= " + eigenTimes[i] + "s\nacc= " + eigenAccuracies[i]+ "%\nFalse negatives = " + numsFalseNegatives[i] + "\n\n");
                bw.write("PCs= " + (i*10) + ", avgTime= " + eigenTimes[i] + "s\nacc= " + eigenAccuracies[i]+ "%\nFalse negatives = " + numsFalseNegatives[i] + "\n\n");
            }

            bw.flush();
            bw.close();
        } catch (Exception e){
            Log.e(TAG, "Exception thrown at JavaCVFaceRecognizerTest: " + e);
        }

        */

        eigenAvgTime /= numImg*1000;
        fisherAvgTime /= numImg*1000;
        lbphAvgTime /= numImg*1000;
        svmAvgTime /= numImg*1000;

        eigenPercentAcc = (eigenNumRight/numImg) * 100;
        fisherPercentAcc = (fisherNumRight/numImg) * 100;
        lbphPercentAcc = (lbphNumRight/numImg) * 100;
        svmPercentAcc = (svmNumRight/numImg) * 100;

        TextView numImgView = (TextView) findViewById(R.id.numImg);
        TextView notification = (TextView) findViewById(R.id.recogNotification);
        TextView timesAndAccuraciesTextView = (TextView) findViewById(R.id.recogTimesAndAccuraciesTextView);

        Log.i(TAG, "Setting up the text fields");
        notification.setText("Recognition complete.");
        numImgView.setText("Number of Training Images: " + numImg);

        timesAndAccuraciesTextView.setText("Eigen time average = " + eigenAvgTime + "s\nEigen accuracy = " + eigenPercentAcc + "%\n\n");
        timesAndAccuraciesTextView.setText(timesAndAccuraciesTextView.getText() + "SVM time average = " + svmAvgTime + "s\nSVM accuracy = " + svmPercentAcc + "%\n\n");

        /*
        eigenAccuracy.setText("Eigen accuracy = " + (int)eigenNumRight + "/" + (int)numImg + " = " + eigenPercentAcc + "%");
        fisherTime.setText("Fisher time average= " + fisherAvgTime + "ms");
        fisherAccuracy.setText("Fisher accuracy = " + (int)fisherNumRight + "/" + (int)numImg + " = " + fisherPercentAcc + "%");
        lbphTime.setText("LBPH time average= " + lbphAvgTime + "ms");
        lbphAccuracy.setText("LBPH accuracy = " + (int)lbphNumRight + "/" + (int)numImg + " = " + lbphPercentAcc + "%");
        svmTime.setText("SVM time average= " + svmAvgTime + "ms");
        svmAccuracy.setText("SVM accuracy = " + (int)svmNumRight + "/" + (int)numImg + " = " + svmPercentAcc + "%");
        */

        Log.i(TAG, "Finique");
    }
}
