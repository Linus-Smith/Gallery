package com.android.gallery3d.ui;

import android.graphics.Rect;

import com.android.gallery3d.glrenderer.GLCanvas;
import com.android.gallery3d.mediaCore.Utils.VideoScreenNail;

import java.io.File;

/**
 * Created by bruce.jiang on 2016/11/30.
 */

public class VideoView extends GLView {
    private static final String TAG = "VideoView";

    private VideoScreenNail mScreenNail;
    private Rect mRect;
    private float mProgress;
    @Override
    protected void onLayout(boolean changeSize, int left, int top, int right, int bottom) {
        super.onLayout(changeSize, left, top, right, bottom);
    }

    @Override
    protected void onMeasure(int widthSpec, int heightSpec) {
        super.onMeasure(widthSpec, heightSpec);
    }

    @Override
    protected void render(GLCanvas canvas) {
        super.render(canvas);
//        GLPaint mPaint = new GLPaint();
//        mPaint.setColor(Color.RED);
//        canvas.drawRect(100,100, 600,600,mPaint);
        if (mScreenNail != null ) {
            Log.i(TAG,"draw");
            mScreenNail.draw(canvas,100,100,800,800);
        }
    }

    public void play(File file){
        mScreenNail.play(file);
    }

    public void stop(){
      //  mScreenNail.stopPlayback();
    }
    public void setmScreenNail(VideoScreenNail mScreenNail) {
        this.mScreenNail = mScreenNail;
    }

}
