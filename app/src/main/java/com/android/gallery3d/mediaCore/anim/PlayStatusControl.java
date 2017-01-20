package com.android.gallery3d.mediaCore.anim;


import com.android.gallery3d.mediaCore.Utils.Utils;

/**
 * Created by linusyang on 17-1-14.
 */

public class PlayStatusControl extends BaseStatusControl {


    @Override
    void prepare() {
        mCurrentDurationTime = 0;
    }

    @Override
    void start() {
        if(mPlayState == PLAY_STATE_STOP) {
            mStartTime = Utils.getCurrentTime();
            mPlayState = PLAY_STATE_START;
        } else if( mPlayState == PLAY_STATE_PAUSE ) {
            mStartTime = Utils.getCurrentTime() -  mCurrentDurationTime;
            mPlayState = PLAY_STATE_START;
            calculate(Utils.getCurrentTime());
        }
    }

    @Override
    void restart() {
        mStartTime = Utils.getCurrentTime();
        mPlayState = PLAY_STATE_START;
        calculate(mStartTime);
    }

    @Override
    void pause() {
        if(mPlayState == PLAY_STATE_START) {
            mPlayState = PLAY_STATE_PAUSE;
        }
    }

    @Override
    void stop() {
        if(mPlayState != PLAY_STATE_STOP) {
            mPlayState = PLAY_STATE_STOP;
            mStartTime = NO_ANIMATION;
        }
    }

    @Override
    void seekTo(long durationT) {
        if(durationT > mDuration && durationT < 0) return;
        if(mPlayState == PLAY_STATE_START) {
            mStartTime = Utils.getCurrentTime() -  durationT;
        } else {
            mCurrentDurationTime = durationT;
            mStartTime = Utils.getCurrentTime() -  durationT;
            calculate(Utils.getCurrentTime());
        }
    }

    @Override
    CalculateEntry calculate(long currentTimeMillis) {
        CalculateEntry calculateEntry = new CalculateEntry();
        if(mPlayState == PLAY_STATE_STOP || mPlayState == PLAY_STATE_PAUSE) {
            calculateEntry.isCanCalculate = false;
            calculateEntry.value = -1;
            return calculateEntry;
        }
        long elapse =  currentTimeMillis - mStartTime;
        mCurrentDurationTime = elapse > mDuration ? mDuration : elapse;
        float x = Utils.clamp((float) elapse / mDuration, 0f, 1f);
        calculateEntry.isCanCalculate = true;
        calculateEntry.value = x;
        return calculateEntry;
    }


    @Override
    protected Object clone() throws CloneNotSupportedException {
        return new PlayStatusControl();
    }
}
