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

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;


public class MainActivity extends AppCompatActivity {

    private static final String TAG = "testMessage";

    String[] testClassNamesAndDataSplits = {"CS 197,1,10,11,19", "CS 133,1,6,7,10"};
    final String testClassDataDir = "sdcard/PresentData/researchMode";

    private static final int REQUEST_TAKE_PHOTO = 1;
    private static final int SELECT_PHOTO = 2;
    private static final int FACE_DETECT = 3;

    private String name;
    private String dir;
    private String selectedImagePath;
    private String researchDir = "sdcard/PresentData/researchMode/capturedImages";
    private ImageView imageView;
    //private Face_Detection_View fd;
    private static final String JPEG_FILE_PREFIX = "IMG_";
    private static final String JPEG_FILE_SUFFIX = ".jpg";



    @Override
    //came from activity template
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Log.i(TAG, "onCreate state na");

        File folder = new File(researchDir);

        if(folder.exists()==false){
            folder.mkdir();
        }

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

    }

    private File getFile(){

        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = JPEG_FILE_PREFIX + timeStamp + JPEG_FILE_SUFFIX;
        name = imageFileName;
        dir = researchDir+"/"+imageFileName;
        File imageFile = new File(new File(researchDir),imageFileName);
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

    public void dispatchFaceRecogTrainActivityIntent(View view) {
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
                String path = researchDir + "/" + name;
                selectedImagePath= researchDir +"/" + name;
                imageView.setImageDrawable(Drawable.createFromPath(path));
                galleryAddPic();
            }

            if(requestCode == SELECT_PHOTO){
                Log.i(TAG, "MainActivity: now in Select Photo");
                Uri selectedImageUri = data.getData();
                selectedImagePath = getPath(selectedImageUri);
                Log.i(TAG, "Selected URI: " + selectedImagePath);
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
        if (id == R.id.action_relabelAndGather) {

            relabelAndGather();

        } else if (id == R.id.action_testFaceDetect) {

            testFaceDetect();

        } else if (id == R.id.action_testRecogTrain) {

            testRecogTrain();
        } else if (id == R.id.action_testRecog) {

            testRecog();
        }


        //git test
        //To infinity and beyond
        //another comment for testing
        return super.onOptionsItemSelected(item);
    }

    public void relabelAndGather(){


        String classDataDir;
        String allCropsDir;
        String trainingCropsDir;
        String classListFilePath;
        String[] testClassDetails;
        String className;
        int trainStart;
        int trainEnd;

        for(int i = 0; i < testClassNamesAndDataSplits.length; i++) {
            Log.i(TAG, "Loop " + i);
            testClassDetails = testClassNamesAndDataSplits[i].split(",");
            className = testClassDetails[0];
            trainStart = Integer.parseInt(testClassDetails[1]);
            trainEnd = Integer.parseInt(testClassDetails[2]);


            classDataDir = testClassDataDir +"/" + className + " Classroom Data";
            allCropsDir = classDataDir + "/allCrops";
            trainingCropsDir = classDataDir + "/trainingCrops";
            classListFilePath = classDataDir + "/" + className + "_studentList.txt";

            //Read class list:
            BufferedReader br;
            HashMap<Integer, Integer> attendanceRecord = new HashMap<Integer, Integer>(); //This ArrayList is parallel with the attendance ArrayList
            HashMap<Integer, String> studentNumsAndNames = new HashMap<Integer, String>(); //Also parallel with the two ArrayLists above
            String line;
            String[] details;


            try {
                br = new BufferedReader(new FileReader(classListFilePath));
                while ((line = br.readLine()) != null) {
                    details = line.split(",");
                    //a line in the studentList has the syntax: <id>,<student number>,<lastname>,<firstname>
                    attendanceRecord.put(Integer.parseInt(details[0]), 0); //(id, attendance)
                    studentNumsAndNames.put(Integer.parseInt(details[0]), details[1] + "," + details[2] + "," + details[3]); //(id, studentnum+lastname+firstname)
                }
            } catch (Exception e) {
                Log.e(TAG, e.getMessage());
            }


            File[] classDataDirFiles = new File(classDataDir).listFiles();
            File[] crops;
            File tempFile;
            int id;
            int secondId;
            int folderNum;
            String date;
            String[] studentNumAndName;
            String cropNewName;
            String dayFolderDir;
            String[] folderNameDetails;
            String[] cNameDetails;
            String cFirstPartName;

            //Clear old folders and then recreate them:
            DirectoryDeleter.deleteDir(new File(allCropsDir));
            new File(allCropsDir).mkdirs();

            DirectoryDeleter.deleteDir(new File(trainingCropsDir));
            new File(trainingCropsDir).mkdirs();

            FilenameFilter ImgFilter = new FilenameFilter() {
                public boolean accept(File dir, String name) {
                    name = name.toLowerCase();
                    return name.endsWith(".jpg");
                }
            };

            Log.i(TAG, "Creating the AllCrops folder...");
            for (File f : classDataDirFiles) {
                folderNameDetails = f.getName().split("_");

                if (f.isDirectory() && 3 == folderNameDetails.length) {
                    Log.i(TAG, "Processing folder " + f.getName() + "...");
                    dayFolderDir = f.getAbsolutePath();
                    crops = new File(dayFolderDir).listFiles(ImgFilter);
                    folderNum = Integer.parseInt(folderNameDetails[0]);
                    date = folderNameDetails[1];

                    for (File c : crops) {
                        //Changing the filenames of training crops to the right format:


                        if(c.getName().startsWith("0_") || c.getName().startsWith("delete")){
                            secondId = 0;
                            do {
                                cropNewName = "0_nonFace_" + secondId + ".jpg";
                                tempFile = new File(dayFolderDir + "/" + cropNewName);
                                secondId++;
                            } while (tempFile.exists());

                        } else {
                            Log.i(TAG, "Handling id: " + c.getName().split("_")[0]);
                            id = Integer.parseInt(c.getName().split("_")[0]);
                            studentNumAndName = studentNumsAndNames.get(id).split(",");

                            secondId = 0;
                            do {
                                cropNewName = id + "_" + studentNumAndName[1] + "," + studentNumAndName[2] + "_" + secondId + ".jpg";
                                tempFile = new File(dayFolderDir + "/" + cropNewName);
                                secondId++;
                            } while (tempFile.exists());
                        }

                        c.renameTo(tempFile);

                        c = tempFile;
                        cNameDetails = c.getName().split("_");
                        cFirstPartName = cNameDetails[0] + "_" + cNameDetails[1] + "_" + date + "_";

                        //For copying the new crop to allCrops:
                        secondId = 0;
                        do {
                            cropNewName = cFirstPartName + secondId + ".jpg";
                            tempFile = new File(allCropsDir + "/" + cropNewName);
                            secondId++;
                        } while (tempFile.exists());

                        try {
                            copyFile(c, tempFile);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }

                        //For copying the new crop to trainingCrops or testingCrops:
                        if(folderNum >= trainStart && folderNum <= trainEnd){
                            secondId = 0;
                            do {
                                cropNewName = cFirstPartName + secondId + ".jpg";
                                tempFile = new File(trainingCropsDir + "/" + cropNewName);
                                secondId++;
                            } while (tempFile.exists());

                            try {
                                copyFile(c, tempFile);
                            } catch (IOException e) {
                                e.printStackTrace();
                            }

                        }

                    }
                }
            }
        }
        Log.i(TAG, "Relabelling and gathering complete.");
    }

    public void testFaceDetect(){
        Log.i(TAG, "Now in testFaceDetect.");

        TaskData td = new TaskData();

        FaceDetectTask fd = new FaceDetectTask(td, this, FaceDetectTask.TEST_USAGE);
        fd.execute();
        /*
        String[] testClassDetails;
        String className;
        File dataFolder;
        FilenameFilter imgFilter = new FilenameFilter() {
            public boolean accept(File dir, String name) {
                name = name.toLowerCase();
                return name.endsWith(".jpg");
            }
        };
        File[] testImages;
        TaskData td;

        for(int i = 0; i < testClassNamesAndDataSplits.length; i++) {
            Log.i(TAG, "testFaceDetect loop " + i);
            testClassDetails = testClassNamesAndDataSplits[i].split(",");
            className = testClassDetails[0];
            dataFolder = new File(testClassDataDir +"/" + className + " Classroom Data");
            testImages = dataFolder.listFiles(imgFilter);

            td = new TaskData();

            FaceDetectTask fd = new FaceDetectTask(td, this, FaceDetectTask.TEST_USAGE);
            fd.execute();

            int y = 0;
            Log.i(TAG, "Face detect thread executed.");
            Log.i(TAG, "Number of images: " + testImages.length + ".Populating detectQueue...");
            for(File f : testImages){
                td.detectQueue.add(imread(f.getAbsolutePath()));
                y++;
                Log.i(TAG, "Populating detectQueue loop " + y);
            }
            Log.i(TAG, "detectQueue populated.");

            td.setThreadsToDie();

            Log.i(TAG, "Waiting for face detect thread to finish...");
            td.mainThreadWaitsForDetectThreadToDie();
            Log.i(TAG, "Face detect thread finished. Goodbye!");
        }*/
    }

    public void testRecogTrain(){

    }

    public void testRecog(){

    }

    private static void copyFile(File source, File dest) throws IOException {
        InputStream input = null;
        OutputStream output = null;
        try {
            input = new FileInputStream(source);
            output = new FileOutputStream(dest);
            byte[] buf = new byte[1024];
            int bytesRead;
            while ((bytesRead = input.read(buf)) > 0) {
                output.write(buf, 0, bytesRead);
            }
        } finally {
            input.close();
            output.close();
        }
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
