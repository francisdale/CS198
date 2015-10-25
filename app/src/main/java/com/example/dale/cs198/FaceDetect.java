package com.example.dale.cs198;

import android.content.Intent;
import android.graphics.Point;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.media.Image;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Display;
import android.view.Menu;
import android.view.MenuItem;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PointF;
import android.media.FaceDetector;
import android.os.Environment;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;


public class FaceDetect extends AppCompatActivity {

    ImageView faceCanvas;

    String filepath;
    private static final String TAG = "testMessage: ";

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
        super.onCreate(savedInstanceState);

        Intent intent = getIntent();
        filepath = intent.getStringExtra("filepath");
        //setContentView(new Face_Detection_View(this,filepath));

        setContentView(R.layout.activity_face_detect);
        faceCanvas = (ImageView)findViewById(R.id.detected);

        updateImage(filepath);
        draw();


        //faceCanvas.setImageDrawable(Drawable.createFromPath(filepath));
       // Face_Detection_View a = new Face_Detection_View(this,filepath);

       // a = (Face_Detection_View)findViewById(R.id.detected);

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





}
