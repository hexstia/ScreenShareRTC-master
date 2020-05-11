//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package org.webrtc;

import android.content.Context;
import android.os.Build.VERSION;

import org.webrtc.NetworkMonitorAutoDetect.ConnectionType;
import org.webrtc.NetworkMonitorAutoDetect.NetworkInformation;
import org.webrtc.NetworkMonitorAutoDetect.NetworkState;
import org.webrtc.NetworkMonitorAutoDetect.Observer;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class NetworkMonitor {
    private static final String TAG = "NetworkMonitor";
    private static NetworkMonitor instance;
    private final ArrayList<Long> nativeNetworkObservers;
    private final ArrayList<NetworkObserver> networkObservers;
    private NetworkMonitorAutoDetect autoDetector;
    private ConnectionType currentConnectionType;

    private NetworkMonitor() {
        this.currentConnectionType = ConnectionType.CONNECTION_UNKNOWN;
        this.nativeNetworkObservers = new ArrayList();
        this.networkObservers = new ArrayList();
    }

    /** @deprecated */
    @Deprecated
    public static void init(Context context) {
    }

    public static NetworkMonitor getInstance() {
        if (instance == null) {
            instance = new NetworkMonitor();
        }

        return instance;
    }

    public static void setAutoDetectConnectivityState(boolean shouldAutoDetect) {
        getInstance().setAutoDetectConnectivityStateInternal(shouldAutoDetect);
    }

    private static void assertIsTrue(boolean condition) {
        if (!condition) {
            throw new AssertionError("Expected to be true");
        }
    }

    private void startMonitoring(long nativeObserver) {
        Logging.d("NetworkMonitor", "Start monitoring from native observer " + nativeObserver);
        this.nativeNetworkObservers.add(nativeObserver);
        this.setAutoDetectConnectivityStateInternal(true);
    }

    private void stopMonitoring(long nativeObserver) {
        Logging.d("NetworkMonitor", "Stop monitoring from native observer " + nativeObserver);
        this.setAutoDetectConnectivityStateInternal(false);
        this.nativeNetworkObservers.remove(nativeObserver);
    }

    private boolean networkBindingSupported() {
        return this.autoDetector != null && this.autoDetector.supportNetworkCallback();
    }

    private static int androidSdkInt() {
        return VERSION.SDK_INT;
    }

    private ConnectionType getCurrentConnectionType() {
        return this.currentConnectionType;
    }

    private long getCurrentDefaultNetId() {
        return this.autoDetector == null ? -1L : this.autoDetector.getDefaultNetId();
    }

    private void destroyAutoDetector() {
        if (this.autoDetector != null) {
            this.autoDetector.destroy();
            this.autoDetector = null;
        }

    }

    private void setAutoDetectConnectivityStateInternal(boolean shouldAutoDetect) {
        if (!shouldAutoDetect) {
            this.destroyAutoDetector();
        } else {
            if (this.autoDetector == null) {
                this.autoDetector = new NetworkMonitorAutoDetect(new Observer() {
                    public void onConnectionTypeChanged(ConnectionType newConnectionType) {
                        NetworkMonitor.this.updateCurrentConnectionType(newConnectionType);
                    }

                    public void onNetworkConnect(NetworkInformation networkInfo) {
                        NetworkMonitor.this.notifyObserversOfNetworkConnect(networkInfo);
                    }

                    public void onNetworkDisconnect(long networkHandle) {
                        NetworkMonitor.this.notifyObserversOfNetworkDisconnect(networkHandle);
                    }
                }, ContextUtils.getApplicationContext());
                NetworkState networkState = this.autoDetector.getCurrentNetworkState();
                this.updateCurrentConnectionType(NetworkMonitorAutoDetect.getConnectionType(networkState));
                this.updateActiveNetworkList();
            }

        }
    }

    private void updateCurrentConnectionType(ConnectionType newConnectionType) {
        this.currentConnectionType = newConnectionType;
        this.notifyObserversOfConnectionTypeChange(newConnectionType);
    }

    private void notifyObserversOfConnectionTypeChange(ConnectionType newConnectionType) {
        Iterator var2 = this.nativeNetworkObservers.iterator();

        while(var2.hasNext()) {
            long nativeObserver = (Long)var2.next();
            this.nativeNotifyConnectionTypeChanged(nativeObserver);
        }

        var2 = this.networkObservers.iterator();

        while(var2.hasNext()) {
            NetworkMonitor.NetworkObserver observer = (NetworkMonitor.NetworkObserver)var2.next();
            observer.onConnectionTypeChanged(newConnectionType);
        }

    }

    private void notifyObserversOfNetworkConnect(NetworkInformation networkInfo) {
        Iterator var2 = this.nativeNetworkObservers.iterator();

        while(var2.hasNext()) {
            long nativeObserver = (Long)var2.next();
            this.nativeNotifyOfNetworkConnect(nativeObserver, networkInfo);
        }

    }

    private void notifyObserversOfNetworkDisconnect(long networkHandle) {
        Iterator var3 = this.nativeNetworkObservers.iterator();

        while(var3.hasNext()) {
            long nativeObserver = (Long)var3.next();
            this.nativeNotifyOfNetworkDisconnect(nativeObserver, networkHandle);
        }

    }

    private void updateActiveNetworkList() {
        List<NetworkInformation> networkInfoList = this.autoDetector.getActiveNetworkList();
        if (networkInfoList != null && networkInfoList.size() != 0) {
            NetworkInformation[] networkInfos = new NetworkInformation[networkInfoList.size()];
            networkInfos = (NetworkInformation[])networkInfoList.toArray(networkInfos);
            Iterator var3 = this.nativeNetworkObservers.iterator();

            while(var3.hasNext()) {
                long nativeObserver = (Long)var3.next();
                this.nativeNotifyOfActiveNetworkList(nativeObserver, networkInfos);
            }

        }
    }

    public static void addNetworkObserver(NetworkMonitor.NetworkObserver observer) {
        getInstance().addNetworkObserverInternal(observer);
    }

    private void addNetworkObserverInternal(NetworkMonitor.NetworkObserver observer) {
        this.networkObservers.add(observer);
    }

    public static void removeNetworkObserver(NetworkMonitor.NetworkObserver observer) {
        getInstance().removeNetworkObserverInternal(observer);
    }

    private void removeNetworkObserverInternal(NetworkMonitor.NetworkObserver observer) {
        this.networkObservers.remove(observer);
    }

    public static boolean isOnline() {
        ConnectionType connectionType = getInstance().getCurrentConnectionType();
        return connectionType != ConnectionType.CONNECTION_NONE;
    }

    private native void nativeNotifyConnectionTypeChanged(long var1);

    private native void nativeNotifyOfNetworkConnect(long var1, NetworkInformation var3);

    private native void nativeNotifyOfNetworkDisconnect(long var1, long var3);

    private native void nativeNotifyOfActiveNetworkList(long var1, NetworkInformation[] var3);

    static void resetInstanceForTests() {
        instance = new NetworkMonitor();
    }

    public static NetworkMonitorAutoDetect getAutoDetectorForTest() {
        return getInstance().autoDetector;
    }

    public interface NetworkObserver {
        void onConnectionTypeChanged(ConnectionType var1);
    }
}
