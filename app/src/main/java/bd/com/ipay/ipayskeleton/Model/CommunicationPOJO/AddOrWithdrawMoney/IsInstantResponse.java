
package bd.com.ipay.ipayskeleton.Model.CommunicationPOJO.AddOrWithdrawMoney;

import java.io.Serializable;
import android.os.Parcel;
import android.os.Parcelable;
import android.os.Parcelable.Creator;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class IsInstantResponse implements Serializable, Parcelable
{

    @SerializedName("message")
    @Expose
    private String message;
    @SerializedName("instant")
    @Expose
    private Instant instant;
    @SerializedName("lazy")
    @Expose
    private Lazy lazy;
    public final static Creator<IsInstantResponse> CREATOR = new Creator<IsInstantResponse>() {


        @SuppressWarnings({
            "unchecked"
        })
        public IsInstantResponse createFromParcel(Parcel in) {
            return new IsInstantResponse(in);
        }

        public IsInstantResponse[] newArray(int size) {
            return (new IsInstantResponse[size]);
        }

    }
    ;
    private final static long serialVersionUID = -6978414530560489151L;

    protected IsInstantResponse(Parcel in) {
        this.message = ((String) in.readValue((String.class.getClassLoader())));
        this.instant = ((Instant) in.readValue((Instant.class.getClassLoader())));
        this.lazy = ((Lazy) in.readValue((Lazy.class.getClassLoader())));
    }

    /**
     * No args constructor for use in serialization
     * 
     */
    public IsInstantResponse() {
    }

    /**
     * 
     * @param message
     * @param lazy
     * @param instant
     */
    public IsInstantResponse(String message, Instant instant, Lazy lazy) {
        super();
        this.message = message;
        this.instant = instant;
        this.lazy = lazy;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public Instant getInstant() {
        return instant;
    }

    public void setInstant(Instant instant) {
        this.instant = instant;
    }

    public Lazy getLazy() {
        return lazy;
    }

    public void setLazy(Lazy lazy) {
        this.lazy = lazy;
    }

    public void writeToParcel(Parcel dest, int flags) {
        dest.writeValue(message);
        dest.writeValue(instant);
        dest.writeValue(lazy);
    }

    public int describeContents() {
        return  0;
    }

}
