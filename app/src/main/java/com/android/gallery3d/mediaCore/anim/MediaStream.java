package com.android.gallery3d.mediaCore.anim;


import com.android.gallery3d.common.Utils;
import com.android.gallery3d.glrenderer.GLCanvas;
import com.android.gallery3d.mediaCore.view.Inte.OnNotifyChangeListener;
import com.android.gallery3d.mediaCore.view.Inte.StateIs;
import com.android.gallery3d.mediaCore.view.Inte.VIPlayControl;

/**
 * Created by linusyang on 16-12-9.
 */

public abstract class MediaStream implements VIPlayControl , StateIs{

    protected static final long NO_ANIMATION = -2;
    protected long mStartTime = NO_ANIMATION;
    protected int  mPlayState = PLAY_STATE_STOP;

    protected long mDuration;
    protected long mCurrentDurationTime = 0;

    protected int mWidth;
    protected int mHeight;

    protected float mAnimProgress;



    @Override
    public void start() {
        if(mPlayState == PLAY_STATE_STOP) {
            mStartTime = getCurrentTime();
            mPlayState = PLAY_STATE_START;
        } else if( mPlayState == PLAY_STATE_PAUSE ) {
            mStartTime = getCurrentTime() -  mCurrentDurationTime;
            mPlayState = PLAY_STATE_START;
            calculate(getCurrentTime());
        }
    }

    public void setResolution(int width, int height) {
        this.mWidth = width;
        this.mHeight = height;
    }


    @Override
    public void prepare() {
    }

    @Override
    public void restart() {
        mStartTime = getCurrentTime();
        mPlayState = PLAY_STATE_START;
        calculate(mStartTime);
    }

    @Override
    public void pause() {
        if(mPlayState == PLAY_STATE_START) {
            mPlayState = PLAY_STATE_PAUSE;
        }
    }

    @Override
    public void seekTo(long durationT) {
        if(durationT > mDuration && durationT < 0) return;
        if(mPlayState == PLAY_STATE_START) {
            mStartTime = getCurrentTime() -  durationT;
        } else {
            mCurrentDurationTime = durationT;
            mStartTime = getCurrentTime() -  durationT;
            calculate(getCurrentTime());
        }

    }

    @Override
    public int getPlayState() {
        return mPlayState;
    }

    @Override
    public long getProgress() {
        return mCurrentDurationTime;
    }

    @Override
    public long getDuration() {
        return mDuration;
    }

    @Override
    public void setDuration(long duration) {
        this.mDuration = duration;
    }

    @Override
    public void stop() {
        if(mPlayState != PLAY_STATE_STOP) {
            mPlayState = PLAY_STATE_STOP;
            mStartTime = NO_ANIMATION;
            mCurrentDurationTime = 0;
        }
    }

    public boolean isCompletion() {
        return mCurrentDurationTime == mDuration &&  mPlayState != PLAY_STATE_STOP;
    }


    /**
     * @param canvas GLCanvas gives a convenient interface to draw using OpenGL.
     */
    public void apply(GLCanvas canvas){
        onDraw(canvas);
    }


    public abstract void onDraw(GLCanvas canvas) ;


    public boolean calculate(long currentTimeMillis) {
        if(mPlayState == PLAY_STATE_STOP || mPlayState == PLAY_STATE_PAUSE) return false;
        long elapse =  currentTimeMillis - mStartTime;
        mCurrentDurationTime = elapse > mDuration ? mDuration : elapse;
        float x = Utils.clamp((float) elapse / mDuration, 0f, 1f);
        onCalculate( x);
        return true;
    }

    /**
     * @return the current time in milliseconds since January 1, 1970 00:00:00.0 UTC.
     */
    public static long getCurrentTime() {
        return System.currentTimeMillis();
    }

    protected void onCalculate(float progress) {
        mAnimProgress = progress;
    }

}
