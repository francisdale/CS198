package com.example.dale.cs198;

import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.GestureDetector;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.nbsp.materialfilepicker.ui.FilePickerActivity;

import java.io.File;
import java.util.ArrayList;

public class CardHome extends AppCompatActivity{

    private static final String TAG = "testMessage";

    RecyclerView recyclerView;
    RecyclerView.Adapter adapter;
    RecyclerView.LayoutManager layoutManager;
    ArrayList<ClassItem> classList = new ArrayList<ClassItem>();
    String[] classNames;
    String[] classDescriptions;

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
        //MODIFY THIS TO GET DYNAMIC CLASSES
        //MIGHT NEED TO MAKE NEW ACTIVITY TO GET CLASS INFO FROM USER INPUT

        //classNames = getResources().getStringArray(R.array.classNames);
        //classDescriptions = getResources().getStringArray(R.array.classDescriptions);

        String dataPath="sdcard/PresentData";

        File folder = new File(dataPath);
        if(!folder.exists()){
            Log.i(TAG, dataPath + " does not exist. Creating...");
            folder.mkdir();
        }


        ////////////////////////////////////////////////
        SharedPreferences sharedPreferences = getSharedPreferences("ClassData", Context.MODE_PRIVATE);
        classNum = sharedPreferences.getInt("classNum",0);

        if(classNum == 0){
            Context context = getApplicationContext();
            int duration = Toast.LENGTH_SHORT;
            Toast toast = Toast.makeText(context,"No Classes Found!",duration);
            toast.show();
        }
        else {
            for (int i = 1; i < classNum+1; i++) {
                String cName = sharedPreferences.getString("name" + i, DEFAULT);
                String cStart = sharedPreferences.getString("start" + i, DEFAULT);
                String cEnd = sharedPreferences.getString("end" + i, DEFAULT);
                Log.i(TAG, "Getting name" + i);
                Log.i(TAG, "Getting start" + i);
                Log.i(TAG, "Getting end" + i);
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
//                Intent intent = new Intent(this, FilePickerActivity.class);
//                startActivityForResult(intent, 1);
                return true;
            case R.id.help_home:
                //show help
                return true;
            case R.id.about_home:
                //show about
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

//    @Override
//    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
//        super.onActivityResult(requestCode, resultCode, data);
//
//        if (requestCode == 1 && resultCode == RESULT_OK) {
//            String filePath = data.getStringExtra(FilePickerActivity.RESULT_FILE_PATH);
//            // Do anything with file
//        }
//    }









}
