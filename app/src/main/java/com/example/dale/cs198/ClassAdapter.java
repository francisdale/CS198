package com.example.dale.cs198;

import android.content.Context;
import android.content.Intent;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import java.util.ArrayList;
/**
 * Created by DALE on 1/21/2016.
 */
public class ClassAdapter extends RecyclerView.Adapter<ClassAdapter.ClassViewHolder> {


    ArrayList<ClassItem> classes = new ArrayList<ClassItem>();
    private static final String TAG = "testMessage";
    private Context context;
    private String classNameString;

    public ClassAdapter(Context context,ArrayList<ClassItem> classes){
        this.classes = classes;
        this.context = context;
    }

    @Override
    public void onBindViewHolder(ClassViewHolder holder, int position) {
        ClassItem c = classes.get(position);
        classNameString = c.getName();
        holder.className.setText(classNameString);
        holder.startTime.setText(c.getStartTime());
        holder.endTime.setText(c.getEndTime());
    }


    @Override
    public ClassViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.card_view_layout, parent, false);
        ClassViewHolder classViewHolder = new ClassViewHolder(view);
        return classViewHolder;
    }

    @Override
    public int getItemCount() {
        return classes.size();
    }


    class ClassViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener{

        TextView className;
        TextView startTime;
        TextView endTime;
        Button takePic;


        // / FragmentActivity fa;

        public ClassViewHolder(View view){
            super(view);
            view.setOnClickListener(this);
            className = (TextView)view.findViewById(R.id.className);
            startTime = (TextView)view.findViewById(R.id.classStartTime);
            endTime = (TextView)view.findViewById(R.id.classEndTime);

            takePic = (Button)view.findViewById(R.id.take_picture);
            takePic.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v){
                    Log.i(TAG, "ClassAdapter Clicked Take Pic");
                    Intent cam = new Intent(context, CustomCamera.class);
                    cam.putExtra("detectUsage", FaceDetectTask.ATTENDANCE_USAGE);
                    cam.putExtra("classNameString", classNameString);
                    context.startActivity(cam);
                    //pass a mode na activity usage integer = 0;
                    //call FaceDetectTask.ATTENDANCE_USAGE

                }
            });
        }

        @Override
        public void onClick(View v) {

            Log.i(TAG, "CALLING FRAGMENT MANAGER");
            //v.setBackgroundColor(Color.LTGRAY);
            Intent cardIntent = new Intent(context,ClassList.class);
            cardIntent.putExtra("name",className.getText());
            cardIntent.putExtra("start",startTime.getText());
            cardIntent.putExtra("end",endTime.getText());
            context.startActivity(cardIntent);

            Log.i(TAG, "CALLING FRAGMENT");
        }




    }






}
