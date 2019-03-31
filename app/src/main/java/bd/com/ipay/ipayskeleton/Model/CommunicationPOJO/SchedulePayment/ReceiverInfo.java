
package bd.com.ipay.ipayskeleton.Model.CommunicationPOJO.SchedulePayment;

import java.io.Serializable;
import java.util.List;
import android.os.Parcel;
import android.os.Parcelable;
import android.os.Parcelable.Creator;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import bd.com.ipay.ipayskeleton.SourceOfFund.models.ProfilePicture;

public class ReceiverInfo implements Serializable, Parcelable
{

    @SerializedName("name")
    @Expose
    private String name;
    @SerializedName("mobileNumber")
    @Expose
    private String mobileNumber;
    @SerializedName("profilePictures")
    @Expose
    private List<ProfilePicture> profilePictures = null;
    @SerializedName("accountType")
    @Expose
    private int accountType;
    @SerializedName("verificationStatus")
    @Expose
    private String verificationStatus;
    public final static Creator<ReceiverInfo> CREATOR = new Creator<ReceiverInfo>() {


        @SuppressWarnings({
            "unchecked"
        })
        public ReceiverInfo createFromParcel(Parcel in) {
            return new ReceiverInfo(in);
        }

        public ReceiverInfo[] newArray(int size) {
            return (new ReceiverInfo[size]);
        }

    }
    ;
    private final static long serialVersionUID = 7190316559871824241L;

    protected ReceiverInfo(Parcel in) {
        this.name = ((String) in.readValue((String.class.getClassLoader())));
        this.mobileNumber = ((String) in.readValue((String.class.getClassLoader())));
        in.readList(this.profilePictures, (ProfilePicture.class.getClassLoader()));
        this.accountType = ((int) in.readValue((int.class.getClassLoader())));
        this.verificationStatus = ((String) in.readValue((String.class.getClassLoader())));
    }

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
    public ReceiverInfo(String name, String mobileNumber, List<ProfilePicture> profilePictures, int accountType, String verificationStatus) {
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

    public List<ProfilePicture> getProfilePictures() {
        return profilePictures;
    }

    public void setProfilePictures(List<ProfilePicture> profilePictures) {
        this.profilePictures = profilePictures;
    }

    public int getAccountType() {
        return accountType;
    }

    public void setAccountType(int accountType) {
        this.accountType = accountType;
    }

    public String getVerificationStatus() {
        return verificationStatus;
    }

    public void setVerificationStatus(String verificationStatus) {
        this.verificationStatus = verificationStatus;
    }

    public void writeToParcel(Parcel dest, int flags) {
        dest.writeValue(name);
        dest.writeValue(mobileNumber);
        dest.writeList(profilePictures);
        dest.writeValue(accountType);
        dest.writeValue(verificationStatus);
    }

    public int describeContents() {
        return  0;
    }

    @Override
    public String toString() {
        return "ReceiverInfo{" +
                "name='" + name + '\'' +
                ", mobileNumber='" + mobileNumber + '\'' +
                ", profilePictures=" + profilePictures +
                ", accountType=" + accountType +
                ", verificationStatus='" + verificationStatus + '\'' +
                '}';
    }
}
