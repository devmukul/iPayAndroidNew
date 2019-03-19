
package bd.com.ipay.ipayskeleton.Model.CommunicationPOJO.TransactionHistory;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class Visibility implements Parcelable
{

    @SerializedName("label")
    @Expose
    private String label;
    @SerializedName("value")
    @Expose
    private String value;
    @SerializedName("viewOrder")
    @Expose
    private long viewOrder;


    @Override
    public int describeContents() {
        return 0;
    }

    public final static Creator<Visibility> CREATOR = new Creator<Visibility>() {
        @Override
        public Visibility createFromParcel(Parcel in) {
            return new Visibility(in);
        }

        @Override
        public Visibility[] newArray(int size) {
            return (new Visibility[size]);
        }

    };

    protected Visibility(Parcel in) {
        this.label = in.readString();
        this.value = in.readString();
        this.viewOrder = in.readLong();
    }

    public Visibility() {
    }

    public Visibility(String label, String value, Long viewOrder) {
        super();
        this.label = label;
        this.value = value;
        this.viewOrder = viewOrder;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public Long getViewOrder() {
        return viewOrder;
    }

    public void setViewOrder(Long viewOrder) {
        this.viewOrder = viewOrder;
    }

    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(label);
        dest.writeString(value);
        dest.writeLong(viewOrder);
    }

}
