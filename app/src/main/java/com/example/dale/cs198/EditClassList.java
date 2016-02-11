package com.example.dale.cs198;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ListView;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;

public class EditClassList extends AppCompatActivity {


    String name; //name of class to be edited
    String start;
    String end;
    BufferedReader br;

    ArrayList<StudentItem> students = new ArrayList<StudentItem>(); // --> arrayList for students in master list
    ArrayList<StudentItem> studentsInClass = new ArrayList<StudentItem>(); // --> arrayList for students in class list
    ArrayList<StudentItem> studentItems = new ArrayList<StudentItem>();
    EditClassAdapter dataAdapter = null;
    Button cancel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_class_list);

        Intent intent = getIntent();
        name = intent.getStringExtra("name");
        start = intent.getStringExtra("start");
        end = intent.getStringExtra("end");

        displayClassList();

        cancel = (Button)findViewById(R.id.cancel_edit);
        cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent cardIntent = new Intent(EditClassList.this, ClassList.class);
                cardIntent.putExtra("name", name);
                cardIntent.putExtra("start", start);
                cardIntent.putExtra("end", end);
                try {
                    br.close();
                } catch (IOException ee) {
                }
                startActivity(cardIntent);
            }
        });


    }

    private void displayClassList() {
        //POPULATE ARRAYLIST OF STUDENTS FROM MASTERLIST
        //READ Master List.txt then check in the class text file if the name of the students
        //is there if both present in the list set checkbox to true;

        readMasterList();
        readClassList();

        for(int i=0;i<students.size();i++){
            for(int j=0;j<studentsInClass.size();j++){
                if(students.get(i).getStudentNumber().equals(studentsInClass.get(j).getStudentNumber())){
                    students.get(i).setSelected(true);
                }
            }

        }


        //create an ArrayAdaptar from the String Array
        dataAdapter = new EditClassAdapter(this, R.layout.add_student_layout, students);
        ListView listView = (ListView) findViewById(R.id.edit_class_list_view);
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

    public void readMasterList(){
        String dataPath = "sdcard/PresentData/";

        File file = new File(dataPath, "Master List.txt");

        try {
            br = new BufferedReader(new FileReader(file));
            String line;
            String[] details;
            while ((line = br.readLine()) != null) {
                details = line.split(",");
                StudentItem si = new StudentItem(details[0], details[1], details[2]);
                students.add(si);
            }
            br.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void readClassList(){
        String dataPath = "sdcard/PresentData/";

        File file = new File(dataPath, name+".txt");

        try {
            br = new BufferedReader(new FileReader(file));
            String line;
            String[] details;
            while ((line = br.readLine()) != null) {
                details = line.split(",");
                StudentItem student = new StudentItem(details[0], details[1], details[2]);
                studentsInClass.add(student);
            }
            br.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void remakeClassFile(){
        int selected;
        try {
            String dataPath = "sdcard/PresentData";
            File classFile = new File(dataPath, name + ".txt");


            selected = 0;
            boolean a = classFile.delete();
            if(a == true){
                File file = new File(dataPath, name + ".txt");
                FileWriter writer = new FileWriter(file);
                for (int i = 0; i < students.size(); i++) {
                    StudentItem s = students.get(i);
                    if (s.isSelected()) {
                        writer.append(s.getStudentNumber() + "," + s.getLastName() + "," + s.getFirstName() + "\n");
                        selected++;
                    }

                }
                if (selected == 0) {
                    Toast.makeText(this, "Please select students!", Toast.LENGTH_SHORT).show();
                } else {
                    writer.flush();
                    writer.close();
                }
            }
            //Toast.makeText(this, "Saved", Toast.LENGTH_SHORT).show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.menu_add_class_activity, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.save_class_id:
                AlertDialog.Builder alertBuilder = new AlertDialog.Builder(this);
                alertBuilder.setMessage("Save changes to this class?")
                        .setCancelable(false)
                        .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                remakeClassFile();
                                Intent cardIntent = new Intent(EditClassList.this, ClassList.class);
                                cardIntent.putExtra("name", name);
                                cardIntent.putExtra("start", start);
                                cardIntent.putExtra("end", end);
                                startActivity(cardIntent);
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
                alert.setTitle("Save Class");
                alert.show();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }


    ///////////////////////////////////////////////////////////////////////////////////////
    ////////////////////////////ADAPTER FOR LISTVIEW//////////////////////////////////////
    /////////////////////////////////////////////////////////////////////////////////////

    public class EditClassAdapter extends ArrayAdapter<StudentItem> {

        private ArrayList<StudentItem> studentList;

        public EditClassAdapter(Context context, int textViewResourceId, ArrayList<StudentItem> studentList) {
            super(context, textViewResourceId, studentList);
            this.studentList = new ArrayList<StudentItem>();
            this.studentList.addAll(studentList);
        }

        private class ViewHolder {
            CheckBox listItem;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {

            ViewHolder holder = null;

            if (convertView == null) {
                LayoutInflater vi = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                convertView = vi.inflate(R.layout.add_student_layout, null);

                holder = new ViewHolder();
                holder.listItem = (CheckBox) convertView.findViewById(R.id.student_check_box);
                convertView.setTag(holder);

                holder.listItem.setOnClickListener(new View.OnClickListener() {
                    public void onClick(View v) {
                        CheckBox cb = (CheckBox) v;
                        StudentItem s = (StudentItem) cb.getTag();
                        //Toast.makeText(getApplicationContext(), "Clicked on Checkbox: " + cb.getText() + " is " + cb.isChecked(), Toast.LENGTH_LONG).show();
                        s.setSelected(cb.isChecked());
                    }
                });
            } else {
                holder = (ViewHolder) convertView.getTag();
            }

            StudentItem si = studentList.get(position);
            holder.listItem.setText(si.getStudentNumber() + "  " + si.getLastName() + " " + si.getFirstName());
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
