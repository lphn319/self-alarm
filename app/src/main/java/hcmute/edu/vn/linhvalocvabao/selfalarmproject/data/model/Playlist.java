package hcmute.edu.vn.linhvalocvabao.selfalarmproject.data.model;

import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.PrimaryKey;
import androidx.room.TypeConverters;


import java.util.ArrayList;
import java.util.List;

import hcmute.edu.vn.linhvalocvabao.selfalarmproject.data.db.Converters;

/**
 * Data model for Playlist objects
 * 
 * Created: 2025-03-10 11:09:31
 * @author lochuungcontinue
 */
@Entity(tableName = "playlists")
public class Playlist implements Parcelable {
    
    @PrimaryKey
    @NonNull
    private String id;
    
    private String title;
    private String thumbnail;
    private String thumbnailM;
    private String description;
    private int songCount;
    private String artists;
    private boolean isOfficial;
    private boolean isPrivate;
    private int privacy;
    
    @TypeConverters(Converters.class)
    private List<String> songIds;
    
    public Playlist() {
        id = "";
        songIds = new ArrayList<>();
    }
    
    protected Playlist(Parcel in) {
        id = in.readString();
        title = in.readString();
        thumbnail = in.readString();
        thumbnailM = in.readString();
        description = in.readString();
        songCount = in.readInt();
        artists = in.readString();
        isOfficial = in.readByte() != 0;
        isPrivate = in.readByte() != 0;
        privacy = in.readInt();
        songIds = in.createStringArrayList();
    }
    
    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(id);
        dest.writeString(title);
        dest.writeString(thumbnail);
        dest.writeString(thumbnailM);
        dest.writeString(description);
        dest.writeInt(songCount);
        dest.writeString(artists);
        dest.writeByte((byte) (isOfficial ? 1 : 0));
        dest.writeByte((byte) (isPrivate ? 1 : 0));
        dest.writeInt(privacy);
        dest.writeStringList(songIds);
    }
    
    @Override
    public int describeContents() {
        return 0;
    }
    
    public static final Creator<Playlist> CREATOR = new Creator<Playlist>() {
        @Override
        public Playlist createFromParcel(Parcel in) {
            return new Playlist(in);
        }
        
        @Override
        public Playlist[] newArray(int size) {
            return new Playlist[size];
        }
    };

    @NonNull
    public String getId() {
        return id;
    }

    public void setId(@NonNull String id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getThumbnail() {
        return thumbnail;
    }

    public void setThumbnail(String thumbnail) {
        this.thumbnail = thumbnail;
    }

    public String getThumbnailM() {
        return thumbnailM;
    }

    public void setThumbnailM(String thumbnailM) {
        this.thumbnailM = thumbnailM;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public int getSongCount() {
        return songCount;
    }

    public void setSongCount(int songCount) {
        this.songCount = songCount;
    }

    public String getArtists() {
        return artists;
    }

    public void setArtists(String artists) {
        this.artists = artists;
    }

    public boolean isOfficial() {
        return isOfficial;
    }

    public void setOfficial(boolean official) {
        isOfficial = official;
    }

    public boolean isPrivate() {
        return isPrivate;
    }

    public void setPrivate(boolean aPrivate) {
        isPrivate = aPrivate;
    }

    public int getPrivacy() {
        return privacy;
    }

    public void setPrivacy(int privacy) {
        this.privacy = privacy;
    }

    public List<String> getSongIds() {
        return songIds;
    }

    public void setSongIds(List<String> songIds) {
        this.songIds = songIds;
    }
    
    public void addSongId(String songId) {
        if (songIds == null) {
            songIds = new ArrayList<>();
        }
        if (!songIds.contains(songId)) {
            songIds.add(songId);
            songCount = songIds.size();
        }
    }
    
    public void removeSongId(String songId) {
        if (songIds != null) {
            songIds.remove(songId);
            songCount = songIds.size();
        }
    }
}