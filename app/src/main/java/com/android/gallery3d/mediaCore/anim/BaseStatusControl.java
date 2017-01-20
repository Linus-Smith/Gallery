package com.android.gallery3d.mediaCore.anim;

import com.android.gallery3d.mediaCore.view.Inte.StatusIs;

/**
 * Created by linusyang on 17-1-14.
 */

public abstract class BaseStatusControl implements StatusIs {

    protected long mStartTime = NO_ANIMATION;
    protected int mPlayState = PLAY_STATE_STOP;

    protected long mDuration;
    protected long mCurrentDurationTime = 0;


    public class CalculateEntry {
        public float value;
        public boolean isCanCalculate;
    }

    void prepare(){

    }

    void pause() {
    }

    void restart() {
    }

    void start() {
    }

    void stop() {
    }


    int getPlayState() {
        return mPlayState;
    }


    long getProgress() {
        return mCurrentDurationTime;
    }

    void setDuration(long duration) {
        this.mDuration = duration;
    }

    void seekTo(long durationT) {

    }


    long getDuration() {
        return mDuration;
    }

    boolean isCompletion() {
        return mCurrentDurationTime == mDuration && mPlayState != PLAY_STATE_STOP;
    }

    abstract CalculateEntry calculate(long currentTimeMillis);

    @Override
    protected Object clone() throws CloneNotSupportedException {
        throw new RuntimeException();
    }
}
