package fr.pchab.androidrtc;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

public class RtcService extends Service {
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();

    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
          String webrtc =  intent.getStringExtra("webrtc");
        Log.i("TAG","webrtc :"+webrtc);
          if(webrtc.equals("start")){
              startActivity(new Intent(this,RtcActivity.class));
          }else if(webrtc.equals("close")){
            RtcActivity.getThiz().getmWebRtcClient().onDestroy();
          }

        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

}
