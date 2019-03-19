package bd.com.ipay.ipayskeleton.Model.CommunicationPOJO;

import java.io.Serializable;
import java.util.List;

import bd.com.ipay.ipayskeleton.Model.CommunicationPOJO.IPDC.ScheduledPaymentInfo;
import bd.com.ipay.ipayskeleton.Model.CommunicationPOJO.IPDC.UserInfo;
import bd.com.ipay.ipayskeleton.SourceOfFund.models.ProfilePicture;

public class GroupedScheduledPaymentInfo implements Serializable {

    private String receiverAccountId;
    private UserInfo receiverInfo;
    private List<ProfilePicture>profilePictures;
    private String verificationStatus;

    public String getReceiverAccountId() {
        return receiverAccountId;
    }

    public void setReceiverAccountId(String receiverAccountId) {
        this.receiverAccountId = receiverAccountId;
    }

    public UserInfo getReceiverInfo() {
        return receiverInfo;
    }

    public void setReceiverInfo(UserInfo receiverInfo) {
        this.receiverInfo = receiverInfo;
    }

    public List<ProfilePicture> getProfilePictures() {
        return profilePictures;
    }

    public void setProfilePictures(List<ProfilePicture> profilePictures) {
        this.profilePictures = profilePictures;
    }

    public String getVerificationStatus() {
        return verificationStatus;
    }

    public void setVerificationStatus(String verificationStatus) {
        this.verificationStatus = verificationStatus;
    }

    private List<ScheduledPaymentInfo> scheduledPaymentInfos;

    public List<ScheduledPaymentInfo> getScheduledPaymentInfos() {
        return scheduledPaymentInfos;
    }

    public void setScheduledPaymentInfos(List<ScheduledPaymentInfo> scheduledPaymentInfos) {
        this.scheduledPaymentInfos = scheduledPaymentInfos;
    }
}
