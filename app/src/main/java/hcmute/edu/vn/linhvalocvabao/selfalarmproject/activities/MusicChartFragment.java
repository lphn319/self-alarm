package hcmute.edu.vn.linhvalocvabao.selfalarmproject.activities;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import dagger.hilt.android.AndroidEntryPoint;
import hcmute.edu.vn.linhvalocvabao.selfalarmproject.R;
import hcmute.edu.vn.linhvalocvabao.selfalarmproject.adapters.ChartAdapter;
import hcmute.edu.vn.linhvalocvabao.selfalarmproject.data.api.ZingMp3Api;
import hcmute.edu.vn.linhvalocvabao.selfalarmproject.data.model.Music;
import hcmute.edu.vn.linhvalocvabao.selfalarmproject.utils.NetworkUtils;

@AndroidEntryPoint
public class MusicChartFragment extends Fragment {

    @Inject
    ZingMp3Api zingMp3Api;
    
    @Inject
    NetworkUtils networkUtils;
    
    private RecyclerView recyclerViewCharts;
    private ProgressBar progressBar;
    private TextView tvEmptyState;
    private SwipeRefreshLayout swipeRefreshLayout;
    private ChartAdapter adapter;

    public MusicChartFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_music_chart, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        
        // Initialize views
        recyclerViewCharts = view.findViewById(R.id.recyclerViewCharts);
        progressBar = view.findViewById(R.id.progressBar);
        tvEmptyState = view.findViewById(R.id.tvEmptyState);
        swipeRefreshLayout = view.findViewById(R.id.swipeRefreshLayout);
        
        // Set up RecyclerView
        recyclerViewCharts.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new ChartAdapter(new ArrayList<>(), requireContext(), new ChartAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(Music music, int position) {
                // Handle song click
                playMusic(music);
            }

            @Override
            public void onMoreClick(Music music, int position) {
                // Handle more options click
                showMoreOptions(music);
            }
        });
        recyclerViewCharts.setAdapter(adapter);
        
        // Set up SwipeRefreshLayout
        swipeRefreshLayout.setOnRefreshListener(this::loadChartData);
        swipeRefreshLayout.setColorSchemeResources(
                android.R.color.holo_blue_bright,
                android.R.color.holo_green_light,
                android.R.color.holo_orange_light,
                android.R.color.holo_red_light
        );
        
        // Load data
        loadChartData();
    }
    
    private void loadChartData() {
        // Check network connection
        if (!networkUtils.isOnline()) {
            showError("No internet connection available");
            swipeRefreshLayout.setRefreshing(false);
            return;
        }
        
        // Show loading state
        if (!swipeRefreshLayout.isRefreshing()) {
            progressBar.setVisibility(View.VISIBLE);
        }
        tvEmptyState.setVisibility(View.GONE);
        
        // Call API to get chart data
        zingMp3Api.getTrendingSongs().observe(getViewLifecycleOwner(), musicList -> {
            // Hide loading indicators
            progressBar.setVisibility(View.GONE);
            swipeRefreshLayout.setRefreshing(false);
            
            if (musicList != null && !musicList.isEmpty()) {
                // Display data
                adapter.updateData(musicList);
                tvEmptyState.setVisibility(View.GONE);
            } else {
                // Show empty state
                tvEmptyState.setVisibility(View.VISIBLE);
                adapter.updateData(new ArrayList<>());
            }
        });
    }
    
    private void showError(String message) {
        if (getContext() != null) {
            Toast.makeText(getContext(), message, Toast.LENGTH_SHORT).show();
        }
        progressBar.setVisibility(View.GONE);
        swipeRefreshLayout.setRefreshing(false);
    }
    
    private void playMusic(Music music) {
        if (music != null && music.getId() != null) {
            Toast.makeText(requireContext(), "Playing: " + music.getTitle(), Toast.LENGTH_SHORT).show();
            // TODO: Implement actual playback using a music service
        }
    }
    
    private void showMoreOptions(Music music) {
        if (music != null) {
            Toast.makeText(requireContext(), "Options for: " + music.getTitle(), Toast.LENGTH_SHORT).show();
            // TODO: Show a bottom sheet with more options (add to favorites, share, etc.)
        }
    }
}