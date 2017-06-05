package com.android.gallery3d.photo.view;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.support.annotation.Nullable;
import android.support.v7.widget.AppCompatImageView;
import android.util.AttributeSet;
import android.widget.ImageView;

/**
 * Created by Linus on 2017/5/16.
 */

public class PrettyImageView extends AppCompatImageView{

    private ScreenNail mScreenNail;

    public PrettyImageView(Context context) {
        super(context);
    }

    public PrettyImageView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public PrettyImageView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public void setScreenNail(ScreenNail screenNail) {
        mScreenNail = screenNail;
        invalidate();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if(mScreenNail != null) {
            mScreenNail.draw(canvas, 0, 0, 0, 0);
        }
    }
}
