package com.example.dale.cs198;

import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;

/**
 * Created by DALE on 2/11/2016.
 * For Swipe to delete function
 * for future use
 *
 */
public class CropImageTouchHelper extends ItemTouchHelper.SimpleCallback  {

    private CropImageAdapter cropImageAdapter;
    public static final float ALPHA_FULL = 1.0f;
    private RecyclerView recyclerView;
    public CropImageTouchHelper(RecyclerView recyclerView,CropImageAdapter cropImageAdapter){
        super(ItemTouchHelper.UP | ItemTouchHelper.DOWN, ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT);
        this.recyclerView = recyclerView;
        this.cropImageAdapter = cropImageAdapter;
    }

    @Override
    public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, RecyclerView.ViewHolder target) {
        //cropImageAdapter.swap(viewHolder.getAdapterPosition(), target.getAdapterPosition());
        return true;
    }

    @Override
    public void onSwiped(RecyclerView.ViewHolder viewHolder, int direction) {
        //cropImageAdapter.removeCrop(viewHolder.getAdapterPosition(),recyclerView);
    }
}
