package bd.com.ipay.ipayskeleton.Model.CommunicationPOJO.SchedulePayment;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

public class SchedulePaymentAcceptReject implements Serializable, Parcelable
{

    @SerializedName("message")
    @Expose
    private String message;
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
    private final static long serialVersionUID = 5053079572004203551L;

    protected SchedulePaymentAcceptReject(Parcel in) {
        this.message = ((String) in.readValue((String.class.getClassLoader())));
    }

    /**
     * No args constructor for use in serialization
     *
     */
    public SchedulePaymentAcceptReject() {
    }

    /**
     *
     * @param message
     */
    public SchedulePaymentAcceptReject(String message) {
        super();
        this.message = message;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public void writeToParcel(Parcel dest, int flags) {
        dest.writeValue(message);
    }

    public int describeContents() {
        return 0;
    }

}