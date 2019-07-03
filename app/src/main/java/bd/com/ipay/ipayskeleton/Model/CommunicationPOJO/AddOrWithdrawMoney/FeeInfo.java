
package bd.com.ipay.ipayskeleton.Model.CommunicationPOJO.AddOrWithdrawMoney;

import java.io.Serializable;
import android.os.Parcel;
import android.os.Parcelable;
import android.os.Parcelable.Creator;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class FeeInfo implements Serializable, Parcelable
{

    @SerializedName("flatFee")
    @Expose
    private long flatFee;
    @SerializedName("variableFee")
    @Expose
    private long variableFee;
    @SerializedName("maxFee")
    @Expose
    private long maxFee;
    public final static Creator<FeeInfo> CREATOR = new Creator<FeeInfo>() {


        @SuppressWarnings({
            "unchecked"
        })
        public FeeInfo createFromParcel(Parcel in) {
            return new FeeInfo(in);
        }

        public FeeInfo[] newArray(int size) {
            return (new FeeInfo[size]);
        }

    }
    ;
    private final static long serialVersionUID = 6578322586332606418L;

    protected FeeInfo(Parcel in) {
        this.flatFee = ((Long) in.readValue((Long.class.getClassLoader())));
        this.variableFee = ((Long) in.readValue((Long.class.getClassLoader())));
        this.maxFee = ((Long) in.readValue((Long.class.getClassLoader())));
    }

    /**
     * No args constructor for use in serialization
     * 
     */
    public FeeInfo() {
    }

    /**
     * 
     * @param variableFee
     * @param flatFee
     * @param maxFee
     */
    public FeeInfo(long flatFee, long variableFee, long maxFee) {
        super();
        this.flatFee = flatFee;
        this.variableFee = variableFee;
        this.maxFee = maxFee;
    }

    public long getFlatFee() {
        return flatFee;
    }

    public void setFlatFee(long flatFee) {
        this.flatFee = flatFee;
    }

    public long getVariableFee() {
        return variableFee;
    }

    public void setVariableFee(long variableFee) {
        this.variableFee = variableFee;
    }

    public long getMaxFee() {
        return maxFee;
    }

    public void setMaxFee(long maxFee) {
        this.maxFee = maxFee;
    }

    public void writeToParcel(Parcel dest, int flags) {
        dest.writeValue(flatFee);
        dest.writeValue(variableFee);
        dest.writeValue(maxFee);
    }

    public int describeContents() {
        return  0;
    }

}
