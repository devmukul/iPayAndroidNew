
package bd.com.ipay.ipayskeleton.Model.CommunicationPOJO.UtilityBill;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

public class MetaData implements Serializable, Parcelable
{

    @SerializedName("notificationForOthers")
    @Expose
    private NotificationForOther notificationForOthers;
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

    };
    private final static long serialVersionUID = 581394799837480699L;

    protected MetaData(Parcel in) {
        this.notificationForOthers = ((NotificationForOther) in.readValue((NotificationForOther.class.getClassLoader())));
    }

    /**
     * No args constructor for use in serialization
     * 
     */
    public MetaData() {
    }

    /**
     * 
     * @param notificationForOthers
     */
    public MetaData(NotificationForOther notificationForOthers) {
        super();
        this.notificationForOthers = notificationForOthers;
    }

    public NotificationForOther getNotification() {
        return notificationForOthers;
    }

    public void setNotification(NotificationForOther notificationForOthers) {
        this.notificationForOthers = notificationForOthers;
    }

    public void writeToParcel(Parcel dest, int flags) {
        dest.writeValue(notificationForOthers);
    }

    public int describeContents() {
        return  0;
    }

}
