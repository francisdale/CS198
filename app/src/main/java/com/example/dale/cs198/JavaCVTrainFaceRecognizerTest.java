package com.example.dale.cs198;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.TextView;

import org.bytedeco.javacpp.opencv_core;
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
import static org.bytedeco.javacpp.opencv_imgproc.resize;
import static org.bytedeco.javacpp.opencv_ml.ROW_SAMPLE;
import static org.bytedeco.javacpp.opencv_ml.SVM;
import static org.bytedeco.javacpp.opencv_ml.TrainData;

public class JavaCVTrainFaceRecognizerTest extends AppCompatActivity {
    private static final String TAG = "testMessage";
    private static final String trainingSetDir = "sdcard/PresentData/att/att_faces";
    private static final String modelDir = "sdcard/PresentData/researchMode/recognizerModels";

    private static double threshold = 10000.0;
    private static final Size dSize = new Size(23, 28);

    int numTrainingImages = 200;
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

        (new File(modelDir)).mkdirs();

        //Image resolution92x112
        for(int s = 1; s <= 40; s++){
            for(int i = 1; i <= 10; i += 2, counter++){
                img = imread(trainingSetDir + "/s" + s + "/" + i + ".pgm", CV_LOAD_IMAGE_GRAYSCALE);
                resize(img, img, new opencv_core.Size(23,28));

                equalizeHist(img, img);

                images.put(counter, img);
                labelsBuf.put(counter, s);

                img.reshape(1, 1).convertTo(img, CV_32FC1);
                trainingMat.push_back(img);

                img.deallocate();

                Log.i(TAG, "s" + s + " i" + i);
            }
        }

        /*//Form kernelMat:

        Log.i(TAG, "Calculating kernelMat...");
        Mat kernelMat = new Mat(numTrainingImages, numTrainingImages, CV_32FC1);
        FloatBufferIndexer kI = kernelMat.createIndexer();

        //The matrix is a mirror along the diagonal, so to reduce computation, copy already computed answers to slots that require the value.
        for(int i = 0; i < numTrainingImages; i++){
            for(int j = 0; j < i; j++){
                kI.put(i,j,kI.get(j,i));
                //oI.put(i,j,1);
            }
            for(int k = i; k < numTrainingImages; k++){
                kI.put(i,k,(float)Math.pow(trainingMat.row(i).dot(trainingMat.row(k)), 2));
                //oI.put(i,k,1);
            }
        }

        Log.i(TAG, "Calculating finalKernelMat...");
        Mat finalKernelMat = new Mat(numTrainingImages, numTrainingImages, CV_32FC1);
        FloatBufferIndexer fkI = finalKernelMat.createIndexer();
        float subA;
        float subB;
        float addC;
        double numTrainingImagesSquared = Math.pow(numTrainingImages, 2);

        for(int i = 0; i < numTrainingImages; i++){
            for(int j = 0; j < numTrainingImages; j++){
                subA = 0;
                subB = 0;
                addC = 0;
                for(int Mn = 0; Mn < numTrainingImages; Mn++){
                    subA += kI.get(Mn,j);
                    subB += kI.get(i,Mn);
                    for(int N = 0; N < numTrainingImages; N++){
                        addC += kI.get(Mn,N);
                    }
                }
                subA /= numTrainingImages;
                subB /= numTrainingImages;
                addC /= numTrainingImagesSquared;

                fkI.put(i,j, kI.get(i,j) - subA - subB + addC);
            }
        }

        kernelMat.deallocate();*/



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

        /*FileStorage pfs = new FileStorage(modelDir + "/pca.xml", opencv_core.FileStorage.READ);
        PCA pca = new PCA();
        pca.read(pfs.root());
        pfs.release();*/
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

        FileStorage fs = new FileStorage(modelDir + "/pca.xml", FileStorage.WRITE);
        pca.write(fs);
        fs.release();

//        //Kernel PCA:
//        //PCA pca = new PCA(finalKernelMat, new Mat(), CV_PCA_DATA_AS_ROW, numPrincipalComponents);
//
//        int numTrainingMatRows = (int)images.size();
//        Mat eVectors = new Mat();
//        Mat eValues = new Mat();
//
//        if(eigen(finalKernelMat, eValues, eVectors)){
//            Log.i(TAG, "eValues and eVectors harvested.");
//        } else {
//            Log.i(TAG, "Failed to harvest eValues and eVectors.");
//        }
//
//        /*FileStorage fs = new FileStorage(modelDir + "/pca.xml", opencv_core.FileStorage.READ);
//        PCA pca = new PCA();
//        pca.read(fs.root());
//        fs.release();
//        eVectors = pca.eigenvectors();
//        eValues = pca.eigenvalues();*/
//
//
//
////        FloatBufferIndexer eValI = eValues.createIndexer();
////        float[] sortedEValues = new float[100];
////
////        for(int i = 100; i < 200; i++){
////            Log.i(TAG, "eValue at " + i + ": " + eValI.get(i, 0));
////            sortedEValues[i] = eValI.get(i, 0);
////        }
////
////
////        Log.i(TAG, "eValues rows = " + eValues.rows() + ", cols = " + eValues.cols() + ", eValues(0,0): " + eValI.get(0,0));
//
//        FloatBufferIndexer eI = eVectors.createIndexer();
//
//        FloatBufferIndexer pI;
//        float val;
//
//        timeStart = System.currentTimeMillis();
//        for(int i = 0; i < numTrainingMatRows; i++) {
//            projectedMat = new Mat(1, numTrainingImages, CV_32FC1);
//            pI = projectedMat.createIndexer();
//
//            //Log.i(TAG, "Loop " + i);
//
//            for(int q = 0; q < numTrainingImages; q++){
//                val = 0;
//                for(int a = 0; a < numTrainingImages; a++){
//                    val += eI.get(q, a) * Math.pow(trainingMat.row(a).dot(trainingMat.row(i)), 2);
//                }
//                pI.put(0,q,val);
//            }
//            data.push_back(projectedMat);
//            projectedMat.deallocate();
//        }
//        timeEnd = System.currentTimeMillis();
//        timeElapsedSVM = timeEnd - timeStart;
//
//        FloatBufferIndexer eVecI = eVectors.createIndexer();
//        Log.i(TAG, "Before saving: eVectors rows = " + eVectors.rows() + ", cols = " + eVectors.cols() + ", (0,0): " + eVecI.get(0, 0));
//
//        data.convertTo(data, CV_32FC1);
//
//
//        Log.i(TAG, "Saving pca to pca.xml...");
//
//        PCA pca = new PCA();
//        pca.eigenvectors(eVectors);
//        FileStorage fsa = new FileStorage(modelDir + "/pca.xml", FileStorage.WRITE);
//        pca.write(fsa);
//        fsa.release();
//
////        FileStorage fr = new FileStorage(modelDir + "/pca.xml", FileStorage.READ);
////        eVectors = new Mat(fr.get("eVectors"));
////        fr.release();
//
//
//        Mat pcaEVectors = pca.eigenvectors();
//        FloatBufferIndexer pEVecI = pcaEVectors.createIndexer();
//        Log.i(TAG, "After reloading: eVectors rows = " + pcaEVectors.rows() + ", cols = " + pcaEVectors.cols() + ", at (0,0): " + pEVecI.get(0,0));

                /*PCA pca2 = new PCA(finalKernelMat, new Mat(), CV_PCA_DATA_AS_ROW, 200);
        Log.i(TAG, "PCA2 eVectors rows: " + pca2.eigenvectors().rows() + ", cols = " + pca2.eigenvectors().cols());
        Mat pcaEValues = pca2.eigenvalues();
        FloatBufferIndexer fpe = pcaEValues.createIndexer();
        Log.i(TAG, "PCA2 eValues rows: " + pcaEValues.rows() + ", cols = " + pcaEValues.cols() + ", at (0,0): " + fpe.get(0,0));*/





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
        svm.setKernel(SVM.POLY);
        //svm.setP(0.01);
        svm.setDegree(2);
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

        fs = new FileStorage(modelDir + "/svmModel.xml", FileStorage.WRITE);
        svm.write(fs);
        fs.release();

        notification.setText("Training complete.\nThreshold used = " + threshold);

    }



}
