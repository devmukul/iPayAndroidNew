package bd.com.ipay.ipayskeleton.Model.CommunicationPOJO;

import java.util.List;

import bd.com.ipay.ipayskeleton.SourceOfFund.models.ProfilePicture;

public class UserInfo {
    private long accountId;
    private String mobileNumber;
    private String name;
    private List<ProfilePicture>profilePictures;
    private long pendingUpdates;

}
