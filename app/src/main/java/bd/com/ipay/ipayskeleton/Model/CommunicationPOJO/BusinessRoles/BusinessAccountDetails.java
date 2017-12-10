package bd.com.ipay.ipayskeleton.Model.CommunicationPOJO.BusinessRoles;

import java.util.List;

import bd.com.ipay.ipayskeleton.Model.CommunicationPOJO.Profile.BasicInfo.UserProfilePictureClass;

public class BusinessAccountDetails {
    private String message;
    private long id;
    private long businessAccountId;
    private String businessName;
    private List<UserProfilePictureClass> profilePictures = null;
    private String roleName;
    private long createdAt;
    private List<BusinessService> serviceList;

    public BusinessAccountDetails() {
    }

    public BusinessAccountDetails(long id, String businessName) {
        this.id = id;
        this.businessName = businessName;
    }

    public BusinessAccountDetails(long id, String businessName, List<UserProfilePictureClass> profilePictures) {
        this.id = id;
        this.businessName = businessName;
        this.profilePictures = profilePictures;
    }

    public long getId() {
        return id;
    }

    public String getMessage() {
        return message;
    }

    public String getBusinessName() {
        return businessName;
    }

    public long getBusinessAccountId() {
        return businessAccountId;
    }

    public long getCreatedAt() {
        return createdAt;
    }

    public List<UserProfilePictureClass> getProfilePictures() {
        return profilePictures;
    }

    public String getRoleName() {
        return roleName;
    }

    public List<BusinessService> getServiceList() {
        return serviceList;
    }
}
