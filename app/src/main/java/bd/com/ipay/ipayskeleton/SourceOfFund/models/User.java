package bd.com.ipay.ipayskeleton.SourceOfFund.models;


import java.io.Serializable;

public class User implements Serializable {

    private String name;
    private String mobileNumber;
    private String profilePictureUrl;
    private long accountId;
    private long accountStatus;

    public long getAccountStatus() {
        return accountStatus;
    }

    public void setAccountStatus(long accountStatus) {
        this.accountStatus = accountStatus;
    }

    public long getAccountId() {
        return accountId;
    }

    public void setAccountId(long accountId) {
        this.accountId = accountId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getMobileNumber() {
        return mobileNumber;
    }

    public void setMobileNumber(String mobileNumber) {
        this.mobileNumber = mobileNumber;
    }

    public String getProfilePictureUrl() {
        return profilePictureUrl;
    }

    public void setProfilePictureUrl(String profilePictureUrl) {
        this.profilePictureUrl = profilePictureUrl;
    }
}
