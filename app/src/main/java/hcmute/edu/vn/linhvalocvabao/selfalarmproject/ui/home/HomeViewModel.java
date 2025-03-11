package hcmute.edu.vn.linhvalocvabao.selfalarmproject.ui.home;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.google.gson.JsonObject;

import java.util.List;

import javax.inject.Inject;

import dagger.hilt.android.lifecycle.HiltViewModel;
import hcmute.edu.vn.linhvalocvabao.selfalarmproject.data.model.Music;
import hcmute.edu.vn.linhvalocvabao.selfalarmproject.data.repository.MusicRepository;

/**
 * ViewModel for the Home screen
 * 
 * Last updated: 2025-03-10 09:30:23
 * @author lochuungcontinue
 */
@HiltViewModel
public class HomeViewModel extends ViewModel {
    
    private final MusicRepository repository;
    private final MutableLiveData<Boolean> isLoading = new MutableLiveData<>(false);
    private final MutableLiveData<String> error = new MutableLiveData<>();
    
    @Inject
    public HomeViewModel(MusicRepository repository) {
        this.repository = repository;
    }
    
    /**
     * Get loading state
     */
    public LiveData<Boolean> isLoading() {
        return isLoading;
    }
    
    /**
     * Get error state
     */
    public LiveData<String> getError() {
        return error;
    }
    
    /**
     * Get trending songs
     */
    public LiveData<List<Music>> getTrendingSongs() {
        isLoading.setValue(true);
        LiveData<List<Music>> result = repository.getTrendingSongs();
        isLoading.setValue(false);
        return result;
    }
    
    /**
     * Get new releases
     */
    public LiveData<List<Music>> getNewReleases() {
        isLoading.setValue(true);
        LiveData<List<Music>> result = repository.getNewReleases();
        isLoading.setValue(false);
        return result;
    }
    
    /**
     * Get home data sections
     */
    public LiveData<List<JsonObject>> getHomeData() {
        isLoading.setValue(true);
        // Implementation would delegate to repository
        isLoading.setValue(false);
        return new MutableLiveData<>(); // Placeholder
    }
}