
package bd.com.ipay.ipayskeleton.Model.CommunicationPOJO.SaveBill;

import java.io.Serializable;
import android.os.Parcel;
import android.os.Parcelable;
import android.os.Parcelable.Creator;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class Notification implements Serializable, Parcelable
{

    @SerializedName("subscriberName")
    @Expose
    private String subscriberName;
    @SerializedName("mobileNumber")
    @Expose
    private String mobileNumber;
    public final static Creator<Notification> CREATOR = new Creator<Notification>() {


        @SuppressWarnings({
            "unchecked"
        })
        public Notification createFromParcel(Parcel in) {
            return new Notification(in);
        }

        public Notification[] newArray(int size) {
            return (new Notification[size]);
        }

    }
    ;
    private final static long serialVersionUID = 5486263814091226082L;

    protected Notification(Parcel in) {
        this.subscriberName = ((String) in.readValue((String.class.getClassLoader())));
        this.mobileNumber = ((String) in.readValue((String.class.getClassLoader())));
    }

    /**
     * No args constructor for use in serialization
     * 
     */
    public Notification() {
    }

    /**
     * 
     * @param subscriberName
     * @param mobileNumber
     */
    public Notification(String subscriberName, String mobileNumber) {
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
