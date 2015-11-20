package com.example.dale.cs198;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PointF;
import android.media.FaceDetector;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Display;
import android.view.Menu;
import android.view.MenuItem;
import android.view.SurfaceHolder;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.android.Utils;
import org.opencv.core.Mat;
import org.opencv.core.MatOfRect;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;
import org.opencv.objdetect.CascadeClassifier;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

public class FaceDetect extends AppCompatActivity {

    String filepath;
    int detectType;
    String detectTypeString;
    ImageView faceCanvas;
    private static final String TAG = "testMessage";
    private int face_count;
    long timeStart;
    long timeEnd;
    long timeElapsed;
    File folder;

    //Variables for Haar face detection:

    /**===========================================================**/
    private static final Scalar FACE_RECT_COLOR     = new Scalar(0, 255, 0, 255);

    private Mat mRgba;
    private Mat mGray;
    private File mCascadeFile;
    private String mCascadeFileName;
    private CascadeClassifier mCascadeFaceDetector;

    private float mRelativeFaceSize   = 0.2f;
    private int mAbsoluteFaceSize   = 0;

    BaseLoaderCallback mLoaderCallback = new BaseLoaderCallback(this) {
        @Override
        public void onManagerConnected(int status) {
            switch (status) {
                case LoaderCallbackInterface.SUCCESS:
                {
                    Log.i(TAG, "OpenCV loaded successfully");

                    // Load native library after(!) OpenCV initialization
                    //System.loadLibrary("detection_based_tracker");

                    try {
                        // load cascade file from application resources
                        InputStream is;
                        if(detectType == 0){
                            is = getResources().openRawResource(R.raw.haarcascade_frontalface_default);
                        } else
                            is = getResources().openRawResource(R.raw.lbpcascade_frontalface);

                        File cascadeDir = getDir("cascade", Context.MODE_PRIVATE);
                        mCascadeFile = new File(cascadeDir, mCascadeFileName);
                        FileOutputStream os = new FileOutputStream(mCascadeFile);

                        byte[] buffer = new byte[4096];
                        int bytesRead;
                        while ((bytesRead = is.read(buffer)) != -1) {
                            os.write(buffer, 0, bytesRead);
                        }
                        is.close();
                        os.close();

                        mCascadeFaceDetector = new CascadeClassifier(mCascadeFile.getAbsolutePath());
                        if (mCascadeFaceDetector.empty()) {
                            Log.e(TAG, "Failed to load cascade classifier");
                            mCascadeFaceDetector = null;
                        } else
                            Log.i(TAG, "Loaded cascade classifier from " + mCascadeFile.getAbsolutePath());



                        cascadeDir.delete();

                    } catch (IOException e) {
                        e.printStackTrace();
                        Log.e(TAG, "Failed to load cascade. Exception thrown: " + e);
                    }

                } break;
                default:
                {
                    super.onManagerConnected(status);
                } break;
            }
        }
    };

    /**===========================================================**/



    // Variables for Android FaceDetect:

    /**===========================================================**/
    private static final int MAX_FACES = 100;
    private Bitmap background_image;
    private SurfaceHolder surfaceHolder;
    private FaceDetector.Face[] faces;

    // preallocate for onDraw(...)
    private PointF tmp_point = new PointF();
    private Paint tmp_paint = new Paint();
    WindowManager wm;
    Display display;
    ImageView iv;
    String path;
    int sizeY;
    int sizeX;
    LinearLayout mLinearLayout;
    Canvas canvas;
    /**===========================================================**/


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.i(TAG, "onCreate na");
        super.onCreate(savedInstanceState);

        Intent intent = getIntent();
        filepath = intent.getStringExtra("filepath");
        detectType = intent.getIntExtra("detectType", 0);

        setContentView(R.layout.activity_face_detect);
        faceCanvas = (ImageView) findViewById(R.id.detected);

        folder = new File("sdcard/CS198Crops");

        deleteDirectory(folder);
        folder.mkdir();

        if(detectType == 0) {
            Log.i(TAG, "Detecting with Haar");
            detectTypeString = "Haar Cascade";
            mCascadeFileName = "haarcascade_frontalface_default.xml";
            if (!OpenCVLoader.initDebug()) {
                Log.d(TAG, "Internal OpenCV library not found. Using OpenCV Manager for initialization");
                OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_3_0_0, this, mLoaderCallback);
            } else {
                Log.d(TAG, "OpenCV library found inside package. Using it!");
                mLoaderCallback.onManagerConnected(LoaderCallbackInterface.SUCCESS);
            }
            detectFacesByCascade(filepath);
            Log.i(TAG, "Detecting with Haar end");
        } else if(detectType == 1){
            Log.i(TAG, "Detecting with LBP");
            detectTypeString = "LBP Cascade";
            mCascadeFileName = "lbpcascade_frontalface.xml";
            if (!OpenCVLoader.initDebug()) {
                Log.d(TAG, "Internal OpenCV library not found. Using OpenCV Manager for initialization");
                OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_3_0_0, this, mLoaderCallback);
            } else {
                Log.d(TAG, "OpenCV library found inside package. Using it!");
                mLoaderCallback.onManagerConnected(LoaderCallbackInterface.SUCCESS);
            }
            detectFacesByCascade(filepath);
            Log.i(TAG, "Detecting with LBP end");
        } else if (detectType == 2){
            Log.i(TAG, "Detecting with Android");
            detectTypeString = "Android";
            OpenCVLoader.initDebug();
            detectFacesByAndroid(filepath);
            Log.i(TAG, "Detecting with Android end");
        }

        TextView tv = (TextView) findViewById(R.id.detectType);
        tv.setText("Detection method: " + detectTypeString);
        tv = (TextView) findViewById(R.id.numFaceDetected);
        tv.setText("Number of faces detected: " + face_count);
        tv = (TextView) findViewById(R.id.timeElapsed);
        tv.setText("Time in seconds: " + (float)timeElapsed/1000);
    }



    public void detectFacesByCascade(String path) {
        timeStart = System.currentTimeMillis();

        //We load the image in grayscale:
        mGray = Imgcodecs.imread(path, Imgcodecs.CV_LOAD_IMAGE_GRAYSCALE);

        //We load the image in color; we need to convert color scheme to rgba because imread() uses bga.
        Mat mBga = Imgcodecs.imread(path);
        mRgba = new Mat(); //RGBA format
        Imgproc.cvtColor(mBga, mRgba, Imgproc.COLOR_BGR2RGBA);

        MatOfRect faces = new MatOfRect();

        //mCascadeFaceDetector.detectMultiScale(mGray, faces, 1.1, 2, 2, new Size(mAbsoluteFaceSize, mAbsoluteFaceSize), new Size());
        mCascadeFaceDetector.detectMultiScale(mGray, faces);

        Rect[] facesArray = faces.toArray();


        String dir;
        Rect rectCrop = null;
        Mat image_roi = null;

        for (int i = 0; i < facesArray.length; i++) {
            //We draw the bounding boxes on the faces on the mRgba image:
            Imgproc.rectangle(mRgba, facesArray[i].tl(), facesArray[i].br(), FACE_RECT_COLOR, 3);
            //We crop the faces from the mGray image and store them in sdcard/CS198Crops:
            rectCrop = new Rect(facesArray[i].x, facesArray[i].y, facesArray[i].width, facesArray[i].height);
            image_roi = mGray.submat(rectCrop);
            dir = "sdcard/CS198Crops/"+i+".jpg";
            Imgcodecs.imwrite(dir, image_roi);
        }
        face_count = facesArray.length;

        // convert Mat to bitmap:
        Bitmap bm = Bitmap.createBitmap(mRgba.cols(), mRgba.rows(), Bitmap.Config.ARGB_8888);
        Utils.matToBitmap(mRgba, bm);

        faceCanvas.setImageBitmap(bm);


        timeEnd = System.currentTimeMillis();
        timeElapsed = timeEnd - timeStart;
    }

    public void detectFacesByAndroid(String path) {
        timeStart = System.currentTimeMillis();
        // Set internal configuration to RGB_565
        BitmapFactory.Options bitmap_options = new BitmapFactory.Options();
        bitmap_options.inPreferredConfig = Bitmap.Config.RGB_565;
        background_image = BitmapFactory.decodeFile(path, bitmap_options);

        /**================================================
        int scaleToUse=0;
        Point size = new Point();
        display.getSize(size);

        if(background_image.getWidth() >= background_image.getHeight()){
            Log.i(TAG, "MAS MALAKI WIDTH");
            scaleToUse = 90;
        }

        if(background_image.getWidth() < background_image.getHeight()){
            Log.i(TAG, "MAS MALAKI HEIGHT");
            scaleToUse = 90;
        }

        sizeY = size.y * scaleToUse / 100;
        sizeX = background_image.getWidth() * sizeY / background_image.getHeight();
        //background_image = Bitmap.createScaledBitmap(background_image,sizeX,sizeY,false);
        /**================================================**/

        //Log.i(TAG, "size " + background_image.getWidth() + "x" + background_image.getHeight());

        FaceDetector face_detector = new FaceDetector(background_image.getWidth(), background_image.getHeight(), MAX_FACES);
        faces = new FaceDetector.Face[MAX_FACES];
        // The bitmap must be in 565 format (for now).
        face_count = face_detector.findFaces(background_image, faces);

        draw();
        timeEnd = System.currentTimeMillis();
        timeElapsed = timeEnd - timeStart;
    }

    public void draw() {
        Log.i(TAG, "entered on draw");
        Bitmap temp = Bitmap.createBitmap(background_image.getWidth(),background_image.getHeight(),Bitmap.Config.RGB_565);
        canvas = new Canvas(temp);


        Log.i(TAG, "entered on draw2");
        canvas.drawBitmap(background_image, 0, 0, null);
        Log.i(TAG, "entered on draw3");

        Paint myPaint = new Paint();
        float myEyesDistance;
        myPaint.setColor(Color.GREEN);
        myPaint.setStyle(Paint.Style.STROKE);
        myPaint.setStrokeWidth(6);

        Log.i(TAG, "Opening mGray");
        mGray = Imgcodecs.imread(filepath, Imgcodecs.CV_LOAD_IMAGE_GRAYSCALE);
        Log.i(TAG, "mGray opened");
        Rect rectCrop;
        Mat image_roi;
        String dir;
        Log.i(TAG, "Starting to crop");
        for (int i = 0; i < face_count; i++) {
            FaceDetector.Face face = faces[i];
            PointF myMidPoint = new PointF();
            face.getMidPoint(myMidPoint);
            myEyesDistance = face.eyesDistance();
            canvas.drawRect(
                    (int) (myMidPoint.x - myEyesDistance * 1.3),
                    (int) (myMidPoint.y - myEyesDistance * 1.3),
                    (int) (myMidPoint.x + myEyesDistance * 1.3),
                    (int) (myMidPoint.y + myEyesDistance * 1.3),
                    myPaint
            );
            Log.i(TAG, "Crop loop " + i);
            rectCrop = new Rect(
                    (int) (myMidPoint.x - myEyesDistance * 1.3),
                    (int) (myMidPoint.y - myEyesDistance * 1.3),
                    (int) (2*myEyesDistance * 1.3),
                    (int) (2*myEyesDistance * 1.3)
            );

            //Check if the crop rectangle is outside the bounds of the image
            if(rectCrop.x < 0){
                rectCrop.x = 0;
            }
            if(rectCrop.y < 0){
                rectCrop.y = 0;
            }
            if (rectCrop.x + rectCrop.width > mGray.width()){
                rectCrop.width = mGray.width() - rectCrop.x;
            }
            if (rectCrop.y + rectCrop.height > mGray.height()){
                rectCrop.height = mGray.height() - rectCrop.y;
            }

            image_roi = mGray.submat(rectCrop);
            dir = "sdcard/CS198Crops/"+i+".jpg";
            Imgcodecs.imwrite(dir, image_roi);

        }

        faceCanvas.setImageBitmap(temp);

    }

    static public boolean deleteDirectory(File path) {
        if (path.exists()) {
            File[] files = path.listFiles();
            for (int i = 0; i < files.length; i++) {
                if (files[i].isDirectory()) {
                    deleteDirectory(files[i]);
                } else {
                    files[i].delete();
                }
            }
        }
        return (path.delete());
    }

    /*
    @Override
    public void onResume()
    {
        super.onResume();
        if (!OpenCVLoader.initDebug()) {
            Log.d(TAG, "Internal OpenCV library not found. Using OpenCV Manager for initialization");
            OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_3_0_0, this, mLoaderCallback);
        } else {
            Log.d(TAG, "OpenCV library found inside package. Using it!");
            mLoaderCallback.onManagerConnected(LoaderCallbackInterface.SUCCESS);
        }
    }
    */

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
