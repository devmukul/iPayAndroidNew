
package bd.com.ipay.ipayskeleton.Model.Rating;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

public class Meta implements Serializable
{

    @SerializedName("latitude")
    @Expose
    private Long latitude;
    @SerializedName("longitude")
    @Expose
    private Long longitude;
    @SerializedName("timeOfAction")
    @Expose
    private Long timeOfAction;
    @SerializedName("transactionId")
    @Expose
    private String transactionId;
    @SerializedName("receiverMobileNumber")
    @Expose
    private String receiverMobileNumber;

    public Long getLatitude() {
        return latitude;
    }

    public void setLatitude(Long latitude) {
        this.latitude = latitude;
    }

    public Long getLongitude() {
        return longitude;
    }

    public void setLongitude(Long longitude) {
        this.longitude = longitude;
    }

    public Long getTimeOfAction() {
        return timeOfAction;
    }

    public void setTimeOfAction(Long timeOfAction) {
        this.timeOfAction = timeOfAction;
    }

    public String getTransactionId() {
        return transactionId;
    }

    public void setTransactionId(String transactionId) {
        this.transactionId = transactionId;
    }

    public String getReceiverMobileNumber() {
        return receiverMobileNumber;
    }

    public void setReceiverMobileNumber(String receiverMobileNumber) {
        this.receiverMobileNumber = receiverMobileNumber;
    }

}
