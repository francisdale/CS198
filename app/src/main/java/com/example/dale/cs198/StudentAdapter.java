package com.example.dale.cs198;

import android.app.Fragment;
import android.content.Context;
import android.content.Intent;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;

/**
 * Created by DALE on 1/26/2016.
 */
public class StudentAdapter extends RecyclerView.Adapter<StudentAdapter.StudentViewHolder>  {


    ArrayList<StudentItem> students = new ArrayList<StudentItem>();
    private static final String TAG = "testMessage";
    private Context context;
    public StudentAdapter(Context context,ArrayList<StudentItem> students){
        this.students = students;
        this.context = context;
    }

    @Override
    public void onBindViewHolder(StudentViewHolder holder, int position) {
        StudentItem s = students.get(position);
        holder.studentName.setText(s.getLastName()+" "+s.getFirstName());
        holder.studentNumber.setText(s.getStudentNumber());
        //holder.studentFace == get file path;
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


    class StudentViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener{

        TextView studentName;
        TextView studentNumber;
        ImageView studentFace;

        // FragmentActivity fa;

        public StudentViewHolder(View view){
            super(view);
            view.setOnClickListener(this);
            studentName = (TextView)view.findViewById(R.id.studName);
            studentNumber = (TextView)view.findViewById(R.id.studNum);
            studentFace = (ImageView)view.findViewById(R.id.student_face);
        }

        @Override
        public void onClick(View v) {

            Log.i(TAG, "CALLING FRAGMENT MANAGER");


            Log.i(TAG, "CALLING FRAGMENT");

            int duration = Toast.LENGTH_SHORT;
            Toast toast = Toast.makeText(v.getContext(), "Selected " + studentName.getText().toString(), duration);
            toast.show();

        }



    }








}
