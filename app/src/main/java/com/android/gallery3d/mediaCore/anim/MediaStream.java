package com.android.gallery3d.mediaCore.anim;


import com.android.gallery3d.glrenderer.GLCanvas;
import com.android.gallery3d.mediaCore.view.Inte.StatusIs;

/**
 * Created by linusyang on 16-12-9.
 */

public abstract class MediaStream implements StatusIs{

    protected int mWidth;
    protected int mHeight;

    protected float mAnimProgress;



    protected BaseStatusControl mStatusControl;

     public MediaStream() {
         mStatusControl = new PlayStatusControl();
     }

   public void setStatusControl(BaseStatusControl statusControl) {
        mStatusControl = statusControl;
    }



    void start() {
        mStatusControl.start();
    }

    void setResolution(int width, int height) {
        this.mWidth = width;
        this.mHeight = height;
    }


     void prepare() {
        mStatusControl.prepare();
    }

    void restart() {
        mStatusControl.restart();
    }


    void pause() {
        mStatusControl.pause();
    }


    void seekTo(long durationT) {
        mStatusControl.seekTo(durationT);
    }


    int getPlayState() {
        return mStatusControl.getPlayState();
    }


    long getProgress() {
        return mStatusControl.getProgress();
    }


    long getDuration() {
        return mStatusControl.getDuration();
    }

    void setDuration(long duration) {
       mStatusControl.setDuration(duration);
    }


    void stop() {
        mStatusControl.stop();
    }

     abstract MediaStream getCurrentStream();


    boolean isCompletion() {
        return mStatusControl.isCompletion();
    }


    /**
     * @param canvas GLCanvas gives a convenient interface to draw using OpenGL.
     */
    void apply(GLCanvas canvas){
        onDraw(canvas);
    }

    protected abstract void onDraw(GLCanvas canvas);


    boolean calculate(long currentTimeMillis) {
        BaseStatusControl.CalculateEntry calculateEntry = mStatusControl.calculate(currentTimeMillis);
        if(calculateEntry.isCanCalculate) {
            onCalculate(calculateEntry.value);
            return true;
        }
        return false;
    }



    protected void onCalculate(float progress) {
        mAnimProgress = progress;
    }

}
