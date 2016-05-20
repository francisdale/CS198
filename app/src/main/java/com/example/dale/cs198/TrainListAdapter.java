package com.example.dale.cs198;


import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.v7.app.AlertDialog;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;

/**
 * Created by DALE on 3/3/2016.
 */
public class TrainListAdapter extends BaseAdapter{

    private static final String TAG = "testMessage";

    Context context;
    int layoutResourceId;
    ArrayList<CropImageItem> crops = new ArrayList<CropImageItem>();

    ArrayList<String> names = new ArrayList<String>();
    ListView namesList = null;
    AlertDialog nameDialog;
    TextView dialogName;
    String label;

    int pos;
    String[] labelArr;

    public TrainListAdapter(Context context,int layoutResourceId,ArrayList<CropImageItem> crops){
        this.context=context;
        this.layoutResourceId=layoutResourceId;
        this.crops = crops;
        labelArr = new String[crops.size()];
        readMasterList();
    }


    @Override
    public int getCount() {
        return crops.size();
    }

    @Override
    public Object getItem(int position) {
        return crops.get(position);
    }


    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {


        final ViewHolder holder;
        LayoutInflater inflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        if (convertView == null) {
            Log.i(TAG,"null");
            convertView = inflater.inflate(layoutResourceId, parent, false);
        }

        holder = new ViewHolder();
        holder.labelItem = (TextView) convertView.findViewById(R.id.crop_label_grid);
        holder.imageItem = (ImageView) convertView.findViewById(R.id.crop_image_grid);
        holder.labelItem.addTextChangedListener(new CustomEtListener(position));


        convertView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                namesList = new ListView(context.getApplicationContext());
                ArrayAdapter<String> namesListAdapter = new ArrayAdapter<String>(context.getApplicationContext(), R.layout.names_dialog_layout, R.id.dialogName, names);
                namesList.setAdapter(namesListAdapter);
                namesList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                        ViewGroup viewGroup = (ViewGroup) view;
                        dialogName = (TextView) viewGroup.findViewById(R.id.dialogName);

                        String idAndName[] = dialogName.getText().toString().split("-");
                        label = idAndName[1];
                        holder.labelItem.setText(label);
                        nameDialog.dismiss();
                    }
                });

                AlertDialog.Builder builder = new AlertDialog.Builder(context);
                builder.setCancelable(true);
                builder.setView(namesList);

                nameDialog = builder.create();
                nameDialog.show();
            }
        });


        CropImageItem c = crops.get(position);
        Bitmap bmImg = BitmapFactory.decodeFile(c.getPath());
        holder.imageItem.setImageBitmap(bmImg);
        return convertView;

    }

    class ViewHolder {
        TextView labelItem;
        ImageView imageItem;
        CustomEtListener customEtListener;
    }


    private class CustomEtListener implements TextWatcher {
        private int position;
        //Updates the position according to onBindViewHolder
        //@param position - position of the focused ite
        public CustomEtListener (int position) {
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
