package com.example.dale.cs198;

import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

public class ViewAttendanceReport extends AppCompatActivity {

    private static final String TAG = "testMessage";
    String name;
    TextView className;
    RecyclerView recyclerView;
    RecyclerView.Adapter adapter;
    RecyclerView.LayoutManager layoutManager;

    ArrayList<ReportItem> reportPath = new ArrayList<ReportItem>();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_attendance_report);

        Intent intent = getIntent();
        name = intent.getStringExtra("name");

        Log.i(TAG, "You are now viewing reports for --> " + name);
        className = (TextView)findViewById(R.id.className_textView);
        className.setText("Attendance Reports for " + name + " Class");

        File sdCardRoot = Environment.getExternalStorageDirectory();
        File reportListDir = new File(sdCardRoot, "PresentData/Classes/"+name+"/attendanceReports");

        if(reportListDir.list().length==0){
            Toast.makeText(getApplicationContext(), "There are no attendance reports for "+name +" yet.", Toast.LENGTH_LONG).show();
        }
        else{
            for (File f : reportListDir.listFiles()) {
                if (f.isFile()) {
                    String nameArr[] = f.getName().split("_");
                    SimpleDateFormat sdf = new SimpleDateFormat("MMddyyyy");
                    try {
                        Date date = sdf.parse(nameArr[1]);
                        ReportItem r = new ReportItem(name,date,f.getAbsolutePath());
                        r.setFileName(f.getName());
                        reportPath.add(r);
                    }catch (ParseException e){
                        e.printStackTrace();
                    }
                    Log.i(TAG,nameArr[1]);
                }
            }


            //DEFINING THE RECYCLERVIEW AND ASSIGNING THE adapter and passing classlist
            recyclerView = (RecyclerView)findViewById(R.id.report_list);
            layoutManager = new LinearLayoutManager(this);
            recyclerView.setLayoutManager(layoutManager);
            recyclerView.setHasFixedSize(true);
            adapter=new AttendanceReportListAdapter(this,reportPath,name);
            recyclerView.setAdapter(adapter);
        }




    }





}



