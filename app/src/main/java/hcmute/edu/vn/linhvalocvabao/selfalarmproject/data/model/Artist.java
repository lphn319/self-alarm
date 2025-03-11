package hcmute.edu.vn.linhvalocvabao.selfalarmproject.data.model;

import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

/**
 * Data model for Artist objects
 * 
 * Created: 2025-03-10 11:09:31
 * @author lochuungcontinue
 */
@Entity(tableName = "artists")
public class Artist implements Parcelable {
    
    @PrimaryKey
    @NonNull
    private String id;
    
    private String name;
    private String alias;
    private String thumbnail;
    private String thumbnailM;
    private String biography;
    private int totalFollow;
    private String realname;
    private String birthday;
    private String national;
    
    public Artist() {
        id = "";
    }
    
    protected Artist(Parcel in) {
        id = in.readString();
        name = in.readString();
        alias = in.readString();
        thumbnail = in.readString();
        thumbnailM = in.readString();
        biography = in.readString();
        totalFollow = in.readInt();
        realname = in.readString();
        birthday = in.readString();
        national = in.readString();
    }
    
    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(id);
        dest.writeString(name);
        dest.writeString(alias);
        dest.writeString(thumbnail);
        dest.writeString(thumbnailM);
        dest.writeString(biography);
        dest.writeInt(totalFollow);
        dest.writeString(realname);
        dest.writeString(birthday);
        dest.writeString(national);
    }
    
    @Override
    public int describeContents() {
        return 0;
    }
    
    public static final Creator<Artist> CREATOR = new Creator<Artist>() {
        @Override
        public Artist createFromParcel(Parcel in) {
            return new Artist(in);
        }
        
        @Override
        public Artist[] newArray(int size) {
            return new Artist[size];
        }
    };

    @NonNull
    public String getId() {
        return id;
    }

    public void setId(@NonNull String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getAlias() {
        return alias;
    }

    public void setAlias(String alias) {
        this.alias = alias;
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

    public String getBiography() {
        return biography;
    }

    public void setBiography(String biography) {
        this.biography = biography;
    }

    public int getTotalFollow() {
        return totalFollow;
    }

    public void setTotalFollow(int totalFollow) {
        this.totalFollow = totalFollow;
    }

    public String getRealname() {
        return realname;
    }

    public void setRealname(String realname) {
        this.realname = realname;
    }

    public String getBirthday() {
        return birthday;
    }

    public void setBirthday(String birthday) {
        this.birthday = birthday;
    }

    public String getNational() {
        return national;
    }

    public void setNational(String national) {
        this.national = national;
    }
}