package com.example.dale.cs198;

import android.app.ActionBar;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Map;

public class ClassList extends AppCompatActivity {

    ActionBar actionBar;
    private static final String TAG = "testMessage";

    String classesDir = "sdcard/PresentData/Classes";
    String classDir;

    String name;
    String start;
    String end;

    ArrayList<StudentItem> students = new ArrayList<StudentItem>();
    RecyclerView recyclerView;
    RecyclerView.Adapter adapter;
    RecyclerView.LayoutManager layoutManager;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_class_list);

        Intent intent = getIntent();
        name = intent.getStringExtra("name");
        start = intent.getStringExtra("start");
        end = intent.getStringExtra("end");
        //read the txt file of student names then populate students ArrayList

        classDir=classesDir + "/" + name;

        File file = new File(classDir, name+"_studentList.txt");
        StringBuilder text = new StringBuilder();

        try {
            BufferedReader br = new BufferedReader(new FileReader(file));
            String line;
            String[] details;
            while ((line = br.readLine()) != null) {
                details = line.split(",");
                StudentItem si = new StudentItem(Integer.parseInt(details[0]),details[1],details[2],details[3]);
                students.add(si);
            }
            br.close();
        }
        catch (IOException e) {
            e.printStackTrace();
        }

        recyclerView = (RecyclerView)findViewById(R.id.student_list);
        layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setHasFixedSize(true);

        adapter=new StudentAdapter(this,students,name);
        recyclerView.setAdapter(adapter);

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.menu_class_list_activity,menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case R.id.delete_list_id:
                AlertDialog.Builder alertBuilder = new AlertDialog.Builder(this);
                alertBuilder.setMessage("Are you sure you want to delete " + name +"?")
                        .setCancelable(false)
                        .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                delToSp();
                                DirectoryDeleter.deleteDir(new File(classDir));
                            }
                        })
                        .setNegativeButton("No", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        dialog.cancel();
                                    }
                                }
                        );
                AlertDialog alert = alertBuilder.create();
                alert.setTitle("Delete Class");
                alert.show();
                return true;
            case R.id.report_id:
                Log.i(TAG, "view report");
                Intent viewReports = new Intent(ClassList.this, ViewAttendanceReport.class);

                viewReports.putExtra("name",name);
                startActivity(viewReports);



                return true;
            case R.id.edit_student_id:
                Log.i(TAG, "EDIT STUDENTS");

                Intent editIntent = new Intent(ClassList.this,EditClassList.class);
                editIntent.putExtra("name", name);
                editIntent.putExtra("start",start);
                editIntent.putExtra("end",end);
                startActivity(editIntent);


                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    public void delToSp() {
        SharedPreferences sharedPreferences = getSharedPreferences("ClassData", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.remove(findKey(sharedPreferences, name));
        editor.remove(findKey(sharedPreferences, start));
        editor.remove(findKey(sharedPreferences, end));
        editor.commit();

        Intent goHome = new Intent(ClassList.this, CardHome.class);
        startActivity(goHome);
    }

    String findKey(SharedPreferences sharedPreferences, String value) {
        Map<String, ?> keys = sharedPreferences.getAll();
        for (Map.Entry<String, ?> entry: keys.entrySet()) {
            if (value.equals(entry.getValue())) {
                Log.i(TAG, "Found key of "+ value +" ---> " + entry.getKey());
                return entry.getKey();

            }
        }
        return null; // not found
    }



}