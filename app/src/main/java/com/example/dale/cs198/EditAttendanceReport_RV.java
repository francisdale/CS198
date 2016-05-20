package com.example.dale.cs198;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
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
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.regex.Pattern;

public class EditAttendanceReport_RV extends AppCompatActivity {

    private static final String TAG = "testMessage";

    private ProgressDialog dialog;
    String name;
    String className;
    String date;
    String reportPath;
    BufferedReader br;

    ArrayList<StudentItem> presentStudents = new ArrayList<StudentItem>();
    ArrayList<StudentItem> absentStudents = new ArrayList<StudentItem>();
    ArrayList<StudentItem> allStudents = new ArrayList<StudentItem>();
    ArrayList<String> nonFaces = new ArrayList<String>();
    TextView note;
    TextView classNameTextView;

    Button viewNonFaces;

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
        viewNonFaces = (Button)findViewById(R.id.view_non_face);

        viewNonFaces.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showNonFaces(v);
            }
        });


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


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.menu_edit_attendance_report_activity, menu);
        Log.i(TAG, "menu created");
        return super.onCreateOptionsMenu(menu);
    }


    protected void onPostExecute(Boolean isSuccess) {
        if (dialog.isShowing()) {
            dialog.dismiss();
        }

        if (isSuccess) {
            Toast.makeText(EditAttendanceReport_RV.this, "Training successful.", Toast.LENGTH_LONG).show();
        } else {
            Toast.makeText(EditAttendanceReport_RV.this, "Training failed. Error encountered.", Toast.LENGTH_LONG).show();
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.add_to_train:
                //Toast.makeText(getApplicationContext(), "Lipat na sa untrained!", Toast.LENGTH_LONG).show();


                 final Handler handler = new Handler() {
                    public void handleMessage(Message msg) {
                        if(msg.arg1 == 1) {
                            Toast.makeText(getApplicationContext(), "Copied to train!", Toast.LENGTH_LONG).show();
                        }
                        if(msg.arg1 == 2) {
                            Toast.makeText(getApplicationContext(), "Crops already copied!", Toast.LENGTH_LONG).show();
                        }

                    }
                };


                dialog = new ProgressDialog(EditAttendanceReport_RV.this);
                dialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
                dialog.setIndeterminate(true);
                dialog.setCancelable(false);
                dialog.setCanceledOnTouchOutside(false);
                dialog.setMessage("Copying images...");
                dialog.show();

                new Thread(new Runnable() {
                    public void run() {

                        String[] x = reportPath.split(Pattern.quote("."));
                        String cropFolder = x[0];

                        File sdCardRoot = Environment.getExternalStorageDirectory();
                        File untrainedCrops = new File(sdCardRoot, "PresentData/faceDatabase/untrainedCrops");
                        File reportCrops = new File(cropFolder);


                        Message msg = handler.obtainMessage();

                        //Before moving the crop to untrainedCrops, find a new filename for the crop that does not conflict with a crop already in untrainedCrops.
                        String[] fNameDetails;
                        int secondaryID;
                        File destFile;
                        String firstPartName;
                        for (File f : reportCrops.listFiles()) {
                            fNameDetails = f.getName().split("_");

                            if(fNameDetails.equals("unlabeled")){
                                firstPartName = "unlabeled";
                            } else {
                                firstPartName = fNameDetails[0] + "_" + fNameDetails[1];
                            }

                            secondaryID = 0;

                            do {
                                destFile = new File(untrainedCrops + "/" + firstPartName + "_" + secondaryID + ".jpg");
                                secondaryID++;
                            } while(destFile.exists());

                            try {
                                copyFile(f, destFile);
                            }catch(IOException e){
                                Log.e(TAG, e.getMessage());
                            }

                        }

                        dialog.dismiss();
                        msg.arg1 = 1;
                        handler.sendMessage(msg);
                    }
                }).start();

                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }


    private static void copyFile(File source, File dest) throws IOException {
        InputStream input = null;
        OutputStream output = null;
        try {
            input = new FileInputStream(source);
            output = new FileOutputStream(dest);
            byte[] buf = new byte[1024];
            int bytesRead;
            while ((bytesRead = input.read(buf)) > 0) {
                output.write(buf, 0, bytesRead);
            }
        } finally {
            input.close();
            output.close();
        }
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

    public void showNonFaces(View v){
        AlertDialog.Builder alertBuilder = new AlertDialog.Builder(EditAttendanceReport_RV.this,R.style.Theme_Holo_Dialog_Alert);

        String[] x = reportPath.split(Pattern.quote("."));
        Log.i(TAG,"cropFolder --> " + x[0]);

        //POPULATE pathList from the cropped faces files from sdCard/PresentData/faceCrops folder
        File faceCropsDir = new File(x[0]);

        FilenameFilter nonFaceCropsImgFilter = new FilenameFilter() {
            public boolean accept(File dir, String name) {
                name = name.toLowerCase();
                return name.startsWith("0_");
            }
        };

        Log.i(TAG,"facecropsdir --> " + faceCropsDir.getAbsolutePath());

        Log.i(TAG,"facecropsdir --> " + faceCropsDir.isDirectory());

        for (File f : faceCropsDir.listFiles(nonFaceCropsImgFilter)) {
            if (f.isFile()) {
                nonFaces.add(f.getAbsolutePath());
            }
        }

        LayoutInflater inflater = (LayoutInflater) EditAttendanceReport_RV.this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View layout = inflater.inflate(R.layout.edit_report_grid_dialog,(ViewGroup) v.findViewById(R.id.layout_root));
        GridView gridview = (GridView)layout.findViewById(R.id.face_grid);
        gridview.setAdapter(new ImageAdapter(EditAttendanceReport_RV.this,nonFaces));

        alertBuilder.setMessage("Non Faces for " + date)
                .setCancelable(false)
                .setNegativeButton("Back", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.cancel();
                            }
                        }
                );

        alertBuilder.setView(layout);
        AlertDialog alert = alertBuilder.create();
        TextView textView = new TextView(EditAttendanceReport_RV.this);
        textView.setTextColor(Color.WHITE);
        textView.setTextSize(20);
        //textView.setText("\tNon Faces");
        alert.setCustomTitle(textView);
        alert.setTitle("Non Faces");
        alert.show();
    }
    ///////////////////////////////////////////////////////////////////////////////////////
    ////////////////////////////ADAPTER FOR GRIDVIEW//////////////////////////////////////
    /////////////////////////////////////////////////////////////////////////////////////

    public class ImageAdapter extends BaseAdapter {
        private Context context;
        private LayoutInflater inflater;
        private ArrayList<String> paths;
        public ImageAdapter(Context c,ArrayList<String> cropPaths) {
            inflater = LayoutInflater.from(c);
            context = c;
            paths = cropPaths;
        }
        public int getCount() {
            return paths.size();
        }
        public Object getItem(int position) {
            return null;
        }
        public long getItemId(int position) {
            return 0;
        }
        // create a new ImageView for each item referenced by the
        public View getView(int position, View convertView, ViewGroup parent) {
            ViewHolder holder;
            if (convertView == null) {  // if it's not recycled,
                convertView = inflater.inflate(R.layout.grid_item, null);
                convertView.setLayoutParams(new GridView.LayoutParams(120, 120));
                holder = new ViewHolder();
                holder.crop = (ImageView)convertView.findViewById(R.id.categoryimage);
                convertView.setTag(holder);
            } else {
                holder = (ViewHolder) convertView.getTag();
            }
            holder.crop.setAdjustViewBounds(true);
            holder.crop.setScaleType(ImageView.ScaleType.CENTER_CROP);

            Bitmap bmImg = BitmapFactory.decodeFile(paths.get(position));
            holder.crop.setImageBitmap(bmImg);

            return convertView;
        }
        class ViewHolder {
            ImageView crop;
        }

    }


///////////////////////////////////////////////////////////////////////////////////////
////////////////////////////ADAPTER FOR GRIDVIEW//////////////////////////////////////
/////////////////////////////////////////////////////////////////////////////////////
}
