package bd.com.ipay.ipayskeleton.SourceOfFund.models;


import android.os.Parcel;

import java.io.Serializable;

import bd.com.ipay.ipayskeleton.Model.CommunicationPOJO.Notification.Notification;
import bd.com.ipay.ipayskeleton.Utilities.Constants;

public class Beneficiary implements Notification, Serializable {
    private long id;
    private String type;

    public Beneficiary() {
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    private String initiatedBy;
    private long monthlyCreditLimit;
    private String relationship;
    private String status;
    private long updatedAt;
    private User user;

    public static final Creator<Beneficiary> CREATOR = new Creator<Beneficiary>() {
        @Override
        public Beneficiary createFromParcel(Parcel in) {
            return new Beneficiary(in);
        }

        @Override
        public Beneficiary[] newArray(int size) {
            return new Beneficiary[size];
        }
    };

    public Beneficiary(Parcel in) {

    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getInitiatedBy() {
        return initiatedBy;
    }

    public void setInitiatedBy(String initiatedBy) {
        this.initiatedBy = initiatedBy;
    }

    public long getMonthlyCreditLimit() {
        return monthlyCreditLimit;
    }

    public void setMonthlyCreditLimit(long monthlyCreditLimit) {
        this.monthlyCreditLimit = monthlyCreditLimit;
    }

    public String getRelationship() {
        return relationship;
    }

    public void setRelationship(String relationship) {
        this.relationship = relationship;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public long getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(long updatedAt) {
        this.updatedAt = updatedAt;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    @Override
    public String getNotificationTitle() {
        return null;
    }

    @Override
    public String getName() {
        return user.getName();
    }

    @Override
    public String getImageUrl() {
        return user.getProfilePictureUrl();
    }

    @Override
    public long getTime() {
        return getUpdatedAt();
    }

    @Override
    public int getNotificationType() {
        return Constants.NOTIFICATION_TYPE_SOURCE_OF_FUND_BENEFICIARIES;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {

    }
}
