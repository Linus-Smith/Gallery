package com.android.gallery3d.mediaCore.recorder;

import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.media.MediaCodec;
import android.media.MediaCodecInfo;
import android.media.MediaCodecList;
import android.media.MediaExtractor;
import android.media.MediaFormat;
import android.media.MediaMuxer;
import android.os.Environment;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import android.view.Surface;

import com.android.gallery3d.glrenderer.GLCanvas;
import com.android.gallery3d.glrenderer.GLES20Canvas;
import com.android.gallery3d.mediaCore.anim.PlayBits;
import com.android.gallery3d.mediaCore.view.Inte.OnNotifyChangeListener;


import java.io.File;
import java.io.FileDescriptor;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.LinkedList;
import java.util.List;

/**
 * Created by bruce.jiang on 2016/12/21.
 */

public class RecorderRender implements OnNotifyChangeListener {

    private static final String TAG = "RecoderRender";
    private GLCanvas mCanvas;

    private PlayBits mPlayBits;
    private int mWidth = 1080;
    private int mHeight = 608;

    private InputSurface inputSurface;
    private OutputSurface outputSurface;

    private static final boolean VERBOSE = true;
   // private List<StoryAlbumClip> mClips;


    private String savePath;
    private String audioSource;

    // encoder / muxer state
    private MediaCodec mEncoder;
    private MediaMuxer mMuxer;
    private int mTrackIndex;
    private boolean mMuxerStarted;

    private MediaFormat mEncoderOutputAudioFormat = null;
    private MediaFormat mEncoderOutputVedioFormat = null;
    // We will get these from the decoders when notified of a format change.
    private MediaFormat mDecoderOutputVideoFormat = null;
    private MediaFormat mDecoderOutputAudioFormat = null;
    // Whether things are done on the video side.
    private boolean mVideoExtractorDone = false;
    private MediaExtractor mVideoExtractor = null;
    private boolean mVideoDecoderDone = false;
    private MediaCodec mVideoDecoder = null;

    // Whether things are done on the audio side.
    private boolean mAudioExtractorDone = false;
    private boolean mAudioDecoderDone = false;
    private boolean mAudioEncoderDone = false;

    private int mOutputAudioTrack = -1;

    private MediaCodec mAudioEncoder = null;
    private MediaExtractor mAudioExtractor = null;
    private MediaCodec mAudioDecoder = null;

    private static final int frameRate = 15;
    // parameters for the video encoder
    private static final String OUTPUT_VIDEO_MIME_TYPE = "video/avc"; // H.264 Advanced Video Coding
    private static final int OUTPUT_VIDEO_BIT_RATE = 2000000; // 2Mbps
    private static final int OUTPUT_VIDEO_FRAME_RATE = 15; // 15fps
    private static final int OUTPUT_VIDEO_IFRAME_INTERVAL = 10; // 10 seconds between I-frames
    private static final int OUTPUT_VIDEO_COLOR_FORMAT =
            MediaCodecInfo.CodecCapabilities.COLOR_FormatSurface;

    // parameters for the audio encoder
    private static final String OUTPUT_AUDIO_MIME_TYPE = "audio/mp4a-latm"; // Advanced Audio Coding
    private static final int OUTPUT_AUDIO_CHANNEL_COUNT = 2; // Must match the input stream.
    private static final int OUTPUT_AUDIO_BIT_RATE = 128 * 1024;
    private static final int OUTPUT_AUDIO_AAC_PROFILE =
            MediaCodecInfo.CodecProfileLevel.AACObjectHE;
    private static final int OUTPUT_AUDIO_SAMPLE_RATE_HZ = 44100; // Must match the input stream.

    private LinkedList<Integer> mPendingAudioDecoderOutputBufferIndices;
    private LinkedList<MediaCodec.BufferInfo> mPendingAudioDecoderOutputBufferInfos;
    private LinkedList<Integer> mPendingAudioEncoderInputBufferIndices;
    private LinkedList<MediaCodec.BufferInfo> mPendingAudioEncoderOutputBufferInfos;
    private LinkedList<Integer> mPendingAudioEncoderOutputBufferIndices;

    // allocate one of these up front so we don't need to do it every time
    private MediaCodec.BufferInfo mBufferInfo;

    // where to put the output file (note: /sdcard requires WRITE_EXTERNAL_STORAGE permission)
    private static final File OUTPUT_DIR = Environment.getExternalStorageDirectory();

    private final Long ONE_MILLION = 1000000L;
    private final Long ONE_BILLION = 1000000000L;

    private float [] mBackgroundColor;
    /**
     * Used for editing the frames.
     * <p>
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


    private int mTotalTime;

    private  boolean isStopEncoding =false;
    private OnCompositeListener listener;


    public interface OnCompositeListener {

        void onSetCompositeProgress(int progress);

    }



    public RecorderRender() {
        mPlayBits = new PlayBits();
        mPlayBits.setOnNotifyChangeListener(this);
        mBackgroundColor = new float[]{0,0,0,0};
    }


    public void setOnCompositeListener(OnCompositeListener listener) {
        this.listener = listener;
    }
//    public void setDataSource(List<StoryAlbumClip> clips){
//        mClips=clips;
//    }

    public void setBGMusic(String music){
        audioSource=music;
    }

    public void setTotalTime(int totalTime){
        mTotalTime=totalTime;
    }

    public void setSavePath(String savePath) {
        this.savePath = savePath;
    }





    public void stopEncoder(){
        isStopEncoding=true;
//        drainEncoder(true);
    }

    public void startEncoder() {

        try {

            mOutputAudioTrack = -1;

            mAudioExtractorDone = false;
            mAudioDecoderDone = false;
            mAudioEncoderDone = false;
            mDecoderOutputAudioFormat = null;
            mEncoderOutputAudioFormat = null;
            mMuxerStarted = false;
            mPendingAudioDecoderOutputBufferIndices = new LinkedList<Integer>();
            mPendingAudioDecoderOutputBufferInfos = new LinkedList<MediaCodec.BufferInfo>();
            mPendingAudioEncoderInputBufferIndices = new LinkedList<Integer>();
            mPendingAudioEncoderOutputBufferInfos = new LinkedList<MediaCodec.BufferInfo>();
            mPendingAudioEncoderOutputBufferIndices = new LinkedList<Integer>();
            checkSource();
            prepareEncoder();

            doEncodeAudio(new File(audioSource),mTotalTime*ONE_MILLION);
//            for(StoryAlbumClip clip:mClips){
//                if(isStopEncoding){
//                    break;
//                }
//                if(clip instanceof StoryAlbumImageClip){
//                    recordImage(clip);
//                }
//                if(clip instanceof StoryAlbumVideoClip){
//                    recordVideo(clip);
//                }
//            }
            awaitAudio();
            drainEncoder(true);
            listener.onSetCompositeProgress(100);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            // release encoder, muxer, and input Surface
            releaseEncoder();

        }

    }

    private void checkSource() throws Exception {
        if(audioSource==null){
            throw new RuntimeException("audioSource is null");
        }
        if(mTotalTime == 0){
            throw new RuntimeException("total vedio time is 0");
        }
//        if(mClips==null){
//            throw new RuntimeException("storyalbum content is null");
//        }
    }

//    private void recordVideo(StoryAlbumClip clip) throws IOException {
//        mVideoExtractorDone = false;
//        mVideoDecoderDone = false;
//        mDecoderOutputVideoFormat = null;
//        mEncoderOutputVedioFormat = null;
//        File file = new File(clip.getPath());
//        if(!file.exists()){
//            return;
//        }
//        Bitmap bg  = rotateBitmap(clip.getBitmap());
//        doEncode(file,clip.getRhythmDuration()*ONE_MILLION,clip.getBeginTime()*ONE_MILLION,clip.getWidth(),clip.getHeight(),bg);
//        isEncodeDone = false;
//        awaitEncode();
//        bg.recycle();
//        bg=null;
//    }

    private Bitmap rotateBitmap(Bitmap bitmap) {
        int w = bitmap.getWidth();
        int h = bitmap.getHeight();
        Matrix m = new Matrix();
        m.postScale(1, -1);   //镜像垂直翻转
        Bitmap new2 = Bitmap.createBitmap(bitmap, 0, 0, w, h, m, true);
        return new2;
    }

//    private void recordImage(StoryAlbumClip clip) {
//        Bitmap bitmap = clip.getBitmap();
//        if(mCanvas == null){
//            inputSurface.makeCurrent();
//            mCanvas = new GLES20Canvas();
//
//        }
//        mCanvas.setSize(mWidth, mHeight);
//        mPlayBits.setResolution(mWidth, mHeight);
//
//        long beginTime = clip.getBeginTime()*ONE_MILLION;
//        long duration = clip.getRhythmDuration() * ONE_MILLION;
//        CompositeStatusControl control = new CompositeStatusControl();
//        control.setStartTime(beginTime);
//        MediaStream mMediaStream = null;
//        switch (clip.getAnimation()) {
//            case StoryAlbumClip.ANIMATION_ZOOM:
//                mMediaStream = new AlphaComboStream(
//                        new ZoomBmStream(bitmap, 0));
//                mMediaStream.setStatusControl(control);
//                mPlayBits.prepare((ComboStream) mMediaStream, duration, 1000 * ONE_MILLION, StatusIs.TRANSITION_PLAY_MODE_MERGE);
//                break;
//            case StoryAlbumClip.ANIMATION_PANO:
//                mMediaStream = new PanoramicBmStream(bitmap, clip.mRotation);
//                mMediaStream.setStatusControl(control);
//                mPlayBits.prepare(mMediaStream, duration);
//                break;
//        }
//
//        long playtime = beginTime;
//        while (playtime - beginTime < duration ) {
//            doFrame(mCanvas, playtime);
//            playtime = playtime + ONE_BILLION / frameRate;
//            if(isStopEncoding){
//                break;
//            }
//        }
//    }

    /**
     * Releases encoder resources.  May be called after partial / failed initialization.
     */
    private void releaseEncoder() {
        if (VERBOSE) Log.d(TAG, "releasing encoder objects");
        if (mEncoder != null) {
            try {
                mEncoder.stop();
                mEncoder.release();
                mEncoder = null;
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        if (inputSurface != null) {
            try {
                inputSurface.release();
                inputSurface = null;
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        if (outputSurface != null) {
            try {
                outputSurface.release();
                outputSurface = null;
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        if (mMuxer != null) {
            try {
                mMuxer.stop();
                mMuxer.release();
                mMuxer = null;
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        if (mVideoDecoder != null) {
            try {
                mVideoDecoder.stop();
                mVideoDecoder.release();
                mVideoDecoder = null;
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        if (mVideoExtractor != null) {
            try {
                mVideoExtractor.release();
                mVideoExtractor = null;
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        if (mAudioEncoder != null) {
            try {
                mAudioEncoder.stop();
                mAudioEncoder.release();
                mAudioEncoder = null;
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        if (mAudioExtractor != null) {
            try {
                mAudioExtractor.release();
                mAudioExtractor = null;
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        if (mAudioDecoder != null) {
            try {
                mAudioDecoder.stop();
                mAudioDecoder.release();
                mAudioDecoder = null;
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        if (mVideoDecoderHandlerThread != null) {
            mVideoDecoderHandlerThread.quitSafely();
            mVideoDecoderHandlerThread = null;
        }
        mPendingAudioDecoderOutputBufferIndices = null;
        mPendingAudioDecoderOutputBufferInfos = null;
        mPendingAudioEncoderInputBufferIndices = null;
        mPendingAudioEncoderOutputBufferInfos = null;
        mPendingAudioEncoderOutputBufferIndices = null;
    }


    /**
     * Configures encoder and muxer state, and prepares the input Surface.
     */
    public void prepareEncoder() throws IOException {
        mBufferInfo = new MediaCodec.BufferInfo();

        MediaFormat format = MediaFormat.createVideoFormat(OUTPUT_VIDEO_MIME_TYPE, mWidth, mHeight);

        // Set some properties.  Failing to specify some of these can cause the MediaCodec
        // configure() call to throw an unhelpful exception.
        format.setInteger(MediaFormat.KEY_COLOR_FORMAT,
                OUTPUT_VIDEO_COLOR_FORMAT);
        format.setInteger(MediaFormat.KEY_BIT_RATE, OUTPUT_VIDEO_BIT_RATE);
        format.setInteger(MediaFormat.KEY_FRAME_RATE, OUTPUT_VIDEO_FRAME_RATE);
        format.setInteger(MediaFormat.KEY_I_FRAME_INTERVAL, OUTPUT_VIDEO_IFRAME_INTERVAL);
        if (VERBOSE) Log.d(TAG, "format: " + format);

        // Create a MediaCodec encoder, and configure it with our format.  Get a Surface
        // we can use for input and wrap it with a class that handles the EGL work.
        //
        // If you want to have two EGL contexts -- one for display, one for recording --
        // you will likely want to defer instantiation of CodecInputSurface until after the
        // "display" EGL context is created, then modify the eglCreateContext call to
        // take eglGetCurrentContext() as the share_context argument.
        mEncoder = MediaCodec.createEncoderByType(OUTPUT_VIDEO_MIME_TYPE);


        mEncoder.configure(format, null, null, MediaCodec.CONFIGURE_FLAG_ENCODE);
        inputSurface = new InputSurface(mEncoder.createInputSurface());

        mEncoder.start();

        // Output filename.  Ideally this would use Context.getFilesDir() rather than a
        // hard-coded output directory.
        Log.d(TAG, "output file is " + savePath);
//        String outputPath = new File(OUTPUT_DIR,
//                "mystory" + ".mp4").toString();
//        Log.d(TAG, "output file is " + outputPath);

        // Create a MediaMuxer.  We can't add the video track and start() the muxer here,
        // because our MediaFormat doesn't have the Magic Goodies.  These can only be
        // obtained from the encoder after it has started processing data.
        //
        // We're not actually interested in multiplexing audio.  We just want to convert
        // the raw H.264 elementary stream we get from MediaCodec into a .mp4 file.
        try {
            mMuxer = new MediaMuxer(savePath, MediaMuxer.OutputFormat.MUXER_OUTPUT_MPEG_4);
        } catch (IOException ioe) {
            throw new RuntimeException("MediaMuxer creation failed", ioe);
        }

        mTrackIndex = -1;
        mMuxerStarted = false;
    }


    @Override
    public void doInvalidate() {


    }


    @Override
    public void notifyCompletion() {

    }

    private void doFrame(GLCanvas canvas, long timeStampNanos) {

        // Feed any pending encoder output into the muxer.
        drainEncoder(false);
        inputSurface.makeCurrent();
        if(mBackgroundColor!=null){
            canvas.clearBuffer(mBackgroundColor);
        }
        mPlayBits.calculate(timeStampNanos);
        mPlayBits.onDraw(canvas);
        inputSurface.setPresentationTime(timeStampNanos);

        // Submit it to the encoder.  The eglSwapBuffers call will block if the input
        // is full, which would be bad if it stayed full until we dequeued an output
        // buffer (which we can't do, since we're stuck here).  So long as we fully drain
        // the encoder before supplying additional input, the system guarantees that we
        // can supply another frame without blocking.
        inputSurface.swapBuffers();
    }

    /**
     * Extracts all pending data from the encoder.
     * <p>
     * If endOfStream is not set, this returns when there is no more data to drain.  If it
     * is set, we send EOS to the encoder, and then iterate until we see EOS on the output.
     * Calling this with endOfStream set should be done once, right before stopping the muxer.
     */
    private void drainEncoder(boolean endOfStream) {

        final int TIMEOUT_USEC = 10000;
        if (VERBOSE) Log.d(TAG, "drainEncoder(" + endOfStream + ")");

        if (endOfStream) {
            if (VERBOSE) Log.d(TAG, "sending EOS to encoder");
            mEncoder.signalEndOfInputStream();
        }

        ByteBuffer[] encoderOutputBuffers = mEncoder.getOutputBuffers();
        while (true) {
            int encoderStatus = mEncoder.dequeueOutputBuffer(mBufferInfo, TIMEOUT_USEC);
            if (VERBOSE) Log.d(TAG, "encoderStatus =" + encoderStatus);
            if (encoderStatus == MediaCodec.INFO_TRY_AGAIN_LATER) {
                // no output available yet
                if (!endOfStream) {
                    break;      // out of while
                } else {
                    if (VERBOSE) Log.d(TAG, "no output available, spinning to await EOS");
                }
            } else if (encoderStatus == MediaCodec.INFO_OUTPUT_BUFFERS_CHANGED) {
                // not expected for an encoder
                encoderOutputBuffers = mEncoder.getOutputBuffers();
            } else if (encoderStatus == MediaCodec.INFO_OUTPUT_FORMAT_CHANGED) {
                // should happen before receiving buffers, and should only happen once
//                if (mMuxerStarted) {
//                    throw new RuntimeException("format changed twice");
//                }
//                MediaFormat newFormat = mEncoder.getOutputFormat();
//                Log.d(TAG, "encoder output format changed: " + newFormat);
//
//                // now that we have the Magic Goodies, start the muxer
//                mTrackIndex = mMuxer.addTrack(newFormat);
//                mMuxer.start();
//                mMuxerStarted = true;

                mEncoderOutputVedioFormat = mEncoder.getOutputFormat();
                setupMuxer();
            } else if (encoderStatus < 0) {
                Log.w(TAG, "unexpected result from encoder.dequeueOutputBuffer: " +
                        encoderStatus);
                // let's ignore it
            } else {
                ByteBuffer encodedData = encoderOutputBuffers[encoderStatus];
                if (encodedData == null) {
                    throw new RuntimeException("encoderOutputBuffer " + encoderStatus +
                            " was null");
                }

                if ((mBufferInfo.flags & MediaCodec.BUFFER_FLAG_CODEC_CONFIG) != 0) {
                    // The codec config data was pulled out and fed to the muxer when we got
                    // the INFO_OUTPUT_FORMAT_CHANGED status.  Ignore it.
                    if (VERBOSE) Log.d(TAG, "ignoring BUFFER_FLAG_CODEC_CONFIG");
                    mBufferInfo.size = 0;
                }

                if (mBufferInfo.size != 0) {
                    if (!mMuxerStarted) {
                        throw new RuntimeException("muxer hasn't started");
                    }

                    // adjust the ByteBuffer values to match BufferInfo (not needed?)
                    encodedData.position(mBufferInfo.offset);
                    encodedData.limit(mBufferInfo.offset + mBufferInfo.size);

                    mMuxer.writeSampleData(mTrackIndex, encodedData, mBufferInfo);
                    if(listener!=null&& mTotalTime!=0){
                        int progress = (int) (mBufferInfo.presentationTimeUs/(mTotalTime*10));
                        listener.onSetCompositeProgress(progress);
                    }
                    if (VERBOSE) Log.d(TAG, "sent " + mBufferInfo.size + " bytes to muxer");
                }

                mEncoder.releaseOutputBuffer(encoderStatus, false);

                if ((mBufferInfo.flags & MediaCodec.BUFFER_FLAG_END_OF_STREAM) != 0) {
                    if (!endOfStream) {
                        Log.w(TAG, "reached end of stream unexpectedly");
                    } else {
                        if (VERBOSE) Log.d(TAG, "end of stream reached");
                        synchronized (this) {
                            isEncodeDone = true;
                            notifyAll();

                        }

                    }
                    break;      // out of while
                }
            }
        }
    }

    boolean isEncodeDone = false;

    private void awaitEncode() {
        synchronized (this) {
            while (!isEncodeDone) {
                try {
                    wait();
                } catch (InterruptedException ie) {
                }
            }
        }
    }

    private void awaitAudio() {
        synchronized (this) {
            while (!mAudioEncoderDone) {
                try {
                    wait();
                } catch (InterruptedException ie) {
                }
            }
        }
    }
    private void doEncodeAudio(File sourceAudio, long duration)throws IOException {
        MediaCodecInfo audioCodecInfo = selectCodec(OUTPUT_AUDIO_MIME_TYPE);
        if (audioCodecInfo == null) {
            // Don't fail CTS if they don't have an AAC codec (not here, anyway).
            Log.e(TAG, "Unable to find an appropriate codec for " + OUTPUT_AUDIO_MIME_TYPE);
            return;
        }
        if (VERBOSE) Log.d(TAG, "audio found codec: " + audioCodecInfo.getName());
        mAudioExtractor = createExtractor(sourceAudio);
        int audioInputTrack = getAndSelectAudioTrackIndex(mAudioExtractor);
        if(audioInputTrack == -1){
            if (VERBOSE) Log.d(TAG, "missing audio track in test video ");
            return;
        }
        MediaFormat inputFormat = mAudioExtractor.getTrackFormat(audioInputTrack);

        MediaFormat outputAudioFormat =
                MediaFormat.createAudioFormat(
                        OUTPUT_AUDIO_MIME_TYPE, OUTPUT_AUDIO_SAMPLE_RATE_HZ,
                        OUTPUT_AUDIO_CHANNEL_COUNT);
        outputAudioFormat.setInteger(MediaFormat.KEY_BIT_RATE, OUTPUT_AUDIO_BIT_RATE);
        outputAudioFormat.setInteger(MediaFormat.KEY_AAC_PROFILE, OUTPUT_AUDIO_AAC_PROFILE);

        // Create a MediaCodec for the desired codec, then configure it as an encoder with
        // our desired properties. Request a Surface to use for input.
        mAudioEncoder = createAudioEncoder(audioCodecInfo, outputAudioFormat);
        // Create a MediaCodec for the decoder, based on the extractor's format.
        mAudioDecoder = createAudioDecoder(inputFormat,duration);
    }


    /**
     * Creates an encoder for the given format using the specified codec.
     *
     * @param codecInfo of the codec to use
     * @param format of the stream to be produced
     */
    private MediaCodec createAudioEncoder(MediaCodecInfo codecInfo, MediaFormat format) throws IOException {
        MediaCodec encoder = MediaCodec.createByCodecName(codecInfo.getName());
        encoder.setCallback(new MediaCodec.Callback() {
            public void onError(MediaCodec codec, MediaCodec.CodecException exception) {
            }
            public void onOutputFormatChanged(MediaCodec codec, MediaFormat format) {
                if (VERBOSE) Log.d(TAG, "audio encoder: output format changed");
                if (mOutputAudioTrack >= 0) {
                    if (VERBOSE) Log.d(TAG, "audio encoder changed its output format again?");
                    return;
                }

                mEncoderOutputAudioFormat = codec.getOutputFormat();
                setupMuxer();
            }
            public void onInputBufferAvailable(MediaCodec codec, int index) {
                if (VERBOSE) {
                    Log.d(TAG, "audio encoder: returned input buffer: " + index);
                }
                mPendingAudioEncoderInputBufferIndices.add(index);
                tryEncodeAudio();
            }
            public void onOutputBufferAvailable(MediaCodec codec, int index, MediaCodec.BufferInfo info) {
                if (VERBOSE) {
                    Log.d(TAG, "audio encoder: returned output buffer: " + index);
                    Log.d(TAG, "audio encoder: returned buffer of size " + info.size);
                }
                muxAudio(index, info);
            }
        });
        encoder.configure(format, null, null, MediaCodec.CONFIGURE_FLAG_ENCODE);
        encoder.start();
        return encoder;
    }


    private void setupMuxer() {
        if (!mMuxerStarted&&mEncoderOutputAudioFormat!=null && mEncoderOutputVedioFormat!=null) {

            Log.d(TAG, "encoder output format changed: " + mEncoderOutputVedioFormat);

            // now that we have the Magic Goodies, start the muxer
            mTrackIndex = mMuxer.addTrack(mEncoderOutputVedioFormat);

            Log.d(TAG, "muxer: adding audio track.");
            mOutputAudioTrack = mMuxer.addTrack(mEncoderOutputAudioFormat);

            Log.d(TAG, "muxer: starting");
            mMuxer.start();
            mMuxerStarted = true;

            MediaCodec.BufferInfo info;
            while ((info = mPendingAudioEncoderOutputBufferInfos.poll()) != null) {
                int index = mPendingAudioEncoderOutputBufferIndices.poll().intValue();
                muxAudio(index, info);
            }
        }
    }

    private void muxAudio(int index, MediaCodec.BufferInfo info) {
        if (!mMuxerStarted) {
            mPendingAudioEncoderOutputBufferIndices.add(new Integer(index));
            mPendingAudioEncoderOutputBufferInfos.add(info);
            return;
        }
        ByteBuffer encoderOutputBuffer = mAudioEncoder.getOutputBuffer(index);
        if ((info.flags & MediaCodec.BUFFER_FLAG_CODEC_CONFIG) != 0) {
            if (VERBOSE) Log.d(TAG, "audio encoder: codec config buffer");
            // Simply ignore codec config buffers.
            mAudioEncoder.releaseOutputBuffer(index, false);
            return;
        }
        if (VERBOSE) {
            Log.d(TAG, "audio encoder: returned buffer for time " + info.presentationTimeUs);
        }
        if (info.size != 0) {
            mMuxer.writeSampleData(
                    mOutputAudioTrack, encoderOutputBuffer, info);
        }
        mAudioEncoder.releaseOutputBuffer(index, false);
        if ((info.flags & MediaCodec.BUFFER_FLAG_END_OF_STREAM) != 0) {
            if (VERBOSE) Log.d(TAG, "audio encoder: EOS");
            synchronized (this) {
                mAudioEncoderDone = true;
                notifyAll();
            }
        }
    }
    /**
     * Creates a decoder for the given format.
     *
     * @param inputFormat the format of the stream to decode
     */
    private MediaCodec createAudioDecoder(MediaFormat inputFormat, final long duration) throws IOException {
        MediaCodec decoder = MediaCodec.createDecoderByType(getMimeTypeFor(inputFormat));
        decoder.setCallback(new MediaCodec.Callback() {
            public void onError(MediaCodec codec, MediaCodec.CodecException exception) {
            }
            public void onOutputFormatChanged(MediaCodec codec, MediaFormat format) {
                mDecoderOutputAudioFormat = codec.getOutputFormat();
                if (VERBOSE) {
                    Log.d(TAG, "audio decoder: output format changed: "
                            + mDecoderOutputAudioFormat);
                }
            }
            public void onInputBufferAvailable(MediaCodec codec, int index) {
                ByteBuffer decoderInputBuffer = codec.getInputBuffer(index);
                while (!mAudioExtractorDone) {
                    int size = mAudioExtractor.readSampleData(decoderInputBuffer, 0);
                    long presentationTime = mAudioExtractor.getSampleTime();
                    if (VERBOSE) {
                        Log.d(TAG, "audio extractor: returned buffer of size " + size);
                        Log.d(TAG, "audio extractor: returned buffer for time " + presentationTime);
                    }

                    if(presentationTime * 1000>=duration||isStopEncoding){
                        mAudioExtractorDone=true;
                        codec.queueInputBuffer(index,0, 0, 0L, MediaCodec.BUFFER_FLAG_END_OF_STREAM);
                        return;
                    }
                    if (size >= 0) {
                        codec.queueInputBuffer(
                                index,
                                0,
                                size,
                                presentationTime,
                                mAudioExtractor.getSampleFlags());
                    }
                    mAudioExtractorDone = !mAudioExtractor.advance();
                    if (mAudioExtractorDone) {
                        if (VERBOSE) Log.d(TAG, "audio extractor: EOS");
                        codec.queueInputBuffer(
                                index,
                                0,
                                0,
                                0,
                                MediaCodec.BUFFER_FLAG_END_OF_STREAM);
                    }
                    if (size >= 0)
                        break;
                }
            }
            public void onOutputBufferAvailable(MediaCodec codec, int index, MediaCodec.BufferInfo info) {
                if (VERBOSE) {
                    Log.d(TAG, "audio decoder: returned output buffer: " + index);
                }
                if (VERBOSE) {
                    Log.d(TAG, "audio decoder: returned buffer of size " + info.size);
                }
                ByteBuffer decoderOutputBuffer = codec.getOutputBuffer(index);
                if ((info.flags & MediaCodec.BUFFER_FLAG_CODEC_CONFIG) != 0) {
                    if (VERBOSE) Log.d(TAG, "audio decoder: codec config buffer");
                    codec.releaseOutputBuffer(index, false);
                    return;
                }
                if (VERBOSE) {
                    Log.d(TAG, "audio decoder: returned buffer for time "
                            + info.presentationTimeUs);
                }
                mPendingAudioDecoderOutputBufferIndices.add(index);
                mPendingAudioDecoderOutputBufferInfos.add(info);
                tryEncodeAudio();
            }
        });
        decoder.configure(inputFormat, null, null, 0);
        decoder.start();
        return decoder;
    }
    // No need to have synchronization around this, since both audio encoder and
    // decoder callbacks are on the same thread.
    private void tryEncodeAudio() {
        if (mPendingAudioEncoderInputBufferIndices.size() == 0 ||
                mPendingAudioDecoderOutputBufferIndices.size() == 0)
            return;
        int decoderIndex = mPendingAudioDecoderOutputBufferIndices.poll();
        int encoderIndex = mPendingAudioEncoderInputBufferIndices.poll();
        MediaCodec.BufferInfo info = mPendingAudioDecoderOutputBufferInfos.poll();

        ByteBuffer encoderInputBuffer = mAudioEncoder.getInputBuffer(encoderIndex);
        int size = info.size;
        long presentationTime = info.presentationTimeUs;
        if (VERBOSE) {
            Log.d(TAG, "audio decoder: processing pending buffer: "
                    + decoderIndex);
        }
        if (VERBOSE) {
            Log.d(TAG, "audio decoder: pending buffer of size " + size);
            Log.d(TAG, "audio decoder: pending buffer for time " + presentationTime);
        }
        if (size >= 0) {
            ByteBuffer decoderOutputBuffer = mAudioDecoder.getOutputBuffer(decoderIndex).duplicate();
            decoderOutputBuffer.position(info.offset);
            decoderOutputBuffer.limit(info.offset + size);
            encoderInputBuffer.position(0);
            encoderInputBuffer.put(decoderOutputBuffer);

            mAudioEncoder.queueInputBuffer(
                    encoderIndex,
                    0,
                    size,
                    presentationTime,
                    info.flags);
        }
        mAudioDecoder.releaseOutputBuffer(decoderIndex, false);
        if ((info.flags
                & MediaCodec.BUFFER_FLAG_END_OF_STREAM) != 0) {
            if (VERBOSE) Log.d(TAG, "audio decoder: EOS");
            mAudioDecoderDone = true;
        }
    }
    ///audio need methed start///
    private int getAndSelectAudioTrackIndex(MediaExtractor extractor) {
        for (int index = 0; index < extractor.getTrackCount(); ++index) {
            if (VERBOSE) {
                Log.d(TAG, "format for track " + index + " is "
                        + getMimeTypeFor(extractor.getTrackFormat(index)));
            }
            if (isAudioFormat(extractor.getTrackFormat(index))) {
                extractor.selectTrack(index);
                return index;
            }
        }
        return -1;
    }

    private static boolean isAudioFormat(MediaFormat format) {
        return getMimeTypeFor(format).startsWith("audio/");
    }



    private void doEncode(File sourceFile, long duration, long beginTime, int videoWidth, int videoHeight, Bitmap bg) throws IOException {

        MediaCodecInfo videoCodecInfo = selectCodec(OUTPUT_VIDEO_MIME_TYPE);
        if (videoCodecInfo == null) {
            // Don't fail CTS if they don't have an AVC codec (not here, anyway).
            Log.e(TAG, "Unable to find an appropriate codec for " + OUTPUT_VIDEO_MIME_TYPE);
            return;
        }
        mVideoExtractor = createExtractor(sourceFile);
        int videoInputTrack = getAndSelectVideoTrackIndex(mVideoExtractor);
        if (videoInputTrack == -1) {
            if (VERBOSE) Log.d(TAG, "missing video track in test video ");
            return;
        }
        MediaFormat inputFormat = mVideoExtractor.getTrackFormat(videoInputTrack);

        inputSurface.makeCurrent();
        // Create a MediaCodec for the decoder, based on the extractor's format.
        outputSurface = new OutputSurface(mWidth,mHeight,videoWidth,videoHeight,bg);
        outputSurface.changeFragmentShader(FRAGMENT_SHADER);
        mVideoDecoder = createVideoDecoder(inputFormat, outputSurface.getSurface(),duration,beginTime);
        inputSurface.releaseEGLContext();


    }


    /**
     * Returns the first codec capable of encoding the specified MIME type, or null if no match was
     * found.
     */
    private static MediaCodecInfo selectCodec(String mimeType) {
        int numCodecs = MediaCodecList.getCodecCount();
        for (int i = 0; i < numCodecs; i++) {
            MediaCodecInfo codecInfo = MediaCodecList.getCodecInfoAt(i);

            if (!codecInfo.isEncoder()) {
                continue;
            }

            String[] types = codecInfo.getSupportedTypes();
            for (int j = 0; j < types.length; j++) {
                if (types[j].equalsIgnoreCase(mimeType)) {
                    return codecInfo;
                }
            }
        }
        return null;
    }


    private MediaExtractor createExtractor(File mFile) throws IOException {
        MediaExtractor extractor;
        extractor = new MediaExtractor();
        FileDescriptor fd = new FileInputStream(mFile).getFD();
        extractor.setDataSource(fd, 0, mFile.length());
        return extractor;
    }


    /**
     * Creates a decoder for the given format, which outputs to the given surface.
     *
     * @param inputFormat the format of the stream to decode
     * @param surface     into which to decode the frames
     */
    private MediaCodec createVideoDecoder(MediaFormat inputFormat, Surface surface, final long duration, final long beginTime) throws IOException {
        mVideoDecoderHandlerThread = new HandlerThread("DecoderThread");
        mVideoDecoderHandlerThread.start();
        mVideoDecoderHandler = new CallbackHandler(mVideoDecoderHandlerThread.getLooper());
        MediaCodec.Callback callback = new MediaCodec.Callback() {
            public void onError(MediaCodec codec, MediaCodec.CodecException exception) {
            }

            public void onOutputFormatChanged(MediaCodec codec, MediaFormat format) {
                mDecoderOutputVideoFormat = codec.getOutputFormat();
                if (VERBOSE) {
                    Log.d(TAG, "video decoder: output format changed: "
                            + mDecoderOutputVideoFormat);
                }
            }

            public void onInputBufferAvailable(MediaCodec codec, int index) {
                // Extract video from file and feed to decoder.
                // We feed packets regardless of whether the muxer is set up or not.
                // If the muxer isn't set up yet, the encoder output will be queued up,
                // finally blocking the decoder as well.
                ByteBuffer decoderInputBuffer = codec.getInputBuffer(index);
                while (!mVideoExtractorDone) {
                    int size = mVideoExtractor.readSampleData(decoderInputBuffer, 0);
                    long presentationTime = mVideoExtractor.getSampleTime();
                    if(presentationTime * 1000>=duration||isStopEncoding){
                        mVideoExtractorDone=true;
                        codec.queueInputBuffer(index,0, 0, 0L, MediaCodec.BUFFER_FLAG_END_OF_STREAM);
                        return;
                    }
                    if (VERBOSE) {
                        Log.d(TAG, "video extractor: returned buffer of size " + size);
                        Log.d(TAG, "video extractor: returned buffer for time " + presentationTime);
                    }
                    if (size >= 0) {
                        codec.queueInputBuffer(
                                index,
                                0,
                                size,
                                presentationTime,
                                mVideoExtractor.getSampleFlags());
                    }
                    mVideoExtractorDone = !mVideoExtractor.advance();
                    if (mVideoExtractorDone) {
                        if (VERBOSE) Log.d(TAG, "video extractor: EOS");
                        codec.queueInputBuffer(
                                index,
                                0,
                                0,
                                0,
                                MediaCodec.BUFFER_FLAG_END_OF_STREAM);
                    }
                    if (size >= 0)
                        break;
                }
            }

            public void onOutputBufferAvailable(MediaCodec codec, int index, MediaCodec.BufferInfo info) {
                if (VERBOSE) {
                    Log.d(TAG, "video decoder: returned output buffer: " + index);
                    Log.d(TAG, "video decoder: returned buffer of size " + info.size);
                }
                if ((info.flags & MediaCodec.BUFFER_FLAG_CODEC_CONFIG) != 0) {
                    if (VERBOSE) Log.d(TAG, "video decoder: codec config buffer");
                    codec.releaseOutputBuffer(index, false);
                    return;
                }
                if (VERBOSE) {
                    Log.d(TAG, "video decoder: returned buffer for time "
                            + info.presentationTimeUs);
                }
                boolean render = info.size != 0;
                codec.releaseOutputBuffer(index, render);
                if (render) {
                    inputSurface.makeCurrent();
                    if (VERBOSE) Log.d(TAG, "output surface: await new image");
                    outputSurface.awaitNewImage();
                    // Edit the frame and send it to the encoder.
                    if (VERBOSE) Log.d(TAG, "output surface: draw image");
                    outputSurface.drawCanvasImage();
//                    outputSurface.drawImage();
                    drainEncoder(false);
                    inputSurface.setPresentationTime(beginTime+
                            info.presentationTimeUs * 1000);
                    if (VERBOSE) Log.d(TAG, "input surface: swap buffers");
                    inputSurface.swapBuffers();
                    if (VERBOSE) Log.d(TAG, "video encoder: notified of new frame");
                    inputSurface.releaseEGLContext();
                }
                if ((info.flags & MediaCodec.BUFFER_FLAG_END_OF_STREAM) != 0) {
                    if (VERBOSE) Log.d(TAG, "video decoder: EOS");
                    mVideoDecoderDone = true;
                    synchronized (RecorderRender.this) {
                        isEncodeDone = true;
                        RecorderRender.this.notifyAll();

                    }
                }
            }
        };
        // Create the decoder on a different thread, in order to have the callbacks there.
        // This makes sure that the blocking waiting and rendering in onOutputBufferAvailable
        // won't block other callbacks (e.g. blocking encoder output callbacks), which
        // would otherwise lead to the transcoding pipeline to lock up.

        // Since API 23, we could just do setCallback(callback, mVideoDecoderHandler) instead
        // of using a custom Handler and passing a message to create the MediaCodec there.

        // When the callbacks are received on a different thread, the updating of the variables
        // that are used for state logging (mVideoExtractedFrameCount, mVideoDecodedFrameCount,
        // mVideoExtractorDone and mVideoDecoderDone) should ideally be synchronized properly
        // against accesses from other threads, but that is left out for brevity since it's
        // not essential to the actual transcoding.
        mVideoDecoderHandler.create(false, getMimeTypeFor(inputFormat), callback);
        MediaCodec decoder = mVideoDecoderHandler.getCodec();
        decoder.configure(inputFormat, surface, null, 0);
        decoder.start();
        return decoder;
    }

    private int getAndSelectVideoTrackIndex(MediaExtractor extractor) {
        for (int index = 0; index < extractor.getTrackCount(); ++index) {
            if (VERBOSE) {
                Log.d(TAG, "format for track " + index + " is "
                        + getMimeTypeFor(extractor.getTrackFormat(index)));
            }
            if (isVideoFormat(extractor.getTrackFormat(index))) {
                extractor.selectTrack(index);
                return index;
            }
        }
        return -1;
    }

    private static boolean isVideoFormat(MediaFormat format) {
        return getMimeTypeFor(format).startsWith("video/");
    }

    private static String getMimeTypeFor(MediaFormat format) {
        return format.getString(MediaFormat.KEY_MIME);
    }


    ///vedio need methed start///
    static class CallbackHandler extends Handler {
        CallbackHandler(Looper l) {
            super(l);
        }

        private MediaCodec mCodec;
        private boolean mEncoder;
        private MediaCodec.Callback mCallback;
        private String mMime;
        private boolean mSetDone;

        @Override
        public void handleMessage(Message msg) {
            try {
                mCodec = mEncoder ? MediaCodec.createEncoderByType(mMime) : MediaCodec.createDecoderByType(mMime);
            } catch (IOException ioe) {
            }
            mCodec.setCallback(mCallback);
            synchronized (this) {
                mSetDone = true;
                notifyAll();
            }
        }

        void create(boolean encoder, String mime, MediaCodec.Callback callback) {
            mEncoder = encoder;
            mMime = mime;
            mCallback = callback;
            mSetDone = false;
            sendEmptyMessage(0);
            synchronized (this) {
                while (!mSetDone) {
                    try {
                        wait();
                    } catch (InterruptedException ie) {
                    }
                }
            }
        }

        MediaCodec getCodec() {
            return mCodec;
        }
    }

    private HandlerThread mVideoDecoderHandlerThread;
    private CallbackHandler mVideoDecoderHandler;

}
