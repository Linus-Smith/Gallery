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

    void setPreStream(MediaStream preStream) {
        if (preStream == null) return;
       this.mPreStream = preStream.getCurrentStream();
    }



    public MediaStream getCurrentStream() {
        return mCurrentStream;
    }

    void setTransitionPlayMode(int mode) {
        this.mTransitionPlayMode = mode;
    }

    int getTransitionPlayMode() {
       return mTransitionPlayMode;
    }

    @Override
    void setResolution(int width, int height) {
        super.setResolution(width, height);
        mCurrentStream.setResolution(width, height);
    }

    @Override
   public void setStatusControl(BaseStatusControl statusControl) {
        super.setStatusControl(statusControl);
        try {
            mTransitionAnimStream.setStatusControl((BaseStatusControl) statusControl.clone());
            mCurrentStream.setStatusControl((BaseStatusControl) statusControl.clone());
        } catch (CloneNotSupportedException e) {
            e.printStackTrace();
        }
    }

    @Override
    void prepare() {
        super.prepare();
        mCurrentStream.prepare();
    }

    @Override
    void start() {
        super.start();
        if(mTransitionPlayMode == TRANSITION_PLAY_MODE_MERGE) {
            mTransitionAnimStream.start();
            mCurrentStream.start();
        } else if(mTransitionPlayMode == TRANSITION_PLAY_MODE_ISOLATE) {
            boolean isStartTransition =  mStatusControl.getProgress() < mTransitionAnimStream.getDuration();
            if(isStartTransition)
                mTransitionAnimStream.start();
            else
                mCurrentStream.start();
        }
    }

    @Override
    void restart() {
        super.restart();
        mTransitionAnimStream.restart();
        mCurrentStream.restart();
    }

    @Override
    void pause() {
        super.pause();
        if(mTransitionPlayMode == TRANSITION_PLAY_MODE_MERGE) {
            mTransitionAnimStream.pause();
            mCurrentStream.pause();
        } else if(mTransitionPlayMode == TRANSITION_PLAY_MODE_ISOLATE) {
            boolean isPauseTransition =  mStatusControl.getProgress() < mTransitionAnimStream.getDuration();
            if(isPauseTransition)
                mTransitionAnimStream.pause();
            else
                mCurrentStream.pause();
        }
    }

    @Override
    void stop() {
        super.stop();
        mTransitionAnimStream.stop();
        mCurrentStream.stop();
    }

    @Override
    void seekTo(long durationT) {
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
    void setDuration(long duration) {
        mTransitionAnimStream.setDuration(duration);
    }

    public void setTotalDuration(long duration) {
        mStatusControl.setDuration(duration);
    }

    @Override
    protected void onDraw(GLCanvas canvas) {
        float gradientIndex = mPreStream == null ? 1f : mTransitionAnimStream.mAnimProgress;
        if(mTransitionPlayMode == TRANSITION_PLAY_MODE_MERGE) {
            if (mPreStream != null && gradientIndex != 1f) {
                canvas.save(GLCanvas.SAVE_FLAG_ALPHA | GLCanvas.SAVE_FLAG_MATRIX);
                onDrawPreStream(canvas, gradientIndex);
                canvas.restore();
            }
            canvas.save(GLCanvas.SAVE_FLAG_ALPHA | GLCanvas.SAVE_FLAG_MATRIX);
            onDrawCurrentStream(canvas, gradientIndex);
            canvas.restore();
        }else if(mTransitionPlayMode == TRANSITION_PLAY_MODE_ISOLATE) {
            if (mPreStream != null && gradientIndex != 1f) {
                canvas.save(GLCanvas.SAVE_FLAG_ALPHA | GLCanvas.SAVE_FLAG_MATRIX);
                onDrawPreStream(canvas, gradientIndex);
                canvas.restore();
            } else {
                if(mCurrentStream != null) {
                    canvas.save(GLCanvas.SAVE_FLAG_ALPHA | GLCanvas.SAVE_FLAG_MATRIX);
                    onDrawCurrentStream(canvas, gradientIndex);
                    canvas.restore();
                }
            }
        }

        if(mTransitionAnimStream.isCompletion() && mTransitionPlayMode == TRANSITION_PLAY_MODE_ISOLATE) {
            mCurrentStream.start();
            mTransitionAnimStream.stop();
        }
    }

    @Override
    boolean calculate(long currentTimeMillis) {
        mCurrentStream.calculate(currentTimeMillis);
        mTransitionAnimStream.calculate(currentTimeMillis);
        return super.calculate(currentTimeMillis);
    }



    protected abstract void onDrawPreStream(GLCanvas canvas, float gradientIndex);
    protected abstract void onDrawCurrentStream(GLCanvas canvas, float gradientIndex);

}
