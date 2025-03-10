package hcmute.edu.vn.linhvalocvabao.selfalarmproject.activities;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.BatteryManager;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.android.material.card.MaterialCardView;
import com.google.android.material.switchmaterial.SwitchMaterial;

import hcmute.edu.vn.linhvalocvabao.selfalarmproject.R;
import hcmute.edu.vn.linhvalocvabao.selfalarmproject.services.BatteryOptimizationService;
import hcmute.edu.vn.linhvalocvabao.selfalarmproject.utils.BatteryUtils;

public class BatteryFragment extends Fragment {
    private static final String TAG = "BatteryFragment";
    private static final String PREFS_NAME = "battery_optimization_prefs";
    private static final String KEY_AUTO_BRIGHTNESS = "auto_brightness";
    private static final String KEY_WIFI_OPTIMIZATION = "wifi_optimization";
    private static final String KEY_BACKGROUND_SYNC = "background_sync";

    private TextView batteryLevelTextView;
    private TextView chargingStatusTextView;
    private TextView temperatureTextView;
    private TextView batteryHealthTextView;
    private SwitchMaterial autoBrightnessSwitch;
    private SwitchMaterial wifiOptimizationSwitch;
    private SwitchMaterial backgroundSyncSwitch;
    private MaterialCardView statusCard;
    private MaterialCardView settingsCard;

    private BroadcastReceiver batteryUpdateReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            updateBatteryInfo(intent);
        }
    };

    public BatteryFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_battery, container, false);

        // Find views
        batteryLevelTextView = view.findViewById(R.id.battery_level);
        chargingStatusTextView = view.findViewById(R.id.charging_status);
        temperatureTextView = view.findViewById(R.id.battery_temperature);
        batteryHealthTextView = view.findViewById(R.id.battery_health);
        autoBrightnessSwitch = view.findViewById(R.id.auto_brightness);
        wifiOptimizationSwitch = view.findViewById(R.id.wifi_optimization);
        backgroundSyncSwitch = view.findViewById(R.id.background_sync);
        statusCard = view.findViewById(R.id.status_card);
        settingsCard = view.findViewById(R.id.settings_card);

        // Set up initial battery info
        updateBatteryInfoFromSystem();

        // Load saved preferences
        loadPreferences();

        // Set up switch listeners
        setupSwitchListeners();

        // Start the battery optimization service
        startBatteryOptimizationService();

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        // Register receiver for battery updates
        IntentFilter filter = new IntentFilter("hcmute.edu.vn.linhvalocvabao.selfalarmproject.BATTERY_UPDATE");
        requireActivity().registerReceiver(batteryUpdateReceiver, filter, Context.RECEIVER_NOT_EXPORTED);

        // Update battery info
        updateBatteryInfoFromSystem();
    }

    @Override
    public void onPause() {
        super.onPause();
        // Unregister receiver
        try {
            requireActivity().unregisterReceiver(batteryUpdateReceiver);
        } catch (Exception e) {
            // Ignore if not registered
        }
    }

    private void updateBatteryInfoFromSystem() {
        // Get battery info directly from system
        int batteryLevel = BatteryUtils.getBatteryLevel(requireContext());
        String chargingStatus = BatteryUtils.getChargingStatus(requireContext());
        float temperature = BatteryUtils.getBatteryTemperature(requireContext());

        // Update UI
        batteryLevelTextView.setText(batteryLevel + "%");
        chargingStatusTextView.setText("Status: " + chargingStatus);
        temperatureTextView.setText(String.format("Temperature: %.1f°C", temperature));

        // Change card color based on battery level
        int cardColor;
        if (batteryLevel <= 15) {
            cardColor = getResources().getColor(R.color.battery_critical, null);
        } else if (batteryLevel <= 30) {
            cardColor = getResources().getColor(R.color.battery_low, null);
        } else {
            cardColor = getResources().getColor(R.color.battery_normal, null);
        }
        statusCard.setCardBackgroundColor(cardColor);
    }

    private void updateBatteryInfo(Intent intent) {
        if (intent != null) {
            int level = intent.getIntExtra("level", -1);
            String status = intent.getStringExtra("status");
            float temperature = intent.getFloatExtra("temperature", 0f);

            if (level != -1) {
                batteryLevelTextView.setText(level + "%");
            }

            if (status != null) {
                chargingStatusTextView.setText("Status: " + status);
            }

            temperatureTextView.setText(String.format("Temperature: %.1f°C", temperature));

            // Change card color based on battery level
            int cardColor;
            if (level <= 15) {
                cardColor = getResources().getColor(R.color.battery_critical, null);
            } else if (level <= 30) {
                cardColor = getResources().getColor(R.color.battery_low, null);
            } else {
                cardColor = getResources().getColor(R.color.battery_normal, null);
            }
            statusCard.setCardBackgroundColor(cardColor);
        }
    }

    private void loadPreferences() {
        SharedPreferences prefs = requireActivity().getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);

        // Load switch states
        autoBrightnessSwitch.setChecked(prefs.getBoolean(KEY_AUTO_BRIGHTNESS, true));
        wifiOptimizationSwitch.setChecked(prefs.getBoolean(KEY_WIFI_OPTIMIZATION, true));
        backgroundSyncSwitch.setChecked(prefs.getBoolean(KEY_BACKGROUND_SYNC, true));
    }

    private void setupSwitchListeners() {
        // Auto-brightness switch
        autoBrightnessSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            savePreference(KEY_AUTO_BRIGHTNESS, isChecked);
            updateBatteryOptimizationService();
        });

        // WiFi optimization switch
        wifiOptimizationSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            savePreference(KEY_WIFI_OPTIMIZATION, isChecked);
            updateBatteryOptimizationService();
        });

        // Background sync switch
        backgroundSyncSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            savePreference(KEY_BACKGROUND_SYNC, isChecked);
            updateBatteryOptimizationService();
        });
    }

    private void savePreference(String key, boolean value) {
        SharedPreferences prefs = requireActivity().getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putBoolean(key, value);
        editor.apply();
    }

    private void startBatteryOptimizationService() {
        Intent serviceIntent = new Intent(requireContext(), BatteryOptimizationService.class);
        serviceIntent.setAction(BatteryOptimizationService.ACTION_START_MONITORING);
        requireActivity().startService(serviceIntent);
    }

    private void updateBatteryOptimizationService() {
        // Get current settings
        SharedPreferences prefs = requireActivity().getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        boolean autoBrightness = prefs.getBoolean(KEY_AUTO_BRIGHTNESS, true);
        boolean wifiOptimization = prefs.getBoolean(KEY_WIFI_OPTIMIZATION, true);
        boolean backgroundSync = prefs.getBoolean(KEY_BACKGROUND_SYNC, true);

        // Send updated settings to service
        Intent serviceIntent = new Intent(requireContext(), BatteryOptimizationService.class);
        serviceIntent.setAction(BatteryOptimizationService.ACTION_UPDATE_SETTINGS);
        serviceIntent.putExtra(KEY_AUTO_BRIGHTNESS, autoBrightness);
        serviceIntent.putExtra(KEY_WIFI_OPTIMIZATION, wifiOptimization);
        serviceIntent.putExtra(KEY_BACKGROUND_SYNC, backgroundSync);
        requireActivity().startService(serviceIntent);
    }
}