package hcmute.edu.vn.linhvalocvabao.selfalarmproject.data.db;

import androidx.room.Database;
import androidx.room.RoomDatabase;
import androidx.room.TypeConverters;

import hcmute.edu.vn.linhvalocvabao.selfalarmproject.data.model.Album;
import hcmute.edu.vn.linhvalocvabao.selfalarmproject.data.model.Artist;
import hcmute.edu.vn.linhvalocvabao.selfalarmproject.data.model.Music;
import hcmute.edu.vn.linhvalocvabao.selfalarmproject.data.model.Playlist;

/**
 * Room database for local storage
 * 
 * Last updated: 2025-03-10 11:07:57
 * @author lochuung
 */
@Database(
    entities = {
        Music.class,
        Album.class,
        Artist.class,
        Playlist.class
    },
    version = 1,
    exportSchema = true
)
@TypeConverters({Converters.class})
public abstract class AppDatabase extends RoomDatabase {
    
    /**
     * Get DAO for Music entities
     */
//    public abstract MusicDao musicDao();
    
    /**
     * Get DAO for Album entities
     */
//    public abstract AlbumDao albumDao();
    
    /**
     * Get DAO for Playlist entities
     */
    public abstract PlaylistDao playlistDao();
    
    /**
     * Get DAO for Artist entities
     */
//    public abstract ArtistDao artistDao();
    
    /**
     * Get DAO for search history
     */
//    public abstract SearchHistoryDao searchHistoryDao();
    
    /**
     * Get DAO for recently played tracks
     */
//    public abstract RecentlyPlayedDao recentlyPlayedDao();
}