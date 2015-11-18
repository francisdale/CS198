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

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.CameraBridgeViewBase;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.android.Utils;
import org.opencv.core.Mat;
import org.opencv.core.MatOfRect;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;
import org.opencv.objdetect.CascadeClassifier;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;


public class FaceDetect extends AppCompatActivity {

    private static final Scalar FACE_RECT_COLOR     = new Scalar(0, 255, 0, 255);
    public static final int        JAVA_DETECTOR       = 0;
    public static final int        NATIVE_DETECTOR     = 1;

    private MenuItem               mItemFace50;
    private MenuItem               mItemFace40;
    private MenuItem               mItemFace30;
    private MenuItem               mItemFace20;
    private MenuItem               mItemType;

    private Mat mRgba;
    private Mat                    mGray;
    private File mCascadeFile;
    private CascadeClassifier mJavaDetector;

    private int                    mDetectorType       = JAVA_DETECTOR;
    private String[]               mDetectorName;

    private float                  mRelativeFaceSize   = 0.2f;
    private int                    mAbsoluteFaceSize   = 0;

    private CameraBridgeViewBase mOpenCvCameraView;

    private BaseLoaderCallback mLoaderCallback = new BaseLoaderCallback(this) {
        @Override
        public void onManagerConnected(int status) {
            switch (status) {
                case LoaderCallbackInterface.SUCCESS:
                {
                    Log.i(TAG, "OpenCV loaded successfully");

                    // Load native library after(!) OpenCV initialization
                    System.loadLibrary("detection_based_tracker");

                    try {
                        // load cascade file from application resources
                        InputStream is = getResources().openRawResource(R.raw.haarcascade_frontalface_default);
                        File cascadeDir = getDir("cascade", Context.MODE_PRIVATE);
                        mCascadeFile = new File(cascadeDir, "haarcascade_frontalface_default.xml");
                        FileOutputStream os = new FileOutputStream(mCascadeFile);

                        byte[] buffer = new byte[4096];
                        int bytesRead;
                        while ((bytesRead = is.read(buffer)) != -1) {
                            os.write(buffer, 0, bytesRead);
                        }
                        is.close();
                        os.close();

                        mJavaDetector = new CascadeClassifier(mCascadeFile.getAbsolutePath());
                        if (mJavaDetector.empty()) {
                            Log.e(TAG, "Failed to load cascade classifier");
                            mJavaDetector = null;
                        } else
                            Log.i(TAG, "Loaded cascade classifier from " + mCascadeFile.getAbsolutePath());



                        cascadeDir.delete();

                    } catch (IOException e) {
                        e.printStackTrace();
                        Log.e(TAG, "Failed to load cascade. Exception thrown: " + e);
                    }

                    /*
                    Log.i(TAG, "Enabling opencvcameraview");
                    mOpenCvCameraView.enableView();
                    Log.i(TAG, "view enabled");
                    */

                } break;
                default:
                {
                    super.onManagerConnected(status);
                } break;
            }
        }
    };


    // Old variables:

    ImageView faceCanvas;

    String filepath;
    private static final String TAG = "testMessage";

    /**===========================================================**/
    private static final int MAX_FACES = 100;
    private Bitmap background_image;
    private SurfaceHolder surfaceHolder;
    private FaceDetector.Face[] faces;
    private int face_count;

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
        //setContentView(new Face_Detection_View(this,filepath));

        setContentView(R.layout.activity_face_detect);

        if (!OpenCVLoader.initDebug()) {
            Log.d(TAG, "Internal OpenCV library not found. Using OpenCV Manager for initialization");
            OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_3_0_0, this, mLoaderCallback);
        } else {
            Log.d(TAG, "OpenCV library found inside package. Using it!");
            mLoaderCallback.onManagerConnected(LoaderCallbackInterface.SUCCESS);
        }
        Log.i(TAG, "LoaderCallback done");
        faceCanvas = (ImageView)findViewById(R.id.detected);
        Log.i(TAG, "about to start updateimage");
        updateImage(filepath);
        //draw();


        //faceCanvas.setImageDrawable(Drawable.createFromPath(filepath));
       // Face_Detection_View a = new Face_Detection_View(this,filepath);

       // a = (Face_Detection_View)findViewById(R.id.detected);
        Log.i(TAG, "onCreate end");
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

    public void updateImage(String path) {
        Log.i(TAG, "updateimage now");

        //Loading the image in grayscale:
        mGray = Imgcodecs.imread(path, Imgcodecs.CV_LOAD_IMAGE_GRAYSCALE);

        //Loading the image in color; need to convert color scheme because imread() uses bga, not rgba:
        Mat mBga = Imgcodecs.imread(path);
        mRgba = new Mat(); //RGBA format
        Imgproc.cvtColor(mBga, mRgba, Imgproc.COLOR_BGR2RGBA);

        MatOfRect faces = new MatOfRect();

        Log.i(TAG, "updateimage mid 1");
        Log.i(TAG, path);
        // TODO: objdetect.CV_HAAR_SCALE_IMAGE

        if ( mGray.empty() )   {  Log.i(TAG, "mGray is empty"); } else {Log.i(TAG, "mGray is not empty");}
        if ( mJavaDetector.empty() )   {  Log.i(TAG, "mJavaDetector is empty"); } else {Log.i(TAG, "mJavaDetector is not empty");}

        mJavaDetector.detectMultiScale(mGray, faces, 1.1, 2, 2, new Size(mAbsoluteFaceSize, mAbsoluteFaceSize), new Size());

        Log.i(TAG, "updateimage mid 2");
        Rect[] facesArray = faces.toArray();
        Log.i(TAG, "updateimage mid 3");

        File folder = new File("sdcard/CS198Crops");

        folder.delete();
        folder.mkdir();


        String dir;
        Rect rectCrop = null;
        Mat image_roi = null;
        /*
        for (Rect rect : facesArray) {
            //timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
            //imageFileName = JPEG_FILE_PREFIX + timeStamp + JPEG_FILE_SUFFIX;
            //Imgproc.rectangle(mGray, new Point(rect.x, rect.y), new Point(rect.x + rect.width, rect.y + rect.height), new Scalar(0, 255, 0));
            rectCrop = new Rect(rect.x, rect.y, rect.width, rect.height);

        }
        */
        Log.i(TAG, "updateimage mid 4");
        for(int i = 0; i < facesArray.length; i++) {
            rectCrop = new Rect(facesArray[i].x, facesArray[i].y, facesArray[i].width, facesArray[i].height);
            image_roi = mGray.submat(rectCrop);
            dir = "sdcard/CS198Crops/img"+i+".jpg";
            Imgcodecs.imwrite(dir, image_roi);
            Log.i(TAG, "updateimage loop " + i);
        }
        //end of cropping

        for (int i = 0; i < facesArray.length; i++)
            Imgproc.rectangle(mRgba, facesArray[i].tl(), facesArray[i].br(), FACE_RECT_COLOR, 3);

        face_count = facesArray.length;
        Log.i(TAG, "updateimage mid 5");

        // convert to bitmap:
        Bitmap bm = Bitmap.createBitmap(mRgba.cols(), mRgba.rows(), Bitmap.Config.ARGB_8888);
        Utils.matToBitmap(mRgba, bm);

        // find the imageview and draw it!
        faceCanvas.setImageBitmap(bm);
        Log.i(TAG, "updateimage done");
    }

    public void updateImageOld(String path) {
        // Set internal configuration to RGB_565
        Log.i(TAG, "updateimage na");
        BitmapFactory.Options bitmap_options = new BitmapFactory.Options();
        Log.i(TAG, "updateimage na2");
        bitmap_options.inPreferredConfig = Bitmap.Config.RGB_565;
        Log.i(TAG, "updateimage n3");
        background_image = BitmapFactory.decodeFile(path, bitmap_options);
        Log.i(TAG, "updateimage na4");

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

        Log.i(TAG, "size " + background_image.getWidth() + "x" + background_image.getHeight());

        FaceDetector face_detector = new FaceDetector(background_image.getWidth(), background_image.getHeight(), MAX_FACES);
        faces = new FaceDetector.Face[MAX_FACES];
        // The bitmap must be in 565 format (for now).
        face_count = face_detector.findFaces(background_image, faces);

        Log.i(TAG,"Face_Detection Face Count: " + String.valueOf(face_count));
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

        Log.i(TAG, "entered on draw4");

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
        }
        Log.i(TAG, "entered on draw5");
        faceCanvas.setImageBitmap(temp);
        Log.i(TAG, "entered on draw6");
    }

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



}
