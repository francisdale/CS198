package com.example.dale.cs198;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Environment;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.ArrayList;

/**
 * Created by DALE on 1/26/2016.
 */
public class StudentAdapter extends RecyclerView.Adapter<StudentAdapter.StudentViewHolder> {


    ArrayList<StudentItem> students = new ArrayList<StudentItem>();
    private static final String TAG = "testMessage";
    private int ID;
    private Context context;
    private String className;
    BufferedReader br;

    public StudentAdapter(Context context, ArrayList<StudentItem> students, String className) {
        this.students = students;
        this.context = context;
        this.className = className;
    }

    @Override
    public void onBindViewHolder(StudentViewHolder holder, int position) {
        StudentItem s = students.get(position);
        holder.studentName.setText(s.getLastName() + " " + s.getFirstName());
        holder.studentNumber.setText(s.getStudentNumber());

        File sdCardRoot = Environment.getExternalStorageDirectory();
        File faceCropsDir = new File(sdCardRoot, "PresentData/faceDatabase/trainedCrops");


        final String ID = Integer.toString(s.getId());



        FilenameFilter IDImgFilter = new FilenameFilter() {
            public boolean accept(File dir, String name) {
                Log.i(TAG, "Filtered ID --> " + ID);
                return name.startsWith(ID + "_");
            }
        };

        File[] fileArr = faceCropsDir.listFiles(IDImgFilter);
        Log.i(TAG, "File arr size -->" + fileArr.length);
        if (fileArr.length > 0) {
            Log.i(TAG, "directory of first occurence -->" + fileArr[0].getAbsolutePath());
            Bitmap bmImg = BitmapFactory.decodeFile(fileArr[0].getAbsolutePath());
            holder.studentFace.setImageBitmap(bmImg);
        } else {
            Log.i(TAG, "no image");
            holder.studentFace.setImageResource(R.mipmap.ic_launcher);
        }


    }


    @Override
    public StudentViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.student_row_layout, parent, false);
        StudentViewHolder studentViewHolder = new StudentViewHolder(view);
        return studentViewHolder;
    }

    @Override
    public int getItemCount() {
        return students.size();
    }


    class StudentViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        TextView studentName;
        TextView studentNumber;
        ImageView studentFace;

        public StudentViewHolder(View view) {
            super(view);
            view.setOnClickListener(this);
            studentName = (TextView) view.findViewById(R.id.studName);
            studentNumber = (TextView) view.findViewById(R.id.studNum);
            studentFace = (ImageView) view.findViewById(R.id.student_face);

        }

        @Override
        public void onClick(View v) {
            File sdCardRoot = Environment.getExternalStorageDirectory();
            File reportListDir = new File(sdCardRoot, "PresentData/Classes/" + className + "/attendanceReports");
            if (reportListDir.list().length == 0) {
                Toast.makeText(context, "There are no attendance reports for " + className + " yet.", Toast.LENGTH_LONG).show();
            }
            else {
                int absentNum = 0;
                for (File f : reportListDir.listFiles()) {
                    if (f.isFile()) {
                        try {
                            br = new BufferedReader(new FileReader(f));
                            String line;
                            String[] details;
                            while ((line = br.readLine()) != null) {
                                details = line.split(",");
                                if(Integer.parseInt(details[0]) == students.get(getAdapterPosition()).getId()){
                                    if(Integer.parseInt(details[4]) == 0){
                                        absentNum++;
                                        break;
                                    }
                                    else{
                                        break;
                                    }
                                }
                            }
                            br.close();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }
                Toast.makeText(context, studentName.getText().toString() + " has " + absentNum + " absences for " + className + ".", Toast.LENGTH_LONG).show();
            }
        }
    }
}

