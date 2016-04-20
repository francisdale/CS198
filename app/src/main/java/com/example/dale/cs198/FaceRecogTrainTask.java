package com.example.dale.cs198;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;

import org.bytedeco.javacpp.opencv_core;
import org.bytedeco.javacpp.opencv_core.Mat;
import org.bytedeco.javacpp.opencv_core.MatVector;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.FilenameFilter;
import java.nio.IntBuffer;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;

import static org.bytedeco.javacpp.opencv_core.CV_32FC1;
import static org.bytedeco.javacpp.opencv_core.CV_32SC1;
import static org.bytedeco.javacpp.opencv_core.CV_PCA_DATA_AS_ROW;
import static org.bytedeco.javacpp.opencv_core.FileStorage;
import static org.bytedeco.javacpp.opencv_core.PCA;
import static org.bytedeco.javacpp.opencv_core.Size;
import static org.bytedeco.javacpp.opencv_imgcodecs.CV_LOAD_IMAGE_GRAYSCALE;
import static org.bytedeco.javacpp.opencv_imgcodecs.imread;
import static org.bytedeco.javacpp.opencv_imgproc.equalizeHist;
import static org.bytedeco.javacpp.opencv_imgproc.resize;
import static org.bytedeco.javacpp.opencv_ml.ROW_SAMPLE;
import static org.bytedeco.javacpp.opencv_ml.SVM;
import static org.bytedeco.javacpp.opencv_ml.TrainData;


/**
 * Created by jedpatrickdatu on 2/12/2016.
 */
public class FaceRecogTrainTask extends AsyncTask<Void, Void, Boolean> {
    private static final String TAG = "testMessage";
    private static final String untrainedCropsDir = "sdcard/PresentData/faceDatabase/untrainedCrops";
    private static final String trainedCropsDir = "sdcard/PresentData/faceDatabase/trainedCrops";
    private static final String modelDir = "sdcard/PresentData/recognizerModels";

    boolean oneClassNonFace = false;

    private static final Size dSize = new Size(160, 160);
    int numPrincipalComponents = 250;
    double threshold = 10000.0;


    long timeStart;
    long timeEnd;
    long timeElapsed;

    Context c;

    private ProgressDialog dialog;
    Boolean isTrainingSuccess = false;



    protected void onPreExecute() {
        dialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        dialog.setIndeterminate(true);
        dialog.setCancelable(false);
        dialog.setCanceledOnTouchOutside(false);
        dialog.setMessage("Training recognizer...");
        dialog.show();

    }

    @Override
    protected void onPostExecute(Boolean isSuccess) {
        if (dialog.isShowing()) {
            dialog.dismiss();
        }

        if (isSuccess) {
            Toast.makeText(c, "Training successful.", Toast.LENGTH_LONG).show();
        } else {
            Toast.makeText(c, "Training failed. Error encountered.", Toast.LENGTH_LONG).show();
        }

    }

    public FaceRecogTrainTask(Context c){
        dialog = new ProgressDialog(c);
        this.c = c;
    }

    @Override
    protected Boolean doInBackground(Void... params) {

        //Unify the names of all crops in CS197
        //gatherAllCrops();

        File tempF = new File(modelDir);
        if (!tempF.exists()) {
            tempF.mkdirs();
        }

        String timeStamp = new SimpleDateFormat("MMddyyy-HHmmss").format(new Date());

        String modelFileName = "eigenModel_" + timeStamp + ".xml";


        //Load training images from trainedCropsDir and untrainedCropsDir
        File trainedCropsFolder = new File(trainedCropsDir);
        File untrainedCropsFolder = new File(untrainedCropsDir);

        //For testing without nonfaces:
        FilenameFilter imgFilter = new FilenameFilter() {
            public boolean accept(File dir, String name) {
                name = name.toLowerCase();
                return !name.startsWith("0_") && name.endsWith(".jpg");
            }
        };

        File[] trainedCrops = trainedCropsFolder.listFiles();

        /*//For Filtering nonfaces:
        FilenameFilter nonFaceFilter = new FilenameFilter() {
            public boolean accept(File dir, String name) {
                name = name.toLowerCase();
                return name.startsWith("0_") && name.endsWith(".jpg");
            }
        };

        File[] nonFaces = trainedCropsFolder.listFiles(nonFaceFilter);
        int numNonFaces = (int)trainedCrops.length/33;
        //*/

        //File[] trainedCrops = trainedCropsFolder.listFiles();

        //For untrainedCrops, first delete the photos named with "delete" or "unlabeled"
        FilenameFilter untrainedCropsDeleteImgFilter = new FilenameFilter() {
            public boolean accept(File dir, String name) {
                name = name.toLowerCase();
                return name.startsWith("-1") || name.startsWith("unlabeled");
            }
        };


        File[] untrainedCrops = untrainedCropsFolder.listFiles(untrainedCropsDeleteImgFilter);

        for (File d : untrainedCrops) {
            d.delete();
        }

        //Now that the deletable crops have been deleted, get the remaining untrainedCrops for training:
        untrainedCrops = untrainedCropsFolder.listFiles();

        if (untrainedCrops.length == 0 && trainedCrops.length == 0) {
            //toast = Toast.makeText(c, "No training images found.", Toast.LENGTH_SHORT);
            //toast.show();
            return null;
        }

        int numTotalCrops = trainedCrops.length + untrainedCrops.length;

        Log.i(TAG, "numTotalCrops: " + numTotalCrops);

        MatVector images = new MatVector(numTotalCrops);
        Mat img;
        Mat labels = new Mat(numTotalCrops, 1, CV_32SC1);
        IntBuffer labelsBuf = labels.getIntBuffer();
        Mat labelsOneClass = new Mat(numTotalCrops, 1, CV_32SC1);
        IntBuffer labelsOneClassBuf = labelsOneClass.getIntBuffer();
        int label;
        String[] nameDetails;
        File f;
        int counter = 0;
        int secondaryID;

        HashMap<Integer, Integer> labelFreq = new HashMap<Integer, Integer>();
        int tempFreq;


        //For SVM:
        Mat trainingMat = new Mat();

        for (File c : trainedCrops) {

            img = imread(c.getAbsolutePath(), CV_LOAD_IMAGE_GRAYSCALE);
            equalizeHist(img, img);
            //fastNlMeansDenoising(mGray,mGray);
            resize(img, img, dSize);

            nameDetails = c.getName().split("_");
            label = Integer.parseInt(nameDetails[0]);

            /*if (labelFreq.containsKey(label)) {
                tempFreq = labelFreq.get(label) + 1;
                labelFreq.put(label, tempFreq);
            } else {
                labelFreq.put(label, 0);
            }*/

            labelsBuf.put(counter, label);
            //labelsOneClassBuf.put(counter, 1);
            images.put(counter, img);

            //For SVM:
            img.reshape(1, 1).convertTo(img, CV_32FC1);
            trainingMat.push_back(img);

            //Before moving the crop to trainedCrops, find a new filename for the crop that does not conflict with a crop already in trainedCrops.
            secondaryID = 0;
            do {
                f = new File(trainedCropsDir + "/" + label + "_" + nameDetails[1] + "_" + secondaryID + ".jpg");
                secondaryID++;
            } while (f.exists());
            c.renameTo(f);

            img.deallocate();
            counter++;
            Log.i(TAG, "trainedCrops " + counter);

        }



        /*for (int i = 0; i < nonFaces.length; i+= numNonFaces) {
            img = imread(nonFaces[i].getAbsolutePath(), CV_LOAD_IMAGE_GRAYSCALE);
            equalizeHist(img, img);
            //fastNlMeansDenoising(img, img);
            resize(img, img, dSize);

            nameDetails = nonFaces[i].getName().split("_");
            label = Integer.parseInt(nameDetails[0]);

            labelsBuf.put(counter, label);
            images.put(counter, img);

            //For SVM:
            img.reshape(1, 1).convertTo(img, CV_32FC1);
            trainingMat.push_back(img);

            //Before moving the crop to trainedCrops, find a new filename for the crop that does not conflict with a crop already in trainedCrops.
            secondaryID = 0;
            do {
                f = new File(trainedCropsDir + "/" + label + "_" + nameDetails[1] + "_" + secondaryID + ".jpg");
                secondaryID++;
            } while (f.exists());
            nonFaces[i].renameTo(f);

            img.deallocate();
            counter++;
            Log.i(TAG, "nonFaces " + counter);

        }*/


        for (File c : untrainedCrops) {
            img = imread(c.getAbsolutePath(), CV_LOAD_IMAGE_GRAYSCALE);
            equalizeHist(img, img);
            //fastNlMeansDenoising(img, img);
            resize(img, img, dSize);

            nameDetails = c.getName().split("_");
            label = Integer.parseInt(nameDetails[0]);

            /*if (labelFreq.containsKey(label)) {
                tempFreq = labelFreq.get(label) + 1;
                labelFreq.put(label, tempFreq);
            } else {
                labelFreq.put(label, 0);
            }*/

            labelsBuf.put(counter, label);
            //labelsOneClassBuf.put(counter, 1);
            images.put(counter, img);

            //For SVM:
            img.reshape(1, 1).convertTo(img, CV_32FC1);
            trainingMat.push_back(img);

            //Before moving the crop to trainedCrops, find a new filename for the crop that does not conflict with a crop already in trainedCrops.
            secondaryID = 0;
            do {
                f = new File(trainedCropsDir + "/" + label + "_" + nameDetails[1] + "_" + secondaryID + ".jpg");
                secondaryID++;
            } while (f.exists());
            c.renameTo(f);

            img.deallocate();
            counter++;
            Log.i(TAG, "untrainedCrops " + counter);

        }

        Log.i(TAG, "Number of images loaded: " + images.size());

        //dialog.setMessage("Training recognizer...");

        int numTrainingImages = (int) images.size();
        //numPrincipalComponents = numTrainingImages - 1;

        //For PCA+SVM recognition:
        trainingMat.convertTo(trainingMat, CV_32FC1);
        Mat data = new Mat();
        Mat projectedMat;
        Mat temp;

        FileStorage pfs = new FileStorage(modelDir + "/pca.xml", opencv_core.FileStorage.READ);
        PCA pca = new PCA();
        pca.read(pfs.root());
        pfs.release();
        //PCA pca = new PCA(trainingMat, new Mat(), CV_PCA_DATA_AS_ROW, numPrincipalComponents);

        for (int i = 0; i < numTrainingImages; i++) {
            projectedMat = new Mat(1, numPrincipalComponents, CV_32FC1);
            Log.i(TAG, "Loop " + i + " - data now has num of rows, cols :" + data.rows() + ", " + data.cols());
            pca.project(trainingMat.row(i), projectedMat);
            temp = pca.project(trainingMat.row(i));

            Log.i(TAG, "Num of rows and cols of projection: " + temp.rows() + ", " + temp.cols());
            Log.i(TAG, "Num of rows and cols of projectedMat: " + projectedMat.rows() + ", " + projectedMat.cols());
            data.push_back(projectedMat);
        }

        Log.i(TAG, "orig mean rows = " + pca.mean().rows() + ", cols = " + pca.mean().cols());
        Log.i(TAG, "orig eigenvectors rows = " + pca.eigenvectors().rows() + ", cols = " + pca.eigenvectors().cols());
        Log.i(TAG, "orig eigenvalues rows = " + pca.eigenvalues().rows() + ", cols = " + pca.eigenvalues().cols());

        data.convertTo(data, CV_32FC1);
        SVM svm = SVM.create();
        svm.setType(SVM.C_SVC);
        svm.setKernel(SVM.LINEAR);
        //svm.setP(0.1);
        //svm.setDegree(2);
        //svm.setGamma(0.00001);
        TrainData td = TrainData.create(data, ROW_SAMPLE, labels);

        /*Mat cw = new Mat(numTotalCrops, 1, CV_32SC1);
        FloatBuffer cwBuf = cw.getFloatBuffer();
        Set labelSet = labelFreq.entrySet();
        Iterator li = labelSet.iterator();
        Map.Entry le;
        counter = 0;
        Log.i(TAG, "Recog: Writing and saving attendance record...");
        // Display elements
        while (li.hasNext()) {
            le = (Map.Entry) li.next();
            bw.write(ae.getKey() + "," + ne.getValue() + "," + ae.getValue() + "\n");
        }*/


        //Log.i(TAG, "kFold selected: " + kFold);
        Log.i(TAG, "Num of rows and cols in data: " + data.rows() + ", " + data.cols());
        Log.i(TAG, "Num of rows and cols in labels: " + labels.rows() + ", " + labels.cols());
        Log.i(TAG, "Training SVM...");
        svm.train(td);
        //svm.trainAuto(td, kFold, SVM.getDefaultGrid(SVM.C), SVM.getDefaultGrid(SVM.GAMMA), SVM.getDefaultGrid(SVM.P), SVM.getDefaultGrid(SVM.NU), SVM.getDefaultGrid(SVM.COEF), SVM.getDefaultGrid(SVM.DEGREE), false);
        Log.i(TAG, "SVM trained");
        Log.i(TAG, "SVM trained. Saving to svmModel.xml...");

        FileStorage fs = new FileStorage(modelDir + "/svmModel.xml", FileStorage.WRITE);
        svm.write(fs);
        fs.release();

        Log.i(TAG, "Saving pca to pca.xml...");
        fs = new FileStorage(modelDir + "/pca.xml", opencv_core.FileStorage.WRITE);
        pca.write(fs);
        fs.release();

        try {
            BufferedWriter bw = new BufferedWriter(new FileWriter(modelDir + "/svmParams.txt"));
            bw.write("Type: " + svm.getType() + "\nKernel: " + svm.getKernelType() + "\nGamma: " + svm.getGamma() + "\nC: " + svm.getC() + "\nP: " + svm.getP() + "\nDegree: " + svm.getDegree() + "\ncoef0: " + svm.getCoef0() + "\n\n");
            bw.write("\nKernel legend:\nLINEAR: " + SVM.LINEAR + "\nPOLY: " + SVM.POLY + "\nRBF: " + SVM.RBF + "\nSIGMOID: " + SVM.SIGMOID + "\nCHI2: " + SVM.CHI2 + "\nINTER: " + SVM.INTER + "\n\n");
            bw.write("\nType legend:\nC_SVC: " + SVM.C_SVC + "\nNU_SVC: " + SVM.NU_SVC + "\nONE_CLASS: " + SVM.ONE_CLASS + "\nEPS_SVR: " + SVM.EPS_SVR + "\nNU_SVR: " + SVM.NU_SVR + "\n\n");

            bw.flush();
            bw.close();

        } catch(Exception e){
            e.printStackTrace();
        }

        Log.i(TAG, "Type: " + svm.getType() + "\nKernel: " + svm.getKernelType() + "\nGamma: " + svm.getGamma() + "\nC: " + svm.getC() + "\nP: " + svm.getP() + "\nDegree: " + svm.getDegree() + "\ncoef0: " + svm.getCoef0() + "\n\n");
        Log.i(TAG, "Kernel legend:\nLINEAR: " + SVM.LINEAR + "\nPOLY: " + SVM.POLY + "\nRBF: " + SVM.RBF + "\nSIGMOID: " + SVM.SIGMOID + "\nCHI2: " + SVM.CHI2 + "\nINTER: " + SVM.INTER + "\n\n");
        Log.i(TAG, "Type legend:\nC_SVC: " + SVM.C_SVC + "\nNU_SVC: " + SVM.NU_SVC + "\nONE_CLASS: " + SVM.ONE_CLASS + "\nEPS_SVR: " + SVM.EPS_SVR + "\nNU_SVR: " + SVM.NU_SVR + "\n\n");

        /*//For one class SVM:
        SVM svmOneClass = SVM.create();
        svmOneClass.setType(SVM.ONE_CLASS);
        svmOneClass.setKernel(SVM.POLY);
        //svm.setP(0.1);

        svmOneClass.setDegree(2);
        svmOneClass.setNu(0.0001);
        svmOneClass.setGamma(0.00001);
        TrainData tdOneClass = TrainData.create(data, ROW_SAMPLE, labelsOneClass);

        Log.i(TAG, "Training one class SVM...");
        svmOneClass.train(tdOneClass);
        //svm.trainAuto(td, kFold, SVM.getDefaultGrid(SVM.C), SVM.getDefaultGrid(SVM.GAMMA), SVM.getDefaultGrid(SVM.P), SVM.getDefaultGrid(SVM.NU), SVM.getDefaultGrid(SVM.COEF), SVM.getDefaultGrid(SVM.DEGREE), false);
        Log.i(TAG, "SVM trained");
        Log.i(TAG, "SVM trained. Saving to svmModel.xml...");

        fs = new FileStorage(modelDir + "/svmModelOneClass.xml", FileStorage.WRITE);
        svmOneClass.write(fs);
        fs.release();*/


//        //For PCA+KNN recognition:
//        FaceRecognizer faceRecognizer = createEigenFaceRecognizer(numPrincipalComponents, threshold);
//        //FaceRecognizer faceRecognizer = createEigenFaceRecognizer();
//
//        /*//Delete the old eigenModel xml model if there is any
//        FilenameFilter eigenModelFilter = new FilenameFilter() {
//            public boolean accept(File dir, String name) {
//                name = name.toLowerCase();
//                return name.startsWith("eigenModel") && name.endsWith(".xml");
//            }
//        };
//
//        File[] modelFileDirFiles = (new File(modelDir)).listFiles(eigenModelFilter);
//        if(modelFileDirFiles.length > 0) {
//            modelFileDirFiles[0].delete();
//        }*/
//
//
//        Log.i(TAG, "Training Eigenface...");
//        timeStart = System.currentTimeMillis();
//        faceRecognizer.train(images, labels);
//        timeEnd = System.currentTimeMillis();
//        timeElapsed = timeEnd - timeStart;
//        Log.i(TAG, "Training completed in " + (float) timeElapsed / 1000 + "s.");
//
//        //dialog.setMessage("Saving recognizer...");
//        faceRecognizer.save(modelDir + "/eigenModel.xml");
//        Log.i(TAG, "Training model saved.");
//        //toast = Toast.makeText(c, "Training complete.", Toast.LENGTH_SHORT);
//        //toast.show();



        if(oneClassNonFace) {
            Log.i(TAG, "nonFace!");

            File trainedCropsFolderNonFace = new File(trainedCropsDir);

            FilenameFilter imgFilterNonFace = new FilenameFilter() {
                public boolean accept(File dir, String name) {
                    name = name.toLowerCase();
                    return name.startsWith("0_") && name.endsWith(".jpg");
                }
            };

            File[] trainedCropsNonFace = trainedCropsFolderNonFace.listFiles(imgFilterNonFace);

            MatVector imagesNonFace = new MatVector(trainedCropsNonFace.length);
            Mat labelsNonFace = new Mat(trainedCropsNonFace.length, 1, CV_32SC1);
            IntBuffer labelsBufNonFace = labelsNonFace.getIntBuffer();
            int counterNonFace = 0;
            Mat imgn;

            Mat trainingMatNonFace = new Mat();



            for (File c : trainedCropsNonFace) {
                imgn = imread(c.getAbsolutePath(), CV_LOAD_IMAGE_GRAYSCALE);
                equalizeHist(imgn, imgn);

                //nameDetails = c.getName().split("_");

                resize(imgn, imgn, dSize);

                labelsBufNonFace.put(counterNonFace, 1);
                //labelsBuf.put(counter, nameDetails[0].equals("0") ? 1 : 0);
                imagesNonFace.put(counterNonFace, imgn);

                counterNonFace++;

                //For SVM:
                imgn.reshape(1, 1).convertTo(imgn, CV_32FC1);
                trainingMatNonFace.push_back(imgn);

                imgn.deallocate();

                Log.i(TAG, "nonFace trainedCrops " + counterNonFace);
            }
            Log.i(TAG, "Number of images loaded: " + imagesNonFace.size());

            int numTrainingImagesNonFace = (int)imagesNonFace.size();
            trainingMatNonFace.convertTo(trainingMatNonFace, CV_32FC1);
            Mat dataNonFace = new Mat();
            PCA pcaNonFace = new PCA(trainingMatNonFace, new Mat(), CV_PCA_DATA_AS_ROW, numPrincipalComponents);
            Mat projectedMatNonFace;
            Mat tempNonFace;

            for (int i = 0; i < numTrainingImagesNonFace; i++) {
                projectedMatNonFace = new Mat(1, numPrincipalComponents, CV_32FC1);
                Log.i(TAG, "Loop " + i + " - data now has num of rows, cols :" + dataNonFace.rows() + ", " + dataNonFace.cols());
                pcaNonFace.project(trainingMatNonFace.row(i), projectedMatNonFace);
                tempNonFace = pcaNonFace.project(trainingMatNonFace.row(i));

                Log.i(TAG, "Num of rows and cols of projection: " + tempNonFace.rows() + ", " + tempNonFace.cols());
                Log.i(TAG, "Num of rows and cols of projectedMat: " + projectedMatNonFace.rows() + ", " + projectedMatNonFace.cols());
                dataNonFace.push_back(projectedMatNonFace);
            }

            /*Log.i(TAG, "orig mean rows = " + pca.mean().rows() + ", cols = " + pca.mean().cols());
            Log.i(TAG, "orig eigenvectors rows = " + pca.eigenvectors().rows() + ", cols = " + pca.eigenvectors().cols());
            Log.i(TAG, "orig eigenvalues rows = " + pca.eigenvalues().rows() + ", cols = " + pca.eigenvalues().cols());*/


            SVM svmNonFace = SVM.create();
            svmNonFace.setType(SVM.ONE_CLASS);
            svmNonFace.setKernel(SVM.POLY);
            //svm.setP(0.1);

            svmNonFace.setDegree(2);
            svmNonFace.setNu(0.0001);
            TrainData tdNonFace = TrainData.create(dataNonFace, ROW_SAMPLE, labelsNonFace);

            Log.i(TAG, "Training SVM...");
            svmNonFace.train(tdNonFace);
            //svm.trainAuto(td, kFold, SVM.getDefaultGrid(SVM.C), SVM.getDefaultGrid(SVM.GAMMA), SVM.getDefaultGrid(SVM.P), SVM.getDefaultGrid(SVM.NU), SVM.getDefaultGrid(SVM.COEF), SVM.getDefaultGrid(SVM.DEGREE), false);
            Log.i(TAG, "SVM trained");
            Log.i(TAG, "SVM trained. Saving to svmModel.xml...");

            FileStorage fsNonFace = new FileStorage(modelDir + "/svmModelNonFace.xml", FileStorage.WRITE);
            svmNonFace.write(fsNonFace);
            fsNonFace.release();

            Log.i(TAG, "Saving pca to pca.xml...");
            fsNonFace = new FileStorage(modelDir + "/pcaNonFace.xml", opencv_core.FileStorage.WRITE);
            pcaNonFace.write(fsNonFace);
            fsNonFace.release();

            Log.i(TAG, "Type: " + svmNonFace.getType() + "\nKernel: " + svmNonFace.getKernelType() + "\nGamma: " + svmNonFace.getGamma() + "\nC: " + svmNonFace.getC() + "\nP: " + svmNonFace.getP() + "\nDegree: " + svmNonFace.getDegree() + "\ncoef0: " + svmNonFace.getCoef0() + "\n\n");
            Log.i(TAG, "Kernel legend:\nLINEAR: " + SVM.LINEAR + "\nPOLY: " + SVM.POLY + "\nRBF: " + SVM.RBF + "\nSIGMOID: " + SVM.SIGMOID + "\nCHI2: " + SVM.CHI2 + "\nINTER: " + SVM.INTER + "\n\n");
            Log.i(TAG, "Type legend:\nC_SVC: " + SVM.C_SVC + "\nNU_SVC: " + SVM.NU_SVC + "\nONE_CLASS: " + SVM.ONE_CLASS + "\nEPS_SVR: " + SVM.EPS_SVR + "\nNU_SVR: " + SVM.NU_SVR + "\n\n");

            Mat resultCW = svm.getClassWeights();
        }

        isTrainingSuccess = true;
        return isTrainingSuccess;
    }

    @Override
    protected void onProgressUpdate(Void... progress){
        //TextView tv = (TextView)((TrainActivity)c).findViewById(R.id.detectNotification);
        //tv.setText("Detected " + faceCount + " faces in " + (float) timeElapsed/1000 + "s." );
    }

}
