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


    private static final String untrainedCropsDir = "sdcard/PresentData/faceDatabase/untrainedCrops";

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

        Bitmap bmImg = BitmapFactory.decodeFile(c.getPath());
        holder.cropImage.setImageBitmap(bmImg);
        holder.customEtListener.updatePosition(position);
        holder.cropName.setText(labelArr[position]);

        String holderName[] = c.getFileName().split("_");


        //1-12-7
        if(holderName[0].equals("unlabeled")){
            holder.cropName.setText(labelArr[position]);
        }else if(holderName[0].equals("0")){
           holder.cropName.setText("NONFACE");
        }else if(holderName[0].equals("-1")){
            holder.cropName.setText("DELETE");
        }else{
            Log.i(TAG, "Labeled crop detected. Labeling the view...");
            String nameInfo[] = names.get(Integer.parseInt(holderName[0]) + 1).split("-");
            Log.i(TAG, "Crop file label: " + holderName[0] + ", Upcoming label on view: " + nameInfo[1]);
            holder.cropName.setText(nameInfo[1]);
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
                        String prefix = dialogName.getText().toString();
                        if(prefix.equals("NONFACE")) {
                            String fileNameOfLabeled = faceCrops.get(getAdapterPosition()).getFileName();
                            String filePathOfLabeled = faceCrops.get(getAdapterPosition()).getPath();

                            Log.i(TAG,"SELECTED CROP TO LABEL: "+fileNameOfLabeled+"\nPath:"+filePathOfLabeled);


                            File oldfile =new File(filePathOfLabeled);
                            File newFilePic;

                            for(int i=0;;i++){
                                newFilePic =new File(untrainedCropsDir+"/0_nonFace_"+i+".jpg");
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

                            cropName.setText(prefix);
                            nameDialog.dismiss();

                        } else if(prefix.equals("DELETE")){
                            String fileNameOfLabeled = faceCrops.get(getAdapterPosition()).getFileName();
                            String filePathOfLabeled = faceCrops.get(getAdapterPosition()).getPath();

                            Log.i(TAG,"SELECTED CROP TO LABEL: "+fileNameOfLabeled+"\n"+filePathOfLabeled);

                            File oldfile =new File(filePathOfLabeled);
                            File newFilePic;

                            for(int i=0;;i++){
                                newFilePic =new File(untrainedCropsDir+"/-1_delete_"+i+".jpg");
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

                            cropName.setText(prefix);
                            nameDialog.dismiss();

                        } else {
                            String idAndName[] =  prefix.split("-");
                            label = idAndName[1];
                            int idNum = Integer.parseInt(idAndName[0]);

                            Log.i(TAG,"");
                            Log.i(TAG,"///////////////////////////////RENAMING PHASE//////////////////////////////");

                            String fileNameOfLabeled = faceCrops.get(getAdapterPosition()).getFileName();
                            String filePathOfLabeled = faceCrops.get(getAdapterPosition()).getPath();

                            Log.i(TAG,"SELECTED CROP TO LABEL: "+fileNameOfLabeled+"\n"+filePathOfLabeled);

                            File oldfile =new File(filePathOfLabeled);
                            File sdCardRoot = Environment.getExternalStorageDirectory();
                            File newFilePic;

                            for(int i=0;;i++){
                                newFilePic = new File(untrainedCropsDir+"/"+idNum+"_"+label+"_"+i+".jpg");
                                if(!newFilePic.exists()){
                                    Log.i(TAG, "Old name: " + filePathOfLabeled);
                                    Log.i(TAG, "Upcoming new name: " + newFilePic.getAbsolutePath());
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
        public void afterTextChanged(Editable editable) { }
    }

    public void readMasterList(){
        String dataPath = "sdcard/PresentData/";

        File file = new File(dataPath, "Master List.txt");

        try {
            BufferedReader br = new BufferedReader(new FileReader(file));
            String line;
            String[] details;
            names.add("DELETE");
            names.add("NONFACE");
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

    /*FOR SWIPE TO DELETE FUNCTION FOR FUTURE USE*/
    public void removeCrop(final int position,final RecyclerView recyclerView) {
        Log.i(TAG,"");
        Log.i(TAG, "////////////////////////////////REMOVING AND IMAGE//////////////////////////////");
        temp = faceCrops.get(position);
        //rename mo nalang yung file and give an indication na dapat siyang madelete

        File oldfile;
        File newfile;
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
    /*FOR SWIPE TO DELETE FUNCTION FOR FUTURE USE*/

}
