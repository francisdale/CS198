package com.example.dale.cs198;

import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.Environment;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;

import java.io.File;
import java.io.FileWriter;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.regex.Pattern;

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
        File faceCropsDir = new File(sdCardRoot, "PresentData/faceDatabase/trainedCrops");


        final String ID = Integer.toString(s.getId());
        FilenameFilter cropFilter = new FilenameFilter() {
            public boolean accept(File dir, String name) {
                name = name.toLowerCase();
                return name.startsWith(ID + "_");
            }
        };
        File [] faceCrops = faceCropsDir.listFiles(cropFilter);

        int index;

        for (index = 0; index < faceCrops.length; index++) {
            if (faceCrops[index].isFile()) {
                Log.i(TAG, "directory of first occurence -->" + faceCrops[index].getAbsolutePath());
                Bitmap bmImg = BitmapFactory.decodeFile(faceCrops[index].getAbsolutePath());
                holder.face.setImageBitmap(bmImg);
                break;
            }
        }

        if(index == faceCrops.length){
            holder.face.setImageResource(R.mipmap.ic_launcher);
        }

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

                String ID = Integer.toString(studentList.get(getAdapterPosition()).getId());
                String[] x = reportPath.split(Pattern.quote("."));
                String cropFolder = x[0];
                Log.i(TAG,"cropFolder --> " + x[0]);

                LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                View layout = inflater.inflate(R.layout.edit_report_grid_dialog,(ViewGroup) v.findViewById(R.id.layout_root));
                GridView gridview = (GridView)layout.findViewById(R.id.face_grid);
                gridview.setAdapter(new ImageAdapter(context,getCropPaths(ID,cropFolder)));

                alertBuilder.setMessage("Are you sure that " + studInfo.getText().toString() + " is ABSENT on " + date + " for " + className + " ?\n")
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

                alertBuilder.setView(layout);
                AlertDialog alert = alertBuilder.create();
                TextView textView = new TextView(context);
                textView.setTextColor(Color.WHITE);
                textView.setTextSize(20);
                textView.setText("\tMake Student ABSENT");
                alert.setCustomTitle(textView);
                //alert.setTitle("Make Student ABSENT");
                alert.show();

            }
            if (detail.isChecked() == false) {
                AlertDialog.Builder alertBuilder = new AlertDialog.Builder(context,R.style.Theme_Holo_Dialog_Alert);

                String ID = Integer.toString(studentList.get(getAdapterPosition()).getId());
                String[] x = reportPath.split(Pattern.quote("."));
                String cropFolder = x[0];

                LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                View layout = inflater.inflate(R.layout.edit_report_grid_dialog,(ViewGroup) v.findViewById(R.id.layout_root));
                GridView gridview = (GridView)layout.findViewById(R.id.face_grid);
                gridview.setAdapter(new ImageAdapter(context,getCropPaths(ID,cropFolder)));


                alertBuilder.setMessage("Are you sure that " + studInfo.getText().toString() + " is PRESENT on " + date + " for " + className + " ?\n")
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


                alertBuilder.setView(layout);
                AlertDialog alert = alertBuilder.create();
                TextView textView = new TextView(context);
                textView.setTextColor(Color.WHITE);
                textView.setTextSize(20);
                textView.setText("\tMake Student PRESENT");
                alert.setCustomTitle(textView);
                //alert.setTitle("Make Student PRESENT");
                alert.show();
            }


        }

    }

    public ArrayList<String> getCropPaths(String ID,String folderPath){

        ArrayList<String> thePaths = new ArrayList<String>();

        File faceCropsDir = new File(folderPath);

        for (File f : faceCropsDir.listFiles()) {
            if (f.isFile()) {
                if(f.getName().startsWith(ID+"_")){
                    thePaths.add(f.getAbsolutePath());
                }
            }
        }

        return thePaths;
    }


    public void rewriteReport(ArrayList<StudentItem> students){
        try {
            File classFile = new File(reportPath);
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
            //holder.crop.setPadding(8, 8, 8, 8);

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
