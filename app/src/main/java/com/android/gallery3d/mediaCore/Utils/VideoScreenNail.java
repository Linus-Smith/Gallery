package com.android.gallery3d.mediaCore.Utils;

import android.app.Activity;
import android.graphics.Color;
import android.graphics.SurfaceTexture;
import android.opengl.GLES20;
import android.util.Log;
import android.view.Surface;

import com.android.gallery3d.app.VideoDomeActivity;
import com.android.gallery3d.glrenderer.GLCanvas;
import com.android.gallery3d.glrenderer.GLPaint;
import com.android.gallery3d.mediaCore.view.Inte.VIPlayControl;
import com.android.gallery3d.ui.GLRoot;
import com.android.gallery3d.ui.SurfaceTextureScreenNail;

import java.io.File;
import java.io.IOException;

/**
 * Created by bruce.jiang on 2016/11/30.
 */

public class VideoScreenNail extends SurfaceTextureScreenNail implements MoviePlayer.PlayerFeedback ,MoviePlayer.UpdatePlayTimeCallback,VIPlayControl {
    private static final String TAG = "VideoScreenNail";

    private MoviePlayer.PlayTask mPlayTask;
    private boolean isPlay = false;

    public void setmFile(File mFile) {
        this.mFile = mFile;
    }

    private File mFile;
    private Activity mActivity;
    private long currentPlayTime;

    public VideoScreenNail(File mFile , Activity mActivity) {
        isPlay = false;
        this.mActivity = mActivity;
        this.mFile = mFile;
    }

    public VideoScreenNail(){

    }
    @Override
    public void noDraw() {

    }

    @Override
    public void recycle() {

    }

    @Override
    public void onFrameAvailable(SurfaceTexture surfaceTexture) {
       // invalidate();
    }

    protected void invalidate() {
//        GLRoot root = mActivity.getGLRoot();
//        if (root != null) root.requestRender();
    }
    @Override
    public void draw(GLCanvas canvas, int x, int y, int width, int height) {
        super.draw(canvas, x, y, width, height);
//        GLPaint mPaint = new GLPaint();
//        mPaint.setColor(Color.RED);
//        canvas.drawRect(100,100, 600,600,mPaint);
        if (getSurfaceTexture() == null)
            super.acquireSurfaceTexture(canvas);
//        if(getSurfaceTexture() != null && !isPlay) {
//            mActivity.runOnUiThread(new Runnable() {
//                @Override
//                public void run() {
//                    play(mFile);
//                    isPlay = true;
//                }
//            });
//
//        }
    }

    public void play(File file){
        if (mPlayTask != null) {
            Log.w(TAG, "movie already playing");
            return;
        }

        Log.d(TAG, "starting movie");
        SpeedControlCallback callback = new SpeedControlCallback();
        Surface surface = new Surface(getSurfaceTexture());

        // Don't leave the last frame of the previous video hanging on the screen.
        // Looks weird if the aspect ratio changes.
//        clearSurface(surface);

        MoviePlayer player = null;
        try {
            player = new MoviePlayer(
                    file, surface, callback,this);
        } catch (IOException ioe) {
            Log.e(TAG, "Unable to play movie", ioe);
            surface.release();
            return;
        }


        mPlayTask = new MoviePlayer.PlayTask(player, this);
        mPlayTask.prepare();

        mPlayTask.execute();
    }

    @Override
    public void playbackStopped() {
        mPlayTask = null;
        Log.i(TAG,"playback callback");
    }

    public void stopPlayback() {
        if (mPlayTask != null) {
            mPlayTask.requestStop();
        }
    }

    @Override
    public void prepare() {

        play(mFile);
//        pause();
    }

    @Override
    public void start() {
//        play(mFile);
//        if (mPlayTask != null) {
//            mPlayTask.execute();
//        }
//        pause();
    }

    @Override
    public void restart() {
        if (mPlayTask != null) {
            mPlayTask.restart();
        }
    }

    @Override
    public void pause() {
        if (mPlayTask != null) {
            mPlayTask.pause();
        }
    }

    @Override
    public void stop() {
        if (mPlayTask != null) {
            mPlayTask.requestStop();
            mPlayTask.waitForStop();
        }
    }

    @Override
    public void seekTo(long durationT) {
        if (mPlayTask != null) {
            mPlayTask.seekToTime(durationT);
        }
    }

    @Override
    public int getPlayState() {
        return 0;
    }

    @Override
    public long getProgress() {
        return (int)currentPlayTime;
    }

    @Override
    public void setDuration(long duration) {

    }

    @Override
    public long getDuration() {
        return 0;
    }

    @Override
    public void updateCurrentPlayTime(long currentPlayTime) {
        this.currentPlayTime = currentPlayTime;
    }



    /**
     * Clears the playback surface to black.
     */
    private void clearSurface(Surface surface) {
        // We need to do this with OpenGL ES (*not* Canvas -- the "software render" bits
        // are sticky).  We can't stay connected to the Surface after we're done because
        // that'd prevent the video encoder from attaching.
        //
        // If the Surface is resized to be larger, the new portions will be black, so
        // clearing to something other than black may look weird unless we do the clear
        // post-resize.
//        EglCore eglCore = new EglCore();
//        WindowSurface win = new WindowSurface(eglCore, surface, false);
//        win.makeCurrent();
//        GLES20.glClearColor(0, 0, 0, 0);
//        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT);
//        win.swapBuffers();
//        win.release();
//        eglCore.release();

    }
}
