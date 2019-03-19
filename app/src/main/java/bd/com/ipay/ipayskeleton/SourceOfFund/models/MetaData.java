package bd.com.ipay.ipayskeleton.SourceOfFund.models;


import android.os.Parcel;
import android.os.Parcelable;

import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;

public class MetaData implements Parcelable {
    @SerializedName("Sponsor name")
    private String sponsorName;

    @SerializedName("Sponsor mobile number")
    private String sponsorMobileNumber;

    @SerializedName("Sponsor profile pictures")
    private ArrayList<ProfilePicture> sponsorProfilePictures;

    @SerializedName("Beneficiary profile pictures")
    private ArrayList<ProfilePicture> beneficiaryProfilePictures;

    @SerializedName("Beneficiary name")
    private String beneficiaryName;

    @SerializedName("Beneficiary mobile number")
    private String beneficiaryMobileNumber;

    private boolean isSponsoredByOther;

    protected MetaData(Parcel in) {
        sponsorName = in.readString();
        sponsorMobileNumber = in.readString();
        sponsorProfilePictures = in.createTypedArrayList(ProfilePicture.CREATOR);
        beneficiaryProfilePictures = in.createTypedArrayList(ProfilePicture.CREATOR);
        beneficiaryName = in.readString();
        beneficiaryMobileNumber = in.readString();
        isSponsoredByOther = in.readByte() != 0;
    }

    public static final Creator<MetaData> CREATOR = new Creator<MetaData>() {
        @Override
        public MetaData createFromParcel(Parcel in) {
            return new MetaData(in);
        }

        @Override
        public MetaData[] newArray(int size) {
            return new MetaData[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(sponsorName);
        dest.writeString(sponsorMobileNumber);
        dest.writeTypedList(sponsorProfilePictures);
        dest.writeTypedList(beneficiaryProfilePictures);
        dest.writeString(beneficiaryName);
        dest.writeString(beneficiaryMobileNumber);
        dest.writeByte((byte) (isSponsoredByOther ? 1 : 0));
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
}


