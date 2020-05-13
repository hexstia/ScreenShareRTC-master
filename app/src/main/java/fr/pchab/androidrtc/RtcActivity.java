
package fr.pchab.androidrtc;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.media.projection.MediaProjection;
import android.media.projection.MediaProjectionManager;
import android.os.Build;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import org.webrtc.ScreenCapturerAndroid;
import org.webrtc.VideoCapturer;
public class RtcActivity extends Activity implements WebRtcClient.RtcListener {
    private  WebRtcClient mWebRtcClient;
    private static final int CAPTURE_PERMISSION_REQUEST_CODE = 1;
    //    private EglBase rootEglBase;
    private static Intent mMediaProjectionPermissionResultData;
    private static int mMediaProjectionPermissionResultCode;
    public final static String TAG = "RtcActivity";
    public static String STREAM_NAME_PREFIX = "android_device_stream";
    // List of mandatory application permissions.／
    private static final String[] MANDATORY_PERMISSIONS = {"android.permission.MODIFY_AUDIO_SETTINGS",
            "android.permission.RECORD_AUDIO", "android.permission.INTERNET"};
    public static String  host  = "https://test.91lanjiang.com:3000/";
    //    private SurfaceViewRenderer pipRenderer;
//    private SurfaceViewRenderer fullscreenRenderer;
    public static int sDeviceWidth;
    public static int sDeviceHeight;
    public static final int SCREEN_RESOLUTION_SCALE = 4;
    private Button btn,display,close,disconnect,connect;
    private EditText et;
    private PeerConnectionClient.PeerConnectionParameters peerConnectionParameters;
    private VideoCapturer videoCapturer;
    private static RtcActivity thiz;

    public static RtcActivity getThiz() {
        return thiz;
    }

    public WebRtcClient getmWebRtcClient() {
        return mWebRtcClient;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        thiz = this;
        getWindow().addFlags(
                WindowManager.LayoutParams.FLAG_FULLSCREEN
                        | WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON
                        | WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD
                        | WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED
                        | WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON);
        setContentView(R.layout.activity_rtc);
        DisplayMetrics metrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getRealMetrics(metrics);
        sDeviceWidth = metrics.widthPixels;
        sDeviceHeight = metrics.heightPixels;
        btn = findViewById(R.id.btn);
        display = findViewById(R.id.display);
        connect = findViewById(R.id.connect);
        close = findViewById(R.id.close);
        disconnect = findViewById(R.id.disconnect);
        et = findViewById(R.id.et);
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
               String  tmphost = et.getText().toString();
                if(!tmphost.equals("")){
                    host = tmphost;
                }
                for (String permission : MANDATORY_PERMISSIONS) {
                    if (checkCallingOrSelfPermission(permission) != PackageManager.PERMISSION_GRANTED) {
                        setResult(RESULT_CANCELED);
                        finish();
                        return;
                    }
                }
                    Log.i(TAG,"startScreenCapture()");
                    startScreenCapture();//创建录屏

            }
        });
        connect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                connect(); //建立连接
            }
        });
        display.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //发送消息，使其用来视频流显示
                   mWebRtcClient.socketstart("abcd");
            }
        });

        close.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mWebRtcClient.onDestroy();
            }
        });
        disconnect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });
//        pipRenderer = (SurfaceViewRenderer) findViewById(R.id.pip_video_view);
//        fullscreenRenderer = (SurfaceViewRenderer) findViewById(R.id.fullscreen_video_view);

//        EglBase rootEglBase = EglBase.create();
//        pipRenderer.init(rootEglBase.getEglBaseContext(), null);
//        pipRenderer.setScalingType(RendererCommon.ScalingType.SCALE_ASPECT_FIT);
//        fullscreenRenderer.init(rootEglBase.getEglBaseContext(), null);
//        fullscreenRenderer.setScalingType(RendererCommon.ScalingType.SCALE_ASPECT_FILL);

//        pipRenderer.setZOrderMediaOverlay(true);
//        pipRenderer.setEnableHardwareScaler(true /* enabled */);
//        fullscreenRenderer.setEnableHardwareScaler(true /* enabled */);
        // Check for mandatory permissions.

    }

    @TargetApi(21)
    private void startScreenCapture() {
        MediaProjectionManager mediaProjectionManager =
                (MediaProjectionManager) getApplication().getSystemService(
                        Context.MEDIA_PROJECTION_SERVICE);
        Log.i(TAG,"startActivityForResult()");
        startActivityForResult(
                mediaProjectionManager.createScreenCaptureIntent(), CAPTURE_PERMISSION_REQUEST_CODE);
    }

    @TargetApi(21)
    private VideoCapturer createScreenCapturer() {
        if (mMediaProjectionPermissionResultCode != Activity.RESULT_OK) {
            report("User didn't give permission to capture the screen.");
            return null;
        }
        return new ScreenCapturerAndroid(
                mMediaProjectionPermissionResultData, new MediaProjection.Callback() {
            @Override
            public void onStop() {
                report("User revoked permission to capture the screen.");
            }
        });
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        Log.i(TAG,"onActivityResult() before");
        if (requestCode != CAPTURE_PERMISSION_REQUEST_CODE)
            return;
        mMediaProjectionPermissionResultCode = resultCode;
        mMediaProjectionPermissionResultData = data;
        Log.i(TAG,"onActivityResult()");
        init();
//        finish();
    }

    private void init() {
        Log.i(TAG,"init()");
        int tmpWidth  = sDeviceWidth / SCREEN_RESOLUTION_SCALE;
        int tmpHeight = sDeviceHeight / SCREEN_RESOLUTION_SCALE;
        tmpWidth &= ~7;
        tmpHeight &= ~7;
         peerConnectionParameters =
                new PeerConnectionClient.PeerConnectionParameters(true, false,
                        true, tmpWidth, tmpHeight, 0,
                        0, "VP8",
                        true,
                        true,
                        0,
                        "OPUS", false, false, false, false, false, false, false, false, null);
//        mWebRtcClient = new WebRtcClient(getApplicationContext(), this, pipRenderer, fullscreenRenderer, createScreenCapturer(), peerConnectionParameters);
             videoCapturer  = createScreenCapturer();
    }
    public void connect(){
        /**
         * 1. 创建socketio
         * 2. 监听socketio
         * 3. 连接socketio
         * 4. ice服务
         */
        mWebRtcClient = new WebRtcClient(getApplicationContext(), this, videoCapturer, peerConnectionParameters);
    }


    public void report(String info) {
        Log.e(TAG, info);
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onDestroy() {
        /*
        if (mWebRtcClient != null) {
            mWebRtcClient.onDestroy();
        }
        */
        super.onDestroy();
    }

    @Override
    public void onReady(String callId) {
        Log.i(TAG,"WebRtcClient start()");
        mWebRtcClient.start(STREAM_NAME_PREFIX);
    }

    @Override
    public void onCall(final String applicant) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
            }
        });
    }

    @Override
    public void onHandup() {

    }

    @Override
    public void onStatusChanged(final String newStatus) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(getApplicationContext(), newStatus, Toast.LENGTH_SHORT).show();
            }
        });
    }

}
