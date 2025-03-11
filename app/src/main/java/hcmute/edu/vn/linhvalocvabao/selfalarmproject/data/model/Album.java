package hcmute.edu.vn.linhvalocvabao.selfalarmproject.data.model;

import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

/**
 * Data model for Album objects
 * 
 * Created: 2025-03-10
 * @author lochuung
 */
@Entity(tableName = "albums")
public class Album implements Parcelable {
    
    @PrimaryKey
    @NonNull
    private String id;
    
    private String title;
    private String artists;
    private String thumbnail;
    private String thumbnailM;
    private String shortDescription;
    private int songCount;
    
    public Album() {
        id = "";
    }
    
    protected Album(Parcel in) {
        id = in.readString();
        title = in.readString();
        artists = in.readString();
        thumbnail = in.readString();
        thumbnailM = in.readString();
        shortDescription = in.readString();
        songCount = in.readInt();
    }
    
    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(id);
        dest.writeString(title);
        dest.writeString(artists);
        dest.writeString(thumbnail);
        dest.writeString(thumbnailM);
        dest.writeString(shortDescription);
        dest.writeInt(songCount);
    }
    
    @Override
    public int describeContents() {
        return 0;
    }
    
    public static final Creator<Album> CREATOR = new Creator<Album>() {
        @Override
        public Album createFromParcel(Parcel in) {
            return new Album(in);
        }
        
        @Override
        public Album[] newArray(int size) {
            return new Album[size];
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

    public String getShortDescription() {
        return shortDescription;
    }

    public void setShortDescription(String shortDescription) {
        this.shortDescription = shortDescription;
    }

    public int getSongCount() {
        return songCount;
    }

    public void setSongCount(int songCount) {
        this.songCount = songCount;
    }

    @Override
    public String toString() {
        return "Album{" +
                "id='" + id + '\'' +
                ", title='" + title + '\'' +
                ", artists='" + artists + '\'' +
                '}';
    }
}