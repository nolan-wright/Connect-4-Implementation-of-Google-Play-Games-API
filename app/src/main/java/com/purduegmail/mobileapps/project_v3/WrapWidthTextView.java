package com.purduegmail.mobileapps.project_v3;

import android.content.Context;
import android.text.Layout;
import android.util.AttributeSet;

/**
 * Created by Nolan Wright on 11/28/2017.
 */

public class WrapWidthTextView extends android.support.v7.widget.AppCompatTextView {

    // constructors
    public WrapWidthTextView(Context context) {
        // this constructor is used when programmatically creating view
        super(context);
    }
    public WrapWidthTextView(Context context, AttributeSet attrs) {
        // this constructor is used when creating view through XML
        super(context, attrs);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        Layout layout = getLayout();
        if (layout != null) {
            int width = (int)Math.ceil(getMaxLineWidth(layout))
                    + getPaddingStart() + getPaddingEnd();
            int height = getMeasuredHeight();
            setMeasuredDimension(width, height);
        }
    }

    /*
     * helper methods
     */
    // called in onMeasure
    private float getMaxLineWidth(Layout layout) {
        float max_width = 0.0f;
        int lines = layout.getLineCount();
        for (int i = 0; i < lines; i++) {
            if (layout.getLineWidth(i) > max_width) {
                max_width = layout.getLineMax(i);
            }
        }
        return max_width;
    }
}
