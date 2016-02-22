package com.example.dale.cs198;

import android.app.AlarmManager;
import android.app.Dialog;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.TimePickerDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;


import org.bytedeco.javacpp.tools.Parser;
import org.bytedeco.javacpp.tools.ParserException;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

public class AddClass extends AppCompatActivity {

    private static final String TAG = "testMessage";

    static final int START_DIALOG_ID = 0;
    static final int END_DIALOG_ID = 1;

    int hour_start;
    int minute_start;

    int hour_end;
    int minute_end;

    EditText etName;
    Button setStart;
    Button setEnd;
    TextView startTime;
    TextView endTime;

    Button alarm;
    AddClassAdapter dataAdapter = null;

    ArrayList<StudentItem> studentList;
    int selected;

    Date x;//start time
    Date y;//end time
    SimpleDateFormat parser = new SimpleDateFormat("HH:mm");
    String start;
    String end;
    String name;

    NotificationManager notificationManager;
    boolean isActive = false;
    int notifID = 29;

    int n;
    /**
     * ATTENTION: This was auto-generated to implement the App Indexing API.
     * See https://g.co/AppIndexing/AndroidStudio for more information.
     */


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_class);
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);
        etName = (EditText) findViewById(R.id.editName);

        setStart = (Button) findViewById(R.id.start_time_button);
        setStart.setOnClickListener(
                new Button.OnClickListener() {
                    public void onClick(View v) {
                        //call the time picker dialog
                        showDialog(START_DIALOG_ID);
                    }
                }
        );
        setEnd = (Button) findViewById(R.id.end_time_button);
        setEnd.setOnClickListener(
                new Button.OnClickListener() {
                    public void onClick(View v) {
                        showDialog(END_DIALOG_ID);
                    }
                }
        );

        etName.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showEditTextDialog();

            }
        });
        displayMasterList();



    }

    private void displayMasterList() {
        //POPULATE ARRAYLIST OF STUDENTS FROM MASTERLIST
        //READ Master List.txt
        ArrayList<StudentItem> studentItems = new ArrayList<StudentItem>();

        String dataPath = "sdcard/PresentData/";

        File file = new File(dataPath, "Master List.txt");

        try {
            BufferedReader br = new BufferedReader(new FileReader(file));
            String line;
            String[] details;
            while ((line = br.readLine()) != null) {
                details = line.split(",");
                StudentItem si = new StudentItem(details[1], details[2], details[3]);
                studentItems.add(si);
            }
            br.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        //create an ArrayAdaptar from the String Array
        dataAdapter = new AddClassAdapter(this, R.layout.add_student_layout, studentItems);
        ListView listView = (ListView) findViewById(R.id.class_list_view);
        // Assign adapter to ListView
        listView.setAdapter(dataAdapter);


        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View view,
                                    int position, long id) {
                // When clicked, show a toast with the TextView text
                StudentItem si = (StudentItem) parent.getItemAtPosition(position);
                Toast.makeText(getApplicationContext(), "Clicked on Row: " + si.getLastName() + " " + si.getFirstName(), Toast.LENGTH_LONG).show();
            }
        });

    }

    @Override
    protected Dialog onCreateDialog(int id) {
        if (id == START_DIALOG_ID) {
            TimePickerDialog startTimePickerDialog = new TimePickerDialog(AddClass.this, startTimePickerListener, hour_start, minute_start, true);
            startTimePickerDialog.setTitle("Set class start time");
            return startTimePickerDialog;
        } else if (id == END_DIALOG_ID) {
            TimePickerDialog endTimePickerDialog = new TimePickerDialog(AddClass.this, endTimePickerListener, hour_end, minute_end, true);
            endTimePickerDialog.setTitle("Set class end time");
            return endTimePickerDialog;
        } else {
            return null;
        }
    }

    protected TimePickerDialog.OnTimeSetListener startTimePickerListener =
            new TimePickerDialog.OnTimeSetListener() {
                @Override
                public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                    startTime = (TextView) findViewById(R.id.start_time_text);
                    hour_start = hourOfDay;
                    minute_start = minute;
                    if (minute_start >= 10) {
                        start = Integer.toString(hour_start) + ":" + Integer.toString(minute_start);
                    } else {
                        start = Integer.toString(hour_start) + ":0" + Integer.toString(minute_start);
                    }
                    try {
                        x = parser.parse(start);
                    } catch (ParseException pe) {
                    }
//                    Log.i(TAG, "HOUR_START: " + hour_start);
//                    Log.i(TAG, "MIN_START: " + minute_start);
//                    Log.i(TAG, "TIME_START-->" + start);
                    startTime.setText(start);
                }
            };

    protected TimePickerDialog.OnTimeSetListener endTimePickerListener =
            new TimePickerDialog.OnTimeSetListener() {
                @Override
                public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                    endTime = (TextView) findViewById(R.id.end_time_text);
                    hour_end = hourOfDay;
                    minute_end = minute;
                    if (minute_end >= 10) {
                        end = Integer.toString(hour_end) + ":" + Integer.toString(minute_end);
                    } else {
                        end = Integer.toString(hour_end) + ":0" + Integer.toString(minute_end);
                    }
                    try {
                        y = parser.parse(end);
                    } catch (ParseException pe) {
                    }
//                    Log.i(TAG, "HOUR_END: " + hour_end);
//                    Log.i(TAG, "MIN_END: " + minute_end);
//                    Log.i(TAG, "TIME_END-->" + end);
                    endTime.setText(end);
                }
            };

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
                alertBuilder.setMessage("Save this class with the selected students?")
                        .setCancelable(false)
                        .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                saveToSp(etName.getText().toString(), start, end);
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

    public void showEditTextDialog() {
        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(this);
        LayoutInflater inflater = this.getLayoutInflater();
        final View dialogView = inflater.inflate(R.layout.edit_text_dialog_layout, null);
        dialogBuilder.setView(dialogView);

        final EditText edt = (EditText) dialogView.findViewById(R.id.class_name_edit);

        dialogBuilder.setTitle("Class Name");
        dialogBuilder.setMessage("Enter name below:");
        dialogBuilder.setPositiveButton("Done", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                //do something with edt.getText().toString();
                etName.setText(edt.getText().toString());
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

    public void saveToSp(String nameToSave, String startTime, String endTime) {

        SharedPreferences sharedPreferences = getSharedPreferences("ClassData", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        studentList = dataAdapter.studentList;
        n = sharedPreferences.getInt("classNum", 0);
        n++;

        if ((nameToSave != null && !nameToSave.isEmpty()) &&
                (startTime != null && !startTime.isEmpty()) &&
                (endTime != null && !endTime.isEmpty())) {
            Log.i(TAG, "DATE X:" + x.toString());
            Log.i(TAG, "DATE Y:" + y.toString());
            if ((x.before(y) == true) && (y.after(x) == true)) {
                editor.putString("name" + n, nameToSave);
                editor.putString("start" + n, startTime);
                editor.putString("end" + n, endTime);
                editor.putInt("classNum", n);
                Log.i(TAG, "Added name" + n + ": " + nameToSave);

                if (generateClassFile(nameToSave) == 0) {
                    Toast.makeText(this, "Please add students.", Toast.LENGTH_SHORT).show();
                } else {
                    editor.commit();
                    Intent goHome = new Intent(AddClass.this, CardHome.class);
                    startActivity(goHome);
                }
            } else {
                Toast.makeText(this, "Wrong time range. Please input the correct time range.", Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(this, "One of the fields is not yet answered!", Toast.LENGTH_SHORT).show();
        }
    }


    int generateClassFile(String fileName) {
        try {
            String dataPath = "sdcard/PresentData";
            File classFile = new File(dataPath, fileName + ".txt");
            FileWriter writer = new FileWriter(classFile);

            selected = 0;
            for (int i = 0; i < studentList.size(); i++) {
                StudentItem s = studentList.get(i);
                if (s.isSelected()) {
                    writer.append(s.getStudentNumber() + "," + s.getLastName() + "," + s.getFirstName() + "\n");
                    selected++;
                }

            }
            if (selected == 0) {
                return selected;
            } else {
                writer.flush();
                writer.close();
                return selected;
            }

            //Toast.makeText(this, "Saved", Toast.LENGTH_SHORT).show();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return selected;
    }


    ///////////////////////////////////////////////////////////////////////////////////////
    ////////////////////////////ADAPTER FOR LISTVIEW//////////////////////////////////////
    /////////////////////////////////////////////////////////////////////////////////////

    public class AddClassAdapter extends ArrayAdapter<StudentItem> {

        private ArrayList<StudentItem> studentList;

        public AddClassAdapter(Context context, int textViewResourceId, ArrayList<StudentItem> studentList) {
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
            holder.listItem.setTag(si);

            return convertView;
        }
    }
    ///////////////////////////////////////////////////////////////////////////////////////
    ////////////////////////////ADAPTER FOR LISTVIEW//////////////////////////////////////
    /////////////////////////////////////////////////////////////////////////////////////

    public void setAlert(View view) {

//        Calendar calendar = Calendar.getInstance();
//        calendar.setTimeInMillis(System.currentTimeMillis());
//        calendar.set(calendar.HOUR_OF_DAY, hour_start);
//        calendar.set(calendar.MINUTE, minute_start - 5);
//
//        Long alertTime = calendar.getTimeInMillis();
        Long f = new GregorianCalendar().getTimeInMillis() + 7 * 1000;

        Intent alertIntent = new Intent(this, AlertReceiver.class);

        AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        //Log.i(TAG, "ALARM GOES IN: " + calendar.HOUR_OF_DAY + ":" + calendar.MINUTE);
        //alarmManager.setRepeating(AlarmManager.RTC_WAKEUP, alertTime, AlarmManager.INTERVAL_DAY,PendingIntent.getBroadcast(this, 1, alertIntent, PendingIntent.FLAG_UPDATE_CURRENT));
        alarmManager.set(AlarmManager.RTC_WAKEUP, f, PendingIntent.getBroadcast(this, 1, alertIntent, PendingIntent.FLAG_UPDATE_CURRENT));
        Toast.makeText(this, "ALARM", Toast.LENGTH_SHORT).show();
    }


}
