package hcmute.edu.vn.linhvalocvabao.selfalarmproject.activities;

import android.os.Bundle;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.lifecycle.Observer;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import dagger.hilt.android.AndroidEntryPoint;
import hcmute.edu.vn.linhvalocvabao.selfalarmproject.R;
import hcmute.edu.vn.linhvalocvabao.selfalarmproject.adapters.MusicAdapter;
import hcmute.edu.vn.linhvalocvabao.selfalarmproject.data.api.ZingMp3Api;
import hcmute.edu.vn.linhvalocvabao.selfalarmproject.data.model.Music;
import hcmute.edu.vn.linhvalocvabao.selfalarmproject.utils.NetworkUtils;

@AndroidEntryPoint
public class TestActivity extends AppCompatActivity {
    
    @Inject
    ZingMp3Api zingMp3Api;
    
    @Inject
    NetworkUtils networkUtils;
    
    private RecyclerView recyclerView;
    private MusicAdapter adapter;
    private ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_test); // Changed from activity_main to activity_test
        
        // Initialize views
        try {
            recyclerView = findViewById(R.id.recyclerView);
            progressBar = findViewById(R.id.progressBar);
            
            ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
                Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
                v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
                return insets;
            });
            
            // Set up RecyclerView
            adapter = new MusicAdapter(new ArrayList<>(), this);
            recyclerView.setLayoutManager(new LinearLayoutManager(this));
            recyclerView.setAdapter(adapter);
            
            // Check if network is available
            if (!networkUtils.isOnline()) {
                Toast.makeText(this, "No internet connection available", Toast.LENGTH_LONG).show();
                return;
            }
            
            // Show progress while loading data
            progressBar.setVisibility(View.VISIBLE);
            
            // Fetch new release data
            zingMp3Api.getNewReleaseData().observe(this, new Observer<List<Music>>() {
                @Override
                public void onChanged(List<Music> musicList) {
                    progressBar.setVisibility(View.GONE);
                    
                    if (musicList != null && !musicList.isEmpty()) {
                        adapter.updateData(musicList);
                    } else {
                        Toast.makeText(TestActivity.this, "No data found or error occurred", Toast.LENGTH_SHORT).show();
                    }
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "Error initializing UI: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }
}