package com.example.flex.gpsalarm;

import android.view.View;
import android.widget.ImageButton;
import android.widget.SeekBar;
import android.widget.TextView;

import com.bignerdranch.expandablerecyclerview.ChildViewHolder;

/**
 * Created by Flex on 4/15/2017.
 */

public class DestinationOptionsViewHolder extends ChildViewHolder implements
        SeekBar.OnSeekBarChangeListener {

    public final static String LOG_TAG = DestinationOptionsViewHolder.class.getSimpleName();
    private final int SEEKBAR_INTERVAL = 50;

    private DestinationAdapter.DestinationItemListener mListener;

    public ImageButton mDeleteButton;
    public TextView mDeleteTextView;
    public TextView mLabelTextView;
    public TextView mProximityTextView;
    public SeekBar mProximitySeekBar;


    public DestinationOptionsViewHolder(View view, final DestinationAdapter.DestinationItemListener listener) {
        super(view);

        mListener = listener;
        mLabelTextView = (TextView) view.findViewById(R.id.textView_label);
        mProximityTextView = (TextView) view.findViewById(R.id.textView_proximity);
        mProximitySeekBar = (SeekBar) view.findViewById(R.id.seekBar_proximity);
        mProximitySeekBar.setOnSeekBarChangeListener(this);
        mProximityTextView.setText(String.valueOf(getCalculatedProximity(mProximitySeekBar.getProgress())));

        mDeleteButton = (ImageButton) view.findViewById(R.id.imageButton_delete);
        mDeleteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                listener.onDeleteClicked(getParentAdapterPosition(), getChildAdapterPosition());
            }
        });

        mDeleteTextView = (TextView) view.findViewById(R.id.textView_delete);
        mDeleteTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                listener.onDeleteClicked(getParentAdapterPosition(), getChildAdapterPosition());
            }
        });
    }

    public void bind(DestinationOptions options) {
        //translate to seekbar values ( 0 to 10 )
        int proximity = (options.getProximity()/SEEKBAR_INTERVAL)-1;

        mLabelTextView.setText(options.getLabel());
        mProximitySeekBar.setProgress(proximity);
        mProximityTextView.setText(String.valueOf(getCalculatedProximity(proximity)));
    }

    @Override
    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
        int proximity = getCalculatedProximity(seekBar.getProgress());

        mProximityTextView.setText(String.valueOf(proximity));
    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {

    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {
        int proximity = getCalculatedProximity(seekBar.getProgress());

        mProximityTextView.setText(String.valueOf(proximity));
        mListener.onProximityChanged(getParentAdapterPosition(), getChildAdapterPosition(), proximity);
    }

    //translate seekbar values (0 - 10) to our values (100 - 500)
    private int getCalculatedProximity(int progress) {
        return (progress+1)*SEEKBAR_INTERVAL;
    }
}
