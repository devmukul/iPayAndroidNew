
package bd.com.ipay.ipayskeleton.Model.CommunicationPOJO.IPDC;

import java.io.Serializable;
import java.util.List;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import bd.com.ipay.ipayskeleton.SourceOfFund.models.ProfilePicture;

public class ReceiverInfo implements Serializable
{

    @SerializedName("accountType")
    @Expose
    private Long accountType;
    @SerializedName("mobileNumber")
    @Expose
    private String mobileNumber;
    @SerializedName("name")
    @Expose
    private String name;
    @SerializedName("profilePictures")
    @Expose
    private List<ProfilePicture> profilePictures = null;
    @SerializedName("verificationStatus")
    @Expose
    private String verificationStatus;
    private final static long serialVersionUID = 4130888797227479561L;

    /**
     * No args constructor for use in serialization
     * 
     */
    public ReceiverInfo() {
    }

    /**
     * 
     * @param name
     * @param accountType
     * @param profilePictures
     * @param verificationStatus
     * @param mobileNumber
     */
    public ReceiverInfo(Long accountType, String mobileNumber, String name, List<ProfilePicture> profilePictures, String verificationStatus) {
        super();
        this.accountType = accountType;
        this.mobileNumber = mobileNumber;
        this.name = name;
        this.profilePictures = profilePictures;
        this.verificationStatus = verificationStatus;
    }

    public Long getAccountType() {
        return accountType;
    }

    public void setAccountType(Long accountType) {
        this.accountType = accountType;
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

    public String getVerificationStatus() {
        return verificationStatus;
    }

    public void setVerificationStatus(String verificationStatus) {
        this.verificationStatus = verificationStatus;
    }

}
