package ca.mohawk.medichelper.utils;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;

public class NetworkChangeReceiver extends BroadcastReceiver {
    private final NetworkStatusListener listener;

    public NetworkChangeReceiver(NetworkStatusListener listener) {
        this.listener = listener;
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        if (ConnectivityManager.CONNECTIVITY_ACTION.equals(intent.getAction())) {
            boolean isConnected = NetworkUtils.isInternetAvailable(context);
            listener.onNetworkStatusChanged(isConnected);
        }
    }

    public interface NetworkStatusListener {
        void onNetworkStatusChanged(boolean isConnected);
    }
}
