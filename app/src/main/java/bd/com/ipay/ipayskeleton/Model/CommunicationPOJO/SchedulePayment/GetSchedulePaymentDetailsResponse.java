
package bd.com.ipay.ipayskeleton.Model.CommunicationPOJO.SchedulePayment;

import java.io.Serializable;
import java.util.List;
import android.os.Parcel;
import android.os.Parcelable;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class GetSchedulePaymentDetailsResponse implements Serializable, Parcelable
{

    @SerializedName("message")
    @Expose
    private String message;
    @SerializedName("scheduledPaymentInfo")
    @Expose
    private ScheduledPaymentInfo scheduledPaymentInfo;
    @SerializedName("installmentInfoList")
    @Expose
    private List<InstallmentInfoList> installmentInfoList = null;
    public final static Creator<GetSchedulePaymentDetailsResponse> CREATOR = new Creator<GetSchedulePaymentDetailsResponse>() {


        @SuppressWarnings({
            "unchecked"
        })
        public GetSchedulePaymentDetailsResponse createFromParcel(Parcel in) {
            return new GetSchedulePaymentDetailsResponse(in);
        }

        public GetSchedulePaymentDetailsResponse[] newArray(int size) {
            return (new GetSchedulePaymentDetailsResponse[size]);
        }

    }
    ;
    private final static long serialVersionUID = -7111930800141794443L;

    protected GetSchedulePaymentDetailsResponse(Parcel in) {
        this.message = ((String) in.readValue((String.class.getClassLoader())));
        this.scheduledPaymentInfo = ((ScheduledPaymentInfo) in.readValue((ScheduledPaymentInfo.class.getClassLoader())));
        in.readList(this.installmentInfoList, (bd.com.ipay.ipayskeleton.Model.CommunicationPOJO.SchedulePayment.InstallmentInfoList.class.getClassLoader()));
    }

    public GetSchedulePaymentDetailsResponse() {
    }

    public GetSchedulePaymentDetailsResponse(String message, ScheduledPaymentInfo scheduledPaymentInfo, List<InstallmentInfoList> installmentInfoList) {
        super();
        this.message = message;
        this.scheduledPaymentInfo = scheduledPaymentInfo;
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

    public List<InstallmentInfoList> getInstallmentInfoList() {
        return installmentInfoList;
    }

    public void setInstallmentInfoList(List<InstallmentInfoList> installmentInfoList) {
        this.installmentInfoList = installmentInfoList;
    }

    public void writeToParcel(Parcel dest, int flags) {
        dest.writeValue(message);
        dest.writeValue(scheduledPaymentInfo);
        dest.writeList(installmentInfoList);
    }

    public int describeContents() {
        return  0;
    }

    @Override
    public String toString() {
        return "GetSchedulePaymentDetailsResponse{" +
                "message='" + message + '\'' +
                ", scheduledPaymentInfo=" + scheduledPaymentInfo +
                ", installmentInfoList=" + installmentInfoList +
                '}';
    }
}
