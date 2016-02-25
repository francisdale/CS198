package com.example.dale.cs198;

import android.content.ClipData;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.database.Cursor;
import android.hardware.Camera;
import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.FileOutputStream;

import static org.bytedeco.javacpp.opencv_highgui.imread;

public class CustomCamera extends AppCompatActivity implements SurfaceHolder.Callback {

    private static final String TAG = "testMessage";
    private final String tempImgDir = "sdcard/PresentData/temp.jpg";
    private static final int SELECT_PHOTO = 2;
    private int MODE;

    TextView status;
    ImageButton capture;
    ImageButton gallery;
    SurfaceView surfaceView;
    SurfaceHolder surfaceHolder;

    int detectUsage;

    Camera camera;
    Camera.PictureCallback jpegCallback;
    Camera.ShutterCallback shutterCallback;

    File capturedImage = new File(tempImgDir);
    FileOutputStream fileOutputStream;

    Sensor accelerometer;
    SensorManager sensorManager;

    TaskData td;
    FaceDetectTask fd;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_custom_camera);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);

        Intent intent = getIntent();
        detectUsage = intent.getIntExtra("detectUsage", 1);
        //accept mode from either train or classAdapter

        status = (TextView)findViewById(R.id.custom_camera_status);
        capture = (ImageButton)findViewById(R.id.capture_button);
        gallery = (ImageButton)findViewById(R.id.gallery_button);

        surfaceView = (SurfaceView)findViewById(R.id.surfaceView);
        surfaceHolder = surfaceView.getHolder();
        surfaceHolder.addCallback(this);
        surfaceHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);

        td = new TaskData();
        fd = new FaceDetectTask(td,this,detectUsage);

        capture.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                captureImage();
            }
        });

        gallery.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dispatchSelectPhotoIntent();
            }
        });

        jpegCallback=new Camera.PictureCallback() {
            @Override
            public void onPictureTaken(byte[] data, Camera camera) {
                try {
                    //THIS IS WHERE IMAGES SAVED ON DCIM/PRESENTDATA
                    fileOutputStream = new FileOutputStream(capturedImage);
                    fileOutputStream.write(data);
                    fileOutputStream.close();

                    td.detectQueue.add(imread(tempImgDir));
                    capturedImage.delete();
                    Log.i(TAG, "CustomCamera: Added image to detectQueue. Its size is now " + td.detectQueue.size());
                } catch (Exception e) {}

                refreshCamera();

                /*
                Bitmap bmp= BitmapFactory.decodeByteArray(data, 0, data.length);
                Mat m = new Mat(bmp.getHeight(), bmp.getWidth(), CV_8U);
                bmp.copyPixelsToBuffer(m.getByteBuffer());
                td.detectQueue.add(m);
                */
                /*
                FileOutputStream fileOutputStream=null;
                File imageFile = getDirectory();
                if(!imageFile.exists() && !imageFile.mkdirs()){
                    Toast.makeText(getApplicationContext(),"Can't create directory to save image", Toast.LENGTH_SHORT).show();
                    return;
                }
                SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyymmddhhss");
                String date = simpleDateFormat.format(new Date());
                String photoFormat = "PresentData_" + date + ".jpg";
                String fileName = imageFile.getAbsolutePath() + "/" + photoFormat;
                File image = new File(fileName);
                try {
                    //THIS IS WHERE IMAGES SAVED ON DCIM/PRESENTDATA
                    fileOutputStream = new FileOutputStream(image);
                    fileOutputStream.write(data);
                    fileOutputStream.close();
                } catch (FileNotFoundException e) {
                } catch (IOException e) {
                } finally {
                }
                //imageFile.getAbsolutePath() --> THIS IS WHERE THE PICTURES ARE GOING
                //info.setText(photoFormat);
                Toast.makeText(getApplicationContext(), fileName + " saved!", Toast.LENGTH_SHORT).show();
                refreshCamera();
                refreshGallery(image);
                */
            }
        };


//        sensorManager = (SensorManager)getSystemService(SENSOR_SERVICE);
//        accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
//        CameraAccelerometer cameraAccelerometer = new CameraAccelerometer(accelerometer,sensorManager);
        //START THE THREADS HERE!!!

    }

    public void refreshGallery(File file){
        Intent intent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
        intent.setData(Uri.fromFile(file));
        sendBroadcast(intent);
    }

    public void refreshCamera(){
        if(surfaceHolder.getSurface() == null){
            return;
        }

        try{
            camera.stopPreview();
        }catch (Exception e){}

        try{
            camera.setPreviewDisplay(surfaceHolder);
            camera.startPreview();
        }catch (Exception e){}


    }

    private File getDirectory(){
        File dir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM);
        return new File(dir,"PresentData");
    }

    public void captureImage(){
        camera.takePicture(null, null, jpegCallback);
    }

    private void dispatchSelectPhotoIntent(){
        Log.i(TAG, "DispatchSelectPhotoIntent");
        Intent selectFromGallery = new Intent(Intent.ACTION_PICK);
        selectFromGallery.setType("image/*");
        selectFromGallery.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
        selectFromGallery.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(selectFromGallery, "Select Picture"), SELECT_PHOTO);
        //startActivityForResult(selectFromGallery, SELECT_PHOTO);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data){
        if(resultCode == RESULT_OK){
            if(requestCode == SELECT_PHOTO){
                ClipData clipData = data.getClipData();
                if(clipData == null){
                    Log.i(TAG, "SELECTED NOTHING");
                }else{
                    for(int i=0; i<clipData.getItemCount(); i++){
                        ClipData.Item item = clipData.getItemAt(i);
                        Uri uri = item.getUri();
                        Log.i(TAG, "Selected URI: "+getPath(uri));

                        //pass the getPath(uri) to the thread

                    }
                    Toast.makeText(getApplicationContext(), "The images you selected are now being analyze.", Toast.LENGTH_LONG).show();
                }
            }
        }
    }

    public String getPath(Uri uri) {
        String[] projection = {MediaStore.Images.Media.DATA};
        Cursor cursor = managedQuery(uri, projection, null, null, null);
        if (cursor != null) {
            //HERE YOU WILL GET A NULLPOINTER IF CURSOR IS NULL
            //THIS CAN BE, IF YOU USED OI FILE MANAGER FOR PICKING THE MEDIA
            int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
            cursor.moveToFirst();
            return cursor.getString(column_index);
        }
        else{
            return null;
        }
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        //open the camera
        try{
            camera = Camera.open();
        }catch (RuntimeException ex){
            Log.i(TAG, "Error opening the camera");
        }
        Camera.Parameters parameters;
        parameters = camera.getParameters();
        //modifying the camera's parameters
        parameters.setPreviewFrameRate(20);
        parameters.setPreviewSize(1000,250);
        camera.setParameters(parameters);
        //camera.setDisplayOrientation(180);
        try {
            //drawing the camera sa surace view
            camera.setPreviewDisplay(surfaceHolder);
            camera.startPreview();
        }catch (Exception e){
            Log.i(TAG, "Error drawing the camera sa surfaceholder");
        }


    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
        refreshCamera();
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        camera.stopPreview();
        camera.release();
        camera = null;
    }

    @Override
    protected void onStart() {
        super.onStart();
        Log.i(TAG, "CustomCamera onStart");
        fd.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }

    @Override
    protected void onPause() {
        super.onPause();
        Log.i(TAG, "CustomCamera onPause");
        td.setIsUIOpenedToFalse();
    }

    @Override
    protected void onStop() {
        super.onStop();
        Log.i(TAG, "CustomCamera onStop");
        td.setIsUIOpenedToFalse();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.i(TAG, "CustomCamera onDestroy");
        td.setIsUIOpenedToFalse();
    }

    //////////////////////////////////////////////////////////////////////////////////////
    ////////////////////////////////////SENSOR CLASS//////////////////////////////////////
    /////////////////////////////////////////////////////////////////////////////////////
    /*
    public class CameraAccelerometer implements SensorEventListener {
        //Sensor accelerometer;
        //SensorManager sensorManager;
        public CameraAccelerometer(Sensor accelerometer,SensorManager sensorManager) {
            sensorManager.registerListener(this,accelerometer,SensorManager.SENSOR_DELAY_NORMAL);
        }
        @Override
        public void onSensorChanged(SensorEvent event) {
//            //info.setText("X: "+event.values[0]+
//            "\nY: "+event.values[1]+
//            "\nZ: "+event.values[2]);
        }
        @Override
        public void onAccuracyChanged(Sensor sensor, int accuracy) {
        }
    }
    //////////////////////////////////////////////////////////////////////////////////////
    ////////////////////////////////////SENSOR CLASS//////////////////////////////////////
    /////////////////////////////////////////////////////////////////////////////////////
    */
}