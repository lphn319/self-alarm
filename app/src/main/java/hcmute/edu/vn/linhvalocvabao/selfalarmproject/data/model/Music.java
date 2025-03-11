package hcmute.edu.vn.linhvalocvabao.selfalarmproject.data.model;

import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

/**
 * Data model for Music objects
 */
@Entity(tableName = "music")
public class Music implements Parcelable {
    
    @PrimaryKey
    @NonNull
    private String id;
    
    private String title;
    private String artists;
    private String thumbnail;
    private String thumbnailM;
    private String duration;
    private int streamingStatus;
    private String streamingUrl;
    private boolean isDownloaded;
    private Album album;
    
    public Music() {
        id = "";
    }

    protected Music(Parcel in) {
        id = in.readString();
        title = in.readString();
        artists = in.readString();
        thumbnail = in.readString();
        thumbnailM = in.readString();
        duration = in.readString();
        streamingStatus = in.readInt();
        streamingUrl = in.readString();
        isDownloaded = in.readByte() != 0;
        album = in.readParcelable(Album.class.getClassLoader());
    }
    
    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(id);
        dest.writeString(title);
        dest.writeString(artists);
        dest.writeString(thumbnail);
        dest.writeString(thumbnailM);
        dest.writeString(duration);
        dest.writeInt(streamingStatus);
        dest.writeString(streamingUrl);
        dest.writeByte((byte) (isDownloaded ? 1 : 0));
        dest.writeParcelable(album, flags);
    }
    
    @Override
    public int describeContents() {
        return 0;
    }
    
    public static final Creator<Music> CREATOR = new Creator<Music>() {
        @Override
        public Music createFromParcel(Parcel in) {
            return new Music(in);
        }
        
        @Override
        public Music[] newArray(int size) {
            return new Music[size];
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

    public String getArtists() {
        return artists;
    }

    public void setArtists(String artists) {
        this.artists = artists;
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

    public String getDuration() {
        return duration;
    }

    public void setDuration(String duration) {
        this.duration = duration;
    }

    public int getStreamingStatus() {
        return streamingStatus;
    }

    public void setStreamingStatus(int streamingStatus) {
        this.streamingStatus = streamingStatus;
    }

    public String getStreamingUrl() {
        return streamingUrl;
    }

    public void setStreamingUrl(String streamingUrl) {
        this.streamingUrl = streamingUrl;
    }

    public boolean isDownloaded() {
        return isDownloaded;
    }

    public void setDownloaded(boolean downloaded) {
        isDownloaded = downloaded;
    }

    public Album getAlbum() {
        return album;
    }

    public void setAlbum(Album album) {
        this.album = album;
    }

    @Override
    public String toString() {
        return "Music{" +
                "id='" + id + '\'' +
                ", title='" + title + '\'' +
                ", artists='" + artists + '\'' +
                '}';
    }
}