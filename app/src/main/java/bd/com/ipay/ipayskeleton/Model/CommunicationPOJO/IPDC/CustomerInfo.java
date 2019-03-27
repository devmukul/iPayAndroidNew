
package bd.com.ipay.ipayskeleton.Model.CommunicationPOJO.IPDC;

import java.io.Serializable;
import java.util.List;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class CustomerInfo implements Serializable
{

    @SerializedName("name")
    @Expose
    private String name;
    @SerializedName("mobileNumber")
    @Expose
    private String mobileNumber;
    @SerializedName("profilePictures")
    @Expose
    private List<ProfilePicture_> profilePictures = null;
    @SerializedName("accountType")
    @Expose
    private Long accountType;
    @SerializedName("verificationStatus")
    @Expose
    private String verificationStatus;
    private final static long serialVersionUID = 5998517406285201199L;

    /**
     * No args constructor for use in serialization
     * 
     */
    public CustomerInfo() {
    }

    /**
     * 
     * @param name
     * @param accountType
     * @param profilePictures
     * @param verificationStatus
     * @param mobileNumber
     */
    public CustomerInfo(String name, String mobileNumber, List<ProfilePicture_> profilePictures, Long accountType, String verificationStatus) {
        super();
        this.name = name;
        this.mobileNumber = mobileNumber;
        this.profilePictures = profilePictures;
        this.accountType = accountType;
        this.verificationStatus = verificationStatus;
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

    public List<ProfilePicture_> getProfilePictures() {
        return profilePictures;
    }

    public void setProfilePictures(List<ProfilePicture_> profilePictures) {
        this.profilePictures = profilePictures;
    }

    public Long getAccountType() {
        return accountType;
    }

    public void setAccountType(Long accountType) {
        this.accountType = accountType;
    }

    public String getVerificationStatus() {
        return verificationStatus;
    }

    public void setVerificationStatus(String verificationStatus) {
        this.verificationStatus = verificationStatus;
    }

}
