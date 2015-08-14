package com.example.thong.astthonggooglemap.adapter;

import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.example.thong.astthonggooglemap.R;
import com.example.thong.astthonggooglemap.model.LocationSearch;

import java.util.ArrayList;

/**
 * Created by thong on 06/08/2015.
 */
public class RecyclerViewAdapter extends RecyclerView.Adapter<RecyclerViewAdapter.PlaceViewHolder> {

    private ArrayList<LocationSearch> mLocationSearches;
    private ItemOnClickListener mItemOnClickListener;

    public RecyclerViewAdapter(ArrayList<LocationSearch> mLocationSearches) {
        this.mLocationSearches = mLocationSearches;
    }

    public void onItemRecyclerViewListener(ItemOnClickListener itemOnClickListener) {
        this.mItemOnClickListener = itemOnClickListener;
    }

    @Override
    public PlaceViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {

        View view = LayoutInflater.from(viewGroup.getContext()).
                inflate(R.layout.item_list_name, viewGroup, false);

        PlaceViewHolder placeViewHolder = new PlaceViewHolder(view);
        return placeViewHolder;
    }

    @Override
    public void onBindViewHolder(PlaceViewHolder placeViewHolder, int i) {
        placeViewHolder.mTxtName.setText(mLocationSearches.get(i).getName());
    }

    @Override
    public int getItemCount() {
        return mLocationSearches.size();
    }

    public class PlaceViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        public TextView mTxtName;

        public PlaceViewHolder(View itemView) {
            super(itemView);
            mTxtName = (TextView) itemView.findViewById(R.id.txtName);
            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            mItemOnClickListener.onClick(mLocationSearches.get(getLayoutPosition()));
        }
    }

    public interface ItemOnClickListener {
        public void onClick(LocationSearch locationSearch);
    }
}
