
package bd.com.ipay.ipayskeleton.Model.CommunicationPOJO.AddOrWithdrawMoney;

import java.io.Serializable;
import android.os.Parcel;
import android.os.Parcelable;
import android.os.Parcelable.Creator;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class IsInstantResponse implements Serializable, Parcelable
{

    @SerializedName("message")
    @Expose
    private String message;
    @SerializedName("isInstant")
    @Expose
    private Boolean isInstant;
    @SerializedName("instantFeeDescription")
    @Expose
    private String instantFeeDescription;
    @SerializedName("lazyFeeDescription")
    @Expose
    private String lazyFeeDescription;
    public final static Creator<IsInstantResponse> CREATOR = new Creator<IsInstantResponse>() {


        @SuppressWarnings({
            "unchecked"
        })
        public IsInstantResponse createFromParcel(Parcel in) {
            return new IsInstantResponse(in);
        }

        public IsInstantResponse[] newArray(int size) {
            return (new IsInstantResponse[size]);
        }

    }
    ;
    private final static long serialVersionUID = 3988749910375153108L;

    protected IsInstantResponse(Parcel in) {
        this.message = ((String) in.readValue((String.class.getClassLoader())));
        this.isInstant = ((Boolean) in.readValue((Boolean.class.getClassLoader())));
        this.instantFeeDescription = ((String) in.readValue((String.class.getClassLoader())));
        this.lazyFeeDescription = ((String) in.readValue((String.class.getClassLoader())));
    }

    /**
     * No args constructor for use in serialization
     * 
     */
    public IsInstantResponse() {
    }

    /**
     * 
     * @param message
     * @param lazyFeeDescription
     * @param instantFeeDescription
     * @param isInstant
     */
    public IsInstantResponse(String message, Boolean isInstant, String instantFeeDescription, String lazyFeeDescription) {
        super();
        this.message = message;
        this.isInstant = isInstant;
        this.instantFeeDescription = instantFeeDescription;
        this.lazyFeeDescription = lazyFeeDescription;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public Boolean getIsInstant() {
        return isInstant;
    }

    public void setIsInstant(Boolean isInstant) {
        this.isInstant = isInstant;
    }

    public String getInstantFeeDescription() {
        return instantFeeDescription;
    }

    public void setInstantFeeDescription(String instantFeeDescription) {
        this.instantFeeDescription = instantFeeDescription;
    }

    public String getLazyFeeDescription() {
        return lazyFeeDescription;
    }

    public void setLazyFeeDescription(String lazyFeeDescription) {
        this.lazyFeeDescription = lazyFeeDescription;
    }

    public void writeToParcel(Parcel dest, int flags) {
        dest.writeValue(message);
        dest.writeValue(isInstant);
        dest.writeValue(instantFeeDescription);
        dest.writeValue(lazyFeeDescription);
    }

    public int describeContents() {
        return  0;
    }

}
