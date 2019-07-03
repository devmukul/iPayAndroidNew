
package bd.com.ipay.ipayskeleton.Model.CommunicationPOJO.AddOrWithdrawMoney;

import java.io.Serializable;
import android.os.Parcel;
import android.os.Parcelable;
import android.os.Parcelable.Creator;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class Lazy implements Serializable, Parcelable
{

    @SerializedName("isEligible")
    @Expose
    private Boolean isEligible;
    @SerializedName("feeDescription")
    @Expose
    private String feeDescription;
    @SerializedName("feeInfo")
    @Expose
    private FeeInfo feeInfo;
    public final static Creator<Lazy> CREATOR = new Creator<Lazy>() {


        @SuppressWarnings({
            "unchecked"
        })
        public Lazy createFromParcel(Parcel in) {
            return new Lazy(in);
        }

        public Lazy[] newArray(int size) {
            return (new Lazy[size]);
        }

    }
    ;
    private final static long serialVersionUID = 1403528739449959874L;

    protected Lazy(Parcel in) {
        this.isEligible = ((Boolean) in.readValue((Boolean.class.getClassLoader())));
        this.feeDescription = ((String) in.readValue((String.class.getClassLoader())));
        this.feeInfo = ((FeeInfo) in.readValue((FeeInfo.class.getClassLoader())));
    }

    /**
     * No args constructor for use in serialization
     * 
     */
    public Lazy() {
    }

    /**
     * 
     * @param feeDescription
     * @param isEligible
     * @param feeInfo
     */
    public Lazy(Boolean isEligible, String feeDescription, FeeInfo feeInfo) {
        super();
        this.isEligible = isEligible;
        this.feeDescription = feeDescription;
        this.feeInfo = feeInfo;
    }

    public Boolean getIsEligible() {
        return isEligible;
    }

    public void setIsEligible(Boolean isEligible) {
        this.isEligible = isEligible;
    }

    public String getFeeDescription() {
        return feeDescription;
    }

    public void setFeeDescription(String feeDescription) {
        this.feeDescription = feeDescription;
    }

    public FeeInfo getFeeInfo() {
        return feeInfo;
    }

    public void setFeeInfo(FeeInfo feeInfo) {
        this.feeInfo = feeInfo;
    }

    public void writeToParcel(Parcel dest, int flags) {
        dest.writeValue(isEligible);
        dest.writeValue(feeDescription);
        dest.writeValue(feeInfo);
    }

    public int describeContents() {
        return  0;
    }

}
