package bd.com.ipay.ipayskeleton.Service.FCM;

public class NotificationMetaData {
    private String imageUrl;
    private String description;

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public NotificationMetaData(String imageUrl, String description) {
        this.imageUrl = imageUrl;
        this.description = description;
    }
}
