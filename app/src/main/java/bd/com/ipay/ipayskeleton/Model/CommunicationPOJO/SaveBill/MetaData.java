
package bd.com.ipay.ipayskeleton.Model.CommunicationPOJO.SaveBill;

import java.io.Serializable;
import android.os.Parcel;
import android.os.Parcelable;
import android.os.Parcelable.Creator;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class MetaData implements Serializable, Parcelable
{

    @SerializedName("notification")
    @Expose
    private Notification notification;
    public final static Creator<MetaData> CREATOR = new Creator<MetaData>() {


        @SuppressWarnings({
            "unchecked"
        })
        public MetaData createFromParcel(Parcel in) {
            return new MetaData(in);
        }

        public MetaData[] newArray(int size) {
            return (new MetaData[size]);
        }

    }
    ;
    private final static long serialVersionUID = 581394799837480699L;

    protected MetaData(Parcel in) {
        this.notification = ((Notification) in.readValue((Notification.class.getClassLoader())));
    }

    /**
     * No args constructor for use in serialization
     * 
     */
    public MetaData() {
    }

    /**
     * 
     * @param notification
     */
    public MetaData(Notification notification) {
        super();
        this.notification = notification;
    }

    public Notification getNotification() {
        return notification;
    }

    public void setNotification(Notification notification) {
        this.notification = notification;
    }

    public void writeToParcel(Parcel dest, int flags) {
        dest.writeValue(notification);
    }

    public int describeContents() {
        return  0;
    }

}
