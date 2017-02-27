package bd.com.ipay.ipayskeleton.Model.CommunicationPOJO.Business.Employee;

import bd.com.ipay.ipayskeleton.Model.CommunicationPOJO.Notification.Notification;
import bd.com.ipay.ipayskeleton.Utilities.Constants;

public class Business implements Notification {
    private int accountId;
    private long associationId;
    private String mobileNumber;
    private String name;
    private String profilePictureUrl;
    private String status;
    private int roleId;
    private String designation;
    private Long timeAdded;

    public String getDesignation() {
        return designation;
    }

    public int getRoleId() {
        return roleId;
    }

    public int getAccountId() {
        return accountId;
    }

    public long getAssociationId() {
        return associationId;
    }

    public String getMobileNumber() {
        return mobileNumber;
    }

    public String getName() {
        return name;
    }

    public Long getTimeAdded() {
        return timeAdded;
    }

    public String getProfilePictureUrl() {
        return profilePictureUrl;
    }

    public String getStatus() {
        return status;
    }

    @Override
    public String getNotificationTitle() {
        return "Business Invitation ";
    }

    @Override
    public String getImageUrl() {
        return getProfilePictureUrl();
    }

    @Override
    public long getTime() {
        return getTimeAdded();
    }

    @Override
    public int getNotificationType() {
        return Constants.NOTIFICATION_TYPE_BUSINESS_ACCOUNT_INVITE;
    }
}