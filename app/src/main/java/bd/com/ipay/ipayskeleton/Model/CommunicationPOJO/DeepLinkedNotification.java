package bd.com.ipay.ipayskeleton.Model.CommunicationPOJO;


import bd.com.ipay.ipayskeleton.Service.FCM.NotificationMetaData;

public class DeepLinkedNotification {
    private String accountId;
    private String deepLink;
    private String icon;
    private String body;
    private String status;
    private String message;
    private int serviceId;
    private long time;
    private String imageUrl;
    private String description;
    private String title;
    private NotificationMetaData meta;

    public DeepLinkedNotification(String accountId, String deepLink, String icon, String status, String message, int serviceId, int time, String title) {
        this.accountId = accountId;
        this.deepLink = deepLink;
        this.icon = icon;
        this.status = status;
        this.message = message;
        this.serviceId = serviceId;
        this.time = time;
        this.title = title;
    }

    public NotificationMetaData getMeta() {
        return meta;
    }

    public void setMeta(NotificationMetaData meta) {
        this.meta = meta;
    }

    public String getBody() {
        return body;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public String getAccountId() {
        return accountId;
    }

    public String getDeepLink() {
        return deepLink;
    }

    public String getIcon() {
        return icon;
    }

    public String getStatus() {
        return status;
    }

    public String getMessage() {
        return message;
    }

    public int getServiceId() {
        return serviceId;
    }

    public long getTime() {
        return time;
    }

    public String getTitle() {
        return title;
    }
}

