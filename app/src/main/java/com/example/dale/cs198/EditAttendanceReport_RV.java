package com.example.dale.cs198;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.widget.TextView;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;

public class EditAttendanceReport_RV extends AppCompatActivity {

    private static final String TAG = "testMessage";
    private static final String TAG2 = "testMessageCrop";
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
