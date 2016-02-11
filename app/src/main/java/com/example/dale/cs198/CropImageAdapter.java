package com.example.dale.cs198;

import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.Image;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.LoaderManager;
import android.support.v4.app.SharedElementCallback;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileDescriptor;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collections;

import android.support.v7.app.AppCompatActivity;
/**
 * Created by DALE on 1/21/2016.
 */
public class CropImageAdapter extends RecyclerView.Adapter<CropImageAdapter.CropImageViewHolder> {

    ArrayList<CropImageItem> faceCrops = new ArrayList<CropImageItem>();
    private static final String TAG = "testMessage";
    private Context context;

    ListView namesList = null;
    ArrayList<String> names = new ArrayList<String>();
    AlertDialog nameDialog;
    TextView dialogName;
    String label;
    //TextView cropName;
    int pos;
    String[] labelArr;


    public CropImageAdapter(Context context,ArrayList<CropImageItem> faceCrops){
        this.faceCrops = faceCrops;
        this.context = context;

        labelArr = new String[faceCrops.size()];

//

    }

    @Override
    public void onBindViewHolder(final CropImageViewHolder holder, int position) {
        CropImageItem c = faceCrops.get(position);
        //holder.cropName.setText(c.getFileName());

        Bitmap bmImg = BitmapFactory.decodeFile(c.getPath());
        holder.cropImage.setImageBitmap(bmImg);

        holder.customEtListener.updatePosition(position);

        holder.cropName.setText(labelArr[position]);

    }

    @Override
    public CropImageViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.crop_image_layout, parent, false);
        CropImageViewHolder cropImageViewHolder = new CropImageViewHolder(view,new CustomEtListener());
        return cropImageViewHolder;
    }

    @Override
    public int getItemCount() {
        return faceCrops.size();
    }


    class CropImageViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener{

        TextView cropName;


        ImageView cropImage;
        //EditText cropName;
        CustomEtListener customEtListener;


        public CropImageViewHolder(View view,CustomEtListener etListener){
            super(view);
            view.setOnClickListener(this);
            cropName = (TextView)view.findViewById(R.id.crop_label);
            cropImage = (ImageView)view.findViewById(R.id.crop_image);

            customEtListener = etListener;
            cropName.addTextChangedListener(customEtListener);

//            cropName.setOnClickListener(new View.OnClickListener() {
//                @Override
//                public void onClick(View v) {
//
//                }
//            });

            //cropName.setText(label);

//            rename = (Button)view.findViewById(R.id.rename_button);
//            rename.setOnClickListener(new View.OnClickListener() {
//                @Override
//                public void onClick(View v) {
//
//                    showNamesDialogList();
//                }
//            });

        }

        @Override
        public void onClick(View v) {
                //Toast.makeText(context.getApplicationContext(), "Tapped on " + cropName.getText().toString(), Toast.LENGTH_LONG).show();

            readMasterList();
            namesList = new ListView(context.getApplicationContext());
            ArrayAdapter<String> namesListAdapter = new ArrayAdapter<String>(context.getApplicationContext(),R.layout.names_dialog_layout,R.id.dialogName,names);
            namesList.setAdapter(namesListAdapter);
            namesList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                ViewGroup viewGroup = (ViewGroup) view;
                dialogName = (TextView) viewGroup.findViewById(R.id.dialogName);
                label = dialogName.getText().toString();
                cropName.setText(label);
                nameDialog.dismiss();
                notifyItemChanged(pos);
                //cropName.setText(labelArr[pos]);


                }
            });

            AlertDialog.Builder builder = new AlertDialog.Builder(context);
            builder.setCancelable(true);
            builder.setView(namesList);

            nameDialog = builder.create();
            nameDialog.show();
        }


    }


    private class CustomEtListener implements TextWatcher {
        private int position;

        /**
         * Updates the position according to onBindViewHolder
         *
         * @param position - position of the focused item
         */
        public void updatePosition(int position) {
            this.position = position;
        }

        @Override
        public void beforeTextChanged(CharSequence charSequence, int i, int i2, int i3) {
        }

        @Override
        public void onTextChanged(CharSequence charSequence, int i, int i2, int i3) {
            // Change the value of array according to the position
            labelArr[position] = charSequence.toString();


        }


        @Override
        public void afterTextChanged(Editable editable) {
        }
    }


    public void remove(int position) {
        faceCrops.remove(position);
        notifyItemRemoved(position);
    }

    public void swap(int firstPosition, int secondPosition){
        Collections.swap(faceCrops, firstPosition, secondPosition);
        notifyItemMoved(firstPosition, secondPosition);
    }

    public void showNamesDialogList(){

        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setCancelable(true);
        builder.setView(namesList);
        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {

            }
        });
        nameDialog = builder.create();
        nameDialog.show();


    }


    public void readMasterList(){
        String dataPath = "sdcard/PresentData/";

        File file = new File(dataPath, "Master List.txt");

        try {
            BufferedReader br = new BufferedReader(new FileReader(file));
            String line;
            String[] details;
            while ((line = br.readLine()) != null) {
                details = line.split(",");
                names.add(details[1]+" "+details[2]);
            }
            br.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }




}
