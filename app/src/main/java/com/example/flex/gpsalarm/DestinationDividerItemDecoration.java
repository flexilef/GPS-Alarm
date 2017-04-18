package com.example.flex.gpsalarm;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.view.View;

/**
 * Created by Flex on 4/9/2017.
 */

public class DestinationDividerItemDecoration extends RecyclerView.ItemDecoration {
    private Drawable mDividerDrawable;

    public DestinationDividerItemDecoration(Context context) {
        mDividerDrawable = ContextCompat.getDrawable(context, R.drawable.destination_list_divider);
    }

    @Override
    //source code showing how to override onDrawOver() from
    //http://stackoverflow.com/questions/31242812/how-can-a-divider-line-be-added-in-an-android-recyclerview
    public void onDrawOver(Canvas canvas, RecyclerView parent, RecyclerView.State state) {
        int left = parent.getPaddingLeft();
        int right = parent.getWidth() - parent.getPaddingRight();

        int childCount = parent.getChildCount();
        for (int i = 0; i < childCount; i++) {
            View child = parent.getChildAt(i);

            RecyclerView.LayoutParams params = (RecyclerView.LayoutParams) child.getLayoutParams();

            int top = child.getBottom() + params.bottomMargin;
            int bottom = top + mDividerDrawable.getIntrinsicHeight();

            mDividerDrawable.setBounds(left, top, right, bottom);
            mDividerDrawable.draw(canvas);
        }
    }
}
