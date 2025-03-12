package hcmute.edu.vn.linhvalocvabao.selfalarmproject.activities;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import dagger.hilt.android.AndroidEntryPoint;
import hcmute.edu.vn.linhvalocvabao.selfalarmproject.R;
import hcmute.edu.vn.linhvalocvabao.selfalarmproject.data.model.Music;
import hcmute.edu.vn.linhvalocvabao.selfalarmproject.services.MusicPlaybackService.PlaybackState;
import hcmute.edu.vn.linhvalocvabao.selfalarmproject.viewmodels.MusicPlayerViewModel;

@AndroidEntryPoint
public class MusicPlayerFragment extends Fragment {
    private static final String TAG = "MusicPlayerFragment";
    
    private ImageView ivAlbumArt;
    private TextView tvTitle;
    private TextView tvArtist;
    private SeekBar seekBar;
    private TextView tvCurrentTime;
    private TextView tvTotalDuration;
    private ImageButton btnShuffle;
    private ImageButton btnPrevious;
    private FloatingActionButton btnPlayPause;
    private ImageButton btnNext;
    private ImageButton btnRepeat;
    private ImageButton btnBack;
    private ImageButton btnOptions;
    
    private MusicPlayerViewModel viewModel;
    private boolean userIsSeeking = false;
    private boolean autoPlayOnLoad = true; // Flag to auto-play when music is loaded
    private View rootView;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.fragment_music_player, container, false);
        return rootView;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        
        // Hide bottom navigation when music player is displayed
        BottomNavigationView bottomNavigationView = requireActivity().findViewById(R.id.bottom_navigation);
        if (bottomNavigationView != null) {
            bottomNavigationView.setVisibility(View.GONE);
        }
        
        initViews(view);
        setupViewModel();
        setupListeners();
    }

    private void initViews(View view) {
        ivAlbumArt = view.findViewById(R.id.ivAlbumArt);
        tvTitle = view.findViewById(R.id.tvTitle);
        tvArtist = view.findViewById(R.id.tvArtist);
        seekBar = view.findViewById(R.id.seekBar);
        tvCurrentTime = view.findViewById(R.id.tvCurrentTime);
        tvTotalDuration = view.findViewById(R.id.tvTotalDuration);
        btnShuffle = view.findViewById(R.id.btnShuffle);
        btnPrevious = view.findViewById(R.id.btnPrevious);
        btnPlayPause = view.findViewById(R.id.btnPlayPause);
        btnNext = view.findViewById(R.id.btnNext);
        btnRepeat = view.findViewById(R.id.btnRepeat);
        btnBack = view.findViewById(R.id.btnBack);
        btnOptions = view.findViewById(R.id.btnOptions);
        
        // Set initial values
        tvCurrentTime.setText("0:00");
        tvTotalDuration.setText("0:00");
    }

    private void setupViewModel() {
        viewModel = new ViewModelProvider(requireActivity()).get(MusicPlayerViewModel.class);
        
        // Observe current music
        viewModel.getCurrentMusic().observe(getViewLifecycleOwner(), this::updateMusicInfo);
        
        // Observe playback progress
        viewModel.getCurrentProgress().observe(getViewLifecycleOwner(), this::updateProgress);
        
        // Observe duration
        viewModel.getDuration().observe(getViewLifecycleOwner(), this::updateDuration);
        
        // Observe playback state
        viewModel.getPlaybackState().observe(getViewLifecycleOwner(), state -> {
            updatePlaybackState(state);
            
            // Auto-play music when prepared
            if (state == PlaybackState.PAUSED && autoPlayOnLoad) {
                // Use a small delay to ensure UI is ready
                rootView.postDelayed(() -> {
                    Log.d(TAG, "Auto-playing music after state change to PAUSED");
                    viewModel.play();
                    autoPlayOnLoad = false;
                }, 300);
            }
        });
        
        // Observe errors
        viewModel.getErrorMessage().observe(getViewLifecycleOwner(), this::showError);
    }

    private void setupListeners() {
        // Play/Pause button
        btnPlayPause.setOnClickListener(v -> {
            Log.d(TAG, "Play/Pause button clicked");
            viewModel.playPause();
        });
        
        // Next button
        btnNext.setOnClickListener(v -> {
            Log.d(TAG, "Next button clicked");
            viewModel.skipToNext();
        });
        
        // Previous button
        btnPrevious.setOnClickListener(v -> {
            Log.d(TAG, "Previous button clicked");
            viewModel.skipToPrevious();
        });
        
        // Shuffle button
        btnShuffle.setOnClickListener(v -> {
            Toast.makeText(requireContext(), "Shuffle not implemented yet", Toast.LENGTH_SHORT).show();
        });
        
        // Repeat button
        btnRepeat.setOnClickListener(v -> {
            Toast.makeText(requireContext(), "Repeat not implemented yet", Toast.LENGTH_SHORT).show();
        });
        
        // Back button
        btnBack.setOnClickListener(v -> {
            // Show bottom navigation before going back
            BottomNavigationView bottomNavigationView = requireActivity().findViewById(R.id.bottom_navigation);
            if (bottomNavigationView != null) {
                bottomNavigationView.setVisibility(View.VISIBLE);
            }
            requireActivity().onBackPressed();
        });
        
        // Options button
        btnOptions.setOnClickListener(v -> {
            Toast.makeText(requireContext(), "Options not implemented yet", Toast.LENGTH_SHORT).show();
        });
        
        // Seekbar
        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (fromUser) {
                    tvCurrentTime.setText(formatTime(progress));
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                userIsSeeking = true;
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                userIsSeeking = false;
                viewModel.seekTo(seekBar.getProgress());
            }
        });
    }

    private void updateMusicInfo(Music music) {
        if (music != null) {
            Log.d(TAG, "Updating music info: " + music.getTitle());
            tvTitle.setText(music.getTitle());
            tvArtist.setText(music.getArtists());
            
            if (music.getThumbnailM() != null && !music.getThumbnailM().isEmpty()) {
                try {
                    Glide.with(requireContext())
                        .load(music.getThumbnailM())
                        .apply(new RequestOptions()
                            .placeholder(R.drawable.placeholder_album)
                            .error(R.drawable.placeholder_album))
                        .into(ivAlbumArt);
                } catch (Exception e) {
                    Log.e(TAG, "Error loading album art", e);
                    ivAlbumArt.setImageResource(R.drawable.placeholder_album);
                }
            } else {
                ivAlbumArt.setImageResource(R.drawable.placeholder_album);
            }
        }
    }

    private void updateProgress(Integer progress) {
        if (!userIsSeeking && progress != null) {
            try {
                seekBar.setProgress(progress);
                tvCurrentTime.setText(formatTime(progress));
            } catch (Exception e) {
                Log.e(TAG, "Error updating progress", e);
            }
        }
    }

    private void updateDuration(Integer duration) {
        if (duration != null && duration > 0) {
            Log.d(TAG, "Setting duration: " + duration);
            try {
                seekBar.setMax(duration);
                tvTotalDuration.setText(formatTime(duration));
            } catch (Exception e) {
                Log.e(TAG, "Error updating duration", e);
            }
        }
    }

    private void updatePlaybackState(PlaybackState state) {
        Log.d(TAG, "Playback state changed to: " + state);
        if (state == PlaybackState.PLAYING) {
            btnPlayPause.setImageResource(R.drawable.exo_icon_pause);
        } else {
            btnPlayPause.setImageResource(R.drawable.exo_icon_play);
        }
    }
    
    private void showError(String message) {
        if (message != null && !message.isEmpty()) {
            Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show();
        }
    }

    private String formatTime(long milliseconds) {
        long seconds = milliseconds / 1000;
        long minutes = seconds / 60;
        seconds = seconds % 60;
        return String.format("%d:%02d", minutes, seconds);
    }
    
    @Override
    public void onStart() {
        super.onStart();
        if (viewModel != null) {
            // Update UI with current state when fragment becomes visible
            PlaybackState currentState = viewModel.getPlaybackState().getValue();
            if (currentState != null) {
                updatePlaybackState(currentState);
            }
            
            // Get current music
            Music currentMusic = viewModel.getCurrentMusic().getValue();
            if (currentMusic != null) {
                updateMusicInfo(currentMusic);
            }
            
            // Get current progress and duration
            Integer progress = viewModel.getCurrentProgress().getValue();
            if (progress != null) {
                updateProgress(progress);
            }
            
            Integer duration = viewModel.getDuration().getValue();
            if (duration != null && duration > 0) {
                updateDuration(duration);
            }
        }
    }
    
    @Override
    public void onStop() {
        super.onStop();
        // Reset flags when fragment is stopped
        autoPlayOnLoad = true;
    }
    
    @Override
    public void onDestroyView() {
        super.onDestroyView();
        
        // Make sure bottom navigation is visible again when fragment is destroyed
        BottomNavigationView bottomNavigationView = requireActivity().findViewById(R.id.bottom_navigation);
        if (bottomNavigationView != null) {
            bottomNavigationView.setVisibility(View.VISIBLE);
        }

        ActionBar actionBar = ((MainActivity) requireActivity()).getSupportActionBar();
        if (actionBar != null) {
            actionBar.show();
        }
    }
}