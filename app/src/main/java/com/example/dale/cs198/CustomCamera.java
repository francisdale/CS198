package com.example.dale.cs198;

import android.app.Activity;
import android.content.ClipData;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.database.Cursor;
import android.hardware.Camera;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import static org.bytedeco.javacpp.opencv_highgui.imread;

public class CustomCamera extends AppCompatActivity implements SurfaceHolder.Callback {

    private static final String TAG = "testMessage";
    private final String tempImgDir = "sdcard/PresentData/temp.jpg";
    private static final int SELECT_PHOTO = 2;
    private int MODE;

    TextView status;
    ImageButton capture;
    ImageButton gallery;
    ImageButton home;
    SurfaceView surfaceView;
    SurfaceHolder surfaceHolder;

    int detectUsage;
    String className;

    Camera camera;
    Camera.PictureCallback jpegCallback;
    Camera.ShutterCallback shutterCallback;

    File capturedImage = new File(tempImgDir);
    FileOutputStream fileOutputStream;

    boolean isGalleryOpen = false;

    //Sensor accelerometer;
    //SensorManager sensorManager;

    TaskData td;
    FaceDetectTask fd;
    FaceRecogTask fr;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.i(TAG, "CustomCamera onCreate");
        setContentView(R.layout.activity_custom_camera);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);

        Intent intent = getIntent();
        detectUsage = intent.getIntExtra("detectUsage", 1);

        DisplayMetrics dm = new DisplayMetrics();
        Activity a = CustomCamera.this;
        a.getWindowManager().getDefaultDisplay().getMetrics(dm);

        Log.i(TAG, "Default display:" + dm.widthPixels + " x " + dm.heightPixels);

        status = (TextView) findViewById(R.id.customCameraStatus);
        capture = (ImageButton) findViewById(R.id.captureButton);
        gallery = (ImageButton) findViewById(R.id.galleryButton);
        home = (ImageButton)findViewById(R.id.homeButton);

        surfaceView = (SurfaceView) findViewById(R.id.surfaceView);
        surfaceHolder = surfaceView.getHolder();
        surfaceHolder.addCallback(this);
        surfaceHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);

        td = new TaskData();
        fd = new FaceDetectTask(td, this, detectUsage);

        if (detectUsage == FaceDetectTask.ATTENDANCE_USAGE) {
            className = intent.getStringExtra("classNameString");
            status.setText(className);
            fr = new FaceRecogTask(td, this, className);
        }

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

        home.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent cardHome = new Intent(CustomCamera.this, CardHome.class);
                startActivity(cardHome);
            }
        });

        jpegCallback = new Camera.PictureCallback() {
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
                } catch (Exception e) {
                }

                //refreshCamera();

                /*
                Bitmap bmp= BitmapFactory.decodeByteArray(data, 0, data.length);
                Mat m = new Mat(bmp.getHeight(), bmp.getWidth(), CV_8U);
                bmp.copyPixelsToBuffer(m.getByteBuffer());
                td.detectQueue.add(m);
                */

                FileOutputStream fileOutputStream = null;
                File imageFile = getDirectory();
                if (!imageFile.exists() && !imageFile.mkdirs()) {
                    Toast.makeText(getApplicationContext(), "Can't create directory to save image", Toast.LENGTH_SHORT).show();
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
            }
        };

        fd.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        if (detectUsage == FaceDetectTask.ATTENDANCE_USAGE) {
            //fr.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);

        }

        Log.i(TAG, "CustomCamera onCreate done");
    }

    public void refreshGallery(File file) {
        Intent intent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
        intent.setData(Uri.fromFile(file));
        sendBroadcast(intent);
    }

    public void refreshCamera() {
        if (surfaceHolder.getSurface() == null) {
            return;
        }
        try {
            camera.stopPreview();
        } catch (Exception e) {
        }

        try {
            camera.setPreviewDisplay(surfaceHolder);
            camera.startPreview();
        } catch (Exception e) {
        }
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        //Threads still don't die at activity closing
        //Gallery won't allow selecting one photo only

        if (resultCode == RESULT_OK) {
            if (requestCode == SELECT_PHOTO) {
                isGalleryOpen = false;
                /*
                ArrayList<Uri> fileList = data.getParcelableArrayListExtra(Intent.ACTION_PICK);
                for(int i=0;i<fileList.size();i++){
                    Log.i(TAG,"fileList:" + fileList.get(i).toString());
                    td.detectQueue.add(imread(fileList.get(i).toString()));
                    //do something
                }
                Toast.makeText(getApplicationContext(), "The images you selected are now being analyzed.", Toast.LENGTH_LONG).show();
                */
                Log.i(TAG, "Getting ClipData...");
                ClipData clipData = data.getClipData();
                if (clipData == null) {
                    Log.i(TAG, "SELECTED NOTHING");
                } else {
                    for (int i = 0; i < clipData.getItemCount(); i++) {
                        ClipData.Item item = clipData.getItemAt(i);
                        Uri uri = item.getUri();
                        Log.i(TAG, "Selected URI: " + getPath(uri));
                        //pass the getPath(uri) to the thread
                        td.detectQueue.add(imread(getPath(uri)));
                        Log.i(TAG, "CustomCamera Gallery: Added image to detectQueue. i = " + (i + 1));
                    }
                    Log.i(TAG, "CustomCamera Gallery: Done filling queue with gallery pics.");

                    Toast.makeText(getApplicationContext(), "The images you selected are now being analyzed.", Toast.LENGTH_LONG).show();
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
        } else {
            return null;
        }
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        //open the camera
        try {
            camera = Camera.open();
        } catch (RuntimeException ex) {
            Log.i(TAG, "Error opening the camera");
        }

        Camera.Parameters parameters;
        parameters = camera.getParameters();
        parameters.setPreviewFrameRate(20);
        parameters.setPreviewSize(1152,864);

        Log.i(TAG,"Surface View Size: " + surfaceView.getWidth() + " x " + surfaceView.getHeight());
        Log.i(TAG,"Preview Size default: " + parameters.getPreviewSize().width + " x " + parameters.getPreviewSize().height);
        Log.i(TAG, "Default picture size " + parameters.getPictureSize().width + " x " + parameters.getPictureSize().height);

        camera.setParameters(parameters);
        //camera.setDisplayOrientation(180);
        try {
            //drawing the camera sa surace view
            camera.setPreviewDisplay(surfaceHolder);
            camera.startPreview();
        } catch (Exception e) {
            Log.i(TAG, "Error drawing the camera sa surfaceholder");
        }
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
//        Camera.Parameters myParameters = camera.getParameters();
//        Camera.Size myBestSize = getBestPreviewSize(width, height, myParameters);
//
//        if (myBestSize != null) {
//            myParameters.setPreviewSize(myBestSize.width, myBestSize.height);
//            Toast.makeText(getApplicationContext(), "Camera Preview Size:\n" + String.valueOf(myBestSize.width) + " : " +
//                    String.valueOf(myBestSize.height), Toast.LENGTH_LONG).show();
//            //setMyPreviewSize(width, height);
//        }
//        camera.setParameters(myParameters);

        refreshCamera();
    }

    private Camera.Size getBestPreviewSize(int width, int height, Camera.Parameters parameters) {
        Camera.Size bestSize = null;
        List<Camera.Size> sizeList = parameters.getSupportedPreviewSizes();

        bestSize = sizeList.get(0);

        for (int i = 1; i < sizeList.size(); i++) {
            if ((sizeList.get(i).width * sizeList.get(i).height) >
                    (bestSize.width * bestSize.height)) {
                bestSize = sizeList.get(i);
            }
        }

        return bestSize;
    }

    private void setMyPreviewSize(int width, int height) {
        // Get the set dimensions
        float newProportion = (float) width / (float) height;

        // Get the width of the screen
        int screenWidth = getWindowManager().getDefaultDisplay().getWidth();
        int screenHeight = getWindowManager().getDefaultDisplay().getHeight();
        float screenProportion = (float) screenWidth / (float) screenHeight;

        // Get the SurfaceView layout parameters
        ViewGroup.LayoutParams lp = surfaceView.getLayoutParams();
        Log.i(TAG, "Layout Params before change: " + lp.width + " x " + lp.height);
        if (newProportion > screenProportion) {
            lp.width = screenWidth;
            lp.height = (int) ((float) screenWidth / newProportion);
        } else {
            lp.width = (int) (newProportion * (float) screenHeight);
            lp.height = screenHeight;
        }
        Log.i(TAG, "Layout Params: " + lp.width + " x " + lp.height);
        // Commit the layout parameters


        //surfaceView.setLayoutParams(lp);
    }


    private File getDirectory() {
        File dir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM);
        return new File(dir, "PresentData");
    }

    public void captureImage() {
        camera.takePicture(null, null, jpegCallback);
    }

    private void dispatchSelectPhotoIntent() {
        Log.i(TAG, "DispatchSelectPhotoIntent");
        Intent selectFromGallery = new Intent(Intent.ACTION_PICK);
        selectFromGallery.setType("image/*");
        selectFromGallery.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
        selectFromGallery.setAction(Intent.ACTION_GET_CONTENT);
        isGalleryOpen = true;
        startActivityForResult(Intent.createChooser(selectFromGallery, "Select Picture"), SELECT_PHOTO);
        Toast.makeText(getApplicationContext(), "Select and hold the photos to be analyze.", Toast.LENGTH_SHORT).show();
        //startActivityForResult(selectFromGallery, SELECT_PHOTO);
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        camera.stopPreview();
        camera.release();
        camera = null;
    }

    @Override
    protected void onRestart() {
        super.onRestart();

        Log.i(TAG, "CustomCamera onRestart");
        if(fd.getStatus() != AsyncTask.Status.RUNNING) {
            fd.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
            if (detectUsage == FaceDetectTask.ATTENDANCE_USAGE) {
                //fr.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
            }
        }
        Log.i(TAG, "CustomCamera onRestart End");

    }

    @Override
    protected void onPause() {
        super.onPause();
        Log.i(TAG, "CustomCamera onPause");
    }

    @Override
    protected void onStop() {
        super.onStop();

        Log.i(TAG, "CustomCamera onStop");
        if(!isGalleryOpen) {
            td.setThreadsToDie();
        }

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.i(TAG, "CustomCamera onDestroy");
        if(!isGalleryOpen) {
            td.setThreadsToDie();
        }
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