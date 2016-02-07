package com.example.dale.cs198;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.TextView;

import org.bytedeco.javacpp.opencv_core.FileStorage;
import org.bytedeco.javacpp.opencv_core.Mat;
import org.bytedeco.javacpp.opencv_core.MatVector;
import org.bytedeco.javacpp.opencv_ml;
import org.bytedeco.javacpp.opencv_ml.CvSVM;
import org.bytedeco.javacpp.opencv_ml.CvSVMParams;

import java.io.File;
import java.nio.IntBuffer;

import static org.bytedeco.javacpp.opencv_contrib.FaceRecognizer;
import static org.bytedeco.javacpp.opencv_contrib.createEigenFaceRecognizer;
import static org.bytedeco.javacpp.opencv_contrib.createFisherFaceRecognizer;
import static org.bytedeco.javacpp.opencv_contrib.createLBPHFaceRecognizer;
import static org.bytedeco.javacpp.opencv_core.CV_32FC1;
import static org.bytedeco.javacpp.opencv_core.CV_32SC1;
import static org.bytedeco.javacpp.opencv_core.CV_PCA_DATA_AS_ROW;
import static org.bytedeco.javacpp.opencv_core.PCA;
import static org.bytedeco.javacpp.opencv_highgui.CV_LOAD_IMAGE_GRAYSCALE;
import static org.bytedeco.javacpp.opencv_highgui.imread;
import static org.bytedeco.javacpp.opencv_highgui.imwrite;


public class JavaCVTrainFaceRecognizerTest extends AppCompatActivity {
    private static final String TAG = "testMessage";
    private static final String trainingDir = "sdcard/CS198/faceDatabase";
    private static final String modelDir = "sdcard/CS198/recognizerModels";

    long timeStart;
    long timeEnd;
    long timeElapsed;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_java_cvtrain_face_recognizer_test);
        trainAlgorithm();
    }


    public void trainAlgorithm() {
        //for AT&T face database:
        String root = trainingDir + "/att_faces";
        String currSPath;
        MatVector images = new MatVector(120);
        Mat labels = new Mat(120, 1, CV_32SC1);
        IntBuffer labelsBuf = labels.getIntBuffer();
        int counter = 0;

        (new File(modelDir)).mkdir();

        //92x112
        Mat trainingMat = new Mat();
        int ii = 0; // Current column in training_mat
        for(int s = 1; s <= 40; s++){
            currSPath = root + "/s" + s;
            for(int i = 1; i <= 3; i++, counter++){
                Mat img = imread(currSPath + "/" + i + ".pgm", CV_LOAD_IMAGE_GRAYSCALE);

                images.put(counter, img);
                labelsBuf.put(counter, s);
                Log.i(TAG, "s" + s + " i" + i);
                //Mat temp = new Mat(1,10304,CV_32FC1);
                img.reshape(1, 1).convertTo(img, CV_32FC1);
                trainingMat.push_back(img);
                //labelsFloatBuf.put(counter, s);

            }
        }
        trainingMat.convertTo(trainingMat, CV_32FC1);
        Log.i(TAG, "Number of images loaded: " + images.size());

        //int nEigens = (int)images.size()*2;
        int nEigens = (int)images.size() - 1; //Number of Eigen Vectors.

        Mat data = new Mat();
        //Mat data = new Mat(trainingMat.rows(), nEigens, CV_32FC1); //This Mat will contain all the Eigenfaces that will be used later with SVM for detection

        PCA pca = new PCA(trainingMat, new Mat(), CV_PCA_DATA_AS_ROW, nEigens);

        for(int i = 0; i < 120; i++) {
            Mat projectedMat = new Mat(1, 120, CV_32FC1);
            Log.i(TAG, "Loop " + i + " - data now has num of rows, cols :" + data.rows() + ", " + data.cols());
            pca.project(trainingMat.row(i), projectedMat);
            Mat temp = pca.project(trainingMat.row(i));

            Log.i(TAG, "Num of rows and cols of projection: " + temp.rows() + ", " + temp.cols());

            Log.i(TAG, "Num of rows and cols of projectedMat: " + projectedMat.rows() + ", " + projectedMat.cols());
            //projectedMat.reshape(1,1).convertTo(projectedMat, CV_32FC1);
            //data.push_back(temp);
            data.push_back(projectedMat);
            //projectedMat.row(0).copyTo(data.row(i));

        }

        data.convertTo(data, CV_32FC1);
        FileStorage fs = new FileStorage(modelDir + "/pca.xml", FileStorage.WRITE);
        fs.writeObj("mean", pca.mean().asCvMat());
        fs.writeObj("eigenvectors", pca.eigenvectors().asCvMat());
        fs.writeObj("eigenvalues", pca.eigenvalues().asCvMat());
        fs.release();

        Log.i(TAG, "train mean row col: " + pca.mean().rows() + ", " + pca.mean().cols());
        Log.i(TAG, "train eigenvectors row col: " + pca.eigenvectors().rows() + ", " + pca.eigenvectors().cols());
        Log.i(TAG, "train eigenvalues row col: " + pca.eigenvalues().rows() + ", " + pca.eigenvalues().cols());

        imwrite("sdcard/CS198/MeanInJTrain.jpg", pca.mean());
        imwrite("sdcard/CS198/EVectorsInJTrain.jpg",pca.eigenvectors());
        imwrite("sdcard/CS198/EValuesInJTrain.jpg",pca.eigenvalues());

        /* For Yale Database:
        File root = new File(trainingDir + "/att_faces");
        FilenameFilter imgFilter = new FilenameFilter() {
            public boolean accept(File dir, String name) {
                name = name.toLowerCase();
                return name.endsWith(".normal") || name.endsWith(".happy") || name.endsWith(".centerlight");
            }
        };

        File[] imageFiles = root.listFiles(imgFilter);
        Log.i(TAG, "okz1");
        MatVector images = new MatVector(imageFiles.length);
        Log.i(TAG, "Number of files in faceDatabase: " + imageFiles.length);
        Mat labels = new Mat(imageFiles.length, 1, CV_32SC1);
        IntBuffer labelsBuf = labels.getIntBuffer();

        int counter = 0;
        int label;

        Log.i(TAG, "okz2");

        //Renaming Yale Images to .gif:
        for (File image : imageFiles) {
            String name = image.getAbsolutePath();
            int startIndex = name.indexOf(".");
            int endIndex = name.length();
            String toBeReplaced = name.substring(startIndex, endIndex);
            Log.i(TAG, "toBeReplaced: " + toBeReplaced);
            name = name.replaceAll(toBeReplaced, ".gif");
            Log.i(TAG, "name after replacement: " + name);
            File newPath = new File(name);
            image.renameTo(newPath);
            Log.i(TAG, "newPath: " + newPath);
        }

        Log.i(TAG, "okz3");
        for (File image : imageFiles) {
            Mat img = imread(image.getAbsolutePath(), CV_LOAD_IMAGE_GRAYSCALE);

            //for Yale database:
            label = Integer.parseInt(image.getName().substring(7,9));

            images.put(counter, img);

            labelsBuf.put(counter, label);

            counter++;
        }
        */
        Log.i(TAG, "okz4");
        FaceRecognizer faceRecognizer;

        File folder = new File(modelDir);
        if(!folder.exists()){
            Log.i(TAG, modelDir + " does not exist. Creating...");
            folder.mkdir();
        }


        TextView numImg = (TextView) findViewById(R.id.numImg);
        TextView notification = (TextView) findViewById(R.id.trainingNotification);
        TextView eigenTime = (TextView) findViewById(R.id.eigenTime);
        TextView fisherTime = (TextView) findViewById(R.id.fisherTime);
        TextView lbphTime = (TextView) findViewById(R.id.lbphTime);
        TextView svmTime = (TextView) findViewById(R.id.svmTime);

        numImg.setText("Number of Training Images: " + images.size());

        //Eigen Recog:
        faceRecognizer = createEigenFaceRecognizer();
        Log.i(TAG, "okz4.1");
        notification.setText("Training Eigenface...");
        timeStart = System.currentTimeMillis();
        Log.i(TAG, "Training Eigenface...");
        faceRecognizer.train(images, labels);
        Log.i(TAG, "okz4.2");
        timeEnd = System.currentTimeMillis();
        timeElapsed = timeEnd - timeStart;
        eigenTime.setText("Eigen Time = " + timeElapsed + "ms");
        faceRecognizer.save(modelDir + "/eigenModel.xml");
        Log.i(TAG, "okz4.3");

        //Fisher Recog:
        faceRecognizer = createFisherFaceRecognizer();
        notification.setText("Training Fisherface...");
        timeStart = System.currentTimeMillis();
        Log.i(TAG, "Training Fisherface...");
        faceRecognizer.train(images, labels);
        timeEnd = System.currentTimeMillis();
        timeElapsed = timeEnd - timeStart;
        fisherTime.setText("Fisher Time = " + timeElapsed + "ms");
        faceRecognizer.save(modelDir + "/fisherModel.xml");

        //LBPH Recog:
        faceRecognizer = createLBPHFaceRecognizer();
        notification.setText("Training LBPH...");
        timeStart = System.currentTimeMillis();
        Log.i(TAG, "Training LBPH...");
        faceRecognizer.train(images, labels);
        timeEnd = System.currentTimeMillis();
        timeElapsed = timeEnd - timeStart;
        lbphTime.setText("LBPH Time = " + timeElapsed + "ms");
        faceRecognizer.save(modelDir + "/lbphModel.xml");

        //SVM Recog:

        CvSVM svm = new CvSVM();
        CvSVMParams params = new opencv_ml.CvSVMParams();
        params.svm_type(CvSVM.C_SVC);
        params.kernel_type(CvSVM.LINEAR);
        params.gamma(3);
        notification.setText("Training SVM...");
        timeStart = System.currentTimeMillis();
        Log.i(TAG, "Training SVM...");
        Log.i(TAG, "Num of rows and cols in data: " + data.rows() + ", " + data.cols());
        Log.i(TAG, "Num of rows and cols in labels: " + labels.rows() + ", " + labels.cols());
        svm.train(data, labels, new Mat(), new Mat(), params);
        Log.i(TAG, "SVM trained");
        timeEnd = System.currentTimeMillis();
        timeElapsed = timeEnd - timeStart;
        svmTime.setText("SVM Time = " + timeElapsed + "ms");
        svm.save(modelDir + "/svmModel.xml");


        notification.setText("Training complete.");
        Log.i(TAG, "okz5");

    }



}
