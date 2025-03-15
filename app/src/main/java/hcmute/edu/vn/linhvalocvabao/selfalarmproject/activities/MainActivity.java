package hcmute.edu.vn.linhvalocvabao.selfalarmproject.activities;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;
import android.content.IntentFilter;
import android.media.AudioManager;
import android.os.Handler;
import android.util.Log;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.Fragment;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationBarView;

import dagger.hilt.android.AndroidEntryPoint;

import java.util.ArrayList;
import java.util.List;

import hcmute.edu.vn.linhvalocvabao.selfalarmproject.R;
import hcmute.edu.vn.linhvalocvabao.selfalarmproject.services.BatteryOptimizationService;
import hcmute.edu.vn.linhvalocvabao.selfalarmproject.services.BlacklistService;
import hcmute.edu.vn.linhvalocvabao.selfalarmproject.utils.NotificationHelper;
import hcmute.edu.vn.linhvalocvabao.selfalarmproject.receivers.BatteryMonitorReceiver;
import hcmute.edu.vn.linhvalocvabao.selfalarmproject.receivers.HeadphoneReceiver;
import hcmute.edu.vn.linhvalocvabao.selfalarmproject.receivers.ScreenStateReceiver;

@AndroidEntryPoint
public class MainActivity extends AppCompatActivity implements
        BatteryMonitorReceiver.BatteryUpdateListener,
        ScreenStateReceiver.ScreenStateListener {

    private static final int REQUEST_CODE_PERMISSIONS = 1001;
    private static final int REQUEST_CODE_WRITE_SETTINGS = 1002;
    private static final String TAG = "MainActivity";
    private BottomNavigationView bottomNavigationView;
    private HeadphoneReceiver headphoneReceiver;
    private BatteryMonitorReceiver batteryMonitorReceiver;
    private ScreenStateReceiver screenStateReceiver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Thiết lập Toolbar
        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        bottomNavigationView = findViewById(R.id.bottom_navigation);

        // Mặc định load fragment Music Chart khi mở app
        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, new MusicChartFragment())
                    .commit();
            bottomNavigationView.setSelectedItemId(R.id.nav_music);
        }

        // Xử lý sự kiện chuyển fragment khi chọn item bottom navigation
        bottomNavigationView.setOnItemSelectedListener(new NavigationBarView.OnItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                Fragment selectedFragment = null;

                // Chọn fragment tương ứng với item được chọn
                int itemId = item.getItemId();
                if (itemId == R.id.nav_music) {
                    selectedFragment = new MusicChartFragment();
                } else if (itemId == R.id.nav_schedule) {
                    selectedFragment = new ScheduleFragment();
                } else if (itemId == R.id.nav_sms_call) {
                    selectedFragment = new SMSCallFragment();
                } else if (itemId == R.id.nav_battery) {
                    selectedFragment = new BatteryFragment();
                }

                // Thay thế fragment hiện tại
                if (selectedFragment != null) {
                    getSupportFragmentManager().beginTransaction()
                            .replace(R.id.fragment_container, selectedFragment)
                            .commit();
                    return true;
                }

                return false;
            }
        });

        // Tạo notification channels
        NotificationHelper.createNotificationChannels(this);

        // Khởi động các dịch vụ
        startServices();

        // Kiểm tra và yêu cầu các quyền cần thiết
        requestRequiredPermissions();

        // Register HeadphoneReceiver for ACTION_AUDIO_BECOMING_NOISY
        IntentFilter headphoneFilter = new IntentFilter(AudioManager.ACTION_AUDIO_BECOMING_NOISY);
        headphoneReceiver = new HeadphoneReceiver();
        registerReceiver(headphoneReceiver, headphoneFilter);

        // Register BatteryMonitorReceiver
        batteryMonitorReceiver = new BatteryMonitorReceiver();
        batteryMonitorReceiver.setBatteryUpdateListener(this);
        IntentFilter batteryFilter = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);
        registerReceiver(batteryMonitorReceiver, batteryFilter);

        // Register ScreenStateReceiver
        screenStateReceiver = new ScreenStateReceiver();
        screenStateReceiver.setScreenStateListener(this);
        IntentFilter screenFilter = new IntentFilter();
        screenFilter.addAction(Intent.ACTION_SCREEN_ON);
        screenFilter.addAction(Intent.ACTION_SCREEN_OFF);
        screenFilter.addAction(Intent.ACTION_USER_PRESENT);
        registerReceiver(screenStateReceiver, screenFilter);

        // Check if we should open the music player directly
        if (savedInstanceState == null) {
            handleIntent(getIntent());
        }
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);
        handleIntent(intent);
    }

    private void handleIntent(Intent intent) {
        Fragment currentFragment = getSupportFragmentManager().findFragmentById(R.id.fragment_container);

        if (intent != null && intent.hasExtra("OPEN_MUSIC_PLAYER")
                && !(currentFragment instanceof MusicPlayerFragment)) {
            // Delay showing music player slightly to ensure activity is fully initialized
            new Handler().postDelayed(this::showMusicPlayer, 100);
        }
    }

    public void showMusicPlayer() {
        // Hide bottom navigation before transaction for smoother transition
        if (bottomNavigationView != null) {
            bottomNavigationView.setVisibility(View.GONE);
        }

        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }

        getSupportFragmentManager().beginTransaction()
                .setCustomAnimations(
                        R.anim.slide_in_up,
                        R.anim.slide_out_down,
                        R.anim.slide_in_down,
                        R.anim.slide_out_up
                )
                .replace(R.id.fragment_container, new MusicPlayerFragment())
                .addToBackStack(null)
                .commit();
    }

    @Override
    public void onBackPressed() {
        // When back is pressed, check if we need to restore bottom navigation
        if (getSupportFragmentManager().getBackStackEntryCount() > 0) {
            // Will be going back to a previous fragment
            Fragment currentFragment = getSupportFragmentManager().findFragmentById(R.id.fragment_container);
            if (currentFragment instanceof MusicPlayerFragment) {
                // Coming back from Music Player, ensure bottom nav is visible
                if (bottomNavigationView != null) {
                    bottomNavigationView.setVisibility(View.VISIBLE);
                }
            }
        }
        super.onBackPressed();
    }

    private void startServices() {
        // Khởi động BlacklistService - sử dụng service thông thường thay vì foreground service
        Intent blacklistIntent = new Intent(this, BlacklistService.class);
        startService(blacklistIntent);

        // Khởi động BatteryOptimizationService
        Intent batteryIntent = new Intent(this, BatteryOptimizationService.class);
        batteryIntent.setAction(BatteryOptimizationService.ACTION_START_MONITORING);

        // Sử dụng startForegroundService cho BatteryOptimizationService
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForegroundService(batteryIntent);
        } else {
            startService(batteryIntent);
        }
    }

    private void requestRequiredPermissions() {
        List<String> permissionsToRequest = new ArrayList<>();

        // Permissions for SMS/Call features
        String[] smsCallPermissions = new String[]{
                Manifest.permission.READ_SMS,
                Manifest.permission.RECEIVE_SMS,
                Manifest.permission.SEND_SMS,
                Manifest.permission.READ_PHONE_STATE,
                Manifest.permission.READ_CALL_LOG,
                Manifest.permission.CALL_PHONE,
                Manifest.permission.PROCESS_OUTGOING_CALLS,
                Manifest.permission.READ_CONTACTS
        };

        // Permissions for Battery features
        String[] batteryPermissions = new String[]{
                Manifest.permission.BATTERY_STATS,
                Manifest.permission.CHANGE_WIFI_STATE,
                Manifest.permission.ACCESS_WIFI_STATE,
                Manifest.permission.RECEIVE_BOOT_COMPLETED,
                Manifest.permission.WAKE_LOCK,
                Manifest.permission.FOREGROUND_SERVICE,
                Manifest.permission.READ_SYNC_SETTINGS,
                Manifest.permission.WRITE_SYNC_SETTINGS
        };

        // Check for SMS/Call permissions
        for (String permission : smsCallPermissions) {
            if (ContextCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED) {
                permissionsToRequest.add(permission);
            }
        }

        // Check for Battery permissions
        for (String permission : batteryPermissions) {
            if (ContextCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED) {
                permissionsToRequest.add(permission);
            }
        }

        // Request permissions if needed
        if (!permissionsToRequest.isEmpty()) {
            ActivityCompat.requestPermissions(this,
                    permissionsToRequest.toArray(new String[0]),
                    REQUEST_CODE_PERMISSIONS);
        }

        // For WRITE_SETTINGS permission (needed for brightness control)
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
//            if (!Settings.System.canWrite(this)) {
//                Intent intent = new Intent(Settings.ACTION_MANAGE_WRITE_SETTINGS);
//                startActivityForResult(intent, REQUEST_CODE_WRITE_SETTINGS);
//                Toast.makeText(this, "Please allow modify system settings for brightness control",
//                        Toast.LENGTH_LONG).show();
//            }
//        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == REQUEST_CODE_PERMISSIONS) {
            boolean allGranted = true;

            for (int result : grantResults) {
                if (result != PackageManager.PERMISSION_GRANTED) {
                    allGranted = false;
                    break;
                }
            }

            if (!allGranted) {
                Toast.makeText(this, "Some permissions were denied. Some features may not work properly.",
                        Toast.LENGTH_LONG).show();
            } else {
                // Restart services with permissions granted
                startServices();
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQUEST_CODE_WRITE_SETTINGS) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                if (Settings.System.canWrite(this)) {
                    Toast.makeText(this, "Write settings permission granted", Toast.LENGTH_SHORT).show();
                    // Restart service with permission granted
                    startServices();
                } else {
                    Toast.makeText(this, "Write settings permission denied. Some battery features may not work.",
                            Toast.LENGTH_LONG).show();
                }
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.toolbar_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int itemId = item.getItemId();

        if (itemId == R.id.action_blacklist) {
            // Mở BlacklistFragment
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, new BlacklistFragment())
                    .addToBackStack(null)
                    .commit();
            return true;
        } else if (itemId == R.id.action_settings) {
            // Xử lý mở trang cài đặt (nếu có)
            Toast.makeText(this, "Cài đặt", Toast.LENGTH_SHORT).show();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        // Unregister all receivers
        if (headphoneReceiver != null) {
            try {
                unregisterReceiver(headphoneReceiver);
            } catch (IllegalArgumentException e) {
                Log.e(TAG, "Error unregistering headphone receiver", e);
            }
        }

        if (batteryMonitorReceiver != null) {
            try {
                unregisterReceiver(batteryMonitorReceiver);
            } catch (IllegalArgumentException e) {
                Log.e(TAG, "Error unregistering battery receiver", e);
            }
        }

        if (screenStateReceiver != null) {
            try {
                unregisterReceiver(screenStateReceiver);
            } catch (IllegalArgumentException e) {
                Log.e(TAG, "Error unregistering screen state receiver", e);
            }
        }
    }

    // BatteryUpdateListener implementation
    @Override
    public void onBatteryLevelChanged(int level, boolean isCharging) {
        Log.d(TAG, "Battery level changed: " + level + "%, Charging: " + isCharging);

        // Update any UI components that show battery status
        // This is especially useful for the BatteryFragment if it's currently visible

        // Apply battery optimizations based on level
        if (level <= 15 && !isCharging) {
            // Critical battery level - consider implementing aggressive power saving
            Toast.makeText(this, "Battery level critical: " + level + "%", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onChargingStateChanged(boolean isCharging) {
        Log.d(TAG, "Charging state changed: " + (isCharging ? "Connected" : "Disconnected"));

        // Adjust app behavior based on charging state
        if (isCharging) {
            // Device is charging - can use more resources if needed
        } else {
            // Device disconnected - consider power saving
        }
    }

    // ScreenStateListener implementation
    @Override
    public void onScreenOn() {
        Log.d(TAG, "Screen turned ON - restoring normal operation");
        // Resume normal operation, increase refresh rates, etc.
    }

    @Override
    public void onScreenOff() {
        Log.d(TAG, "Screen turned OFF - optimizing resource usage");
        // Reduce resource usage, decrease refresh rates, pause animations, etc.
    }

    @Override
    public void onUserPresent() {
        Log.d(TAG, "User present - device unlocked");
        // Refresh UI or perform actions needed when user is actively using the device
    }
}