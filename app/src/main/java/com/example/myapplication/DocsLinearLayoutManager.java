package com.example.myapplication;

import android.content.Context;
import android.support.v7.widget.LinearLayoutManager;
import android.util.AttributeSet;

/**
 * Created by zhiqing on 2018/1/12.
 */

public class DocsLinearLayoutManager extends LinearLayoutManager {
    private boolean enableScrollVertically=true;
    public DocsLinearLayoutManager(Context context) {
        super(context);
    }

    public DocsLinearLayoutManager(Context context, int orientation, boolean reverseLayout) {
        super(context, orientation, reverseLayout);
    }

    public DocsLinearLayoutManager(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    public void enableScrollVertically(boolean enable){
        enableScrollVertically=enable;
    }
    @Override
    public boolean canScrollVertically() {
        return enableScrollVertically && super.canScrollVertically();
    }
}
