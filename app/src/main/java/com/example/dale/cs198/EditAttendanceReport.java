package com.example.dale.cs198;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.Image;
import android.os.Environment;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;

public class EditAttendanceReport extends AppCompatActivity {

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

    EditReportAdapter dataAdapter=null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_attendance_report);

        Intent intent = getIntent();
        name = intent.getStringExtra("name");
        className = intent.getStringExtra("className");
        date = intent.getStringExtra("date");
        reportPath = intent.getStringExtra("reportPath");

        note = (TextView)findViewById(R.id.note);

        classNameTextView = (TextView)findViewById(R.id.report_name);
        Log.i(TAG,"report name --> "+name);
        classNameTextView.setText("Viewing report file for "+className + " class on " + date);

        displayClassList();

    }

    private void displayClassList() {
        //POPULATE ARRAYLIST OF STUDENTS FROM MASTERLIST
        //READ Master List.txt then check in the class text file if the name of the students
        //is there if both present in the list set checkbox to true;

        readReport();

        for(int i=0;i<absentStudents.size();i++){
            absentStudents.get(i).setSelected(false);
            allStudents.add(absentStudents.get(i));
        }

        for(int i=0;i<presentStudents.size();i++){
            presentStudents.get(i).setSelected(true);
            allStudents.add(presentStudents.get(i));
        }

        //create an ArrayAdaptar from the String Array
        dataAdapter = new EditReportAdapter(this, R.layout.edit_report_item_detail, allStudents);
        ListView listView = (ListView) findViewById(R.id.edit_report_list_view);
        listView.setAdapter(dataAdapter);

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View view,
                                    int position, long id) {
                // When clicked, show a toast with the TextView text
                StudentItem si = (StudentItem) parent.getItemAtPosition(position);
                //Toast.makeText(getApplicationContext(), "Clicked on Row: " + si.getLastName() + " " + si.getFirstName(), Toast.LENGTH_LONG).show();
            }
        });

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
    }

    public void rewriteReport(ArrayList<StudentItem> students){

        int selected;
        try {
            //String dataPath = "sdcard/PresentData/Classes/"+name;
            File classFile = new File(reportPath);


            selected = 0;
            boolean a = classFile.delete();
            if(a == true){
                File file = new File(reportPath);
                FileWriter writer = new FileWriter(file);
                for (int i = 0; i < students.size(); i++) {
                    StudentItem s = students.get(i);
                    if (s.isSelected() == true) {
                        writer.append(s.getId() + "," + s.getStudentNumber() + "," + s.getLastName() + "," + s.getFirstName() + ",1" + "\n");
                    }
                    else{
                        writer.append(s.getId()+","+s.getStudentNumber() + "," + s.getLastName() + "," + s.getFirstName() + ",0"+"\n");
                    }
                }

                writer.flush();
                writer.close();
            }
            //Toast.makeText(this, "Saved", Toast.LENGTH_SHORT).show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    ///////////////////////////////////////////////////////////////////////////////////////
    ////////////////////////////ADAPTER FOR LISTVIEW//////////////////////////////////////
    /////////////////////////////////////////////////////////////////////////////////////

    public class EditReportAdapter extends ArrayAdapter<StudentItem> {

        private ArrayList<StudentItem> studentList;

        public EditReportAdapter(Context context, int textViewResourceId, ArrayList<StudentItem> studentList) {
            super(context, textViewResourceId, studentList);
            this.studentList = new ArrayList<StudentItem>();
            this.studentList.addAll(studentList);
        }

        private class ViewHolder {
            ImageView studentFace;
            CheckBox listItem;
        }

        @Override
        public View getView(final int position, View convertView, ViewGroup parent) {

            ViewHolder holder = null;

            if (convertView == null) {
                LayoutInflater vi = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                convertView = vi.inflate(R.layout.edit_report_item_secondary, null);

                holder = new ViewHolder();
                holder.listItem = (CheckBox) convertView.findViewById(R.id.report_detail_item);
                holder.studentFace = (ImageView) convertView.findViewById(R.id.student_face);


                convertView.setTag(holder);
                final boolean previousState = studentList.get(position).isSelected();
                holder.listItem.setOnClickListener(new View.OnClickListener() {
                    public void onClick(final View v) {
                        final CheckBox cb = (CheckBox) v;
                        StudentItem s = (StudentItem) cb.getTag();

                        Log.i(TAG, "Previous state --> " + previousState);
                        //if cb is checked, then student is present
                        //so dapat tanungin kung absent yung student
                        Log.i(TAG,"position -->" + position);
                        if (studentList.get((Integer)v.getTag()).isSelected() == true) {
                            AlertDialog.Builder alertBuilder = new AlertDialog.Builder(getContext(),R.style.Theme_Holo_Dialog_Alert);
                            alertBuilder.setMessage("Are you sure that " + cb.getText().toString() + " is ABSENT on " + date + " for " + className + " ?")
                                    .setCancelable(false)
                                    .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            cb.setChecked(false);
                                            studentList.get((Integer)v.getTag()).setSelected(false);
                                            rewriteReport(studentList);
                                            Log.i(TAG,studentList.get((Integer)v.getTag()).getLastName() + " is " + studentList.get((Integer)v.getTag()).isSelected());
                                        }
                                    })
                                    .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                                                @Override
                                                public void onClick(DialogInterface dialog, int which) {
                                                    cb.setChecked(previousState);
                                                    dialog.cancel();
                                                }
                                            }
                                    );
                            AlertDialog alert = alertBuilder.create();
                            alert.setTitle("Make Student ABSENT");
                            alert.show();

                        }
                        if (studentList.get((Integer)v.getTag()).isSelected() == false) {
                            AlertDialog.Builder alertBuilder = new AlertDialog.Builder(getContext(),R.style.Theme_Holo_Dialog_Alert);
                            alertBuilder.setMessage("Are you sure that " + cb.getText().toString() + " is PRESENT on " + date + " for " + className + " ?")
                                    .setCancelable(false)
                                    .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            cb.setChecked(true);
                                            studentList.get((Integer)v.getTag()).setSelected(true);
                                            rewriteReport(studentList);
                                            Log.i(TAG, studentList.get((Integer)v.getTag()).getLastName() + "is " + studentList.get((Integer)v.getTag()).isSelected());
                                        }
                                    })
                                    .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                                                @Override
                                                public void onClick(DialogInterface dialog, int which) {
                                                    cb.setChecked(previousState);
                                                    dialog.cancel();
                                                }
                                            }
                                    );
                            AlertDialog alert = alertBuilder.create();
                            alert.setTitle("Make Student PRESENT");
                            alert.show();
                        }

                        //Toast.makeText(getApplicationContext(), "Clicked on Checkbox: " + cb.getText() + " is " + cb.isChecked(), Toast.LENGTH_LONG).show();
                        s.setSelected(cb.isChecked());
                    }
                });
            } else {
                holder = (ViewHolder) convertView.getTag();
            }

            StudentItem si = studentList.get(position);
            String a[] = name.split("\\.");
            File sdCardRoot = Environment.getExternalStorageDirectory();
            File faceCropsDir = new File(sdCardRoot, "PresentData/Classes/"+className + "/attendanceReports/" + a[0]);

            final String ID = Integer.toString(si.getId());
            FilenameFilter IDImgFilter = new FilenameFilter() {
                public boolean accept(File dir, String name) {
                    Log.i(TAG2, "Filtered ID --> " + ID);
                    return name.startsWith(ID);
                }
            };

            File[] fileArr = faceCropsDir.listFiles(IDImgFilter);
            Log.i(TAG2, "File arr size -->" + fileArr.length);
            if (fileArr.length > 0) {
                Log.i(TAG2, "directory of first occurence -->" + fileArr[0].getAbsolutePath());
                Bitmap bmImg = BitmapFactory.decodeFile(fileArr[0].getAbsolutePath());
                holder.studentFace.setImageBitmap(bmImg);
            } else {
                Log.i(TAG2, "no image");
                holder.studentFace.setImageResource(R.mipmap.ic_launcher);
            }



            holder.listItem.setText(si.getStudentNumber() + " " + si.getLastName() + ", " + si.getFirstName());
            holder.listItem.setChecked(si.isSelected());
            //holder.listItem.setChecked(true);
            holder.listItem.setTag(si);

            return convertView;
        }
    }
    ///////////////////////////////////////////////////////////////////////////////////////
    ////////////////////////////ADAPTER FOR LISTVIEW//////////////////////////////////////
    /////////////////////////////////////////////////////////////////////////////////////





}
