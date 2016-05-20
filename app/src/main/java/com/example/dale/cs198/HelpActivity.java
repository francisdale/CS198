package com.example.dale.cs198;

import android.graphics.Color;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import org.w3c.dom.Text;

public class HelpActivity extends AppCompatActivity {

    TextView uploadMasterHeader;
    TextView uploadMasterContent;

    TextView addStudentToMasterHeader;
    TextView addStudentToMasterContent;

    TextView addClassHeader;
    TextView addClassContent;

    TextView editStudentClassHeader;
    TextView editStudentClassContent;

    TextView trainHeader;
    TextView trainContent;

    TextView labelHeader;
    TextView labelContent;


    TextView takePictureHeader;
    TextView takePictureContent;

    TextView generateReportHeader;
    TextView generateReportContent;

    TextView editReportHeader;
    TextView editReportContent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_help);

        uploadMasterContent = (TextView)findViewById(R.id.uploadMaster_content);
        uploadMasterContent.setVisibility(View.GONE);

        addStudentToMasterContent = (TextView)findViewById(R.id.addStudentToMaster_content);
        addStudentToMasterContent.setVisibility(View.GONE);

        addClassContent = (TextView)findViewById(R.id.addClass_content);
        addClassContent.setVisibility(View.GONE);

        editStudentClassContent = (TextView)findViewById(R.id.editStudentClass_content);
        editStudentClassContent.setVisibility(View.GONE);

        trainContent = (TextView)findViewById(R.id.train_content);
        trainContent.setVisibility(View.GONE);

        labelContent = (TextView)findViewById(R.id.labelCrops_content);
        labelContent.setVisibility(View.GONE);

        takePictureContent = (TextView)findViewById(R.id.takePicture_content);
        takePictureContent.setVisibility(View.GONE);

        generateReportContent = (TextView)findViewById(R.id.generateReport_content);
        generateReportContent.setVisibility(View.GONE);

        editReportContent = (TextView)findViewById(R.id.editReport_content);
        editReportContent.setVisibility(View.GONE);

        /*=====================================================================================*/
        uploadMasterHeader = (TextView)findViewById(R.id.uploadMaster_header);
        uploadMasterHeader.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                toggle_contents(v, uploadMasterContent);
            }
        });

        addStudentToMasterHeader = (TextView)findViewById(R.id.addStudentToMaster_header);
        addStudentToMasterHeader.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                toggle_contents(v, addStudentToMasterContent);
            }
        });

        addClassHeader = (TextView)findViewById(R.id.addClass_header);
        addClassHeader.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                toggle_contents(v, addClassContent);
            }
        });


        editStudentClassHeader = (TextView)findViewById(R.id.editStudentClass_header);
        editStudentClassHeader.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                toggle_contents(v,editStudentClassContent);
            }
        });

        trainHeader = (TextView)findViewById(R.id.train_header);
        trainHeader.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                toggle_contents(v,trainContent);
            }
        });

        labelHeader = (TextView)findViewById(R.id.labelCrops_header);
        labelHeader.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                toggle_contents(v,labelContent);
            }
        });

        takePictureHeader = (TextView)findViewById(R.id.takePicture_header);
        takePictureHeader.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                toggle_contents(v,takePictureContent);
            }
        });

        generateReportHeader = (TextView)findViewById(R.id.generateReport_header);
        generateReportHeader.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                toggle_contents(v,generateReportContent);
            }
        });

        editReportHeader = (TextView)findViewById(R.id.editReport_header);
        editReportHeader.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                toggle_contents(v,editReportContent);
            }
        });
    }

    public void toggle_contents(View v,TextView content) {
        if (content.isShown()) {
            AnimationEffects.slide_down(this, content);
            content.setVisibility(View.GONE);
        } else {
            content.setVisibility(View.VISIBLE);
            AnimationEffects.slide_up(this, content);
        }
    }

}
