package com.example.dale.cs198;

import android.content.Context;
import android.content.Intent;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;

/**
 * Created by DALE on 3/12/2016.
 */
public class AttendanceReportListAdapter extends RecyclerView.Adapter<AttendanceReportListAdapter.ReportViewHolder>{

    ArrayList<ReportItem> reportItems = new ArrayList<ReportItem>();
    String finalDay;
    private static final String TAG = "testMessage";
    private Context context;
    private String className;

    public AttendanceReportListAdapter(Context context,ArrayList<ReportItem> reportItems,String className){
        this.reportItems = reportItems;
        this.context = context;
        this.className = className;
    }

    @Override
    public void onBindViewHolder(ReportViewHolder holder, int position) {
        try {
            //DateFormat df=new SimpleDateFormat("yyyy-MM-dd");
            DateFormat df=new SimpleDateFormat("EEEE MMM dd yyyy");
            finalDay=df.format(reportItems.get(position).getDate());
            holder.reportName.setText(finalDay);

        }catch (Exception e){
            e.printStackTrace();
        }
        //holder.reportName.setText(reportPaths.get(position));
    }


    @Override
    public ReportViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.card_view_report_item, parent, false);
        ReportViewHolder reportViewHolder = new ReportViewHolder(view);
        return reportViewHolder;
    }

    @Override
    public int getItemCount() {
        return reportItems.size();
    }


    class ReportViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener{

        TextView reportName;

        public ReportViewHolder(View view){
            super(view);
            view.setOnClickListener(this);
            reportName = (TextView)view.findViewById(R.id.attendanceReportTextView);

        }

        @Override
        public void onClick(View v) {

            Log.i(TAG, "CALLING FRAGMENT MANAGER");
            Intent openReport = new Intent(context,EditAttendanceReport_RV.class);
            openReport.putExtra("name", reportItems.get(getAdapterPosition()).getFileName());
            openReport.putExtra("className",className);
            openReport.putExtra("date",reportName.getText().toString());
            openReport.putExtra("reportPath",reportItems.get(getAdapterPosition()).getReportPath());
            //openReport.putExtra("dateOfFile",reportItems.get(getAdapterPosition()).)


            //Log.i(TAG, "OPENING --> " + reportItems.get(getAdapterPosition()).getFileName());
            //openReport.putExtra("name",reportName.getText().toString());

            //Log.i(TAG,"classname --> "+className);
            context.startActivity(openReport);
            //v.setBackgroundColor(Color.LTGRAY);
//            Intent reportIntent = new Intent(context,ClassList.class);
//            cardIntent.putExtra("name",className.getText());
//            cardIntent.putExtra("start",startTime.getText());
//            cardIntent.putExtra("end",endTime.getText());
//            context.startActivity(cardIntent);

            Log.i(TAG, "CALLING FRAGMENT");
        }




    }





}
