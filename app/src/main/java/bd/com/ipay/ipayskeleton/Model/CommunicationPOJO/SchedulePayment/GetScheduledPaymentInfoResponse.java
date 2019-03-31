
package bd.com.ipay.ipayskeleton.Model.CommunicationPOJO.SchedulePayment;

import java.io.Serializable;
import java.util.List;
import android.os.Parcel;
import android.os.Parcelable;
import android.os.Parcelable.Creator;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class GetScheduledPaymentInfoResponse implements Serializable, Parcelable
{

    @SerializedName("message")
    @Expose
    private String message;
    @SerializedName("groupedScheduledPaymentList")
    @Expose
    private List<GroupedScheduledPaymentList> groupedScheduledPaymentList = null;
    public final static Creator<GetScheduledPaymentInfoResponse> CREATOR = new Creator<GetScheduledPaymentInfoResponse>() {


        @SuppressWarnings({
            "unchecked"
        })
        public GetScheduledPaymentInfoResponse createFromParcel(Parcel in) {
            return new GetScheduledPaymentInfoResponse(in);
        }

        public GetScheduledPaymentInfoResponse[] newArray(int size) {
            return (new GetScheduledPaymentInfoResponse[size]);
        }

    }
    ;
    private final static long serialVersionUID = -7058573684186087244L;

    protected GetScheduledPaymentInfoResponse(Parcel in) {
        this.message = ((String) in.readValue((String.class.getClassLoader())));
        in.readList(this.groupedScheduledPaymentList, (bd.com.ipay.ipayskeleton.Model.CommunicationPOJO.SchedulePayment.GroupedScheduledPaymentList.class.getClassLoader()));
    }

    /**
     * No args constructor for use in serialization
     * 
     */
    public GetScheduledPaymentInfoResponse() {
    }

    /**
     * 
     * @param message
     * @param groupedScheduledPaymentList
     */
    public GetScheduledPaymentInfoResponse(String message, List<GroupedScheduledPaymentList> groupedScheduledPaymentList) {
        super();
        this.message = message;
        this.groupedScheduledPaymentList = groupedScheduledPaymentList;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public List<GroupedScheduledPaymentList> getGroupedScheduledPaymentList() {
        return groupedScheduledPaymentList;
    }

    public void setGroupedScheduledPaymentList(List<GroupedScheduledPaymentList> groupedScheduledPaymentList) {
        this.groupedScheduledPaymentList = groupedScheduledPaymentList;
    }

    public void writeToParcel(Parcel dest, int flags) {
        dest.writeValue(message);
        dest.writeList(groupedScheduledPaymentList);
    }

    public int describeContents() {
        return  0;
    }

    @Override
    public String toString() {
        return "GetScheduledPaymentInfoResponse{" +
                "message='" + message + '\'' +
                ", groupedScheduledPaymentList=" + groupedScheduledPaymentList +
                '}';
    }
}
