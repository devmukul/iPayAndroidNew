
package bd.com.ipay.ipayskeleton.Model.CommunicationPOJO.SaveBill;

import java.io.Serializable;
import android.os.Parcel;
import android.os.Parcelable;
import android.os.Parcelable.Creator;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class BillParam implements Serializable, Parcelable
{

    @SerializedName("paramLabel")
    @Expose
    private String paramLabel;
    @SerializedName("paramId")
    @Expose
    private String paramId;
    @SerializedName("paramValue")
    @Expose
    private String paramValue;
    @SerializedName("paramType")
    @Expose
    private String paramType;
    public final static Creator<BillParam> CREATOR = new Creator<BillParam>() {


        @SuppressWarnings({
            "unchecked"
        })
        public BillParam createFromParcel(Parcel in) {
            return new BillParam(in);
        }

        public BillParam[] newArray(int size) {
            return (new BillParam[size]);
        }

    }
    ;
    private final static long serialVersionUID = 6951144696152599484L;

    protected BillParam(Parcel in) {
        this.paramLabel = ((String) in.readValue((String.class.getClassLoader())));
        this.paramId = ((String) in.readValue((String.class.getClassLoader())));
        this.paramValue = ((String) in.readValue((String.class.getClassLoader())));
        this.paramType = ((String) in.readValue((String.class.getClassLoader())));
    }

    /**
     * No args constructor for use in serialization
     * 
     */
    public BillParam() {
    }

    /**
     * 
     * @param paramId
     * @param paramLabel
     * @param paramValue
     * @param paramType
     */
    public BillParam(String paramLabel, String paramId, String paramValue, String paramType) {
        super();
        this.paramLabel = paramLabel;
        this.paramId = paramId;
        this.paramValue = paramValue;
        this.paramType = paramType;
    }

    public String getParamLabel() {
        return paramLabel;
    }

    public void setParamLabel(String paramLabel) {
        this.paramLabel = paramLabel;
    }

    public String getParamId() {
        return paramId;
    }

    public void setParamId(String paramId) {
        this.paramId = paramId;
    }

    public String getParamValue() {
        return paramValue;
    }

    public void setParamValue(String paramValue) {
        this.paramValue = paramValue;
    }

    public String getParamType() {
        return paramType;
    }

    public void setParamType(String paramType) {
        this.paramType = paramType;
    }

    public void writeToParcel(Parcel dest, int flags) {
        dest.writeValue(paramLabel);
        dest.writeValue(paramId);
        dest.writeValue(paramValue);
        dest.writeValue(paramType);
    }

    public int describeContents() {
        return  0;
    }

}
