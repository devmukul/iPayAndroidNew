package bd.com.ipay.ipayskeleton.SourceOfFund.models;


import android.os.Parcel;
import android.os.Parcelable;

public class ProfilePicture implements Parcelable {
    private String url;
    private String quality;

    protected ProfilePicture(Parcel in) {
        url = in.readString();
        quality = in.readString();
    }

    public static final Creator<ProfilePicture> CREATOR = new Creator<ProfilePicture>() {
        @Override
        public ProfilePicture createFromParcel(Parcel in) {
            return new ProfilePicture(in);
        }

        @Override
        public ProfilePicture[] newArray(int size) {
            return new ProfilePicture[size];
        }
    };

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getQuality() {
        return quality;
    }

    public void setQuality(String quality) {
        this.quality = quality;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(url);
        dest.writeString(quality);
    }
}
