package com.example.dale.cs198;

import android.graphics.Canvas;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;

/**
 * Created by DALE on 2/11/2016.
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
        cropImageAdapter.swap(viewHolder.getAdapterPosition(), target.getAdapterPosition());
        return true;
    }

    @Override
    public void onSwiped(RecyclerView.ViewHolder viewHolder, int direction) {
        cropImageAdapter.removeCrop(viewHolder.getAdapterPosition(),recyclerView);
    }
//    @Override
//    public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder source, RecyclerView.ViewHolder target) {
//        if (source.getItemViewType() != target.getItemViewType()) {
//            return false;
//        }
//
//        // Notify the adapter of the move
//        cropImageAdapter.onItemMove(source.getAdapterPosition(), target.getAdapterPosition());
//        return true;
//    }
//
//    @Override
//    public void onSwiped(RecyclerView.ViewHolder viewHolder, int i) {
//        // Notify the adapter of the dismissal
//        cropImageAdapter.remove(viewHolder.getAdapterPosition(),recyclerView);
//        //cropImageAdapter.onItemDismiss(viewHolder.getAdapterPosition(),recyclerView);
//    }

//    @Override
//    public boolean isItemViewSwipeEnabled() {
//        return true;
//    }
//
//    @Override
//    public int getMovementFlags(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder) {
//        // Set movement flags based on the layout manager
//        if (recyclerView.getLayoutManager() instanceof GridLayoutManager) {
//            final int dragFlags = ItemTouchHelper.UP | ItemTouchHelper.DOWN | ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT;
//            final int swipeFlags = 0;
//            return makeMovementFlags(dragFlags, swipeFlags);
//        } else {
//            final int dragFlags = ItemTouchHelper.UP | ItemTouchHelper.DOWN;
//            final int swipeFlags = ItemTouchHelper.START | ItemTouchHelper.END;
//            return makeMovementFlags(dragFlags, swipeFlags);
//        }
//    }
//
//
//
//    @Override
//    public void onChildDraw(Canvas c, RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, float dX, float dY, int actionState, boolean isCurrentlyActive) {
//        if (actionState == ItemTouchHelper.ACTION_STATE_SWIPE) {
//            // Fade out the view as it is swiped out of the parent's bounds
//            final float alpha = ALPHA_FULL - Math.abs(dX) / (float) viewHolder.itemView.getWidth();
//            viewHolder.itemView.setAlpha(alpha);
//            viewHolder.itemView.setTranslationX(dX);
//        } else {
//            super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive);
//        }
//    }
//
//    @Override
//    public void onSelectedChanged(RecyclerView.ViewHolder viewHolder, int actionState) {
//        // We only want the active item to change
//        if (actionState != ItemTouchHelper.ACTION_STATE_IDLE) {
//            if (viewHolder instanceof ItemTouchHelperViewHolder) {
//                // Let the view holder know that this item is being moved or dragged
//                ItemTouchHelperViewHolder itemViewHolder = (ItemTouchHelperViewHolder) viewHolder;
//                itemViewHolder.onItemSelected();
//            }
//        }
//
//        super.onSelectedChanged(viewHolder, actionState);
//    }
//
//    @Override
//    public void clearView(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder) {
//        super.clearView(recyclerView, viewHolder);
//
//        viewHolder.itemView.setAlpha(ALPHA_FULL);
//
//        if (viewHolder instanceof ItemTouchHelperViewHolder) {
//            // Tell the view holder it's time to restore the idle state
//            ItemTouchHelperViewHolder itemViewHolder = (ItemTouchHelperViewHolder) viewHolder;
//            itemViewHolder.onItemClear();
//        }
//    }




}
