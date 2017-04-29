package com.example.flex.gpsalarm;

import android.content.Context;
import android.support.annotation.NonNull;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.bignerdranch.expandablerecyclerview.ExpandableRecyclerAdapter;

import java.util.List;

/**
 * Created by Flex on 4/15/2017.
 */

public class DestinationAdapter
        extends ExpandableRecyclerAdapter<DestinationHeader, DestinationOptions, DestinationHeaderViewHolder, DestinationOptionsViewHolder>
{
    private final String TAG = "DestinationAdapter";

    private LayoutInflater mInflater;
    private DestinationItemListener mListener;

    public interface DestinationItemListener {
        void onDestinationClicked(int position);
        void onDeleteClicked(int position);
        void onSwitchClicked(int position, boolean switchValue);
    }

    public DestinationAdapter(Context context, @NonNull List<DestinationHeader> destinationList) {
        super(destinationList);

        mListener = (DestinationItemListener) context;
        mInflater = LayoutInflater.from(context);
    }

    // onCreate ...
    @Override
    public DestinationHeaderViewHolder onCreateParentViewHolder(@NonNull ViewGroup parentViewGroup, int viewType) {
        View destinationView = mInflater.inflate(R.layout.destinationheader_view, parentViewGroup, false);

        return new DestinationHeaderViewHolder(destinationView, mListener);
    }

    @Override
    public DestinationOptionsViewHolder onCreateChildViewHolder(@NonNull ViewGroup childViewGroup, int viewType) {
        View destinationOptionsView = mInflater.inflate(R.layout.destinationoptions_view, childViewGroup, false);

        return new DestinationOptionsViewHolder(destinationOptionsView, mListener);
    }

    // onBind ...
    @Override
    public void onBindParentViewHolder(@NonNull DestinationHeaderViewHolder destinationHeaderViewHolder, int parentPosition, @NonNull DestinationHeader destinationHeader) {
        Log.d(TAG, "Switched on: " + destinationHeader.isSwitchChecked());
        //TODO: see if you can pass in the child view holder or viewgroup so that you can modify it here
        destinationHeaderViewHolder.bind(destinationHeader);
    }

    @Override
    public void onBindChildViewHolder(@NonNull DestinationOptionsViewHolder destinationOpionsViewHolder, int parentPosition, int childPosition, @NonNull DestinationOptions destinationOptions) {
        destinationOpionsViewHolder.bind(destinationOptions);
    }
}