package com.example.flex.gpsalarm;

import android.app.Activity;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.SwitchCompat;
import android.util.DisplayMetrics;
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

public class DestinationHeaderViewHolder extends ParentViewHolder {
    private static final String LOG_TAG = DestinationHeaderViewHolder.class.getSimpleName();

    private final float PIVOT_VALUE = 0.5f;
    private final float INITIAL_POSITION = 0.0f;
    private final float ROTATED_POSITION = 180f;
    private final long DEFAULT_ROTATE_DURATION_MS = 200;
    private final boolean HONEYCOMB_AND_ABOVE = Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB;

    public ImageView mExpandImage;
    public TextView mDestinationText;
    public SwitchCompat mDestinationSwitch;

    public DestinationHeaderViewHolder(final View view, final DestinationAdapter.DestinationItemListener listener) {
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

                /* For holo theme
                    int pixels = (int) getPixelsFromDp(view.getContext(), PADDING_DP);
                    view.setPadding(pixels, pixels, pixels, pixels);
                    view.setBackground(null);*/
                    //view.setBackground(null);

                    //view.setSelected(false);
                }
                else {
                    expandView();
                    //view.setSelected(true);

                    //view.setBackgroundColor(ContextCompat.getColor(view.getContext(), R.color.colorPrimaryDark));

/*
                    //For holo theme
                    Drawable drawable = ContextCompat.getDrawable(view.getContext(), android.R.drawable.dialog_holo_light_frame);
                    drawable.setColorFilter(new PorterDuffColorFilter(ContextCompat.getColor(view.getContext(), R.color.colorPrimaryDark), PorterDuff.Mode.MULTIPLY));
                    view.setBackground(drawable);*/

                    /* For single color
                    int pixels = (int) getPixelsFromDp(view.getContext(), -8);
                    ViewGroup.MarginLayoutParams  layout = (ViewGroup.MarginLayoutParams) view.getLayoutParams();
                    layout.setMargins(pixels, pixels, pixels, 0);
                    view.setBackgroundColor(ContextCompat.getColor(view.getContext(), R.color.colorPrimaryDark));*/
                }
            }
        });

        mDestinationSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(isChecked) {
                    mDestinationText.setTextColor(ContextCompat.getColor(view.getContext(), R.color.colorTextNormal));
                }
                else {
                    mDestinationText.setTextColor(ContextCompat.getColor(view.getContext(), R.color.colorTextDisabled));
                }

                //0 because only one list of options
                listener.onSwitchClicked(getParentAdapterPosition(), 0, isChecked);
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

    private double getPixelsFromDp(Context context, int dp) {
        DisplayMetrics metrics = context.getResources().getDisplayMetrics();

        return dp * ((double)metrics.densityDpi/ DisplayMetrics.DENSITY_DEFAULT);
    }
}
