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
import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
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
import hcmute.edu.vn.linhvalocvabao.selfalarmproject.viewmodels.MusicPlayerViewModel;

@AndroidEntryPoint
public class MusicChartFragment extends Fragment {

    @Inject
    ZingMp3Api zingMp3Api;
    
    @Inject
    NetworkUtils networkUtils;
    
    private RecyclerView recyclerViewCharts;
    private ProgressBar progressBar;
    private TextView tvEmptyState;
    private CardView cardEmptyState;
    private SwipeRefreshLayout swipeRefreshLayout;
    private ChartAdapter adapter;
    private MusicPlayerViewModel musicPlayerViewModel;

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
        
        // Initialize ViewModel
        musicPlayerViewModel = new ViewModelProvider(requireActivity()).get(MusicPlayerViewModel.class);
        
        // Initialize views
        recyclerViewCharts = view.findViewById(R.id.recyclerViewCharts);
        progressBar = view.findViewById(R.id.progressBar);
        tvEmptyState = view.findViewById(R.id.tvEmptyState);
        cardEmptyState = view.findViewById(R.id.cardEmptyState);
        swipeRefreshLayout = view.findViewById(R.id.swipeRefreshLayout);
        
        // Set up RecyclerView
        recyclerViewCharts.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new ChartAdapter(new ArrayList<>(), requireContext(), new ChartAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(Music music, int position) {
                // Play the selected music
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
        
        // Observe ViewModel for errors
        musicPlayerViewModel.getErrorMessage().observe(getViewLifecycleOwner(), message -> {
            if (message != null && !message.isEmpty()) {
                Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show();
            }
        });
        
        // Load data
        loadChartData();
    }
    
    private void loadChartData() {
        // Check network connection
        if (!networkUtils.isOnline()) {
            showError("No internet connection available");
            swipeRefreshLayout.setRefreshing(false);
            showEmptyState(true, "No internet connection");
            return;
        }
        
        // Show loading state
        showLoadingState(true);
        
        // Call API to get chart data
        zingMp3Api.getTrendingSongs().observe(getViewLifecycleOwner(), musicList -> {
            // Hide loading indicators
            showLoadingState(false);
            
            if (musicList != null && !musicList.isEmpty()) {
                // Display data
                adapter.updateData(musicList);
                showEmptyState(false, null);
            } else {
                // Show empty state
                showEmptyState(true, "No songs found");
                adapter.updateData(new ArrayList<>());
            }
        });
    }
    
    private void showLoadingState(boolean isLoading) {
        if (!swipeRefreshLayout.isRefreshing()) {
            progressBar.setVisibility(isLoading ? View.VISIBLE : View.GONE);
        }
        if (!isLoading) {
            swipeRefreshLayout.setRefreshing(false);
        }
    }
    
    private void showEmptyState(boolean isEmpty, String message) {
        cardEmptyState.setVisibility(isEmpty ? View.VISIBLE : View.GONE);
        if (isEmpty && message != null) {
            tvEmptyState.setText(message);
        }
    }
    
    private void showError(String message) {
        if (getContext() != null) {
            Toast.makeText(getContext(), message, Toast.LENGTH_SHORT).show();
        }
        showLoadingState(false);
    }
    
    private void playMusic(Music music) {
        if (music != null && music.getId() != null) {
            // Use ViewModel to play the music
            musicPlayerViewModel.playMusic(music);
            Toast.makeText(requireContext(), "Playing: " + music.getTitle(), Toast.LENGTH_SHORT).show();
        }
    }
    
    private void playAllFromPosition(int position) {
        List<Music> songs = adapter.getMusicList();  // This will now work with our added method
        if (songs != null && !songs.isEmpty() && position >= 0 && position < songs.size()) {
            musicPlayerViewModel.playPlaylist(songs, position);
        }
    }
    
    private void showMoreOptions(Music music) {
        if (music != null) {
            // Create bottom sheet dialog with actions like:
            // - Add to Favorites
            // - Add to Playlist
            // - Share
            // For now, just show toast
            Toast.makeText(requireContext(), "Options for: " + music.getTitle(), Toast.LENGTH_SHORT).show();
        }
    }
}