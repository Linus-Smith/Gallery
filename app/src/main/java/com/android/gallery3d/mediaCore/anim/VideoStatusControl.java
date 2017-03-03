package com.android.gallery3d.mediaCore.anim;

import android.media.MediaCodec;
import android.media.MediaExtractor;
import android.media.MediaFormat;
import android.util.Log;
import android.view.Surface;

import com.android.gallery3d.mediaCore.Utils.Utils;

import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;

/**
 * Created by caihuiyang on 2017/2/1.
 */

public class VideoStatusControl extends BaseStatusControl {

    private static final String TAG = "VideoStatusControl";
    private static final boolean DEBUG = true;

    private MediaExtractor mMediaExtractor;
    private MediaCodec mMediaCodec;
    private int mTrackIndex = -1;
    private int inputChunk = 0;
    private long mDecodeTime = 0;
    private final int TIMEOUT_USE = 10000;

    private int bit = 0;

    // Declare this here to reduce allocations.
    private MediaCodec.BufferInfo mBufferInfo = new MediaCodec.BufferInfo();


    public VideoStatusControl(File file, Surface surface) {
        initMedia(file, surface);
    }

    private void initMedia(File file, Surface outSurface) {

        try {
            mMediaExtractor = new MediaExtractor();
            mMediaExtractor.setDataSource(file.toString());

            int trackIndex = selectTrack(mMediaExtractor);
            if (trackIndex < 0) {
                throw new RuntimeException("No video track found in " + file);
            }
            mTrackIndex = trackIndex;
            mMediaExtractor.selectTrack(trackIndex);

            MediaFormat format = mMediaExtractor.getTrackFormat(trackIndex);
            format.setInteger(MediaFormat.KEY_I_FRAME_INTERVAL, 0);
            String mime = format.getString(MediaFormat.KEY_MIME);
            mMediaCodec = MediaCodec.createDecoderByType(mime);
            mMediaCodec.configure(format, outSurface, null, 0);
            mMediaCodec.start();
        } catch (IOException e) {
            e.printStackTrace();
        }


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

    @Override
    void prepare() {
        mCurrentDurationTime = 0;
        runDecode(0);
    }

    @Override
    void start() {
        if(mPlayState == PLAY_STATE_STOP) {
            mStartTime = System.currentTimeMillis();
            mPlayState = PLAY_STATE_START;
        } else if( mPlayState == PLAY_STATE_PAUSE ) {
            mStartTime = System.currentTimeMillis() -  mCurrentDurationTime;
            mPlayState = PLAY_STATE_START;
            calculate(System.currentTimeMillis());
        }
    }

    @Override
    void restart() {
        mStartTime = System.currentTimeMillis();
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
            if (mMediaCodec != null) {
                mMediaCodec.stop();
                mMediaCodec.release();
                mMediaCodec = null;
            }
            if (mMediaExtractor != null) {
                mMediaExtractor.release();
                mMediaExtractor = null;
            }
        }
    }

    @Override
    void seekTo(long durationT) {
        if(durationT > mDuration && durationT < 0) return;
        if(mPlayState == PLAY_STATE_START) {
            mStartTime = System.currentTimeMillis() -  durationT;
        } else {
            mCurrentDurationTime = durationT;
            mStartTime = System.currentTimeMillis() -  durationT;
            calculate(System.currentTimeMillis());
        }

        mMediaCodec.flush();

        mMediaExtractor.seekTo( durationT * 1000, MediaExtractor.SEEK_TO_PREVIOUS_SYNC);
        mDecodeTime = durationT;

    }


    @Override
    CalculateEntry calculate(long currentTimeMillis) {
        CalculateEntry calculateEntry = new CalculateEntry();
        if (mPlayState == PLAY_STATE_STOP || mPlayState == PLAY_STATE_PAUSE) {
            calculateEntry.isCanCalculate = false;
            calculateEntry.value = -1;
            return calculateEntry;
        }
        long elapse = currentTimeMillis - mStartTime;
        mCurrentDurationTime = elapse > mDuration ? mDuration : elapse;
        float x = Utils.clamp((float) elapse / mDuration, 0f, 1f);
        calculateEntry.isCanCalculate = true;
        calculateEntry.value = x;
        runDecode(elapse);
        return calculateEntry;
    }


    private void runDecode(long  time) {
        if(mDecodeTime > time) return;
        //input
        int inputBufIndex = mMediaCodec.dequeueInputBuffer(TIMEOUT_USE);
        if (inputBufIndex >= 0) {
            ByteBuffer inputBuf =  mMediaCodec.getInputBuffer(inputBufIndex);
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
                Log.d(TAG, " presentationTimeUs = "+ (presentationTimeUs / 1000));
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
            System.out.println(doRender+"===="+"哈哈哈");
            mMediaCodec.releaseOutputBuffer(decoderStatus, doRender);
            Log.d(TAG, " mBufferInfo. 2presentationTimeUs = "+mBufferInfo.presentationTimeUs);
        }
    }
}
