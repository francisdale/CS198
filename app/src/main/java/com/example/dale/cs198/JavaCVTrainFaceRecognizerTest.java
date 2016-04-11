package com.example.dale.cs198;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.TextView;

import org.bytedeco.javacpp.opencv_core.FileStorage;
import org.bytedeco.javacpp.opencv_core.Mat;
import org.bytedeco.javacpp.opencv_core.MatVector;

import java.io.File;
import java.nio.IntBuffer;

import static org.bytedeco.javacpp.opencv_core.CV_32FC1;
import static org.bytedeco.javacpp.opencv_core.CV_32SC1;
import static org.bytedeco.javacpp.opencv_core.CV_PCA_DATA_AS_ROW;
import static org.bytedeco.javacpp.opencv_core.PCA;
import static org.bytedeco.javacpp.opencv_core.Size;
import static org.bytedeco.javacpp.opencv_face.FaceRecognizer;
import static org.bytedeco.javacpp.opencv_face.createEigenFaceRecognizer;
import static org.bytedeco.javacpp.opencv_imgcodecs.CV_LOAD_IMAGE_GRAYSCALE;
import static org.bytedeco.javacpp.opencv_imgcodecs.imread;
import static org.bytedeco.javacpp.opencv_imgproc.equalizeHist;
import static org.bytedeco.javacpp.opencv_ml.ROW_SAMPLE;
import static org.bytedeco.javacpp.opencv_ml.SVM;
import static org.bytedeco.javacpp.opencv_ml.TrainData;

public class JavaCVTrainFaceRecognizerTest extends AppCompatActivity {
    private static final String TAG = "testMessage";
    private static final String trainingSetDir = "sdcard/PresentData/att/att_faces";
    private static final String modelDir = "sdcard/PresentData/researchMode/recognizerModels";

    private static double threshold = 5000.0;
    private static final Size dSize = new Size(160, 160);

    int numTrainingImages = 160;
    int numPrincipalComponents = 250;

    long timeStart;
    long timeEnd;
    long timeElapsed;
    long timeElapsedSVM;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_java_cvtrain_face_recognizer_test);
        trainAlgorithm();
    }


    public void trainAlgorithm() {
        //for AT&T face database:
        MatVector images = new MatVector(numTrainingImages);
        Mat labels = new Mat(numTrainingImages, 1, CV_32SC1);
        IntBuffer labelsBuf = labels.getIntBuffer();
        int counter = 0;
        Mat img;

        //For SVM:
        Mat trainingMat = new Mat();

        /*//For training with classroom data:
        File trainedCropsFolder = new File("sdcard/PresentData/faceDatabase/trainedCrops");
        File[] trainedCrops = trainedCropsFolder.listFiles();
        images = new MatVector(trainedCrops.length);
        labels = new Mat(trainedCrops.length, 1, CV_32SC1);

        labelsBuf = labels.getIntBuffer();
        int label;
        for (File c : trainedCrops) {
            img = imread(c.getAbsolutePath(), CV_LOAD_IMAGE_GRAYSCALE);
            label = Integer.parseInt(c.getName().split("_")[0]);

            resize(img, img, dSize);

            labelsBuf.put(counter, label);
            images.put(counter, img);

            //For SVM:
            img.reshape(1, 1).convertTo(img, CV_32FC1);
            trainingMat.push_back(img);

            img.deallocate();
            counter++;
            Log.i(TAG, "trainedCrops " + counter);
        }*/

        (new File(modelDir)).mkdirs();

        //Image resolution92x112
        for(int s = 1; s <= 40; s++){
            for(int i = 1; i <= 4; i++, counter++){
                img = imread(trainingSetDir + "/s" + s + "/" + i + ".pgm", CV_LOAD_IMAGE_GRAYSCALE);
                //resize(img, img, dSize);

                equalizeHist(img, img);

                images.put(counter, img);
                labelsBuf.put(counter, s);

                img.reshape(1, 1).convertTo(img, CV_32FC1);
                trainingMat.push_back(img);

                img.deallocate();

                Log.i(TAG, "s" + s + " i" + i);
            }
        }


        /*
        //For stitching up a big matrix of faces:

        //For training:
        int sbWidth = 92;
        int sbHeight = 112;
        int cWidth = sbWidth*4;
        int cHeight = sbHeight*40;

        Vector<CvMat> smallBlocks = new Vector<CvMat>(numTrainingImages);
        for(int s = 1; s <= 40; s++) {
            for (int i = 1; i <= 7; i += 2, counter++) {
                CvMat img = cvLoadImageM(trainingSetDir + "/" + s + "_" + i + ".jpg", CV_LOAD_IMAGE_GRAYSCALE);
                smallBlocks.add(counter, img);
            }
        }

        Mat combined = new Mat(cWidth, cHeight, smallBlocks.get(0).type());
        long smallBlocksSize = smallBlocks.size();


        for( int i = 0; i < smallBlocksSize; i++ )
        {
            for  ( int y = 0; y < cHeight; y += sbHeight)
            {
                for  ( int  x= 0 ; x < cWidth; x += sbWidth)
                {
                    opencv_core.IplImage imgz = new opencv_core.IplImage();
                    BufferedImage bi = imgz.getBufferedImage();
                    // get the correct slice
                    CvMat roi = new Mat(combined, new Rect(x, y, sbWidth, sbHeight));

                    smallBlocks.get(i).copyTo(roi);
                }
            }
        }

        imwrite("sdcard/PresentData/att/att_trainingSetMatrix.jpg", combined);
        */

        /*
        //For saving to jpg and labelling:
        (new File("sdcard/PresentData/att_faces_labeled_jpg")).mkdirs();

        for(int s = 1; s <= 40; s++){
            currSPath = trainingSetDir + "/s" + s;
            for(int i = 1; i <= 10; i++, counter++){
                Mat imgj = imread(currSPath + "/" + i + ".pgm");
                imwrite("sdcard/PresentData/att_faces_labeled_jpg/" + s + "_" + i + ".jpg", imgj);
                //imgj.deallocate();

                Log.i(TAG, "label s" + s + " i" + i);
            }
        }
        */

        //For training SVM:
        trainingMat.convertTo(trainingMat, CV_32FC1);
        Log.i(TAG, "Number of images loaded: " + images.size());

        //For training SVM:
        Mat data = new Mat();
        Mat projectedMat;
        Mat temp;
        PCA pca = new PCA(trainingMat, new Mat(), CV_PCA_DATA_AS_ROW, numPrincipalComponents);
        int numTrainingMatRows = trainingMat.rows();

        timeStart = System.currentTimeMillis();
        for(int i = 0; i < numTrainingMatRows; i++) {
            projectedMat = new Mat(1, numPrincipalComponents, CV_32FC1);
            Log.i(TAG, "Loop " + i + " - data now has num of rows, cols :" + data.rows() + ", " + data.cols());
            pca.project(trainingMat.row(i), projectedMat);
            temp = pca.project(trainingMat.row(i));

            Log.i(TAG, "Num of rows and cols of projection: " + temp.rows() + ", " + temp.cols());
            Log.i(TAG, "Num of rows and cols of projectedMat: " + projectedMat.rows() + ", " + projectedMat.cols());
            data.push_back(projectedMat);
        }
        timeEnd = System.currentTimeMillis();
        timeElapsedSVM = timeEnd - timeStart;

        Log.i(TAG, "orig mean rows = " + pca.mean().rows() + ", cols = " + pca.mean().cols());
        Log.i(TAG, "orig eigenvectors rows = " + pca.eigenvectors().rows() + ", cols = " + pca.eigenvectors().cols());
        Log.i(TAG, "orig eigenvalues rows = " + pca.eigenvalues().rows() + ", cols = " + pca.eigenvalues().cols());

        data.convertTo(data, CV_32FC1);

        Log.i(TAG, "Saving pca to pca.xml...");
        FileStorage fs = new FileStorage(modelDir + "/pca.xml", FileStorage.WRITE);
        pca.write(fs);
        fs.release();

        fs = new FileStorage(modelDir + "/pca.xml", FileStorage.READ);

        Log.i(TAG, "Loading pca.xml...");
        PCA acp = new PCA();

        acp.read(fs.root());
        fs.release();

        Log.i(TAG, "loaded mean rows = " + acp.mean().rows() + ", cols = " + acp.mean().cols());
        Log.i(TAG, "loaded eigenvectors rows = " + acp.eigenvectors().rows() + ", cols = " + acp.eigenvectors().cols());
        Log.i(TAG, "loaded eigenvalues rows = " + acp.eigenvalues().rows() + ", cols = " + acp.eigenvalues().cols());



        //imwrite("sdcard/CS198/MeanInJTrain.jpg", pca.mean());
        //imwrite("sdcard/CS198/EVectorsInJTrain.jpg",pca.eigenvectors());
        //imwrite("sdcard/CS198/EValuesInJTrain.jpg",pca.eigenvalues());

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


        File folder = new File(modelDir);
        if(!folder.exists()){
            Log.i(TAG, modelDir + " does not exist. Creating...");
            folder.mkdir();
        }


        TextView numImg = (TextView) findViewById(R.id.numImg);
        TextView notification = (TextView) findViewById(R.id.trainingNotification);
        TextView trainTimesTextView = (TextView) findViewById(R.id.trainTimesTextView);


        FaceRecognizer faceRecognizer;



        //Eigen Recog:
        faceRecognizer = createEigenFaceRecognizer(numPrincipalComponents, threshold);
        timeStart = System.currentTimeMillis();
        Log.i(TAG, "Training PCA+KNN...");
        faceRecognizer.train(images, labels);
        timeEnd = System.currentTimeMillis();
        timeElapsed = timeEnd - timeStart;
        trainTimesTextView.setText(trainTimesTextView.getText() + "PCA+KNN : " + ((float) timeElapsed / 1000) + "s\n");
        faceRecognizer.save(modelDir + "/eigenModel.xml");


        //Eigen Recog 20 times:
        /*
        try {
            BufferedWriter bw = new BufferedWriter(new FileWriter(modelDir + "/eigenTrainingTimes.txt"));

            bw.write("Training complete.\nThreshold used = " + threshold + "\nNumber of Training Images: " + images.size() + "\n\n");
            FaceRecognizer faceRecognizer;
            /*
            for (int i = 0; i <= 200; i += 10) {
                //faceRecognizer = createEigenFaceRecognizer(150, threshold);
                faceRecognizer = createEigenFaceRecognizer(i, threshold);
                timeStart = System.currentTimeMillis();
                Log.i(TAG, "Training Eigenface " + i + "...");
                faceRecognizer.train(images, labels);
                timeEnd = System.currentTimeMillis();
                timeElapsed = timeEnd - timeStart;
                trainTimesTextView.setText(trainTimesTextView.getText() + "PCs: " + i + ": " + ((float) timeElapsed / 1000) + "s\n");
                bw.write("PCs: " + i + ": " + ((float) timeElapsed / 1000) + "s\n");
                faceRecognizer.save(modelDir + "/eigenModel_" + i + ".xml");
            }

            bw.flush();
            bw.close();

        } catch (Exception e){
        }
        */
        /*
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
        */

        //SVM Recog:

        SVM svm = SVM.create();
        svm.setType(SVM.C_SVC);
        svm.setKernel(SVM.LINEAR);
        //svm.setP(0.01);
        //svm.setDegree(2);
        //svm.setGamma(1);
        TrainData td = TrainData.create(data, ROW_SAMPLE, labels);
        notification.setText("Training SVM...");
        timeStart = System.currentTimeMillis();
        Log.i(TAG, "Training SVM...");
        Log.i(TAG, "Num of rows and cols in data: " + data.rows() + ", " + data.cols());
        Log.i(TAG, "Num of rows and cols in labels: " + labels.rows() + ", " + labels.cols());
        svm.train(td);
        //svm.trainAuto(td, 10, SVM.getDefaultGrid(SVM.C), SVM.getDefaultGrid(SVM.GAMMA), SVM.getDefaultGrid(SVM.P), SVM.getDefaultGrid(SVM.NU), SVM.getDefaultGrid(SVM.COEF), SVM.getDefaultGrid(SVM.DEGREE), false);
        Log.i(TAG, "SVM trained");
        timeEnd = System.currentTimeMillis();
        timeElapsedSVM += timeEnd - timeStart;
        trainTimesTextView.setText(trainTimesTextView.getText() + "PCA+SVM: " + ((float) timeElapsedSVM / 1000) + "s\n\n");
        trainTimesTextView.setText(trainTimesTextView.getText() + "Type: " + svm.getType() + "\nKernel: " + svm.getKernelType() + "\nGamma: " + svm.getGamma() +"\nC: " + svm.getC() + "\nDegree: " + svm.getDegree() + "\ncoef0: " + svm.getCoef0() + "\n\n");
        trainTimesTextView.setText(trainTimesTextView.getText() + "Kernel legend:\nLINEAR: " + SVM.LINEAR + "\nPOLY: " + SVM.POLY + "\nRBF: " + SVM.RBF + "\nSIGMOID: " + SVM.SIGMOID + "\nCHI2: " + SVM.CHI2 + "\nINTER: " + SVM.INTER + "\n\n");
        trainTimesTextView.setText(trainTimesTextView.getText() + "Type legend:\nC_SVC: " + SVM.C_SVC + "\nNU_SVC: " + SVM.NU_SVC + "\nONE_CLASS: " + SVM.ONE_CLASS + "\nEPS_SVR: " + SVM.EPS_SVR + "\nNU_SVR: " + SVM.NU_SVR + "\n\n");

        //svm.save(modelDir + "/svmModel.xml");

        fs = new FileStorage(modelDir + "/svmModel.xml", FileStorage.WRITE);
        svm.write(fs);
        fs.release();

        SVM mvs = SVM.create();
        fs = new FileStorage(modelDir + "/svmModel.xml", FileStorage.READ);
        mvs.read(fs.root());
        fs.release();

        notification.setText("Training complete.\nThreshold used = " + threshold);

    }



}
