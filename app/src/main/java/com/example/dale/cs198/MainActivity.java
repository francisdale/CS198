package com.example.dale.cs198;

import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.util.Log;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.Button;
import android.view.View;
import android.widget.TextView;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;


public class MainActivity extends AppCompatActivity {

    private static final String TAG = "testMessage: ";

    private static final int REQUEST_TAKE_PHOTO = 1;
    private static final int SELECT_PHOTO = 2;

    private String name;
    private String dir;
    private String selectedImagePath;
    private ImageView imageView;


    private static final String JPEG_FILE_PREFIX = "IMG_";
    private static final String JPEG_FILE_SUFFIX = ".jpg";

    //for debugging lang yung mga may LOG

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

        Button analyze = (Button) findViewById(R.id.analyze);
        analyze.setOnClickListener(
                new Button.OnClickListener() {
                    public void onClick(View v) {
                       //imageView.setImageDrawable(Drawable.createFromPath("/sdcard/DCIM/samp/sample.jpg"));
                        imageView.setImageDrawable(null);
                    }
                }
        );



    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data){

        if(resultCode == RESULT_OK){
            if(requestCode == REQUEST_TAKE_PHOTO){
                String path = "sdcard/CS198Photos/"+name;
                imageView.setImageDrawable(Drawable.createFromPath(path));
                galleryAddPic();
            }

            if(requestCode == SELECT_PHOTO){
                Uri selectedImageUri = data.getData();
                selectedImagePath = getPath(selectedImageUri);
                imageView.setImageDrawable(Drawable.createFromPath(selectedImagePath));

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
