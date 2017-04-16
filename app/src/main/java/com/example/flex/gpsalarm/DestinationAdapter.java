package com.example.flex.gpsalarm;

import android.content.Context;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.bignerdranch.expandablerecyclerview.ExpandableRecyclerAdapter;

import java.util.List;

/**
 * Created by Flex on 4/15/2017.
 */

public class DestinationAdapter
        extends ExpandableRecyclerAdapter<DestinationHeader, DestinationOptions, DestinationViewHolder, DestinationOptionsViewHolder>
{
    private LayoutInflater mInflater;

    public DestinationAdapter(Context context, @NonNull List<DestinationHeader> destinationList) {
        super(destinationList);

        mInflater = LayoutInflater.from(context);
    }

    // onCreate ...
    @Override
    public DestinationViewHolder onCreateParentViewHolder(@NonNull ViewGroup parentViewGroup, int viewType) {
        View destinationView = mInflater.inflate(R.layout.destinationheader_view, parentViewGroup, false);
        return new DestinationViewHolder(destinationView);
    }

    @Override
    public DestinationOptionsViewHolder onCreateChildViewHolder(@NonNull ViewGroup childViewGroup, int viewType) {
        View destinationOptionsView = mInflater.inflate(R.layout.destinationoptions_view, childViewGroup, false);
        return new DestinationOptionsViewHolder(destinationOptionsView);
    }

    // onBind ...
    @Override
    public void onBindParentViewHolder(@NonNull DestinationViewHolder destinationViewHolder, int parentPosition, @NonNull DestinationHeader destinationHeader) {
        destinationViewHolder.bind(destinationHeader);
    }

    @Override
    public void onBindChildViewHolder(@NonNull DestinationOptionsViewHolder destinationOpionsViewHolder, int parentPosition, int childPosition, @NonNull DestinationOptions destinationOptions) {
        destinationOpionsViewHolder.bind(destinationOptions);
    }
}