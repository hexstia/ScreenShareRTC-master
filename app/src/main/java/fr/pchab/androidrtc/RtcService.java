package fr.pchab.androidrtc;

import android.app.Service;
import android.content.Intent;
import android.os.Bundle;
import android.os.IBinder;
import android.util.DisplayMetrics;
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
        DisplayMetrics metrics = new DisplayMetrics();
        Log.i("TAG","webrtc :"+webrtc);
          if(webrtc.equals("start")){
              int displaywidth = intent.getIntExtra("displaywidth",metrics.widthPixels);
              int displayheight =intent.getIntExtra("displayheight",metrics.heightPixels);
              int contentwidth = intent.getIntExtra("contentwidth",metrics.widthPixels);
              int contentheight =intent.getIntExtra("contentheight",metrics.heightPixels);
              int bufferWidth = intent.getIntExtra("bufferwidth",metrics.widthPixels);
              int bufferHeight =intent.getIntExtra("bufferheight",metrics.heightPixels);
              int density =intent.getIntExtra("density",240);
              String nameId = intent.getStringExtra("nameid");
              String host = intent.getStringExtra("host");
              Log.i("TAG","displaywidth :"+displaywidth);
              Log.i("TAG","displayheight :"+displayheight);
              Log.i("TAG","contentwidth :"+contentwidth);
              Log.i("TAG","contentheight :"+contentheight);
              Log.i("TAG","bufferwidth :"+bufferWidth);
              Log.i("TAG","bufferheight :"+bufferHeight);
              Log.i("TAG","host :"+host);
              Log.i("TAG","density :"+density);
              Log.i("TAG","nameid :"+nameId);
              Intent i = new Intent(this,RtcActivity.class);
              Bundle bundle = new Bundle();
              bundle.putInt("displaywidth",displaywidth);
              bundle.putInt("displayheight",displayheight);
              bundle.putInt("contentwidth",contentwidth);
              bundle.putInt("contentheight",contentheight);
              bundle.putInt("bufferwidth",bufferWidth);
              bundle.putInt("bufferheight",bufferHeight);
              bundle.putInt("density",density);
              bundle.putString("host",host);
              bundle.putString("nameid",nameId);
              i.putExtras(bundle);
              startActivity(i);
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
