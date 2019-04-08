
package bd.com.ipay.ipayskeleton.Model.CommunicationPOJO.SchedulePayment;

import java.io.Serializable;
import java.util.List;
import android.os.Parcel;
import android.os.Parcelable;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class GroupedScheduledPaymentList implements Serializable, Parcelable
{

    @SerializedName("receiverAccountId")
    @Expose
    private String receiverAccountId;
    @SerializedName("customerAccountId")
    @Expose
    private String customerAccountId;
    @SerializedName("receiverInfo")
    @Expose
    private ReceiverInfo receiverInfo;
    @SerializedName("customerInfo")
    @Expose
    private CustomerInfo customerInfo;
    @SerializedName("scheduledPaymentInfos")
    @Expose
    private List<ScheduledPaymentInfo> scheduledPaymentInfos = null;
    public final static Creator<GroupedScheduledPaymentList> CREATOR = new Creator<GroupedScheduledPaymentList>() {


        @SuppressWarnings({
            "unchecked"
        })
        public GroupedScheduledPaymentList createFromParcel(Parcel in) {
            return new GroupedScheduledPaymentList(in);
        }

        public GroupedScheduledPaymentList[] newArray(int size) {
            return (new GroupedScheduledPaymentList[size]);
        }

    }
    ;
    private final static long serialVersionUID = -3995143735911292320L;

    protected GroupedScheduledPaymentList(Parcel in) {
        this.receiverAccountId = ((String) in.readValue((String.class.getClassLoader())));
        this.customerAccountId = ((String) in.readValue((String.class.getClassLoader())));
        this.receiverInfo = ((ReceiverInfo) in.readValue((ReceiverInfo.class.getClassLoader())));
        this.customerInfo = ((CustomerInfo) in.readValue((CustomerInfo.class.getClassLoader())));
        in.readList(this.scheduledPaymentInfos, (ScheduledPaymentInfo.class.getClassLoader()));
    }

    /**
     * No args constructor for use in serialization
     * 
     */
    public GroupedScheduledPaymentList() {
    }

    /**
     * 
     * @param scheduledPaymentInfos
     * @param customerAccountId
     * @param receiverAccountId
     * @param receiverInfo
     * @param customerInfo
     */
    public GroupedScheduledPaymentList(String receiverAccountId, String customerAccountId, ReceiverInfo receiverInfo, CustomerInfo customerInfo, List<ScheduledPaymentInfo> scheduledPaymentInfos) {
        super();
        this.receiverAccountId = receiverAccountId;
        this.customerAccountId = customerAccountId;
        this.receiverInfo = receiverInfo;
        this.customerInfo = customerInfo;
        this.scheduledPaymentInfos = scheduledPaymentInfos;
    }

    public String getReceiverAccountId() {
        return receiverAccountId;
    }

    public void setReceiverAccountId(String receiverAccountId) {
        this.receiverAccountId = receiverAccountId;
    }

    public String getCustomerAccountId() {
        return customerAccountId;
    }

    public void setCustomerAccountId(String customerAccountId) {
        this.customerAccountId = customerAccountId;
    }

    public ReceiverInfo getReceiverInfo() {
        return receiverInfo;
    }

    public void setReceiverInfo(ReceiverInfo receiverInfo) {
        this.receiverInfo = receiverInfo;
    }

    public CustomerInfo getCustomerInfo() {
        return customerInfo;
    }

    public void setCustomerInfo(CustomerInfo customerInfo) {
        this.customerInfo = customerInfo;
    }

    public List<ScheduledPaymentInfo> getScheduledPaymentInfos() {
        return scheduledPaymentInfos;
    }

    public void setScheduledPaymentInfos(List<ScheduledPaymentInfo> scheduledPaymentInfos) {
        this.scheduledPaymentInfos = scheduledPaymentInfos;
    }

    public void writeToParcel(Parcel dest, int flags) {
        dest.writeValue(receiverAccountId);
        dest.writeValue(customerAccountId);
        dest.writeValue(receiverInfo);
        dest.writeValue(customerInfo);
        dest.writeList(scheduledPaymentInfos);
    }

    public int describeContents() {
        return  0;
    }

    @Override
    public String toString() {
        return "GroupedScheduledPaymentList{" +
                "receiverAccountId='" + receiverAccountId + '\'' +
                ", customerAccountId='" + customerAccountId + '\'' +
                ", receiverInfo=" + receiverInfo +
                ", customerInfo=" + customerInfo +
                ", scheduledPaymentInfos=" + scheduledPaymentInfos +
                '}';
    }
}
