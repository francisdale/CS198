package com.example.dale.cs198;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.PointF;
import android.media.FaceDetector;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ImageView;
import android.widget.TextView;

import org.bytedeco.javacpp.opencv_core;
import org.bytedeco.javacpp.opencv_imgproc;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import static org.bytedeco.javacpp.opencv_core.CV_AA;
import static org.bytedeco.javacpp.opencv_core.CvRect;
import static org.bytedeco.javacpp.opencv_core.CvScalar;
import static org.bytedeco.javacpp.opencv_core.IPL_DEPTH_8U;
import static org.bytedeco.javacpp.opencv_core.IplImage;
import static org.bytedeco.javacpp.opencv_core.Mat;
import static org.bytedeco.javacpp.opencv_core.Rect;
import static org.bytedeco.javacpp.opencv_core.Size;
import static org.bytedeco.javacpp.opencv_core.cvCopy;
import static org.bytedeco.javacpp.opencv_core.cvCreateImage;
import static org.bytedeco.javacpp.opencv_core.cvGetSize;
import static org.bytedeco.javacpp.opencv_core.cvPoint;
import static org.bytedeco.javacpp.opencv_core.cvRectangle;
import static org.bytedeco.javacpp.opencv_core.cvResetImageROI;
import static org.bytedeco.javacpp.opencv_core.cvSetImageROI;
import static org.bytedeco.javacpp.opencv_highgui.CV_LOAD_IMAGE_GRAYSCALE;
import static org.bytedeco.javacpp.opencv_highgui.cvLoadImage;
import static org.bytedeco.javacpp.opencv_highgui.cvSaveImage;
import static org.bytedeco.javacpp.opencv_highgui.imread;
import static org.bytedeco.javacpp.opencv_highgui.imwrite;
import static org.bytedeco.javacpp.opencv_imgproc.CV_BGR2GRAY;
import static org.bytedeco.javacpp.opencv_imgproc.cvCvtColor;
import static org.bytedeco.javacpp.opencv_objdetect.CascadeClassifier;

public class FaceDetect extends AppCompatActivity {

    public static final String haarCascadeXML = "haarcascade_frontalface_default.xml";
    public static final String haar20CascadeXML = "haarcascade_frontalface_alt.xml";
    public static final String lbpCascadeXML = "lbpcascade_frontalface.xml";
    CascadeClassifier cascadeFaceDetector;
    private static final int SCALE = 2; // scaling factor to reduce size of input image

    String filepath;
    String imgName;
    String cropPath;
    String cropName;
    String outputImgPath;
    int detectType;
    String detectTypeString;
    ImageView imgWindow;
    private static final String TAG = "testMessage";
    private int faceCount;
    long timeStart;
    long timeEnd;
    long timeElapsed;
    private static final int MAX_FACES = 100;

    private IplImage imgRgba;
    private IplImage imgGray;
    private Mat mRgba;
    private Mat mGray;
    private int mAbsoluteFaceSize = 30;

    // Variables for Android FaceDetect:

    /**===========================================================**/

    private FaceDetector.Face[] faces;


    /**===========================================================**/


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.i(TAG, "onCreate na");
        super.onCreate(savedInstanceState);

        Intent intent = getIntent();
        filepath = intent.getStringExtra("filepath");
        detectType = intent.getIntExtra("detectType", 0);

        setContentView(R.layout.activity_face_detect);
        imgWindow = (ImageView) findViewById(R.id.detected);

        String[] stringArray = filepath.split("/");
        imgName = stringArray[stringArray.length - 1];
        imgName = imgName.substring(0, imgName.length() - 4);
        Log.i(TAG, "filepath: " + filepath);
        Log.i(TAG, "imgName: " + imgName);

        if(detectType == 0) {
            cropName = imgName + "Haar";
        } else if(detectType == 1) {
            cropName = imgName + "LBP";
        } else if(detectType == 2) {
            cropName = imgName + "Android";
        } else {
            cropName = imgName + "Haar20";
        }

        Log.i(TAG, "Creating folders:");
        cropPath = "sdcard/CS198Crops";
        outputImgPath = "sdcard/CS198OutputImgs";

        File folder = new File(cropPath);
        if(!folder.exists()){
            Log.i(TAG, cropPath + " does not exist. Creating...");
            folder.mkdir();
        }

        folder = new File(outputImgPath);
        if(!folder.exists()){
            Log.i(TAG, outputImgPath + " does not exist. Creating...");
            folder.mkdir();
        }

        cropPath = "sdcard/CS198Crops/" + cropName;

        folder = new File(cropPath);
        if(!folder.exists()){
            Log.i(TAG, cropPath + " does not exist. Creating...");
            folder.mkdir();
        }

        if(detectType == 2) {
            Log.i(TAG, "Detecting with Android");
            detectTypeString = "Android";
            detectFacesByAndroid(filepath);
        } else {
            try {
                // load cascade file from application resources
                File cascadeDir = getDir("cascade", Context.MODE_PRIVATE);
                InputStream is;
                File cascadeFile;
                if (detectType == 0) {
                    Log.i(TAG, "Detecting with Haar");
                    detectTypeString = "Haar Cascade";
                    is = getResources().openRawResource(R.raw.haarcascade_frontalface_default);
                    cascadeFile = new File(cascadeDir, haarCascadeXML);
                } else if (detectType == 1) {
                    Log.i(TAG, "Detecting with LBP");
                    detectTypeString = "LBP Cascade";
                    is = getResources().openRawResource(R.raw.lbpcascade_frontalface);
                    cascadeFile = new File(cascadeDir, lbpCascadeXML);
                } else {
                    Log.i(TAG, "Detecting with Haar20");
                    detectTypeString = "Haar 20 Cascade";
                    is = getResources().openRawResource(R.raw.haarcascade_frontalface_alt);
                    cascadeFile = new File(cascadeDir, haar20CascadeXML);
                }

                FileOutputStream os = new FileOutputStream(cascadeFile);

                byte[] buffer = new byte[4096];
                int bytesRead;
                while ((bytesRead = is.read(buffer)) != -1) {
                    os.write(buffer, 0, bytesRead);
                }
                is.close();
                os.close();
                Log.i(TAG, "cascadeFile.getAbsolutePath: " + cascadeFile.getAbsolutePath());
                Log.i(TAG, "Does the file above exist: " + (new File(cascadeFile.getAbsolutePath())).exists());

                cascadeFaceDetector = new CascadeClassifier(cascadeFile.getAbsolutePath());
                cascadeDir.delete();

                /*
                if (cascadeFaceDetector.empty()) {
                    Log.e(TAG, "Failed to load cascade classifier");
                    cascadeFaceDetector = null;
                } else {
                    Log.i(TAG, "Loaded cascade classifier from " + cascadeFile.getAbsolutePath());
                }
                */
                detectFacesByCascade(filepath);

            } catch (IOException e) {
                e.printStackTrace();
                Log.e(TAG, "Failed to load cascade. Exception thrown: " + e);
            }
        }

        TextView tv = (TextView) findViewById(R.id.detectType);
        tv.setText("Detection method: " + detectTypeString);
        tv = (TextView) findViewById(R.id.numFaceDetected);
        tv.setText("Number of faces detected: " + faceCount);
        tv = (TextView) findViewById(R.id.timeElapsed);
        tv.setText("Time in seconds: " + (float) timeElapsed /1000);
    }



    public void detectFacesByCascade(String path) {
        timeStart = System.currentTimeMillis();

        mGray = imread(path, CV_LOAD_IMAGE_GRAYSCALE);
        imgRgba = cvLoadImage(path);

        Rect faces = new Rect();

        //cascadeFaceDetector.detectMultiScale(mGray, faces, 1.2, 3, 0, new Size(mAbsoluteFaceSize, mAbsoluteFaceSize), new Size());
        cascadeFaceDetector.detectMultiScale(mGray, faces, 1.1, 2, 2, new Size(mAbsoluteFaceSize, mAbsoluteFaceSize), new Size());

        // draw thick green rectangles around all the faces
        faceCount = faces.capacity();

        Rect roi;
        Mat crop;
        for (int i = 0; i < faceCount; i++) {
            Rect r = faces.position(i);
            int x = r.x();
            int y = r.y();


            cvRectangle(imgRgba, cvPoint(x, y), cvPoint((r.x() + r.width()), (r.y() + r.height())), CvScalar.GREEN, 6, CV_AA, 0);
            //undo image scaling when calculating rect coordinates


            roi = new Rect(x, y, r.width(), r.height());
            crop = new Mat(mGray, roi);
            imwrite(cropPath + "/" + cropName + "_" + (i+1) + ".jpg", crop);
        }

        cvSaveImage(outputImgPath + "/" + cropName + ".jpg", imgRgba);

        IplImage temp = IplImage.create(imgRgba.width(), imgRgba.height(), opencv_core.IPL_DEPTH_8U, 4);
        cvCvtColor(imgRgba, temp, opencv_imgproc.CV_BGR2RGBA);
        // Now we make an Android Bitmap with matching size ... Nb. at this point we functionally have 3 buffers == image size. Watch your memory usage!
        Bitmap bm = Bitmap.createBitmap(temp.width(), temp.height(), Bitmap.Config.ARGB_8888);
        bm.copyPixelsFromBuffer(temp.getByteBuffer());

        imgWindow.setImageBitmap(bm);

        timeEnd = System.currentTimeMillis();
        timeElapsed = timeEnd - timeStart;
    }



    public void detectFacesByAndroid(String path) {
        timeStart = System.currentTimeMillis();
        // Set internal configuration to RGB_565
        Bitmap background_image;
        FaceDetector.Face[] faces;

        BitmapFactory.Options bitmap_options = new BitmapFactory.Options();
        bitmap_options.inPreferredConfig = Bitmap.Config.RGB_565;
        background_image = BitmapFactory.decodeFile(path, bitmap_options);

        FaceDetector face_detector = new FaceDetector(background_image.getWidth(), background_image.getHeight(), MAX_FACES);
        faces = new FaceDetector.Face[MAX_FACES];
        // The bitmap must be in 565 format (for now).
        faceCount = face_detector.findFaces(background_image, faces);

        Log.i(TAG, "entered on draw");


        imgRgba = cvLoadImage(path);
        imgGray = IplImage.create(imgRgba.width(),imgRgba.height(), IPL_DEPTH_8U, 1);
        cvCvtColor(imgRgba, imgGray, CV_BGR2GRAY);


        Rect rectCrop = null;
        opencv_core.Mat image_roi = null;
        String dir;
        float myEyesDistance;
        Log.i(TAG, "Starting to crop");
        for (int i = 0; i < faceCount; i++) {
            FaceDetector.Face face = faces[i];
            PointF myMidPoint = new PointF();
            face.getMidPoint(myMidPoint);
            myEyesDistance = face.eyesDistance();
            rectCrop = new Rect(
                    (int) (myMidPoint.x - myEyesDistance * 1.3),
                    (int) (myMidPoint.y - myEyesDistance * 1.3),
                    (int) (2*myEyesDistance * 1.3),
                    (int) (2*myEyesDistance * 1.3)
            );

            cvRectangle(imgRgba, cvPoint(rectCrop.x(), rectCrop.y()), cvPoint(rectCrop.x() + rectCrop.width(), rectCrop.y() + rectCrop.height()), CvScalar.GREEN, 6, CV_AA, 0);

            Log.i(TAG, "Crop loop " + i);


            //Check if the crop rectangle is outside the bounds of the image
            if(rectCrop.x() < 0){
                rectCrop.x(0);
            }
            if(rectCrop.y() < 0){
                rectCrop.y(0);
            }
            if (rectCrop.x() + rectCrop.width() > imgGray.width()){
                rectCrop.width(imgGray.width() - rectCrop.x());
            }
            if (rectCrop.y() + rectCrop.height() > imgGray.height()){
                rectCrop.height(imgGray.height() - rectCrop.y());
            }

            CvRect cropROI = new CvRect();
            cropROI.x(rectCrop.x());
            cropROI.y(rectCrop.y());
            cropROI.width(rectCrop.width());
            cropROI.height(rectCrop.height());
            //After setting ROI (Region-Of-Interest) all processing will only be done on the ROI
            cvSetImageROI(imgGray, cropROI);
            IplImage cropped = cvCreateImage(cvGetSize(imgGray), imgGray.depth(), imgGray.nChannels());
            //Copy original image (only ROI) to the cropped image
            cvCopy(imgGray, cropped);
            cvResetImageROI(imgGray);
            cvSaveImage(cropPath + "/" + cropName + "_" + (i+1) + ".jpg", cropped);
        }

        cvSaveImage(outputImgPath + "/" + cropName + ".jpg", imgRgba);

        IplImage temp = IplImage.create(imgRgba.width(), imgRgba.height(), opencv_core.IPL_DEPTH_8U, 4);
        cvCvtColor(imgRgba, temp, opencv_imgproc.CV_BGR2RGBA);
        // Now we make an Android Bitmap with matching size ... Nb. at this point we functionally have 3 buffers == image size. Watch your memory usage!
        Bitmap bm = Bitmap.createBitmap(temp.width(), temp.height(), Bitmap.Config.ARGB_8888);
        bm.copyPixelsFromBuffer(temp.getByteBuffer());

        imgWindow.setImageBitmap(bm);

        timeEnd = System.currentTimeMillis();
        timeElapsed = timeEnd - timeStart;
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_face_detect, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }


}
