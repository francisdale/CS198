package com.example.dale.cs198;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.TextView;
import android.widget.Toast;

import com.nbsp.materialfilepicker.utils.FileUtils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.regex.Pattern;

public class EditAttendanceReport_RV extends AppCompatActivity {

    private static final String TAG = "testMessage";
    private static final String TAG2 = "testMessageCrop";


    private int progressBarStatus = 0;
    private Handler progressBarHandler = new Handler();
    private ProgressDialog dialog;


    Boolean copySuccess = false;

    String name;
    String className;
    String date;
    String reportPath;
    BufferedReader br;

    ArrayList<StudentItem> presentStudents = new ArrayList<StudentItem>();
    ArrayList<StudentItem> absentStudents = new ArrayList<StudentItem>();
    ArrayList<StudentItem> allStudents = new ArrayList<StudentItem>();
    TextView note;
    TextView classNameTextView;

    RecyclerView recyclerView;
    RecyclerView.Adapter adapter;
    RecyclerView.LayoutManager layoutManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_attendance_report__rv);


        Intent intent = getIntent();
        name = intent.getStringExtra("name");
        className = intent.getStringExtra("className");
        date = intent.getStringExtra("date");
        reportPath = intent.getStringExtra("reportPath");

        note = (TextView)findViewById(R.id.note);

        classNameTextView = (TextView)findViewById(R.id.report_name);
        Log.i(TAG, "report name --> " + name);
        classNameTextView.setText("Viewing report file for " + className + " class on " + date);


        readReport();

        recyclerView = (RecyclerView)findViewById(R.id.studentReport_list);
        layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setHasFixedSize(true);

        adapter=new EditAttendanceReport_RVAdapter(this,allStudents,name,className,date,reportPath);
        recyclerView.setAdapter(adapter);






    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.menu_edit_attendance_report_activity, menu);
        Log.i(TAG, "menu created");
        return super.onCreateOptionsMenu(menu);
    }


    protected void onPostExecute(Boolean isSuccess) {
        if (dialog.isShowing()) {
            dialog.dismiss();
        }

        if (isSuccess) {
            Toast.makeText(EditAttendanceReport_RV.this, "Training successful.", Toast.LENGTH_LONG).show();
        } else {
            Toast.makeText(EditAttendanceReport_RV.this, "Training failed. Error encountered.", Toast.LENGTH_LONG).show();
        }

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.add_to_train:
                //Toast.makeText(getApplicationContext(), "Lipat na sa untrained!", Toast.LENGTH_LONG).show();


                 final Handler handler = new Handler() {
                    public void handleMessage(Message msg) {
                        if(msg.arg1 == 1) {
                            Toast.makeText(getApplicationContext(), "Copied to train!", Toast.LENGTH_LONG).show();
                        }
                        if(msg.arg1 == 2) {
                            Toast.makeText(getApplicationContext(), "Crops already copied!", Toast.LENGTH_LONG).show();
                        }

                    }
                };


                dialog = new ProgressDialog(EditAttendanceReport_RV.this);
                dialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
                dialog.setIndeterminate(true);
                dialog.setCancelable(false);
                dialog.setCanceledOnTouchOutside(false);
                dialog.setMessage("Copying images...");
                dialog.show();

                new Thread(new Runnable() {
                    public void run() {

                        String[] x = reportPath.split(Pattern.quote("."));
                        String cropFolder = x[0];

                        File sdCardRoot = Environment.getExternalStorageDirectory();
                        File untrainedCrops = new File(sdCardRoot, "PresentData/faceDatabase/untrainedCrops/");
                        File reportCrops = new File(cropFolder);


                        Message msg = handler.obtainMessage();
                        File[] fileArr = reportCrops.listFiles();

                        boolean meron = false;
                        for (File f : reportCrops.listFiles()) {
                            if (f.isFile()) {
                                for (File file : untrainedCrops.listFiles()) {
                                    if(file.getName().equals(f.getName())){
                                        Log.i(TAG,f.getName());
                                        Log.i(TAG,file.getName());
                                        meron = true;
                                        //Log.i(TAG,"duplicate");
                                        //copiedAlready = true;
                                    }
                                }
                                if(meron == false){
                                    try {
                                        copyFile(new File(f.getAbsolutePath()), new File(untrainedCrops, f.getName()));
                                    }catch(IOException e){
                                        e.printStackTrace();
                                    }
                                }
                            }
                        }

                        dialog.dismiss();
                        msg.arg1 = 1;
                        handler.sendMessage(msg);



                        //Toast.makeText(getApplicationContext(), "Copied to train!", Toast.LENGTH_LONG).show();
                    }
                }).start();

                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
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


    public void readReport(){
        String dataPath = "sdcard/PresentData/Classes/"+className+"/attendanceReports";

        File file = new File(dataPath, name);
        //Log.i(TAG, "Reading -->"+"sdcard/PresentData/Classes/"+className+"/attendanceReports"+className+"_"+name+".txt");
        try {
            br = new BufferedReader(new FileReader(file));
            String line;
            String[] details;
            while ((line = br.readLine()) != null) {
                details = line.split(",");
                if(Integer.parseInt(details[4]) == 1){
                    StudentItem present = new StudentItem(Integer.parseInt(details[0]),details[1], details[2], details[3]);
                    present.setAttendanceStatus(1);
                    presentStudents.add(present);
                }

                else{
                    StudentItem absent = new StudentItem(Integer.parseInt(details[0]),details[1], details[2], details[3]);
                    absent.setAttendanceStatus(0);
                    absentStudents.add(absent);
                }
            }
            br.close();
        } catch (IOException e) {
            e.printStackTrace();
        }


        for(int i=0;i<absentStudents.size();i++){
            absentStudents.get(i).setSelected(false);
            allStudents.add(absentStudents.get(i));
        }

        for(int i=0;i<presentStudents.size();i++){
            presentStudents.get(i).setSelected(true);
            allStudents.add(presentStudents.get(i));
        }
    }


}
