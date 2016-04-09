package com.example.dale.cs198;

import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Environment;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
/**
 * Created by DALE on 1/21/2016.
 */
public class EditAttendanceReport_RVAdapter extends RecyclerView.Adapter<EditAttendanceReport_RVAdapter.EditAttendanceViewHolder> {


    ArrayList<StudentItem> studentList = new ArrayList<StudentItem>();
    private static final String TAG = "testMessage";
    private Context context;
    private String classNameString;
    private String name;
    private String className;
    private String date;
    private String reportPath;


    public EditAttendanceReport_RVAdapter(Context context,ArrayList<StudentItem> studentList,String name, String className, String date,String reportPath){
        this.studentList = studentList;
        this.context = context;
        this.name = name;
        this.className = className;
        this.date = date;
        this.reportPath = reportPath;
    }

    @Override
    public void onBindViewHolder(EditAttendanceViewHolder holder, int position) {
        StudentItem s = studentList.get(position);
        holder.studInfo.setText(s.getStudentNumber() + " " + s.getLastName() + ", " + s.getFirstName());
        holder.detail.setChecked(s.isSelected());


        String a[] = name.split("\\.");
        File sdCardRoot = Environment.getExternalStorageDirectory();
        File faceCropsDir = new File(sdCardRoot, "PresentData/Classes/"+className + "/attendanceReports/" + a[0]);


        String ID = Integer.toString(s.getId());
        boolean meron = false;
        for (File f : faceCropsDir.listFiles()) {
            if (f.isFile()) {
                if(f.getName().startsWith(ID+"_0")){
                    Log.i(TAG, "directory of first occurence -->" + f.getAbsolutePath());
                    Bitmap bmImg = BitmapFactory.decodeFile(f.getAbsolutePath());
                    holder.face.setImageBitmap(bmImg);
                    meron = true;
                }
            }
        }

        if(meron == false){
            holder.face.setImageResource(R.mipmap.ic_launcher);
        }

//        final String ID = Integer.toString(s.getId());
//        FilenameFilter IDImgFilter = new FilenameFilter() {
//            public boolean accept(File dir, String fileName) {
//                Log.i(TAG, "Filtered ID --> " + ID);
//                return fileName.startsWith(ID,0);
//            }
//        };
//
//        File[] fileArr = faceCropsDir.listFiles(IDImgFilter);
//        Log.i(TAG, "File arr size -->" + fileArr.length);
//        if (fileArr.length > 0) {
//            Log.i(TAG, "directory of first occurence -->" + fileArr[0].getAbsolutePath());
//            Bitmap bmImg = BitmapFactory.decodeFile(fileArr[0].getAbsolutePath());
//            holder.face.setImageBitmap(bmImg);
//        } else {
//            Log.i(TAG, "no image");
//            holder.face.setImageResource(R.mipmap.ic_launcher);
//        }


    }


    @Override
    public EditAttendanceViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.card_view_edit_report_item, parent, false);
        EditAttendanceViewHolder classViewHolder = new EditAttendanceViewHolder(view);
        return classViewHolder;
    }

    @Override
    public int getItemCount() {
        return studentList.size();
    }


    class EditAttendanceViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener{

        ImageView face;
        CheckBox detail;
        TextView studInfo;


        // / FragmentActivity fa;

        public EditAttendanceViewHolder(View view){
            super(view);
            view.setOnClickListener(this);
            face = (ImageView)view.findViewById(R.id.student_face);
            detail = (CheckBox)view.findViewById(R.id.report_detail_item);
            studInfo = (TextView)view.findViewById(R.id.student_info);

        }

        @Override
        public void onClick(View v) {
            boolean previousState = detail.isChecked();

            Log.i(TAG, "Previous state --> " + previousState);
            Log.i(TAG,"position -->" + getAdapterPosition());
            if (detail.isChecked() == true) {
                AlertDialog.Builder alertBuilder = new AlertDialog.Builder(context,R.style.Theme_Holo_Dialog_Alert);
                alertBuilder.setMessage("Are you sure that " + studInfo.getText().toString() + " is ABSENT on " + date + " for " + className + " ?")
                        .setCancelable(false)
                        .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                detail.setChecked(false);
                                studentList.get(getAdapterPosition()).setSelected(false);
                                rewriteReport(studentList);
                                Log.i(TAG,studentList.get(getAdapterPosition()).getLastName() + " is " + studentList.get(getAdapterPosition()).isSelected());
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
                alert.setTitle("Make Student ABSENT");
                alert.show();

            }
            if (detail.isChecked() == false) {
                AlertDialog.Builder alertBuilder = new AlertDialog.Builder(context,R.style.Theme_Holo_Dialog_Alert);
                alertBuilder.setMessage("Are you sure that " + studInfo.getText().toString() + " is PRESENT on " + date + " for " + className + " ?")
                        .setCancelable(false)
                        .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                detail.setChecked(true);
                                studentList.get(getAdapterPosition()).setSelected(true);
                                rewriteReport(studentList);
                                Log.i(TAG, studentList.get(getAdapterPosition()).getLastName() + "is " + studentList.get(getAdapterPosition()).isSelected());
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
                alert.setTitle("Make Student PRESENT");
                alert.show();
            }


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



}
