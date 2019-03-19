
package bd.com.ipay.ipayskeleton.Model.CommunicationPOJO.IPayHere.V2;

import android.text.TextUtils;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

public class NearbyBusinessResponseList implements Serializable
{

    @SerializedName("accountId")
    @Expose
    private Long accountId;
    @SerializedName("coordinate")
    @Expose
    private Coordinate coordinate;
    @SerializedName("businessName")
    @Expose
    private String businessName;
    @SerializedName("imageUrl")
    @Expose
    private String imageUrl;
    @SerializedName("outletId")
    @Expose
    private Long outletId;
    @SerializedName("address")
    @Expose
    private Address address;
    @SerializedName("mobileNumber")
    @Expose
    private String mobileNumber;
    @SerializedName("outletName")
    @Expose
    private String outletName;
    private final static long serialVersionUID = -385019561751604768L;

    public Long getAccountId() {
        return accountId;
    }

    public void setAccountId(Long accountId) {
        this.accountId = accountId;
    }

    public Coordinate getCoordinate() {
        return coordinate;
    }

    public void setCoordinate(Coordinate coordinate) {
        this.coordinate = coordinate;
    }

    public String getBusinessName() {
        return businessName;
    }

    public void setBusinessName(String businessName) {
        this.businessName = businessName;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public Long getOutletId() {
        return outletId;
    }

    public void setOutletId(Long outletId) {
        this.outletId = outletId;
    }

    public Address getAddress() {
        return address;
    }

    public void setAddress(Address address) {
        this.address = address;
    }

    public String getMobileNumber() {
        return mobileNumber;
    }

    public void setMobileNumber(String mobileNumber) {
        this.mobileNumber = mobileNumber;
    }

    public String getOutletName() {
        return outletName;
    }

    public void setOutletName(String outletName) {
        this.outletName = outletName;
    }

    public String getAddressString() {
        String addressString = null;
        if (address != null) {
            addressString = address.getAddressLine1();

            if (!TextUtils.isEmpty(address.getAddressLine2()))
                addressString += " "+address.getAddressLine2();

        }
        return addressString;
    }

}
