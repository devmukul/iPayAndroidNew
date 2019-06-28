
package bd.com.ipay.ipayskeleton.Model.CommunicationPOJO.MakePayment.Card;

import java.io.Serializable;
import java.util.List;
import android.os.Parcel;
import android.os.Parcelable;
import android.os.Parcelable.Creator;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class GetSavedCardResponse implements Serializable, Parcelable
{

    @SerializedName("message")
    @Expose
    private String message;
    @SerializedName("savedCardInfos")
    @Expose
    private List<SavedCardInfo> savedCardInfos = null;
    public final static Creator<GetSavedCardResponse> CREATOR = new Creator<GetSavedCardResponse>() {


        @SuppressWarnings({
            "unchecked"
        })
        public GetSavedCardResponse createFromParcel(Parcel in) {
            return new GetSavedCardResponse(in);
        }

        public GetSavedCardResponse[] newArray(int size) {
            return (new GetSavedCardResponse[size]);
        }

    }
    ;
    private final static long serialVersionUID = -7935512686643427035L;

    protected GetSavedCardResponse(Parcel in) {
        this.message = ((String) in.readValue((String.class.getClassLoader())));
        in.readList(this.savedCardInfos, (bd.com.ipay.ipayskeleton.Model.CommunicationPOJO.MakePayment.Card.SavedCardInfo.class.getClassLoader()));
    }

    /**
     * No args constructor for use in serialization
     * 
     */
    public GetSavedCardResponse() {
    }

    /**
     * 
     * @param message
     * @param savedCardInfos
     */
    public GetSavedCardResponse(String message, List<SavedCardInfo> savedCardInfos) {
        super();
        this.message = message;
        this.savedCardInfos = savedCardInfos;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public List<SavedCardInfo> getSavedCardInfos() {
        return savedCardInfos;
    }

    public void setSavedCardInfos(List<SavedCardInfo> savedCardInfos) {
        this.savedCardInfos = savedCardInfos;
    }

    public void writeToParcel(Parcel dest, int flags) {
        dest.writeValue(message);
        dest.writeList(savedCardInfos);
    }

    public int describeContents() {
        return  0;
    }

}
