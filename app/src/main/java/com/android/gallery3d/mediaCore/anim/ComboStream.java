package com.android.gallery3d.mediaCore.anim;

import com.android.gallery3d.glrenderer.GLCanvas;

/**
 * Created by linus.yang on 2016/12/14.
 */

public abstract class ComboStream extends MediaStream {

    protected MediaStream mPreStream;
    protected MediaStream mCurrentStream;
    protected TransitionAnimStream mTransitionAnimStream;
    protected int mTransitionPlayMode = TRANSITION_PLAY_MODE_MERGE;


    public ComboStream(MediaStream mediaStream) {
        this.mCurrentStream = mediaStream;
        mTransitionAnimStream = new TransitionAnimStream();
    }

    public void setPreStream(MediaStream preStream) {
        if(preStream instanceof ComboStream) {
            this.mPreStream = ((ComboStream) preStream).getCurrentStream();
        } else {
            this.mPreStream = preStream;
        }
    }

    public void setTransitionAnimDuration(long duration) {
        mTransitionAnimStream.setDuration(duration);
    }

    public MediaStream getCurrentStream() {
        return mCurrentStream;
    }

    public void setTransitionPlayMode(int mode) {
        this.mTransitionPlayMode = mode;
    }

    @Override
    public void setResolution(int width, int height) {
        super.setResolution(width, height);
        mCurrentStream.setResolution(width, height);
    }

    @Override
    public void start() {
        super.start();
        if(mTransitionPlayMode == TRANSITION_PLAY_MODE_MERGE) {
            mTransitionAnimStream.start();
            mCurrentStream.start();
        } else if(mTransitionPlayMode == TRANSITION_PLAY_MODE_ISOLATE) {
            boolean isStartTransition =  mCurrentDurationTime < mTransitionAnimStream.getDuration();
            if(isStartTransition)
                mTransitionAnimStream.start();
            else
                mCurrentStream.start();
        }
    }

    @Override
    public void restart() {
        super.restart();
        mTransitionAnimStream.restart();
        mCurrentStream.restart();
    }

    @Override
    public void pause() {
        super.pause();
        if(mTransitionPlayMode == TRANSITION_PLAY_MODE_MERGE) {
            mTransitionAnimStream.pause();
            mCurrentStream.pause();
        } else if(mTransitionPlayMode == TRANSITION_PLAY_MODE_ISOLATE) {
            boolean isPauseTransition =  mCurrentDurationTime < mTransitionAnimStream.getDuration();
            if(isPauseTransition)
                mTransitionAnimStream.pause();
            else
                mCurrentStream.pause();
        }
    }

    @Override
    public void stop() {
        super.stop();
        mTransitionAnimStream.stop();
        mCurrentStream.stop();
    }

    @Override
    public void seekTo(long durationT) {
        super.seekTo(durationT);
        if(mTransitionPlayMode == TRANSITION_PLAY_MODE_MERGE) {
            mTransitionAnimStream.seekTo(durationT);
            mCurrentStream.seekTo(durationT);
        } else {
            boolean isSeekToTransition =  durationT < mTransitionAnimStream.getDuration();
            if(isSeekToTransition) {
                mTransitionAnimStream.restart();
                mTransitionAnimStream.seekTo(durationT);
                mCurrentStream.stop();
            } else {
                mCurrentStream.seekTo(durationT);
            }
        }
    }

    @Override
    public void setDuration(long duration) {
        super.setDuration(duration);
        long playDuration = 0;
        if(mTransitionPlayMode == TRANSITION_PLAY_MODE_ISOLATE) {
            playDuration = duration - mTransitionAnimStream.getDuration();
        } else if(mTransitionPlayMode == TRANSITION_PLAY_MODE_MERGE) {
            playDuration = duration;
        }
        mCurrentStream.setDuration(playDuration);
    }

    @Override
    public void onDraw(GLCanvas canvas) {
        long animTime = MediaStream.getCurrentTime();
        mTransitionAnimStream.calculate(animTime);
        float gradientIndex = mPreStream == null ? 1f : mTransitionAnimStream.mAnimProgress;
        if(mTransitionPlayMode == TRANSITION_PLAY_MODE_MERGE) {
            if (mPreStream != null && gradientIndex != 1f) {
                canvas.save(GLCanvas.SAVE_FLAG_ALPHA | GLCanvas.SAVE_FLAG_MATRIX);
                onDrawPreStream(canvas, animTime, gradientIndex);
                canvas.restore();
            }
            canvas.save(GLCanvas.SAVE_FLAG_ALPHA | GLCanvas.SAVE_FLAG_MATRIX);
            onDrawCurrentStream(canvas, animTime, gradientIndex);
            canvas.restore();
        }else if(mTransitionPlayMode == TRANSITION_PLAY_MODE_ISOLATE) {
            if (mPreStream != null && gradientIndex != 1f) {
                canvas.save(GLCanvas.SAVE_FLAG_ALPHA | GLCanvas.SAVE_FLAG_MATRIX);
                onDrawPreStream(canvas, animTime, gradientIndex);
                canvas.restore();
            } else {
                if(mCurrentStream != null) {
                    canvas.save(GLCanvas.SAVE_FLAG_ALPHA | GLCanvas.SAVE_FLAG_MATRIX);
                    onDrawCurrentStream(canvas, animTime, gradientIndex);
                    canvas.restore();
                }
            }
        }

        if(mTransitionAnimStream.isCompletion() && mTransitionPlayMode == TRANSITION_PLAY_MODE_ISOLATE) {
            mCurrentStream.start();
            mTransitionAnimStream.stop();
        }
    }

    public abstract void onDrawPreStream(GLCanvas canvas, long animTime, float gradientIndex);
    public abstract void onDrawCurrentStream(GLCanvas canvas, long animTime, float gradientIndex);


}
