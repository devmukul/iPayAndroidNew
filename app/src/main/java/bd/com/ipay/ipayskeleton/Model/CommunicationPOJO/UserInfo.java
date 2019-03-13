package bd.com.ipay.ipayskeleton.Model.CommunicationPOJO;

import java.util.List;

import bd.com.ipay.ipayskeleton.SourceOfFund.models.ProfilePicture;

public class UserInfo {
    private long accountId;
    private String mobileNumber;
    private String name;
    private List<ProfilePicture>profilePictures;
    private String photoUrl;
    private long pendingUpdates;

    public long getAccountId() {
        return accountId;
    }

    public void setAccountId(long accountId) {
        this.accountId = accountId;
    }

    public String getMobileNumber() {
        return mobileNumber;
    }

    public void setMobileNumber(String mobileNumber) {
        this.mobileNumber = mobileNumber;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<ProfilePicture> getProfilePictures() {
        return profilePictures;
    }

    public void setProfilePictures(List<ProfilePicture> profilePictures) {
        this.profilePictures = profilePictures;
    }

    public String getPhotoUrl() {
        return photoUrl;
    }

    public void setPhotoUrl(String photoUrl) {
        this.photoUrl = photoUrl;
    }

    public long getPendingUpdates() {
        return pendingUpdates;
    }

    public void setPendingUpdates(long pendingUpdates) {
        this.pendingUpdates = pendingUpdates;
    }
}
