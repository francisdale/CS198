package com.example.dale.cs198;

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
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.nbsp.materialfilepicker.ui.FilePickerActivity;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;

public class CardHome extends AppCompatActivity{

    private static final String TAG = "testMessage";

    RecyclerView recyclerView;
    RecyclerView.Adapter adapter;
    RecyclerView.LayoutManager layoutManager;
    ArrayList<ClassItem> classList = new ArrayList<ClassItem>();
    String[] classNames;
    String[] classDescriptions;
    String masterFilePath;
    Button takePic;
    Button addClass;
    Button train;
    int classNum;

    public static  final String DEFAULT = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_card_home);

        getSupportActionBar().setDefaultDisplayHomeAsUpEnabled(true);


        generateFolders();

        ////////////////////////////////////////////////
        SharedPreferences sharedPreferences = getSharedPreferences("ClassData", Context.MODE_PRIVATE);
        classNum = sharedPreferences.getInt("classNum",0);

        if(classNum == 0){
            Context context = getApplicationContext();
            int duration = Toast.LENGTH_LONG;
            Toast toast = Toast.makeText(context,"No Classes Found! Please add a Master List of students!",duration);
            toast.show();
        }
        else {
            for (int i = 1; i < classNum+1; i++) {
                String cName = sharedPreferences.getString("name" + i, DEFAULT);
                String cStart = sharedPreferences.getString("start" + i, DEFAULT);
                String cEnd = sharedPreferences.getString("end" + i, DEFAULT);
                //Log.i(TAG, "Getting name" + i);
                //Log.i(TAG, "Getting start" + i);
                //Log.i(TAG, "Getting end" + i);
                if(cName.equals(DEFAULT)){

                }
                else{
                    ClassItem cl = new ClassItem(cName, cStart,cEnd);
                    classList.add(cl);
                }
            }
        }

        addClass = (Button)findViewById(R.id.addClass);
        train = (Button)findViewById(R.id.train);

        addClass.setOnClickListener(
                new Button.OnClickListener() {
                    public void onClick(View v) {
                        Intent addClass = new Intent(CardHome.this, AddClass.class);
                        startActivity(addClass);
                    }
                }
        );

        train.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent train = new Intent(CardHome.this, TrainActivity.class);
                startActivity(train);
//                Intent train = new Intent(CardHome.this, TrainListActivity.class);
//                startActivity(train);
            }
        });

        //DEFINING THE RECYCLERVIEW AND ASSIGNING THE adapter and passing classlist
        recyclerView = (RecyclerView)findViewById(R.id.recycler_view);
        layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setHasFixedSize(true);
        adapter=new ClassAdapter(this,classList);
        recyclerView.setAdapter(adapter);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.menu_card_home, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.upload_master:
                //save the new class
                //call saveToSP method
                Intent intent = new Intent(this, FilePickerActivity.class);
                startActivityForResult(intent, 1);
                return true;
            case R.id.help_home:
                Intent helpIntent = new Intent(this, HelpActivity.class);
                startActivity(helpIntent);
                return true;
            case R.id.about_home:
                //show about
                return true;
            case R.id.add_student:
                showAddStudentDialog();
                //just show a dialog box with edit text fields
                return true;
            case R.id.research_mode:
                Log.i(TAG, "CardHome: Activate Research Mode");
                Intent faceDetect = new Intent(CardHome.this,MainActivity.class);
                startActivity(faceDetect);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == 1 && resultCode == RESULT_OK) {
            String filePath = data.getStringExtra(FilePickerActivity.RESULT_FILE_PATH);
            generateMasterList(filePath);
            // Do anything with file
            //Log.i(TAG,"MASTER AFTER INTENT--> "+masterFilePath);
        }
    }


    public void showAddStudentDialog() {
        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(this,R.style.Theme_Holo_Dialog_Alert);
        LayoutInflater inflater = this.getLayoutInflater();
        final View dialogView = inflater.inflate(R.layout.add_student_to_master_dialog, null);
        dialogBuilder.setView(dialogView);


        final EditText studNum = (EditText) dialogView.findViewById(R.id.toAdd_student_number);
        final EditText studSurname = (EditText) dialogView.findViewById(R.id.toAdd_student_surname);
        final EditText studFirstname = (EditText) dialogView.findViewById(R.id.toAdd_student_firstname);

        dialogBuilder.setTitle("Add New Student to Master List");
        dialogBuilder.setMessage("Enter the following details:");
        dialogBuilder.setPositiveButton("Add New Student", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                //do something with edt.getText().toString();
                String newStudNum = studNum.getText().toString();
                String newStudSurname = studSurname.getText().toString();
                String newStudFirstname = studFirstname.getText().toString();

                if(!newStudNum.isEmpty() && !newStudSurname.isEmpty() && !newStudFirstname.isEmpty()){
                    addStudentToMasterList(newStudNum,newStudSurname,newStudFirstname);

                }
                else{
                    Toast.makeText(getApplicationContext(),"Please fill out all the field details",Toast.LENGTH_SHORT).show();
                }

            }
        });
        dialogBuilder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                dialog.cancel();
            }
        });
        AlertDialog b = dialogBuilder.create();
        b.show();
    }

    public void addStudentToMasterList(String newStudNum,String newStudSurname,String newStudFirstname){
        try {
            final String dataPath = "sdcard/PresentData/";
            final File masterFile = new File(dataPath,"Master List.txt");
            if(!masterFile.exists()){
                AlertDialog.Builder alertBuilder = new AlertDialog.Builder(this);
                AlertDialog.Builder builder = alertBuilder.setMessage("You have not yet uploaded a MASTER LIST of your students. Please make sure to upload one.")
                        .setCancelable(false)
                        .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.cancel();
                            }
                        });
                AlertDialog alert = alertBuilder.create();
                alert.setTitle("Uh oh!");
                alert.show();
            }
            else{

                FileWriter writer = new FileWriter(masterFile,true);
                int studCtr =0;
                try {
                    BufferedReader br = new BufferedReader(new FileReader(masterFile));
                    while (br.readLine() != null) {
                        studCtr++;
                    }
                    studCtr++;
                    writer.write(studCtr + "," + newStudNum + "," + newStudSurname + "," + newStudFirstname + "\n");
                    writer.close();
                    br.close();
                    Toast.makeText(getApplicationContext(),"Added "+newStudNum+" "+newStudSurname+","+newStudFirstname+" to Master List." ,Toast.LENGTH_SHORT).show();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    public void generateMasterList(String filePath) {
        try {
            final String dataPath = "sdcard/PresentData/";
            final File masterFile = new File(dataPath,"Master List.txt");
            if(masterFile.exists()){
                AlertDialog.Builder alertBuilder = new AlertDialog.Builder(this);
                AlertDialog.Builder builder = alertBuilder.setMessage("There is already a master list uploaded! Uploading another one would delete all your current data. Do you wish to continue?")
                        .setCancelable(false)
                        .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                boolean a = masterFile.delete();
                                if (a == true) {
                                    Log.i(TAG, "DELETED");
                                }
                                Intent intent = new Intent(CardHome.this, FilePickerActivity.class);
                                startActivityForResult(intent, 1);
                            }
                        })
                        .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        dialog.cancel();
                                    }
                                }
                        );
                AlertDialog alert = alertBuilder.create();
                alert.setTitle("Uh oh!");
                alert.show();
            }
            else{
                //Log.i(TAG,"MASTER--> "+masterFilePath);

                File file = new File(filePath);
                FileWriter writer = new FileWriter(masterFile);
                BufferedReader br = new BufferedReader(new FileReader(file));
                String line;
                String[] details;
                int ctr = 1;
                while ((line = br.readLine()) != null) {
                    details = line.split(",");
                    writer.append(ctr+","+details[0]+","+details[1]+","+details[2]+"\n");
                    ctr++;
                }
                br.close();
                writer.flush();
                writer.close();
                Toast.makeText(this, "Uploaded Master List of Students", Toast.LENGTH_SHORT).show();
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void generateFolders(){

        String dataPath="sdcard/PresentData";
        File folder = new File(dataPath);
        if(!folder.exists()){
            Log.i(TAG, dataPath + " does not exist. Creating...");
            folder.mkdir();
        }

        File classFolder = new File(dataPath+"/Classes");
        if(!classFolder.exists()){
            Log.i(TAG, dataPath + " does not exist. Creating...");
            classFolder.mkdir();
        }

        File faceFolder = new File(dataPath+"/faceDatabase");
        if(!faceFolder.exists()){
            Log.i(TAG, dataPath + " does not exist. Creating...");
            faceFolder.mkdir();
        }

        File trainedFaceFolder = new File(dataPath+"/faceDatabase/trainedCrops");
        if(!trainedFaceFolder.exists()){
            Log.i(TAG, dataPath + " does not exist. Creating...");
            trainedFaceFolder.mkdir();
        }

        File untrainedFaceFolder = new File(dataPath+"/faceDatabase/untrainedCrops");
        if(!untrainedFaceFolder.exists()){
            Log.i(TAG, dataPath + " does not exist. Creating...");
            untrainedFaceFolder.mkdir();
        }
    }
}
