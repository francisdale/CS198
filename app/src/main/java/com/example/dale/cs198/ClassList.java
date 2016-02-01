package com.example.dale.cs198;

import android.app.ActionBar;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Environment;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.ContextMenu;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Map;

public class ClassList extends AppCompatActivity {

    ActionBar actionBar;
    private static final String TAG = "testMessage";

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

        String dataPath="sdcard/PresentData/";

        File file = new File(dataPath,name+".txt");

        StringBuilder text = new StringBuilder();
        try {
            BufferedReader br = new BufferedReader(new FileReader(file));
            String line;
            String[] details;
            while ((line = br.readLine()) != null) {
                details = line.split(",");
                StudentItem si = new StudentItem(details[0],details[1],details[2]);
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

        adapter=new StudentAdapter(this,students);
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
                Log.i(TAG, "make report");
                return true;
            case R.id.edit_student_id:
                Log.i(TAG, "EDIT STUDENTS");
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


//        del = (Button)findViewById(R.id.deleteClass);
//        del.setOnClickListener(
//                new Button.OnClickListener() {
//                    public void onClick(View v) {
//
//                        delToSp();
//
//                        Context context = getApplicationContext();
//                        int duration = Toast.LENGTH_SHORT;
//                        Toast toast = Toast.makeText(context, "Deleted " + name + " from your class list.", duration);
//                        toast.show();
//                        Intent backHome = new Intent(ClassList.this, CardHome.class);
//                        startActivity(backHome);
//                    }
//                }
//        );

}