package com.android.gallery3d.mediaCore.anim;

import android.graphics.SurfaceTexture;
import android.media.MediaCodec;
import android.media.MediaExtractor;
import android.media.MediaFormat;
import android.media.MediaPlayer;
import android.opengl.GLES20;
import android.util.Log;
import android.view.Surface;

import com.android.gallery3d.glrenderer.GLES20Canvas;

import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;

/**
 * Created by Linus on 2017/2/28.
 */

public class MediaPlayerC {

    private static final String TAG = "MediaPlayerC";
    private static final boolean DEBUG = true;

    private Surface mSurface;

    private MediaExtractor mMediaExtractor;
    private MediaCodec mMediaCodec;

    private int mTrackIndex = -1;
    private int inputChunk = 0;
    private long mDecodeTime = 0;
    private final int TIMEOUT_USE = 10000;
    private int bit = 0;
    // Declare this here to reduce allocations.
    private MediaCodec.BufferInfo mBufferInfo = new MediaCodec.BufferInfo();

    private MediaPlayerC(File file) {
        initMediaExtractor(file);

    }

    public static MediaPlayerC create(File file) {
        return new MediaPlayerC(file);
    }

    private void initMediaExtractor(File file) {
        try {
            mMediaExtractor = new MediaExtractor();
            mMediaExtractor.setDataSource(file.toString());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void initMediaCodec(Surface surface) {
        try {
            int trackIndex = selectTrack(mMediaExtractor);
            mTrackIndex = trackIndex;
            mMediaExtractor.selectTrack(trackIndex);

            MediaFormat format = mMediaExtractor.getTrackFormat(trackIndex);
            format.setInteger(MediaFormat.KEY_I_FRAME_INTERVAL, 0);
            String mime = format.getString(MediaFormat.KEY_MIME);
            mMediaCodec = MediaCodec.createDecoderByType(mime);
            mMediaCodec.configure(format, mSurface, null, 0);
            mMediaCodec.start();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void setSurface(Surface surface) {
        mSurface = surface;
        initMediaCodec(surface);
    }

    private int selectTrack(MediaExtractor extractor) {
        // Select the first video track we find, ignore the rest.
        int numTracks = extractor.getTrackCount();
        for (int i = 0; i < numTracks; i++) {
            MediaFormat format = extractor.getTrackFormat(i);
            String mime = format.getString(MediaFormat.KEY_MIME);
            if (mime.startsWith("video/")) {
                return i;
            }
        }
        return -1;
    }

    public void release (){
        if (mMediaCodec != null) {
            mMediaCodec.stop();
            mMediaCodec.release();
            mMediaCodec = null;
        }
        if (mMediaExtractor != null) {
            mMediaExtractor.release();
            mMediaCodec = null;
        }
    }


    public void readBuffer() {
        int inputBufIndex = mMediaCodec.dequeueInputBuffer(TIMEOUT_USE);
        if (inputBufIndex >= 0) {
            ByteBuffer inputBuf = mMediaCodec.getInputBuffer(inputBufIndex);
            int chunkSize = mMediaExtractor.readSampleData(inputBuf, 0);
            if (chunkSize < 0) {
                // End of stream -- send empty frame with EOS flag set.
                mMediaCodec.queueInputBuffer(inputBufIndex, 0, 0, 0L,
                        MediaCodec.BUFFER_FLAG_END_OF_STREAM);
            } else {
                if (mMediaExtractor.getSampleTrackIndex() != mTrackIndex) {
                    Log.w(TAG, "WEIRD: got sample from track " +
                            mMediaExtractor.getSampleTrackIndex() + ", expected " + mTrackIndex);
                }
                long presentationTimeUs = mMediaExtractor.getSampleTime();
                mDecodeTime = presentationTimeUs / 1000;
                Log.d(TAG, " presentationTimeUs = " + (presentationTimeUs / 1000));
                mMediaCodec.queueInputBuffer(inputBufIndex, 0, chunkSize,
                        presentationTimeUs, 0 /*flags*/);
                if (DEBUG) {
                    Log.d(TAG, "submitted frame " + inputChunk + " to dec, size=" +
                            chunkSize);
                }
                inputChunk++;
                mMediaExtractor.advance();
            }
        } else {
            if (DEBUG) Log.d(TAG, "input buffer not available");
        }
    }

    public long getSimpleTime() {
        return mMediaExtractor.getSampleTime();
    }

    public void outBufferToSurface() {
        //output
        int decoderStatus = mMediaCodec.dequeueOutputBuffer(mBufferInfo, TIMEOUT_USE);
        if (decoderStatus == MediaCodec.INFO_TRY_AGAIN_LATER) {
            // no output available yet
            if (DEBUG) Log.d(TAG, "no output from decoder available");
        } else if (decoderStatus == MediaCodec.INFO_OUTPUT_BUFFERS_CHANGED) {
            // not important for us, since we're using Surface
            if (DEBUG) Log.d(TAG, "decoder output buffers changed");
        } else if (decoderStatus == MediaCodec.INFO_OUTPUT_FORMAT_CHANGED) {
            MediaFormat newFormat = mMediaCodec.getOutputFormat();
            if (DEBUG) Log.d(TAG, "decoder output format changed: " + newFormat);
        } else if (decoderStatus < 0) {
            throw new RuntimeException(
                    "unexpected result from decoder.dequeueOutputBuffer: " +
                            decoderStatus);
        } else {
            boolean doRender = (mBufferInfo.size != 0);
            mMediaCodec.releaseOutputBuffer(decoderStatus, doRender);
            Log.d(TAG, " mBufferInfo. 2presentationTimeUs = " + mBufferInfo.presentationTimeUs);
        }
    }

}
