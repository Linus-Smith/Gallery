package com.android.gallery3d.mediaCore.recoder;

import android.annotation.TargetApi;
import android.graphics.SurfaceTexture;
import android.opengl.GLSurfaceView;
import android.util.Log;
import android.view.Surface;

import com.android.gallery3d.common.ApiHelper;
import com.android.gallery3d.glrenderer.ExtTexture;
import com.android.gallery3d.glrenderer.GLCanvas;
import com.android.gallery3d.glrenderer.GLES20Canvas;
import com.android.gallery3d.mediaCore.Utils.Utils;
import com.android.gallery3d.mediaCore.anim.MediaStream;
import com.android.gallery3d.mediaCore.view.Inte.OnNotifyChangeListener;
import com.android.gallery3d.mediaCore.view.PlayBits;
import com.android.gallery3d.mediaCore.view.VideoView;
import com.android.photos.views.BlockingGLTextureView;

import java.io.File;
import java.io.IOException;

import static android.opengl.GLES11Ext.GL_TEXTURE_EXTERNAL_OES;

/**
 * Created by bruce.jiang on 2016/12/21.
 */

public class RecoderRender implements RecoderInterface,SurfaceTexture.OnFrameAvailableListener ,OnNotifyChangeListener {

    private static final String TAG = "RecoderRender";
    private GLCanvas mCanvas;

    private ExtTexture mExtTexture;
    private SurfaceTexture mSurfaceTexture;
    private PlayBits mPlayBits;
    private VideoView.PlayStateListener mPlayStateListener;
    private RenderThread mThread;
    private int mWidth =800;
    private int mHeight = 600;

    private InputSurface inputSurface;
    private OutputSurface outputSurface;
    private File mOutputFile;

    private Surface mSurface;
    private StoryAlbumEncoder mVideoEncoder;
    /**
     * Used for editing the frames.
     *
     * <p>Swaps green and blue channels by storing an RBGA color in an RGBA buffer.
     */
    private static final String FRAGMENT_SHADER =
            "#extension GL_OES_EGL_image_external : require\n" +
                    "precision mediump float;\n" +
                    "varying vec2 vTextureCoord;\n" +
                    "uniform samplerExternalOES sTexture;\n" +
                    "void main() {\n" +
                    "  gl_FragColor = texture2D(sTexture, vTextureCoord).rbga;\n" +
                    "}\n";


    public RecoderRender() {
        outputSurface= new OutputSurface();
        outputSurface.changeFragmentShader(FRAGMENT_SHADER);
        mCanvas = new GLES20Canvas();
        mPlayBits = new PlayBits();
        mPlayBits.setOnNotifyChangeListener(this);
        acquireSurfaceTexture(mCanvas);
    }


    public void setmOutputFile(File mOutputFile) {
        this.mOutputFile = mOutputFile;
    }

    public GLCanvas getmCanvas() {
        return mCanvas;
    }

    public void setmCanvas(GLCanvas mCanvas) {
        this.mCanvas = mCanvas;
    }

    public void acquireSurfaceTexture(GLCanvas canvas) {
        mExtTexture = new ExtTexture(canvas, GL_TEXTURE_EXTERNAL_OES);
        mExtTexture.setSize(mWidth, mHeight);
        mSurfaceTexture = new SurfaceTexture(mExtTexture.getId());
        setDefaultBufferSize(mSurfaceTexture, mWidth, mHeight);
        mSurfaceTexture.setOnFrameAvailableListener(this);
    }


    public void setPlayStateListener(VideoView.PlayStateListener playStateListener) {
        mPlayStateListener = playStateListener;
    }

    @TargetApi(ApiHelper.VERSION_CODES.ICE_CREAM_SANDWICH_MR1)
    private static void setDefaultBufferSize(SurfaceTexture st, int width, int height) {
        if (ApiHelper.HAS_SET_DEFALT_BUFFER_SIZE) {
            st.setDefaultBufferSize(width, height);
        }
    }

    public void startEncoder(){
        mThread = new RenderThread();
        mThread.start();
    }

    public void prepare(MediaStream mediaStream) {
        Utils.checkNull(mediaStream, "VideoVIew prepare NULL");
        mPlayBits.prepare(mediaStream);
    }


    private void draw(GLCanvas canvas){
        mPlayBits.onDraw(canvas);
        Log.i("jzf","render draw");

    }




    @Override
    public void setDuration(long duration) {
        mPlayBits.setDuration(duration);
    }


    @Override
    public void prepare() {
        mPlayBits.prepare();
    }

    @Override
    public void start() {
        mPlayBits.start();
    }

    @Override
    public void pause() {

    }

    @Override
    public void stop() {

    }

    @Override
    public void onFrameAvailable(SurfaceTexture surfaceTexture) {

    }

    @Override
    public void doInvalidate() {
        draw(mCanvas);
    }

    @Override
    public void notifyCompletion() {
        if(mPlayStateListener != null)  mPlayStateListener.onCompletion();
    }


    class RenderThread extends Thread{


        /**
         * Creates the video encoder object and starts the encoder thread.  Creates an EGL
         * surface for encoder input.
         */
        public void startEncoder() {
            Log.d(TAG, "starting to record");
            // Record at 1280x720, regardless of the window dimensions.  The encoder may
            // explode if given "strange" dimensions, e.g. a width that is not a multiple
            // of 16.  We can box it as needed to preserve dimensions.
            final int BIT_RATE = 4000000;   // 4Mbps
            final int VIDEO_WIDTH = 1280;
            final int VIDEO_HEIGHT = 720;
            mSurface = new Surface(mSurfaceTexture);
//        int windowWidth = stream.getWidth();
//        int windowHeight = mWindowSurface.getHeight();
//        float windowAspect = (float) windowHeight / (float) windowWidth;
//        int outWidth, outHeight;
//        if (VIDEO_HEIGHT > VIDEO_WIDTH * windowAspect) {
//            // limited by narrow width; reduce height
//            outWidth = VIDEO_WIDTH;
//            outHeight = (int) (VIDEO_WIDTH * windowAspect);
//        } else {
//            // limited by short height; restrict width
//            outHeight = VIDEO_HEIGHT;
//            outWidth = (int) (VIDEO_HEIGHT / windowAspect);
//        }
//        int offX = (VIDEO_WIDTH - outWidth) / 2;
//        int offY = (VIDEO_HEIGHT - outHeight) / 2;
//        mVideoRect.set(offX, offY, offX + outWidth, offY + outHeight);
//        Log.d(TAG, "Adjusting window " + windowWidth + "x" + windowHeight +
//                " to +" + offX + ",+" + offY + " " +
//                mVideoRect.width() + "x" + mVideoRect.height());

            VideoEncoderCore encoderCore;
            try {
                encoderCore = new VideoEncoderCore(VIDEO_WIDTH, VIDEO_HEIGHT,
                        BIT_RATE, mOutputFile);
            } catch (IOException ioe) {
                throw new RuntimeException(ioe);
            }
//        mInputWindowSurface = new WindowSurface(mEglCore, encoderCore.getInputSurface(), true);
            inputSurface = new InputSurface(mSurface);
            mVideoEncoder = new StoryAlbumEncoder(encoderCore);
        }



    }
}
