
package bd.com.ipay.ipayskeleton.Model.CommunicationPOJO.TransactionHistory;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;
import java.util.List;

import bd.com.ipay.ipayskeleton.SourceOfFund.models.ProfilePicture;

public class TransactionMetaData implements Parcelable
{

    @SerializedName("visibility")
    @Expose
    private List<Visibility> visibility = null;

    @SerializedName("Sponsor name")
    @Expose
    private String sponsorName;

    @SerializedName("Sponsor mobile number")
    @Expose
    private String sponsorMobileNumber;

    @SerializedName("Sponsor profile pictures")
    @Expose
    private ArrayList<ProfilePicture> sponsorProfilePictures;

    @SerializedName("Beneficiary profile pictures")
    @Expose
    private ArrayList<ProfilePicture> beneficiaryProfilePictures;

    @SerializedName("Beneficiary name")
    @Expose
    private String beneficiaryName;

    @SerializedName("Beneficiary mobile number")
    @Expose
    private String beneficiaryMobileNumber;

    private boolean isSponsoredByOther;

    @Override
    public int describeContents() {
        return 0;
    }

    public String getSponsorName() {
        return sponsorName;
    }

    public void setSponsorName(String sponsorName) {
        this.sponsorName = sponsorName;
    }

    public String getSponsorMobileNumber() {
        return sponsorMobileNumber;
    }

    public void setSponsorMobileNumber(String sponsorMobileNumber) {
        this.sponsorMobileNumber = sponsorMobileNumber;
    }

    public ArrayList<ProfilePicture> getSponsorProfilePictures() {
        return sponsorProfilePictures;
    }

    public void setSponsorProfilePictures(ArrayList<ProfilePicture> sponsorProfilePictures) {
        this.sponsorProfilePictures = sponsorProfilePictures;
    }

    public ArrayList<ProfilePicture> getBeneficiaryProfilePictures() {
        return beneficiaryProfilePictures;
    }

    public void setBeneficiaryProfilePictures(ArrayList<ProfilePicture> beneficiaryProfilePictures) {
        this.beneficiaryProfilePictures = beneficiaryProfilePictures;
    }

    public String getBeneficiaryName() {
        return beneficiaryName;
    }

    public void setBeneficiaryName(String beneficiaryName) {
        this.beneficiaryName = beneficiaryName;
    }

    public String getBeneficiaryMobileNumber() {
        return beneficiaryMobileNumber;
    }

    public void setBeneficiaryMobileNumber(String beneficiaryMobileNumber) {
        this.beneficiaryMobileNumber = beneficiaryMobileNumber;
    }

    public boolean isSponsoredByOther() {
        return isSponsoredByOther;
    }

    public void setSponsoredByOther(boolean sponsoredByOther) {
        isSponsoredByOther = sponsoredByOther;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeList(visibility);
        parcel.writeString(sponsorName);
        parcel.writeString(sponsorMobileNumber);
        parcel.writeTypedList(sponsorProfilePictures);
        parcel.writeTypedList(beneficiaryProfilePictures);
        parcel.writeString(beneficiaryName);
        parcel.writeString(beneficiaryMobileNumber);
        parcel.writeByte((byte) (isSponsoredByOther ? 1 : 0));
    }

    public final static Creator<TransactionMetaData> CREATOR = new Creator<TransactionMetaData>() {

        @Override
        public TransactionMetaData createFromParcel(Parcel in) {
            return new TransactionMetaData(in);
        }

        @Override
        public TransactionMetaData[] newArray(int size) {
            return (new TransactionMetaData[size]);
        }

    }
    ;

    protected TransactionMetaData(Parcel in) {
        visibility = in.readArrayList(Visibility.class.getClassLoader());
        sponsorName = in.readString();
        sponsorMobileNumber = in.readString();
        sponsorProfilePictures = in.createTypedArrayList(ProfilePicture.CREATOR);
        beneficiaryProfilePictures = in.createTypedArrayList(ProfilePicture.CREATOR);
        beneficiaryName = in.readString();
        beneficiaryMobileNumber = in.readString();
        isSponsoredByOther = in.readByte() != 0;
    }


    public TransactionMetaData() {
    }

    public TransactionMetaData(ArrayList<Visibility> visibility) {
        super();
        this.visibility = visibility;
    }

    public List<Visibility> getVisibility() {
        return visibility;
    }

    public void setVisibility(List<Visibility> visibility) {
        this.visibility = visibility;
    }


    @Override
    public String toString() {
        return "TransactionMetaData{" +
                "visibility=" + visibility +
                '}';
    }
}
