package com.example.instagram;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;
import static com.example.instagram.HomeFragment.mRootView;

public class ExpandableRecyclerView extends RecyclerView {

    boolean mExpandState = false;
    View parent = mRootView;

    public ExpandableRecyclerView(@NonNull Context context) {
        super(context);
    }

    public ExpandableRecyclerView(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public ExpandableRecyclerView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    public boolean isExpanded() {
        return this.mExpandState;
    }

    public void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        if (isExpanded()) {
            int parentHeight = parent.getMeasuredHeight();
            ViewGroup.LayoutParams lp = getLayoutParams();
            lp.height = parentHeight;
            setLayoutParams(lp);
            super.onMeasure(widthMeasureSpec, heightMeasureSpec);
            return;
        }
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }

    public void setExpandState(boolean expandState) {
        this.mExpandState = expandState;
    }



}
