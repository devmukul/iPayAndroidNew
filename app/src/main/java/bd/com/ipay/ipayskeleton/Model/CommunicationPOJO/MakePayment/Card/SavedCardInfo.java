
package bd.com.ipay.ipayskeleton.Model.CommunicationPOJO.MakePayment.Card;

import java.io.Serializable;
import android.os.Parcel;
import android.os.Parcelable;
import android.os.Parcelable.Creator;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class SavedCardInfo implements Serializable, Parcelable
{

    @SerializedName("cardId")
    @Expose
    private String cardId;
    @SerializedName("maskedCardNumber")
    @Expose
    private String maskedCardNumber;
    @SerializedName("addedOn")
    @Expose
    private Long addedOn;
    @SerializedName("lastUsed")
    @Expose
    private Long lastUsed;
    @SerializedName("cardBrand")
    @Expose
    private String cardBrand;
    public final static Creator<SavedCardInfo> CREATOR = new Creator<SavedCardInfo>() {


        @SuppressWarnings({
            "unchecked"
        })
        public SavedCardInfo createFromParcel(Parcel in) {
            return new SavedCardInfo(in);
        }

        public SavedCardInfo[] newArray(int size) {
            return (new SavedCardInfo[size]);
        }

    }
    ;
    private final static long serialVersionUID = 4772990241428006969L;

    protected SavedCardInfo(Parcel in) {
        this.cardId = ((String) in.readValue((String.class.getClassLoader())));
        this.maskedCardNumber = ((String) in.readValue((String.class.getClassLoader())));
        this.addedOn = ((Long) in.readValue((Long.class.getClassLoader())));
        this.lastUsed = ((Long) in.readValue((Long.class.getClassLoader())));
        this.cardBrand = ((String) in.readValue((String.class.getClassLoader())));
    }

    /**
     * No args constructor for use in serialization
     * 
     */
    public SavedCardInfo() {
    }

    /**
     * 
     * @param cardId
     * @param maskedCardNumber
     * @param addedOn
     * @param cardBrand
     * @param lastUsed
     */
    public SavedCardInfo(String cardId, String maskedCardNumber, Long addedOn, Long lastUsed, String cardBrand) {
        super();
        this.cardId = cardId;
        this.maskedCardNumber = maskedCardNumber;
        this.addedOn = addedOn;
        this.lastUsed = lastUsed;
        this.cardBrand = cardBrand;
    }

    public String getCardId() {
        return cardId;
    }

    public void setCardId(String cardId) {
        this.cardId = cardId;
    }

    public String getMaskedCardNumber() {
        return maskedCardNumber;
    }

    public void setMaskedCardNumber(String maskedCardNumber) {
        this.maskedCardNumber = maskedCardNumber;
    }

    public Long getAddedOn() {
        return addedOn;
    }

    public void setAddedOn(Long addedOn) {
        this.addedOn = addedOn;
    }

    public Long getLastUsed() {
        return lastUsed;
    }

    public void setLastUsed(Long lastUsed) {
        this.lastUsed = lastUsed;
    }

    public String getCardBrand() {
        return cardBrand;
    }

    public void setCardBrand(String cardBrand) {
        this.cardBrand = cardBrand;
    }

    public void writeToParcel(Parcel dest, int flags) {
        dest.writeValue(cardId);
        dest.writeValue(maskedCardNumber);
        dest.writeValue(addedOn);
        dest.writeValue(lastUsed);
        dest.writeValue(cardBrand);
    }

    public int describeContents() {
        return  0;
    }

}
