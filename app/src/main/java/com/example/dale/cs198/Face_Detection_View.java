package com.example.dale.cs198;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.PointF;
import android.graphics.drawable.BitmapDrawable;
import android.media.FaceDetector;
import android.os.Environment;
import android.util.AttributeSet;
import android.util.Log;
import android.view.Display;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.WindowManager;
import android.widget.AbsoluteLayout;
import android.widget.Gallery;
import android.widget.ImageView;
import android.widget.LinearLayout;

/**
 * Created by DALE on 10/18/2015.
 */
public class Face_Detection_View extends View {

    private static final String TAG = "testMessage: ";
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


    public Face_Detection_View(Context context,String path) {
        super(context);

        wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        display = wm.getDefaultDisplay();

        //iv = (ImageView)findViewById(R.id.detected);
        //iv.setLayoutParams(new Gallery.LayoutParams(AbsoluteLayout.LayoutParams.WRAP_CONTENT, AbsoluteLayout.LayoutParams.WRAP_CONTENT));

        updateImage(path);
    }


    public void updateImage(String path) {
        // Set internal configuration to RGB_565
        Log.i(TAG, "updateimage na");
        BitmapFactory.Options bitmap_options = new BitmapFactory.Options();
        bitmap_options.inPreferredConfig = Bitmap.Config.RGB_565;
        background_image = BitmapFactory.decodeFile(path, bitmap_options);

        /**================================================**/
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

    @Override
    public void onDraw(Canvas canvas) {
        Log.i(TAG, "entered on draw");

        //canvas = new Canvas(background_image);
        canvas.drawBitmap(background_image, 0, 0, null);
        Log.i(TAG, "entered on draw2");


        Paint myPaint = new Paint();
        float myEyesDistance;
        myPaint.setColor(Color.GREEN);
        myPaint.setStyle(Paint.Style.STROKE);
        myPaint.setStrokeWidth(6);

        Log.i(TAG, "entered on draw3");

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

        //iv.setImageDrawable(new BitmapDrawable(getResources(), background_image));





    }


    public void okay(){
        Log.i(TAG, "fd okay");
    }

}