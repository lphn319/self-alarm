package hcmute.edu.vn.linhvalocvabao.selfalarmproject.data.db;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;


import java.util.List;

import hcmute.edu.vn.linhvalocvabao.selfalarmproject.data.model.Playlist;

/**
 * Data Access Object for Playlist entities
 * 
 * Last updated: 2025-03-10 11:11:08
 * @author lochuungcontinue
 */
@Dao
public interface PlaylistDao {
    
    /**
     * Insert a playlist
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    long insert(Playlist playlist);
    
    /**
     * Insert multiple playlists
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    List<Long> insertAll(List<Playlist> playlists);
    
    /**
     * Update a playlist
     */
    @Update
    int update(Playlist playlist);
    
    /**
     * Delete a playlist
     */
    @Delete
    int delete(Playlist playlist);
    
    /**
     * Get playlist by ID
     */
    @Query("SELECT * FROM playlists WHERE id = :id")
    LiveData<Playlist> getPlaylistById(String id);
    
    /**
     * Get all playlists
     */
    @Query("SELECT * FROM playlists ORDER BY title ASC")
    LiveData<List<Playlist>> getAllPlaylists();
    
    /**
     * Get user playlists (non-official)
     */
    @Query("SELECT * FROM playlists WHERE isOfficial = 0 ORDER BY title ASC")
    LiveData<List<Playlist>> getUserPlaylists();
    
    /**
     * Get official playlists
     */
    @Query("SELECT * FROM playlists WHERE isOfficial = 1 ORDER BY title ASC")
    LiveData<List<Playlist>> getOfficialPlaylists();
    
    /**
     * Search playlists by title
     */
    @Query("SELECT * FROM playlists WHERE title LIKE '%' || :query || '%'")
    LiveData<List<Playlist>> searchPlaylists(String query);
}