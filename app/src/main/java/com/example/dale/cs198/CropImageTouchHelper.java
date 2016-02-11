package com.example.dale.cs198;

import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;

/**
 * Created by DALE on 2/11/2016.
 */
public class CropImageTouchHelper extends ItemTouchHelper.SimpleCallback  {

    private CropImageAdapter cropImageAdapter;

    public CropImageTouchHelper(CropImageAdapter cropImageAdapter){
        super(ItemTouchHelper.UP | ItemTouchHelper.DOWN, ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT);
        this.cropImageAdapter = cropImageAdapter;
    }

    @Override
    public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, RecyclerView.ViewHolder target) {
        cropImageAdapter.swap(viewHolder.getAdapterPosition(), target.getAdapterPosition());
        return true;
    }

    @Override
    public void onSwiped(RecyclerView.ViewHolder viewHolder, int direction) {
        cropImageAdapter.remove(viewHolder.getAdapterPosition());
    }






}
