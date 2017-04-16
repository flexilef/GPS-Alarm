package com.example.flex.gpsalarm;

import android.view.View;
import android.widget.ImageButton;

import com.bignerdranch.expandablerecyclerview.ChildViewHolder;

/**
 * Created by Flex on 4/15/2017.
 */

public class DestinationOptionsViewHolder extends ChildViewHolder {
    private ImageButton mDeleteButton;

    public DestinationOptionsViewHolder(View view) {
        super(view);

        mDeleteButton = (ImageButton) view.findViewById(R.id.imageButton_delete);
    }

    public void bind(DestinationOptions options) {
    }
}
