package com.example.dale.cs198;

import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.hardware.Camera;
import android.net.Uri;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class CustomCamera extends AppCompatActivity implements SurfaceHolder.Callback {

    private static final String TAG = "testMessage";

    TextView info;
    Button capture;
    SurfaceView surfaceView;
    SurfaceHolder surfaceHolder;

    Camera camera;
    Camera.PictureCallback jpegCallback;
    Camera.ShutterCallback shutterCallback;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_custom_camera);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);

        info = (TextView)findViewById(R.id.detected_textView);
        capture = (Button)findViewById(R.id.capture_button);

        surfaceView = (SurfaceView)findViewById(R.id.surfaceView);
        surfaceHolder = surfaceView.getHolder();
        surfaceHolder.addCallback(this);
        surfaceHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);

        capture.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                captureImage();
            }
        });

        jpegCallback=new Camera.PictureCallback() {
            @Override
            public void onPictureTaken(byte[] data, Camera camera) {
                FileOutputStream fileOutputStream=null;
                File imageFile = getDirectory();
                if(!imageFile.exists() && !imageFile.mkdirs()){
                    Toast.makeText(getApplicationContext(),"Can't create directory to save image", Toast.LENGTH_SHORT).show();
                    return;
                }
                SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyymmddhhss");
                String date = simpleDateFormat.format(new Date());
                String photoFormat = "PresentData_"+date+".jpg";
                String fileName = imageFile.getAbsolutePath()+"/"+photoFormat;
                File image = new File(fileName);
                try{
                    fileOutputStream = new FileOutputStream(image);
                    fileOutputStream.write(data);
                    fileOutputStream.close();
                }catch(FileNotFoundException e){}
                catch(IOException e){}
                finally {}

                info.setText(photoFormat);
                Toast.makeText(getApplicationContext(),fileName+" saved!", Toast.LENGTH_SHORT).show();

                refreshCamera();
                refreshGallery(image);

            }
        };
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
        camera.takePicture(null,null,jpegCallback);
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


}
