package hcmute.edu.vn.linhvalocvabao.selfalarmproject.activities;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

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

import java.util.ArrayList;
import java.util.List;

import hcmute.edu.vn.linhvalocvabao.selfalarmproject.R;
import hcmute.edu.vn.linhvalocvabao.selfalarmproject.services.BatteryOptimizationService;
import hcmute.edu.vn.linhvalocvabao.selfalarmproject.services.BlacklistService;
import hcmute.edu.vn.linhvalocvabao.selfalarmproject.utils.NotificationHelper;

public class MainActivity extends AppCompatActivity {

    private static final int REQUEST_CODE_PERMISSIONS = 1001;
    private static final int REQUEST_CODE_WRITE_SETTINGS = 1002;
    private BottomNavigationView bottomNavigationView;

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

        // Mặc định load fragment Music khi mở app
        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, new MusicPlayerFragment())
                    .commit();
        }

        // Xử lý sự kiện chuyển fragment khi chọn item bottom navigation
        bottomNavigationView.setOnItemSelectedListener(new NavigationBarView.OnItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                Fragment selectedFragment = null;

                // Chọn fragment tương ứng với item được chọn
                int itemId = item.getItemId();
                if (itemId == R.id.nav_music) {
                    selectedFragment = new MusicPlayerFragment();
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
        String[] smsCallPermissions = new String[] {
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
        String[] batteryPermissions = new String[] {
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
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (!Settings.System.canWrite(this)) {
                Intent intent = new Intent(Settings.ACTION_MANAGE_WRITE_SETTINGS);
                startActivityForResult(intent, REQUEST_CODE_WRITE_SETTINGS);
                Toast.makeText(this, "Please allow modify system settings for brightness control",
                        Toast.LENGTH_LONG).show();
            }
        }
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
        }
        else if (itemId == R.id.action_settings) {
            // Xử lý mở trang cài đặt (nếu có)
            Toast.makeText(this, "Cài đặt", Toast.LENGTH_SHORT).show();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}