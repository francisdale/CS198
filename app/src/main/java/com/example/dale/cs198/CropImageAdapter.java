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
    ArrayList<CropImageItem> copyFaceCrops;
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
        copyFaceCrops = new ArrayList<CropImageItem>(faceCrops);

        readMasterList();


    }

    @Override
    public void onBindViewHolder(final CropImageViewHolder holder, int position) {
        //Log.i(TAG, "onBindViewHolder Recycling...");
        CropImageItem c = faceCrops.get(position);
        //holder.cropName.setText(c.getFileName());

        Bitmap bmImg = BitmapFactory.decodeFile(c.getPath());
        holder.cropImage.setImageBitmap(bmImg);
        holder.customEtListener.updatePosition(position);
        holder.cropName.setText(labelArr[position]);
        //holder.cropName.setText(c.getPos() + "x");

        String holderName[] = c.getFileName().split("_");

        if(holderName[0].length()<=2){
            String nameInfo[] = names.get(Integer.parseInt(holderName[0])).split("-");
            holder.cropName.setText(nameInfo[1]);
        }
        //1-12-7
        if(holderName[0].equals("unlabeled")){
            holder.cropName.setText(labelArr[position]);
        }
        if(holderName[0].equals("delete")){
           holder.cropName.setText("NOT A FACE");
        }

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
                        //Log.i(TAG,"");
                        //Log.i(TAG,"///////////////////////////////RENAMING PHASE//////////////////////////////");
                        if(dialogName.getText().toString().equals("NOT A FACE") == false){
                            String idAndName[] =  dialogName.getText().toString().split("-");
                            label = idAndName[1];
                            int idNum = Integer.parseInt(idAndName[0]);

                            Log.i(TAG,"");
                            Log.i(TAG,"///////////////////////////////RENAMING PHASE//////////////////////////////");

                            String fileNameOfLabeled = faceCrops.get(getAdapterPosition()).getFileName();
                            String filePathOfLabeled = faceCrops.get(getAdapterPosition()).getPath();

                            Log.i(TAG,"SELECTED CROP TO LABEL: "+fileNameOfLabeled+"\n"+filePathOfLabeled);

                            /*//CHECK IF MAY EXISTING CROP NA SI ID SA TRAINED FOLDER
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
                            File newfile =new File(sdCardRoot, "PresentData/faceDatabase/untrainedCrops/"+idNum+"_"+count+"_"+label+".jpg");*/

                            File oldfile =new File(filePathOfLabeled);
                            File sdCardRoot = Environment.getExternalStorageDirectory();

                            for(int i=0;;i++){
                                File newFilePic =new File(sdCardRoot, "PresentData/faceDatabase/untrainedCrops/"+idNum+"_"+i+"_"+label+".jpg");
                                if(!newFilePic.exists()){
                                    if (oldfile.renameTo(newFilePic)) {
                                        faceCrops.get(getAdapterPosition()).setFileName(newFilePic.getName());
                                        faceCrops.get(getAdapterPosition()).setPath(newFilePic.getAbsolutePath());
                                        Log.i(TAG, "Rename succesful");
                                        Log.i(TAG, "The renamed file is: " + faceCrops.get(getAdapterPosition()).getFileName() + "\n" + faceCrops.get(getAdapterPosition()).getPath());
                                    } else {
                                        Log.i(TAG, "Rename failed");
                                    }
                                    break;
                                }
                            }
                            cropName.setText(label);
                            nameDialog.dismiss();
                        }

                        else{
                            label = dialogName.getText().toString();

                            String fileNameOfNotFace = faceCrops.get(getAdapterPosition()).getFileName();
                            String filePathOfNotFace = faceCrops.get(getAdapterPosition()).getPath();

                            File sdCardRoot = Environment.getExternalStorageDirectory();
                            File faceCropsDir = new File(sdCardRoot, "PresentData/faceDatabase/untrainedCrops");
                            Log.i(TAG,"");
                            Log.i(TAG,"///////////////////////////////RENAMING PHASE//////////////////////////////");

                            File oldfile = new File(filePathOfNotFace);
                            File newfile = new File(sdCardRoot, "PresentData/faceDatabase/untrainedCrops/"+"delete_"+fileNameOfNotFace);

                            if(oldfile.renameTo(newfile)){
                                faceCrops.get(getAdapterPosition()).setFileName(newfile.getName());
                                faceCrops.get(getAdapterPosition()).setPath(newfile.getAbsolutePath());
                                Log.i(TAG, "Rename succesful");
                                Log.i(TAG,"The renamed file is: "+ faceCrops.get(getAdapterPosition()).getFileName()+"\n"+ faceCrops.get(getAdapterPosition()).getPath());
                            }
                            else{
                                Log.i(TAG, "Rename failed");
                            }


                            cropName.setText(label);
                            nameDialog.dismiss();
                        }
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
            //pos = position;
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
        public void afterTextChanged(Editable editable) { }
    }

    public void removeCrop(final int position,final RecyclerView recyclerView) {
        Log.i(TAG,"");
        Log.i(TAG, "////////////////////////////////REMOVING AND IMAGE//////////////////////////////");
        temp = faceCrops.get(position);
        //rename mo nalang yung file and give an indication na dapat siyang madelete

        File oldfile;
        File newfile;
        CropImageItem rename;
        final String fileNameOfRemoved = temp.getFileName();
        String filePathOfRemoved = temp.getPath();
        Log.i(TAG,"TO BE DELETED: "+fileNameOfRemoved+"\n"+filePathOfRemoved);
        //DELETE AND MAKE A COPY OF CROP TO BE REMOVED WITH DIFF FILE NAME IN DIRECTORY

        //RENAME CROP WITH CHOSEN IDNUM

        final File sdCardRoot = Environment.getExternalStorageDirectory();
        oldfile = new File(filePathOfRemoved);
        newfile = new File(sdCardRoot, "PresentData/faceDatabase/untrainedCrops/"+"delete_"+fileNameOfRemoved);

        if(oldfile.renameTo(newfile)){
            temp.setFileName(newfile.getName());
            temp.setPath(newfile.getAbsolutePath());
            Log.i(TAG, "Rename succesful");
            Log.i(TAG,"The renamed file is: "+temp.getFileName()+"\n"+temp.getPath());
        }
        else{
            Log.i(TAG, "Rename failed");
        }

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

                        String filePathOfUndo = faceCrops.get(position).getPath();
                        String fileNameOfUndo = faceCrops.get(position).getFileName();

                        File old = new File(filePathOfUndo);
                        File newF = new File(sdCardRoot,"PresentData/faceDatabase/untrainedCrops/"+fileNameOfRemoved);

                        if(old.renameTo(newF)){
                            faceCrops.get(position).setFileName(newF.getName());
                            faceCrops.get(position).setPath(newF.getAbsolutePath());
                            Log.i(TAG,"UNDO SUCCESSFUL");
                            Log.i(TAG,"RETURNED : "+faceCrops.get(position).getFileName()+"\n"+faceCrops.get(position).getPath());
                        }
                        else{
                            Log.i(TAG, "Rename failed");
                        }

                        Log.i(TAG,"You just undo and returned: " + temp.getFileName() + "\n"+temp.getPath());

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
