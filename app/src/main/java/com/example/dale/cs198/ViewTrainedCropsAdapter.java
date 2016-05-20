package com.example.dale.cs198;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;

/**
 * Created by DALE on 1/21/2016.
 */
public class ViewTrainedCropsAdapter extends RecyclerView.Adapter<ViewTrainedCropsAdapter.TrainedCropImageViewHolder> {


    ArrayList<CropImageItem> faceCrops = new ArrayList<CropImageItem>();
    ArrayList<CropImageItem> copyFaceCrops;
    ArrayList<String> names = new ArrayList<String>();

    private Context context;
    String[] labelArr;



    public ViewTrainedCropsAdapter(Context context,ArrayList<CropImageItem> faceCrops){
        this.faceCrops = faceCrops;
        this.context = context;
        labelArr = new String[faceCrops.size()];
        copyFaceCrops = new ArrayList<CropImageItem>(faceCrops);
        readMasterList();

    }

    @Override
    public void onBindViewHolder(final TrainedCropImageViewHolder holder, int position) {
        //Log.i(TAG, "onBindViewHolder Recycling...");
        CropImageItem c = faceCrops.get(position);
        //holder.cropName.setText(c.getFileName());

        Bitmap bmImg = BitmapFactory.decodeFile(c.getPath());
        holder.cropImage.setImageBitmap(bmImg);
        holder.customEtListener.updatePosition(position);
        holder.cropName.setText(labelArr[position]);
        //holder.cropName.setText(c.getPos() + "x");

        String holderName[] = c.getFileName().split("_");

        if (holderName[0].equals("0")) {
            holder.cropName.setText("NONFACE");
        } else if (holderName[0].equals("unlabeled_")){
            holder.cropName.setText("");
        } else {
            String nameInfo[] = names.get(Integer.parseInt(holderName[0])).split("-");
            holder.cropName.setText(nameInfo[1]);
        }
    }

    @Override
    public TrainedCropImageViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.crop_image_layout, parent, false);
        TrainedCropImageViewHolder trainedCropImageViewHolder = new TrainedCropImageViewHolder(view,new CustomEtListener());
        return trainedCropImageViewHolder;
    }

    @Override
    public int getItemCount() {
        return faceCrops.size();
    }


    class TrainedCropImageViewHolder extends RecyclerView.ViewHolder{

        TextView cropName;
        ImageView cropImage;
        CustomEtListener customEtListener;

        public TrainedCropImageViewHolder(View view,CustomEtListener etListener){
            super(view);
            //view.setOnClickListener(this);

            cropName = (TextView)view.findViewById(R.id.crop_label);
            cropImage = (ImageView)view.findViewById(R.id.crop_image);
            customEtListener = etListener;
            //cropName.addTextChangedListener(customEtListener);
        }

    }

    private class CustomEtListener implements TextWatcher {
        private int position;
        public void updatePosition(int position) {
            this.position = position;
        }

        @Override
        public void beforeTextChanged(CharSequence charSequence, int i, int i2, int i3) {
        }

        @Override
        public void onTextChanged(CharSequence charSequence, int i, int i2, int i3) {
            // Change the value of array according to the position
            //labelArr[position] = charSequence.toString();

        }

        @Override
        public void afterTextChanged(Editable editable) { }
    }

    public void readMasterList(){
        String dataPath = "sdcard/PresentData/";

        File file = new File(dataPath, "Master List.txt");

        try {
            BufferedReader br = new BufferedReader(new FileReader(file));
            String line;
            String[] details;
            names.add("NOT A FACE");
            while ((line = br.readLine()) != null) {
                details = line.split(",");
                names.add(details[0]+"-"+details[2]+", "+details[3]);
                //lagay yung mga mapping if ids
            }
            br.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
