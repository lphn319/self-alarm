package hcmute.edu.vn.linhvalocvabao.selfalarmproject.activities;

import android.os.Bundle;
import android.view.MenuItem;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.Fragment;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationBarView;

import dagger.hilt.android.AndroidEntryPoint;
import hcmute.edu.vn.linhvalocvabao.selfalarmproject.R;


@AndroidEntryPoint
public class MainActivity extends AppCompatActivity {

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
    }


}