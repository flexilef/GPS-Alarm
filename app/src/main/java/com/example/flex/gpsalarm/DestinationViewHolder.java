package com.example.flex.gpsalarm;

import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.os.Build;
import android.support.v7.widget.SwitchCompat;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.RotateAnimation;
import android.widget.CompoundButton;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.bignerdranch.expandablerecyclerview.ParentViewHolder;

/**
 * Created by Flex on 4/15/2017.
 */

public class DestinationViewHolder extends ParentViewHolder {
    private static final String TAG = "DestinationViewHolder";

    private static final float INITIAL_POSITION = 0.0f;
    private static final float ROTATED_POSITION = 180f;
    private static final boolean HONEYCOMB_AND_ABOVE = Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB;
    private static final float PIVOT_VALUE = 0.5f;
    private static final long DEFAULT_ROTATE_DURATION_MS = 200;

    public TextView mDestinationText;
    public SwitchCompat mDestinationSwitch;
    public ImageView mExpandImage;

    public DestinationViewHolder(View view, final DestinationAdapter.DestinationItemListener listener) {
        super(view);

        mDestinationText = (TextView) view.findViewById(R.id.TextView_savedDestination);
        mDestinationSwitch = (SwitchCompat) view.findViewById(R.id.switch_destination);
        mExpandImage = (ImageView) view.findViewById(R.id.imageView_arrowDown);

        view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                listener.onDestinationClicked(getParentAdapterPosition());
            }
        });

        mExpandImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(isExpanded()) {
                    collapseView();
                }
                else {
                    expandView();
                }
            }
        });

        mDestinationSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(isChecked) {
                    mDestinationText.setTextColor(Color.rgb(255,255,255));
                }
                else {
                    mDestinationText.setTextColor(Color.rgb(204,204,204));
                }

                listener.onSwitchClicked(getParentAdapterPosition(), isChecked);
            }
        });
    }

    public void bind(DestinationHeader destination) {
        mDestinationText.setText(destination.getDestinationAddress());
        mDestinationSwitch.setChecked(destination.isSwitchChecked());
    }

    @Override
    public boolean shouldItemViewClickToggleExpansion() {
        return false;
    }

    @Override
    public void setExpanded(boolean expanded) {
        super.setExpanded(expanded);
        if (!HONEYCOMB_AND_ABOVE) {
            return;
        }

        if (expanded) {
            mExpandImage.setRotation(ROTATED_POSITION);
        } else {
            mExpandImage.setRotation(INITIAL_POSITION);
        }
    }

    @Override
    public void onExpansionToggled(boolean expanded) {
        super.onExpansionToggled(expanded);
        if (!HONEYCOMB_AND_ABOVE) {
            return;
        }

        RotateAnimation rotateAnimation = new RotateAnimation(ROTATED_POSITION,
                INITIAL_POSITION,
                RotateAnimation.RELATIVE_TO_SELF, PIVOT_VALUE,
                RotateAnimation.RELATIVE_TO_SELF, PIVOT_VALUE);
        rotateAnimation.setDuration(DEFAULT_ROTATE_DURATION_MS);
        rotateAnimation.setFillAfter(true);

        mExpandImage.startAnimation(rotateAnimation);
    }
}
