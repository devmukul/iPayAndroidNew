package bd.com.ipay.ipayskeleton.Model.CommunicationPOJO.SchedulePayment;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

public class SetSchedulePaymentDecisionRequest implements Serializable, Parcelable
{

    @SerializedName("id")
    @Expose
    private int id;
    @SerializedName("status")
    @Expose
    private String status;
    public final static Parcelable.Creator<SetSchedulePaymentDecisionRequest> CREATOR = new Creator<SetSchedulePaymentDecisionRequest>() {


        @SuppressWarnings({
                "unchecked"
        })
        public SetSchedulePaymentDecisionRequest createFromParcel(Parcel in) {
            return new SetSchedulePaymentDecisionRequest(in);
        }

        public SetSchedulePaymentDecisionRequest[] newArray(int size) {
            return (new SetSchedulePaymentDecisionRequest[size]);
        }

    }
            ;
    private final static long serialVersionUID = 7420402930707503468L;

    protected SetSchedulePaymentDecisionRequest(Parcel in) {
        this.id = ((int) in.readValue((int.class.getClassLoader())));
        this.status = ((String) in.readValue((String.class.getClassLoader())));
    }

    /**
     * No args constructor for use in serialization
     *
     */
    public SetSchedulePaymentDecisionRequest() {
    }

    /**
     *
     * @param id
     * @param status
     */
    public SetSchedulePaymentDecisionRequest(int id, String status) {
        super();
        this.id = id;
        this.status = status;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public void writeToParcel(Parcel dest, int flags) {
        dest.writeValue(id);
        dest.writeValue(status);
    }

    public int describeContents() {
        return 0;
    }

}