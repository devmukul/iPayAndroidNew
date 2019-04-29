
package bd.com.ipay.ipayskeleton.Model.CommunicationPOJO.UtilityBill;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

public class NotificationForOther implements Serializable, Parcelable
{

    @SerializedName("subscriberName")
    @Expose
    private String subscriberName;
    @SerializedName("mobileNumber")
    @Expose
    private String mobileNumber;
    public final static Creator<NotificationForOther> CREATOR = new Creator<NotificationForOther>() {


        @SuppressWarnings({
            "unchecked"
        })
        public NotificationForOther createFromParcel(Parcel in) {
            return new NotificationForOther(in);
        }

        public NotificationForOther[] newArray(int size) {
            return (new NotificationForOther[size]);
        }

    }
    ;
    private final static long serialVersionUID = 5486263814091226082L;

    protected NotificationForOther(Parcel in) {
        this.subscriberName = ((String) in.readValue((String.class.getClassLoader())));
        this.mobileNumber = ((String) in.readValue((String.class.getClassLoader())));
    }

    /**
     * No args constructor for use in serialization
     * 
     */
    public NotificationForOther() {
    }

    /**
     * 
     * @param subscriberName
     * @param mobileNumber
     */
    public NotificationForOther(String subscriberName, String mobileNumber) {
        super();
        this.subscriberName = subscriberName;
        this.mobileNumber = mobileNumber;
    }

    public String getSubscriberName() {
        return subscriberName;
    }

    public void setSubscriberName(String subscriberName) {
        this.subscriberName = subscriberName;
    }

    public String getMobileNumber() {
        return mobileNumber;
    }

    public void setMobileNumber(String mobileNumber) {
        this.mobileNumber = mobileNumber;
    }

    public void writeToParcel(Parcel dest, int flags) {
        dest.writeValue(subscriberName);
        dest.writeValue(mobileNumber);
    }

    public int describeContents() {
        return  0;
    }

}
