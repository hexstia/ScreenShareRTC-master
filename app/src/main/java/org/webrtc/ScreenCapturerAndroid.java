//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package org.webrtc;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.Intent;
import android.hardware.display.VirtualDisplay;
import android.media.projection.MediaProjection;
import android.media.projection.MediaProjection.Callback;
import android.media.projection.MediaProjectionManager;
import android.os.Handler;
import android.util.Log;
import android.view.Surface;

import org.webrtc.SurfaceTextureHelper.OnTextureFrameAvailableListener;

import fr.pchab.androidrtc.RtcActivity;

@TargetApi(21)
public class ScreenCapturerAndroid implements VideoCapturer, OnTextureFrameAvailableListener {
    private static final int DISPLAY_FLAGS = 3;
    private static final int VIRTUAL_DISPLAY_DPI = 400;
    private final Intent mediaProjectionPermissionResultData;
    private final Callback mediaProjectionCallback;
    private int width;
    private int height;
    private VirtualDisplay virtualDisplay;
    private SurfaceTextureHelper surfaceTextureHelper;
    private CapturerObserver capturerObserver;
    private long numCapturedFrames = 0L;
    private MediaProjection mediaProjection;
    private boolean isDisposed = false;
    private MediaProjectionManager mediaProjectionManager;

    public ScreenCapturerAndroid(Intent mediaProjectionPermissionResultData, Callback mediaProjectionCallback) {
        this.mediaProjectionPermissionResultData = mediaProjectionPermissionResultData;
        this.mediaProjectionCallback = mediaProjectionCallback;
    }

    private void checkNotDisposed() {
        if (this.isDisposed) {
            throw new RuntimeException("capturer is disposed.");
        }
    }

    public synchronized void initialize(SurfaceTextureHelper surfaceTextureHelper, Context applicationContext, CapturerObserver capturerObserver) {
        this.checkNotDisposed();
        if (capturerObserver == null) {
            throw new RuntimeException("capturerObserver not set.");
        } else {
            this.capturerObserver = capturerObserver;
            if (surfaceTextureHelper == null) {
                throw new RuntimeException("surfaceTextureHelper not set.");
            } else {
                this.surfaceTextureHelper = surfaceTextureHelper;
                this.mediaProjectionManager = (MediaProjectionManager)applicationContext.getSystemService("media_projection");
            }
        }
    }

    public synchronized void startCapture(int width, int height, int ignoredFramerate) {
        //hexstia
        /**
         *     java.lang.IllegalStateException: Cannot start already started MediaProjection
         *         at android.os.Parcel.readException(Parcel.java:2013)
         *         at android.os.Parcel.readException(Parcel.java:1951)
         *         at android.media.projection.IMediaProjection$Stub$Proxy.start(IMediaProjection.java:140)
         *         at android.media.projection.MediaProjection.<init>(MediaProjection.java:59)
         *         at android.media.projection.MediaProjectionManager.getMediaProjection(MediaProjectionManager.java:97)
         *         at org.webrtc.ScreenCapturerAndroid.startCapture(ScreenCapturerAndroid.java:71)
         *         at fr.pchab.androidrtc.WebRtcClient.initScreenCapturStream(WebRtcClient.java:447)
         *         at fr.pchab.androidrtc.WebRtcClient.start(WebRtcClient.java:412)
         *         at fr.pchab.androidrtc.RtcActivity.onReady(RtcActivity.java:218)
         *         at fr.pchab.androidrtc.WebRtcClient$MessageHandler$2.call(WebRtcClient.java:205)
         */
        Log.i(RtcActivity.TAG,"ScreenCapturerAndroid startCapture()");
        this.checkNotDisposed();
        this.width = width;
        this.height = height;
        this.mediaProjection = this.mediaProjectionManager.getMediaProjection(-1, this.mediaProjectionPermissionResultData);
        this.mediaProjection.registerCallback(this.mediaProjectionCallback, this.surfaceTextureHelper.getHandler());
        this.createVirtualDisplay();
        this.capturerObserver.onCapturerStarted(true);
        this.surfaceTextureHelper.startListening(this);
    }

    public synchronized void stopCapture() {
        this.checkNotDisposed();
        ThreadUtils.invokeAtFrontUninterruptibly(this.surfaceTextureHelper.getHandler(), new Runnable() {
            public void run() {
                ScreenCapturerAndroid.this.surfaceTextureHelper.stopListening();
                ScreenCapturerAndroid.this.capturerObserver.onCapturerStopped();
                if (ScreenCapturerAndroid.this.virtualDisplay != null) {
                    ScreenCapturerAndroid.this.virtualDisplay.release();
                    ScreenCapturerAndroid.this.virtualDisplay = null;
                }

                if (ScreenCapturerAndroid.this.mediaProjection != null) {
                    ScreenCapturerAndroid.this.mediaProjection.unregisterCallback(ScreenCapturerAndroid.this.mediaProjectionCallback);
                    ScreenCapturerAndroid.this.mediaProjection.stop();
                    ScreenCapturerAndroid.this.mediaProjection = null;
                }

            }
        });
    }

    public synchronized void dispose() {
        this.isDisposed = true;
    }

    public synchronized void changeCaptureFormat(int width, int height, int ignoredFramerate) {
        this.checkNotDisposed();
        this.width = width;
        this.height = height;
        if (this.virtualDisplay != null) {
            ThreadUtils.invokeAtFrontUninterruptibly(this.surfaceTextureHelper.getHandler(), new Runnable() {
                public void run() {
                    ScreenCapturerAndroid.this.virtualDisplay.release();
                    ScreenCapturerAndroid.this.createVirtualDisplay();
                }
            });
        }
    }

    private void createVirtualDisplay() {
        Log.i(RtcActivity.TAG,"setDefaultBufferSize WIDTH :"+this.width);
        Log.i(RtcActivity.TAG,"setDefaultBufferSize HEIGHT :"+this.height);
        this.surfaceTextureHelper.getSurfaceTexture().setDefaultBufferSize(width, height);
        this.virtualDisplay = this.mediaProjection.createVirtualDisplay("WebRTC_ScreenCapture", width, height, 240, 3, new Surface(this.surfaceTextureHelper.getSurfaceTexture()), (VirtualDisplay.Callback)null, (Handler)null);
    }

    public void onTextureFrameAvailable(int oesTextureId, float[] transformMatrix, long timestampNs) {
        ++this.numCapturedFrames;
//        Log.i(RtcActivity.TAG,"onTextureFrameAvailable WIDTH :"+this.width);
//        Log.i(RtcActivity.TAG,"onTextureFrameAvailable HEIGHT :"+this.height);
        //当传入奇数值， web页面端显示偶数值 例如传入267 561 但是页面 266 560 ，所以不用管

        this.capturerObserver.onTextureFrameCaptured(width, height, oesTextureId, transformMatrix, 0, timestampNs);
    }

    public boolean isScreencast() {
        return true;
    }

    public long getNumCapturedFrames() {
        return this.numCapturedFrames;
    }
}
