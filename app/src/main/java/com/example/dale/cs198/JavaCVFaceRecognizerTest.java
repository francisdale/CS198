package com.example.dale.cs198;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.TextView;

import org.bytedeco.javacpp.opencv_core;

import java.io.File;
import java.nio.IntBuffer;

import static org.bytedeco.javacpp.opencv_contrib.FaceRecognizer;
import static org.bytedeco.javacpp.opencv_contrib.createEigenFaceRecognizer;
import static org.bytedeco.javacpp.opencv_contrib.createFisherFaceRecognizer;
import static org.bytedeco.javacpp.opencv_contrib.createLBPHFaceRecognizer;
import static org.bytedeco.javacpp.opencv_core.CV_32FC1;
import static org.bytedeco.javacpp.opencv_core.CV_32SC1;
import static org.bytedeco.javacpp.opencv_core.CV_PCA_DATA_AS_ROW;
import static org.bytedeco.javacpp.opencv_core.Mat;
import static org.bytedeco.javacpp.opencv_core.PCA;
import static org.bytedeco.javacpp.opencv_highgui.CV_LOAD_IMAGE_GRAYSCALE;
import static org.bytedeco.javacpp.opencv_highgui.imread;
import static org.bytedeco.javacpp.opencv_highgui.imwrite;
import static org.bytedeco.javacpp.opencv_ml.CvSVM;
import static org.bytedeco.javacpp.opencv_ml.CvSVMParams;

public class JavaCVFaceRecognizerTest extends AppCompatActivity {

    private static final String TAG = "testMessage";

    private static final String trainingSetDir = "sdcard/PresentData/att_faces";

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
    }

    public void recog(){
        FaceRecognizer efr = createEigenFaceRecognizer();
        FaceRecognizer ffr = createFisherFaceRecognizer();
        FaceRecognizer lfr = createLBPHFaceRecognizer();
        CvSVM sfr = new CvSVM();

        Log.i(TAG, "Does " + modelDir + "/" + eigenModelYML + " exist = " + (new File(modelDir + "/" + eigenModelYML)).exists());
        efr.load(modelDir + "/" + eigenModelYML);
        ffr.load(modelDir + "/" + fisherModelYML);
        lfr.load(modelDir + "/" + lbphModelYML);

        Log.i(TAG, "Three KNN recog loading complete");

        CvSVMParams params = new CvSVMParams();
        params = params.svm_type(CvSVM.C_SVC);
        params = params.kernel_type(CvSVM.LINEAR);
        params = params.gamma(3);
        //params = params.C(1);
        //params = params.gamma(0.001);
        //params = params.degree(3);


        //sfr.load(modelDir + "/" + svmModelXML);

        Mat trainingMat = new Mat();
        opencv_core.MatVector images = new opencv_core.MatVector(numTrainingImages);
        Mat labels = new Mat(numTrainingImages, 1, CV_32SC1);
        IntBuffer labelsBuf = labels.getIntBuffer();
        String currrSPath;
        int counter = 0;

        for(int s = 1; s <= 40; s++){
            currrSPath = trainingSetDir + "/s" + s;
            for(int i = 1; i <= 4; i++, counter++){
                Mat img = imread(currrSPath + "/" + i + ".pgm", CV_LOAD_IMAGE_GRAYSCALE);

                images.put(counter, img);
                labelsBuf.put(counter, s);
                img.reshape(1, 1).convertTo(img, CV_32FC1);
                trainingMat.push_back(img);
            }
        }
        trainingMat.convertTo(trainingMat, CV_32FC1);
        Log.i(TAG, "Number of images loaded for SVM: " + trainingMat.rows());

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

        data.convertTo(data, CV_32FC1);

        sfr.train(data, labels, new Mat(), new Mat(), params);


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
        String currSPath;
        Mat img;

        Log.i(TAG, "recog initialization complete");
        for(int s = 1; s <= 40; s++) {
            currSPath = trainingSetDir + "/s" + s;
            for (int i = 5; i <= 10; i++, numImg++) {

                Log.i(TAG, "s" + s + " i" + i);

                img = imread(currSPath + "/" + i + ".pgm", CV_LOAD_IMAGE_GRAYSCALE);
                int intNumImg = (int) numImg;

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

                timeStart = System.currentTimeMillis();
                img.reshape(1, 1).convertTo(img, CV_32FC1);
                predictedLabel = (int) sfr.predict(pca.project(img));
                timeEnd = System.currentTimeMillis();
                svmAvgTime += timeEnd - timeStart;
                if (s == predictedLabel) {
                    imwrite(svmOutputDirRight + "/" + intNumImg + "_" + s + "_" + predictedLabel + ".jpg", img);
                    svmNumRight++;
                } else {
                    imwrite(svmOutputDirWrong + "/" + intNumImg + "_" + s + "_" + predictedLabel + ".jpg", img);
                }
                Log.i(TAG, "SVM done");

            }
        }

        eigenAvgTime /= numImg;
        fisherAvgTime /= numImg;
        lbphAvgTime /= numImg;
        svmAvgTime /= numImg;

        eigenPercentAcc = (eigenNumRight/numImg) * 100;
        fisherPercentAcc = (fisherNumRight/numImg) * 100;
        lbphPercentAcc = (lbphNumRight/numImg) * 100;
        svmPercentAcc = (svmNumRight/numImg) * 100;

        TextView numImgView = (TextView) findViewById(R.id.numImg);
        TextView notification = (TextView) findViewById(R.id.recogNotification);
        TextView eigenTime = (TextView) findViewById(R.id.eigenTime);
        TextView eigenAccuracy = (TextView) findViewById(R.id.eigenAccuracy);
        TextView fisherTime = (TextView) findViewById(R.id.fisherTime);
        TextView fisherAccuracy = (TextView) findViewById(R.id.fisherAccuracy);
        TextView lbphTime = (TextView) findViewById(R.id.lbphTime);
        TextView lbphAccuracy = (TextView) findViewById(R.id.lbphAccuracy);
        TextView svmTime = (TextView) findViewById(R.id.svmTime);
        TextView svmAccuracy = (TextView) findViewById(R.id.svmAccuracy);

        Log.i(TAG, "Setting up the text fields");
        notification.setText("Recognition complete.");
        numImgView.setText("Number of Training Images: " + numImg);

        eigenTime.setText("Eigen time average= " + eigenAvgTime + "ms");
        eigenAccuracy.setText("Eigen accuracy = " + (int)eigenNumRight + "/" + (int)numImg + " = " + eigenPercentAcc + "%");
        fisherTime.setText("Fisher time average= " + fisherAvgTime + "ms");
        fisherAccuracy.setText("Fisher accuracy = " + (int)fisherNumRight + "/" + (int)numImg + " = " + fisherPercentAcc + "%");
        lbphTime.setText("LBPH time average= " + lbphAvgTime + "ms");
        lbphAccuracy.setText("LBPH accuracy = " + (int)lbphNumRight + "/" + (int)numImg + " = " + lbphPercentAcc + "%");
        svmTime.setText("SVM time average= " + svmAvgTime + "ms");
        svmAccuracy.setText("SVM accuracy = " + (int)svmNumRight + "/" + (int)numImg + " = " + svmPercentAcc + "%");

        Log.i(TAG, "Finique");
    }
}
