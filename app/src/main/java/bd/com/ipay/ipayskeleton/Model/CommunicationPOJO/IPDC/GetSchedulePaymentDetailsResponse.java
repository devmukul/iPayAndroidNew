
package bd.com.ipay.ipayskeleton.Model.CommunicationPOJO.IPDC;

import java.io.Serializable;
import java.util.List;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class GetSchedulePaymentDetailsResponse implements Serializable
{

    @SerializedName("installmentInfoList")
    @Expose
    private List<InstallmentInfoList> installmentInfoList = null;
    @SerializedName("message")
    @Expose
    private String message;
    @SerializedName("scheduledPaymentInfo")
    @Expose
    private ScheduledPaymentInfo scheduledPaymentInfo;
    private final static long serialVersionUID = -3484804125751886107L;

    /**
     * No args constructor for use in serialization
     * 
     */
    public GetSchedulePaymentDetailsResponse() {
    }

    /**
     * 
     * @param message
     * @param installmentInfoList
     * @param scheduledPaymentInfo
     */
    public GetSchedulePaymentDetailsResponse(List<InstallmentInfoList> installmentInfoList, String message, ScheduledPaymentInfo scheduledPaymentInfo) {
        super();
        this.installmentInfoList = installmentInfoList;
        this.message = message;
        this.scheduledPaymentInfo = scheduledPaymentInfo;
    }

    public List<InstallmentInfoList> getInstallmentInfoList() {
        return installmentInfoList;
    }

    public void setInstallmentInfoList(List<InstallmentInfoList> installmentInfoList) {
        this.installmentInfoList = installmentInfoList;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public ScheduledPaymentInfo getScheduledPaymentInfo() {
        return scheduledPaymentInfo;
    }

    public void setScheduledPaymentInfo(ScheduledPaymentInfo scheduledPaymentInfo) {
        this.scheduledPaymentInfo = scheduledPaymentInfo;
    }

}
