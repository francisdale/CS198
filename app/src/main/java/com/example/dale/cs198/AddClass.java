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
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import org.bytedeco.javacpp.tools.Parser;
import org.bytedeco.javacpp.tools.ParserException;

import java.io.File;
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

    Date x;//start time
    Date y;//end time
    SimpleDateFormat parser = new SimpleDateFormat("HH:mm");
    String start;
    String end;

    NotificationManager notificationManager;
    boolean isActive = false;
    int notifID = 29;

    int n;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_class);

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

        alarm = (Button)findViewById(R.id.alarm_button);
        alarm.setOnClickListener(
                new Button.OnClickListener() {
                    public void onClick(View v) {
                        setAlert(v);
                    }
                }
        );
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
                //save the new class
                //call saveToSP method
                saveToSp(etName.getText().toString(), start, end);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    public void setAlert(View view){

//        Calendar calendar = Calendar.getInstance();
//        calendar.setTimeInMillis(System.currentTimeMillis());
//        calendar.set(calendar.HOUR_OF_DAY, hour_start);
//        calendar.set(calendar.MINUTE, minute_start - 5);
//
//        Long alertTime = calendar.getTimeInMillis();
        Long f = new GregorianCalendar().getTimeInMillis()+7*1000;

        Intent alertIntent = new Intent(this,AlertReceiver.class);

        AlarmManager alarmManager = (AlarmManager)getSystemService(Context.ALARM_SERVICE);
        //Log.i(TAG, "ALARM GOES IN: " + calendar.HOUR_OF_DAY + ":" + calendar.MINUTE);
        //alarmManager.setRepeating(AlarmManager.RTC_WAKEUP, alertTime, AlarmManager.INTERVAL_DAY,PendingIntent.getBroadcast(this, 1, alertIntent, PendingIntent.FLAG_UPDATE_CURRENT));
        alarmManager.set(AlarmManager.RTC_WAKEUP,f,PendingIntent.getBroadcast(this, 1, alertIntent, PendingIntent.FLAG_UPDATE_CURRENT));
        Toast.makeText(this, "ALARM", Toast.LENGTH_SHORT).show();
    }

    public void saveToSp(String nameToSave, String startTime, String endTime) {

        SharedPreferences sharedPreferences = getSharedPreferences("ClassData", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();

        n = sharedPreferences.getInt("classNum", 0);
        n++;

        if ((nameToSave != null && !nameToSave.isEmpty()) &&
                (startTime != null && !startTime.isEmpty()) &&
                (endTime != null && !endTime.isEmpty())) {
            Log.i(TAG, "DATE X:" + x.toString());
            Log.i(TAG, "DATE Y:" + y.toString());
            if((x.before(y) == true) && (y.after(x) == true)) {
                editor.putString("name" + n, nameToSave);
                editor.putString("start" + n, startTime);
                editor.putString("end" + n, endTime);

                editor.putInt("classNum", n);
                Log.i(TAG, "Added name" + n + ": " + nameToSave);
                editor.commit();
                //setAlert();
                generateClassFile(nameToSave);
                Intent goHome = new Intent(AddClass.this, CardHome.class);
                startActivity(goHome);
            }
            else{
                Toast.makeText(this, "Wrong time range. Please input the correct time range", Toast.LENGTH_SHORT).show();
            }

        } else {
            Toast.makeText(this, "One of the fields is not yet answered!", Toast.LENGTH_SHORT).show();
        }

    }


    public void generateClassFile(String fileName) {
        try {
            String dataPath = "sdcard/PresentData";
            File classFile = new File(dataPath, fileName + ".txt");
            FileWriter writer = new FileWriter(classFile);

            writer.append("201204618,Ambrocio,Francis Dale\n");
            writer.append("201204656,Manaois,Meryll Ysabel\n");
            writer.append("201400000,Datu,Jed Patrick\n");
            writer.append("201300000,Swift,Taylor\n");
            writer.append("201199999,Simpson,Homer\n");
            writer.append("200933333,Simpson,Bart\n");
            writer.append("201204618,Ambrocio,Francis Dale\n");
            writer.append("201204656,Manaois,Meryll Ysabel\n");
            writer.append("201400000,Datu,Jed Patrick\n");
            writer.append("201300000,Swift,Taylor\n");
            writer.append("201199999,Simpson,Homer\n");
            writer.append("200933333,Simpson,Bart\n");
            writer.append("201204618,Ambrocio,Francis Dale\n");
            writer.append("201204656,Manaois,Meryll Ysabel\n");
            writer.append("201400000,Datu,Jed Patrick\n");
            writer.append("201300000,Swift,Taylor\n");
            writer.append("201199999,Simpson,Homer\n");
            writer.append("200933333,Simpson,Bart\n");
            writer.append("201204618,Ambrocio,Francis Dale\n");
            writer.append("201204656,Manaois,Meryll Ysabel\n");
            writer.append("201400000,Datu,Jed Patrick\n");
            writer.append("201300000,Swift,Taylor\n");
            writer.append("201199999,Simpson,Homer\n");
            writer.append("200933333,Simpson,Bart");


            writer.flush();
            writer.close();
            //Toast.makeText(this, "Saved", Toast.LENGTH_SHORT).show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


//    @Override
//    public void onStart() {
//        super.onStart();
//
//        // ATTENTION: This was auto-generated to implement the App Indexing API.
//        // See https://g.co/AppIndexing/AndroidStudio for more information.
//        client.connect();
//        Action viewAction = Action.newAction(
//                Action.TYPE_VIEW, // TODO: choose an action type.
//                "AddClass Page", // TODO: Define a title for the content shown.
//                // TODO: If you have web page content that matches this app activity's content,
//                // make sure this auto-generated web page URL is correct.
//                // Otherwise, set the URL to null.
//                Uri.parse("http://host/path"),
//                // TODO: Make sure this auto-generated app deep link URI is correct.
//                Uri.parse("android-app://com.example.dale.cs198/http/host/path")
//        );
//        AppIndex.AppIndexApi.start(client, viewAction);
//    }
//
//    @Override
//    public void onStop() {
//        super.onStop();
//
//        // ATTENTION: This was auto-generated to implement the App Indexing API.
//        // See https://g.co/AppIndexing/AndroidStudio for more information.
//        Action viewAction = Action.newAction(
//                Action.TYPE_VIEW, // TODO: choose an action type.
//                "AddClass Page", // TODO: Define a title for the content shown.
//                // TODO: If you have web page content that matches this app activity's content,
//                // make sure this auto-generated web page URL is correct.
//                // Otherwise, set the URL to null.
//                Uri.parse("http://host/path"),
//                // TODO: Make sure this auto-generated app deep link URI is correct.
//                Uri.parse("android-app://com.example.dale.cs198/http/host/path")
//        );
//        AppIndex.AppIndexApi.end(client, viewAction);
//        client.disconnect();
//    }
}
