package bd.com.ipay.ipayskeleton.Model.Contact;

public class ProfilePictureURL {
    private String low;
    private String medium;
    private String high;

    public ProfilePictureURL(String low) {
        this.low = low;
    }

    public ProfilePictureURL(String low, String medium, String high) {
        this.low = low;
        this.medium = medium;
        this.high = high;
    }

    public String getLow() {
        return low;
    }

    public String getMedium() {
        return medium;
    }

    public String getHigh() {
        return high;
    }

    public void setLow(String low) {
        this.low = low;
    }

    public void setMedium(String medium) {
        this.medium = medium;
    }

    public void setHigh(String high) {
        this.high = high;
    }
}
