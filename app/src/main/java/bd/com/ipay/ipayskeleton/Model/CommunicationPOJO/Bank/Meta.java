
package bd.com.ipay.ipayskeleton.Model.CommunicationPOJO.Bank;

import java.io.Serializable;
import android.os.Parcel;
import android.os.Parcelable;
import android.os.Parcelable.Creator;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class Meta implements Serializable, Parcelable
{

    @SerializedName("ResponseCode")
    @Expose
    private String responseCode;
    @SerializedName("getTokenResponseDescription")
    @Expose
    private String getTokenResponseDescription;
    @SerializedName("WalletNo")
    @Expose
    private String walletNo;
    @SerializedName("addBankResponseDescription")
    @Expose
    private String addBankResponseDescription;
    @SerializedName("AccountToken")
    @Expose
    private String accountToken;
    @SerializedName("SessionIdURL")
    @Expose
    private String sessionIdURL;
    public final static Creator<Meta> CREATOR = new Creator<Meta>() {


        @SuppressWarnings({
            "unchecked"
        })
        public Meta createFromParcel(Parcel in) {
            return new Meta(in);
        }

        public Meta[] newArray(int size) {
            return (new Meta[size]);
        }

    }
    ;
    private final static long serialVersionUID = 2495747369779918678L;

    protected Meta(Parcel in) {
        this.responseCode = ((String) in.readValue((String.class.getClassLoader())));
        this.getTokenResponseDescription = ((String) in.readValue((String.class.getClassLoader())));
        this.walletNo = ((String) in.readValue((String.class.getClassLoader())));
        this.addBankResponseDescription = ((String) in.readValue((String.class.getClassLoader())));
        this.accountToken = ((String) in.readValue((String.class.getClassLoader())));
        this.sessionIdURL = ((String) in.readValue((String.class.getClassLoader())));
    }

    /**
     * No args constructor for use in serialization
     * 
     */
    public Meta() {
    }

    /**
     * 
     * @param responseCode
     * @param sessionIdURL
     * @param getTokenResponseDescription
     * @param walletNo
     * @param addBankResponseDescription
     * @param accountToken
     */
    public Meta(String responseCode, String getTokenResponseDescription, String walletNo, String addBankResponseDescription, String accountToken, String sessionIdURL) {
        super();
        this.responseCode = responseCode;
        this.getTokenResponseDescription = getTokenResponseDescription;
        this.walletNo = walletNo;
        this.addBankResponseDescription = addBankResponseDescription;
        this.accountToken = accountToken;
        this.sessionIdURL = sessionIdURL;
    }

    public String getResponseCode() {
        return responseCode;
    }

    public void setResponseCode(String responseCode) {
        this.responseCode = responseCode;
    }

    public String getGetTokenResponseDescription() {
        return getTokenResponseDescription;
    }

    public void setGetTokenResponseDescription(String getTokenResponseDescription) {
        this.getTokenResponseDescription = getTokenResponseDescription;
    }

    public String getWalletNo() {
        return walletNo;
    }

    public void setWalletNo(String walletNo) {
        this.walletNo = walletNo;
    }

    public String getAddBankResponseDescription() {
        return addBankResponseDescription;
    }

    public void setAddBankResponseDescription(String addBankResponseDescription) {
        this.addBankResponseDescription = addBankResponseDescription;
    }

    public String getAccountToken() {
        return accountToken;
    }

    public void setAccountToken(String accountToken) {
        this.accountToken = accountToken;
    }

    public String getSessionIdURL() {
        return sessionIdURL;
    }

    public void setSessionIdURL(String sessionIdURL) {
        this.sessionIdURL = sessionIdURL;
    }

    public void writeToParcel(Parcel dest, int flags) {
        dest.writeValue(responseCode);
        dest.writeValue(getTokenResponseDescription);
        dest.writeValue(walletNo);
        dest.writeValue(addBankResponseDescription);
        dest.writeValue(accountToken);
        dest.writeValue(sessionIdURL);
    }

    public int describeContents() {
        return  0;
    }

}
