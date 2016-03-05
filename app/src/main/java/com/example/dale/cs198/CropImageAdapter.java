package com.example.dale.cs198;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Environment;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;

/**
 * Created by DALE on 1/21/2016.
 */
public class CropImageAdapter extends RecyclerView.Adapter<CropImageAdapter.CropImageViewHolder> {


    ArrayList<CropImageItem> faceCrops = new ArrayList<CropImageItem>();
    ArrayList<Integer> ids = new ArrayList<>();
    ArrayList<String> names = new ArrayList<String>();

    CropImageItem temp;
    private static final String TAG = "testMessage";
    private Context context;

    ListView namesList = null;

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
        readMasterList();


    }

    @Override
    public void onBindViewHolder(final CropImageViewHolder holder, int position) {
        Log.i(TAG, "onBindViewHolder Recycling...");
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
        CustomEtListener customEtListener;



        public CropImageViewHolder(View view,CustomEtListener etListener){
            super(view);
            view.setOnClickListener(this);
            cropName = (TextView)view.findViewById(R.id.crop_label);
            cropImage = (ImageView)view.findViewById(R.id.crop_image);
            customEtListener = etListener;
            cropName.addTextChangedListener(customEtListener);
        }

        @Override
        public void onClick(View v) {
            //Toast.makeText(context.getApplicationContext(), "Tapped on " + cropName.getText().toString(), Toast.LENGTH_LONG).show();

            namesList = new ListView(context.getApplicationContext());
            ArrayAdapter<String> namesListAdapter = new ArrayAdapter<String>(context.getApplicationContext(),R.layout.names_dialog_layout,R.id.dialogName,names);
            namesList.setAdapter(namesListAdapter);
            namesList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    ViewGroup viewGroup = (ViewGroup) view;
                    dialogName = (TextView) viewGroup.findViewById(R.id.dialogName);
                    //label is the name of the cro
                    Log.i(TAG,"");
                    Log.i(TAG,"///////////////////////////////RENAMING PHASE//////////////////////////////");

                    String idAndName[] =  dialogName.getText().toString().split("-");
                    label = idAndName[1];
                    int idNum = Integer.parseInt(idAndName[0]);
                    String fileNameOfLabeled = faceCrops.get(pos).getFileName();
                    String filePathOfLabeled = faceCrops.get(pos).getPath();

                    Log.i(TAG,"SELECTED CROP TO LABEL: "+fileNameOfLabeled+"\n"+filePathOfLabeled);

                    //CHECK IF MAY EXISTING CROP NA SI ID SA TRAINED FOLDER
                    File sdCardRoot = Environment.getExternalStorageDirectory();
                    File faceCropsDir = new File(sdCardRoot, "PresentData/faceDatabase/untrainedCrops");
                    int count=0;
                    for (File f : faceCropsDir.listFiles()) {
                        if (f.isFile()) {
                            String name = f.getName();
                            String nameArr[] = name.split("_");
                            if(nameArr[0].equals(idAndName[0])){
                                count++;
                            }
                        }
                    }
                    Log.i(TAG,"COUNT = "+count);

                    //RENAME CROP WITH CHOSEN IDNUM
                    File oldfile =new File(filePathOfLabeled);
                    File newfile =new File(sdCardRoot, "PresentData/faceDatabase/untrainedCrops/"+idNum+"_"+count+".jpg");
                    if(oldfile.renameTo(newfile)){
                        faceCrops.get(pos).setFileName(newfile.getName());
                        faceCrops.get(pos).setPath(newfile.getAbsolutePath());
                        Log.i(TAG,"The renamed file is: "+faceCrops.get(pos).getFileName()+"\n"+faceCrops.get(pos).getPath());
                    }
                    else{
                        Log.i(TAG, "Rename failed");
                    }

                    cropName.setText(label);
                    nameDialog.dismiss();
                    notifyItemChanged(pos);

                    Log.i(TAG, "////////////////////////////////END OF RENAMING PHASE//////////////////////////////");
                    Log.i(TAG,"");
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
        //Updates the position according to onBindViewHolder
        //@param position - position of the focused ite
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
            pos = position;
        }

        @Override
        public void afterTextChanged(Editable editable) { }
    }

    public void removeCrop(final int position,final RecyclerView recyclerView) {
        Log.i(TAG,"");
        Log.i(TAG, "////////////////////////////////REMOVING AND IMAGE//////////////////////////////");
        temp = faceCrops.get(position);
        //rename mo nalang yung file and give an indication na dapat siyang madelete

        String fileNameOfRemoved = temp.getFileName();
        String filePathOfRemoved = temp.getPath();
        Log.i(TAG,"TO BE DELETED: "+fileNameOfRemoved+"\n"+filePathOfRemoved);
        //DELETE AND MAKE A COPY OF CROP TO BE REMOVED WITH DIFF FILE NAME IN DIRECTORY

        faceCrops.remove(position);
        notifyItemRemoved(position);
        recyclerView.scrollToPosition(position);

        Snackbar snackbar = Snackbar
                .make(recyclerView, "FACE CROP REMOVED", Snackbar.LENGTH_LONG)
                .setAction("UNDO", new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        faceCrops.add(position, temp);
                        notifyItemInserted(position);
                        recyclerView.scrollToPosition(position);

                        Toast toast = Toast.makeText(context, "Face Crop Number = " + faceCrops.size(), Toast.LENGTH_SHORT);
                        toast.show();
                    }
                });
        snackbar.show();

        Toast toast = Toast.makeText(context, "Face Crop Number = " + faceCrops.size(), Toast.LENGTH_SHORT);
        toast.show();

        Log.i(TAG, "////////////////////////////////END OF REMOVING AND IMAGE//////////////////////////////");
        Log.i(TAG,"");
    }

    public void swap(int firstPosition, int secondPosition){
        Collections.swap(faceCrops, firstPosition, secondPosition);
        notifyItemMoved(firstPosition, secondPosition);
    }

    public boolean onItemMove(int fromPosition, int toPosition) {
        Collections.swap(faceCrops, fromPosition, toPosition);
        notifyItemMoved(fromPosition, toPosition);
        return true;
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
                names.add(details[0]+"-"+details[2]+" "+details[3]);
                //lagay yung mga mapping if ids
            }
            br.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }




}
