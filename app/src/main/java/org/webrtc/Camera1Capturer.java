//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package org.webrtc;

import android.content.Context;
import android.media.MediaRecorder;

import org.webrtc.CameraSession.CreateSessionCallback;
import org.webrtc.CameraSession.Events;

public class Camera1Capturer extends CameraCapturer {
    private final boolean captureToTexture;

    public Camera1Capturer(String cameraName, CameraEventsHandler eventsHandler, boolean captureToTexture) {
        super(cameraName, eventsHandler, new Camera1Enumerator(captureToTexture));
        this.captureToTexture = captureToTexture;
    }

    protected void createCameraSession(CreateSessionCallback createSessionCallback, Events events, Context applicationContext, SurfaceTextureHelper surfaceTextureHelper, MediaRecorder mediaRecorder, String cameraName, int width, int height, int framerate) {
        Camera1Session.create(createSessionCallback, events, this.captureToTexture || mediaRecorder != null, applicationContext, surfaceTextureHelper, mediaRecorder, Camera1Enumerator.getCameraIndex(cameraName), width, height, framerate);
    }
}
