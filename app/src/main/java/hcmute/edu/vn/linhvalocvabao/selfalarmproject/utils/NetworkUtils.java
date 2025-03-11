package hcmute.edu.vn.linhvalocvabao.selfalarmproject.utils;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.net.NetworkRequest;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import javax.inject.Inject;
import javax.inject.Singleton;

/**
 * Utility class for network operations and connectivity status
 * 
 * Created: 2025-03-10
 * @author lochuungcontinue
 */
@Singleton
public class NetworkUtils {
    private final Context context;
    private final MutableLiveData<Boolean> networkStatusLiveData = new MutableLiveData<>();
    private final ConnectivityManager connectivityManager;
    
    @Inject
    public NetworkUtils(Context context) {
        this.context = context;
        this.connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        
        // Register network callback
        NetworkRequest networkRequest = new NetworkRequest.Builder()
                .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
                .build();
                
        connectivityManager.registerNetworkCallback(networkRequest, 
                new ConnectivityManager.NetworkCallback() {
                    @Override
                    public void onAvailable(@NonNull Network network) {
                        super.onAvailable(network);
                        networkStatusLiveData.postValue(true);
                    }

                    @Override
                    public void onLost(@NonNull Network network) {
                        super.onLost(network);
                        networkStatusLiveData.postValue(false);
                    }
                });
                
        // Set initial value
        networkStatusLiveData.setValue(isOnline());
    }
    
    /**
     * Check if device is currently connected to the internet
     */
    public boolean isOnline() {
        if (connectivityManager != null) {
            Network network = connectivityManager.getActiveNetwork();
            if (network != null) {
                NetworkCapabilities capabilities = connectivityManager.getNetworkCapabilities(network);
                return capabilities != null && 
                       (capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) ||
                        capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) ||
                        capabilities.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET));
            }
        }
        return false;
    }
    
    /**
     * Get network status as LiveData
     */
    public LiveData<Boolean> getNetworkStatus() {
        return networkStatusLiveData;
    }
}