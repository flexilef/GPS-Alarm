package com.example.flex.gpsalarm;

import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;

import com.bignerdranch.expandablerecyclerview.ChildViewHolder;

/**
 * Created by Flex on 4/15/2017.
 */

public class DestinationOptionsViewHolder extends ChildViewHolder {
    public ImageButton mDeleteButton;

    public DestinationOptionsViewHolder(View view, final DestinationAdapter.DestinationItemListener listener) {
        super(view);

        mDeleteButton = (ImageButton) view.findViewById(R.id.imageButton_delete);
        mDeleteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                listener.onDeleteClicked(getParentAdapterPosition());
            }
        });
    }

    public void bind(DestinationOptions options) {
    }
}
