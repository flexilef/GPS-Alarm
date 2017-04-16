package com.example.flex.gpsalarm;

import android.support.v7.widget.SwitchCompat;
import android.view.View;
import android.widget.TextView;

import com.bignerdranch.expandablerecyclerview.ParentViewHolder;

/**
 * Created by Flex on 4/15/2017.
 */

public class DestinationViewHolder extends ParentViewHolder {
    public TextView mDestinationText;
    public SwitchCompat mDestinationSwitch;

    public DestinationViewHolder(View view) {
        super(view);

        mDestinationText = (TextView) view.findViewById(R.id.TextView_savedDestination);
        mDestinationSwitch = (SwitchCompat) view.findViewById(R.id.switch_destination);
    }

    public void bind(DestinationHeader destination) {
        mDestinationText.setText(destination.getDestinationAddress());
        mDestinationSwitch.setChecked(destination.isSwitchedOn());
    }
}
