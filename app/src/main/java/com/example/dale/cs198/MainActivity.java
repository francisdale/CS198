package com.example.dale.cs198;

import android.content.Intent;
import android.database.Cursor;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;

import static org.bytedeco.javacpp.opencv_highgui.imread;


public class MainActivity extends AppCompatActivity {

    private static final String TAG = "testMessage";

    private static final int REQUEST_TAKE_PHOTO = 1;
    private static final int SELECT_PHOTO = 2;
    private static final int FACE_DETECT = 3;

    private String name;
    private String dir;
    private String selectedImagePath;
    private ImageView imageView;
    //private Face_Detection_View fd;
    private static final String JPEG_FILE_PREFIX = "IMG_";
    private static final String JPEG_FILE_SUFFIX = ".jpg";

    TaskData td = new TaskData();
    FaceDetectTask fd = new FaceDetectTask(td,this,FaceDetectTask.TRAIN_USAGE);

    //for debugging lang yung mga may LOG

    private static final String trainingDir = "sdcard/CS198/faceDatabase";

    @Override
    //came from activity template
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Log.i(TAG, "onCreate state na");

        //PUT ALL LISTENERS HERE FOR ALL WIDGETS OF ACTIVITY
        Button camButton = (Button) findViewById(R.id.camButton);
        imageView = (ImageView) findViewById(R.id.imageView);

        camButton.setOnClickListener(
                new Button.OnClickListener() {
                    public void onClick(View v) {
                        //type the things we want to happen
                        //TextView someText = (TextView) findViewById(R.id.someText);
                        //someText.setText("Yeah!");
                        Log.i(TAG, "CALLING CAMERA INTENT=======================================================");
                        dispatchTakePictureIntent();
                        Log.i(TAG, "DONE CAMERA INTENT==========================================================");
                    }
                }
        );


        Button galButton = (Button) findViewById(R.id.galButton);
        galButton.setOnClickListener(
                new Button.OnClickListener() {
                    public void onClick(View v) {
                        //type the things we want to happen
                        //TextView someText = (TextView) findViewById(R.id.someText);
                        //someText.setText("Yooooow!");
                        dispatchSelectPhotoIntent();
                    }
                }
        );


        fd.execute();
    }

    private File getFile(){
        File folder = new File("sdcard/CS198Photos");

        if(folder.exists()==false){
            folder.mkdir();
        }
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = JPEG_FILE_PREFIX + timeStamp + JPEG_FILE_SUFFIX;
        name = imageFileName;
        dir = "sdcard/CS198Photos/"+imageFileName;
        File imageFile = new File(folder,imageFileName);
        return imageFile;
    }

    private void galleryAddPic() {
        Intent mediaScanIntent = new Intent("android.intent.action.MEDIA_SCANNER_SCAN_FILE");
        File f = new File(dir);
        Uri contentUri = Uri.fromFile(f);
        mediaScanIntent.setData(contentUri);
        this.sendBroadcast(mediaScanIntent);
    }

    private void dispatchSelectPhotoIntent(){
        Log.i(TAG, "DispatchSelectPhotoIntent");
        Intent selectFromGallery = new Intent(Intent.ACTION_PICK);
        selectFromGallery.setType("image/*");
        //selectFromGallery.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(selectFromGallery, SELECT_PHOTO);
    }


    private void dispatchTakePictureIntent() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        File file = getFile();
        takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(file));
        startActivityForResult(takePictureIntent, REQUEST_TAKE_PHOTO);
    }

    public void dispatchFaceDetectHaarActivityIntent(View view){
        Log.i(TAG, "dispatchFaceDetectHaarActivityIntent");
        Intent faceDetect = new Intent(MainActivity.this,FaceDetect.class);
        faceDetect.putExtra("filepath", selectedImagePath);
        faceDetect.putExtra("detectType", 0);
        startActivity(faceDetect);
    }

    public void dispatchFaceDetectLBPActivityIntent(View view){
        Log.i(TAG, "dispatchFaceDetectLBPActivityIntent");
        Intent faceDetect = new Intent(MainActivity.this,FaceDetect.class);
        faceDetect.putExtra("filepath", selectedImagePath);
        faceDetect.putExtra("detectType", 1);
        startActivity(faceDetect);
    }

    public void dispatchFaceDetectAndroidActivityIntent(View view){
        Log.i(TAG, "dispatchFaceDetectAndroidActivityIntent");
        Intent faceDetect = new Intent(MainActivity.this,FaceDetect.class);
        faceDetect.putExtra("filepath",selectedImagePath);
        faceDetect.putExtra("detectType", 2);
        startActivity(faceDetect);
    }

    public void dispatchFaceDetectHaar20ActivityIntent(View view){
        Log.i(TAG, "dispatchFaceDetectHaar20ActivityIntent");
        Intent faceDetect = new Intent(MainActivity.this,FaceDetect.class);
        faceDetect.putExtra("filepath", selectedImagePath);
        faceDetect.putExtra("detectType", 3);
        startActivity(faceDetect);
    }

    public void dispatchFaceRecogTrainActivityIntent(View view){
        Log.i(TAG, "dispatchFaceRecogTrainActivityIntent");
        Intent faceRecogTrain = new Intent(MainActivity.this,JavaCVTrainFaceRecognizerTest.class);
        startActivity(faceRecogTrain);
    }

    public void dispatchFaceRecogActivityIntent(View view){
        Intent faceRecog = new Intent(MainActivity.this,JavaCVFaceRecognizerTest.class);
        faceRecog.putExtra("filepath", selectedImagePath);
        startActivity(faceRecog);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data){

        if(resultCode == RESULT_OK){
            if(requestCode == REQUEST_TAKE_PHOTO){
                String path = "sdcard/CS198Photos/"+name;
                selectedImagePath="sdcard/CS198Photos/"+name;
                imageView.setImageDrawable(Drawable.createFromPath(path));
                galleryAddPic();
                td.detectQueue.add(imread(selectedImagePath));
                Log.i(TAG, "MainActivity, Request Take Photo: Added image to detectQueue. Its size is now " + td.detectQueue.size());
            }

            if(requestCode == SELECT_PHOTO){
                Log.i(TAG, "MainActivity: now in Select Photo");
                Uri selectedImageUri = data.getData();
                selectedImagePath = getPath(selectedImageUri);
                Log.i(TAG, "Selected URI: "+selectedImagePath);
                imageView.setImageDrawable(Drawable.createFromPath(selectedImagePath));
                Log.i(TAG, "MainActivity: about to add to detectQueue");
                td.detectQueue.add(imread(selectedImagePath));
                Log.i(TAG, "MainActivity, Select Photo: Added image to detectQueue. Its size is now " + td.detectQueue.size());
            }

        }
    }


    public String getPath(Uri uri) {
        String[] projection = { MediaStore.Images.Media.DATA };
        Cursor cursor = managedQuery(uri, projection, null, null, null);
        if(cursor!=null)
        {
            //HERE YOU WILL GET A NULLPOINTER IF CURSOR IS NULL
            //THIS CAN BE, IF YOU USED OI FILE MANAGER FOR PICKING THE MEDIA
            int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
            cursor.moveToFirst();
            return cursor.getString(column_index);
        }
        else return null;
    }

    @Override
    protected void onStart() {
        super.onStart();
        Log.i(TAG, "onStart state na");
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        Log.i(TAG, "onCreateOptionsMenu state na");
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
        //git test
        //To infinity and beyond
        //another comment for testing
        Log.i(TAG, "onOptionsItemSelected state na");
        return super.onOptionsItemSelected(item);
    }



    @Override
    protected void onResume() {
        super.onResume();
        Log.i(TAG, "onResume");
    }


    @Override
    protected void onPause() {
        super.onPause();
        Log.i(TAG, "onPause");
    }


    @Override
    protected void onStop() {
        super.onStop();
        Log.i(TAG, "onStop");
        //notifyAll();
    }


    @Override
    protected void onRestart() {
        super.onRestart();
        Log.i(TAG, "onRestart");
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.i(TAG, "onDestroy");
    }


    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        Log.i(TAG, "onSaveInstanceState");
    }


    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        Log.i(TAG, "onRestoreInstanceState");
    }

}
