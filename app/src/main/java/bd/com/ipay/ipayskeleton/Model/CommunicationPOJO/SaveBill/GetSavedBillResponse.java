
package bd.com.ipay.ipayskeleton.Model.CommunicationPOJO.SaveBill;

import java.io.Serializable;
import java.util.List;
import android.os.Parcel;
import android.os.Parcelable;
import android.os.Parcelable.Creator;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class GetSavedBillResponse implements Serializable, Parcelable
{

    @SerializedName("message")
    @Expose
    private String message;
    @SerializedName("savedBills")
    @Expose
    private List<SavedBill> savedBills = null;
    public final static Creator<GetSavedBillResponse> CREATOR = new Creator<GetSavedBillResponse>() {


        @SuppressWarnings({
            "unchecked"
        })
        public GetSavedBillResponse createFromParcel(Parcel in) {
            return new GetSavedBillResponse(in);
        }

        public GetSavedBillResponse[] newArray(int size) {
            return (new GetSavedBillResponse[size]);
        }

    }
    ;
    private final static long serialVersionUID = 103219995829882220L;

    protected GetSavedBillResponse(Parcel in) {
        this.message = ((String) in.readValue((String.class.getClassLoader())));
        in.readList(this.savedBills, (bd.com.ipay.ipayskeleton.Model.CommunicationPOJO.SaveBill.SavedBill.class.getClassLoader()));
    }

    /**
     * No args constructor for use in serialization
     * 
     */
    public GetSavedBillResponse() {
    }

    /**
     * 
     * @param message
     * @param savedBills
     */
    public GetSavedBillResponse(String message, List<SavedBill> savedBills) {
        super();
        this.message = message;
        this.savedBills = savedBills;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public List<SavedBill> getSavedBills() {
        return savedBills;
    }

    public void setSavedBills(List<SavedBill> savedBills) {
        this.savedBills = savedBills;
    }

    public void writeToParcel(Parcel dest, int flags) {
        dest.writeValue(message);
        dest.writeList(savedBills);
    }

    public int describeContents() {
        return  0;
    }

}
