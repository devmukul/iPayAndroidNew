
package bd.com.ipay.ipayskeleton.Model.CommunicationPOJO.Profile.ProfileCompletion;

import java.io.Serializable;
import android.os.Parcel;
import android.os.Parcelable;
import android.os.Parcelable.Creator;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class ProfileCompletionResponse implements Serializable, Parcelable
{

    @SerializedName("identificationDocumentSubmitted")
    @Expose
    private Boolean identificationDocumentSubmitted;
    @SerializedName("message")
    @Expose
    private String message;
    @SerializedName("profilePictureSubmitted")
    @Expose
    private Boolean profilePictureSubmitted;
    @SerializedName("sourceOfFundSubmitted")
    @Expose
    private Boolean sourceOfFundSubmitted;
    public final static Creator<ProfileCompletionResponse> CREATOR = new Creator<ProfileCompletionResponse>() {


        @SuppressWarnings({
            "unchecked"
        })
        public ProfileCompletionResponse createFromParcel(Parcel in) {
            return new ProfileCompletionResponse(in);
        }

        public ProfileCompletionResponse[] newArray(int size) {
            return (new ProfileCompletionResponse[size]);
        }

    }
    ;
    private final static long serialVersionUID = 7861963386589998322L;

    protected ProfileCompletionResponse(Parcel in) {
        this.identificationDocumentSubmitted = ((Boolean) in.readValue((Boolean.class.getClassLoader())));
        this.message = ((String) in.readValue((String.class.getClassLoader())));
        this.profilePictureSubmitted = ((Boolean) in.readValue((Boolean.class.getClassLoader())));
        this.sourceOfFundSubmitted = ((Boolean) in.readValue((Boolean.class.getClassLoader())));
    }

    /**
     * No args constructor for use in serialization
     * 
     */
    public ProfileCompletionResponse() {
    }

    /**
     * 
     * @param message
     * @param sourceOfFundSubmitted
     * @param profilePictureSubmitted
     * @param identificationDocumentSubmitted
     */
    public ProfileCompletionResponse(Boolean identificationDocumentSubmitted, String message, Boolean profilePictureSubmitted, Boolean sourceOfFundSubmitted) {
        super();
        this.identificationDocumentSubmitted = identificationDocumentSubmitted;
        this.message = message;
        this.profilePictureSubmitted = profilePictureSubmitted;
        this.sourceOfFundSubmitted = sourceOfFundSubmitted;
    }

    public Boolean getIdentificationDocumentSubmitted() {
        return identificationDocumentSubmitted;
    }

    public void setIdentificationDocumentSubmitted(Boolean identificationDocumentSubmitted) {
        this.identificationDocumentSubmitted = identificationDocumentSubmitted;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public Boolean getProfilePictureSubmitted() {
        return profilePictureSubmitted;
    }

    public void setProfilePictureSubmitted(Boolean profilePictureSubmitted) {
        this.profilePictureSubmitted = profilePictureSubmitted;
    }

    public Boolean getSourceOfFundSubmitted() {
        return sourceOfFundSubmitted;
    }

    public void setSourceOfFundSubmitted(Boolean sourceOfFundSubmitted) {
        this.sourceOfFundSubmitted = sourceOfFundSubmitted;
    }

    public void writeToParcel(Parcel dest, int flags) {
        dest.writeValue(identificationDocumentSubmitted);
        dest.writeValue(message);
        dest.writeValue(profilePictureSubmitted);
        dest.writeValue(sourceOfFundSubmitted);
    }

    public int describeContents() {
        return  0;
    }

}
