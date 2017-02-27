package bd.com.ipay.ipayskeleton.Model.CommunicationPOJO.Profile.BasicInfo;

public class UserProfilePictureClass {

    private final String url;
    private final String quality;

    public UserProfilePictureClass(String url, String quality) {
        this.url = url;
        this.quality = quality;
    }

    public String getUrl() {
        return url;
    }

    public String getQuality() {
        return quality;
    }

    @Override
    public String toString() {
        return "UserProfilePictureClass{" +
                "url='" + url + '\'' +
                ", quality='" + quality + '\'' +
                '}';
    }
}