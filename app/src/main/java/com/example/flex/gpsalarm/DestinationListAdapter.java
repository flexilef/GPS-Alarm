package com.example.flex.gpsalarm;


import android.media.Image;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SwitchCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Flex on 4/9/2017.
 */

public class DestinationListAdapter extends RecyclerView.Adapter<DestinationListAdapter.ViewHolder> {

    private List<DestinationRowItem> destinationsList;

    public static class ViewHolder extends RecyclerView.ViewHolder {

        public TextView destinationText;
        public SwitchCompat destinationSwitch;
        public ImageButton buttonDropdown;
        public ImageButton buttonDelete;
        public View dropdownDivider;

        public ViewHolder(View view) {
            super(view);

            destinationText = (TextView) view.findViewById(R.id.TextView_savedDestination);
            destinationSwitch = (SwitchCompat) view.findViewById(R.id.switch_destination);
            buttonDropdown = (ImageButton) view.findViewById(R.id.imageButton_arrowdown);
            buttonDelete = (ImageButton) view.findViewById(R.id.imageButton_delete);
            dropdownDivider = (View) view.findViewById(R.id.view_divider);

            buttonDropdown.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if(buttonDelete.getVisibility() != View.VISIBLE) {
                        buttonDelete.setVisibility(View.VISIBLE);
                        dropdownDivider.setVisibility(View.VISIBLE);
                    }
                    else {
                        buttonDelete.setVisibility(View.GONE);
                        dropdownDivider.setVisibility(View.GONE);
                    }
                }
            });
        }
    }

    public DestinationListAdapter(List<DestinationRowItem> itemList) {
        destinationsList = itemList;
    }

    @Override
    public DestinationListAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.destination_row_item, parent, false);

        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        holder.destinationText.setText(destinationsList.get(position).getDestination());
        holder.destinationSwitch.setChecked(destinationsList.get(position).isDestinationSet());
    }

    @Override
    public int getItemCount() {
        return destinationsList.size();
    }
}

