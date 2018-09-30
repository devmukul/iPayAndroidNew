package bd.com.ipay.ipayskeleton.Model.SqLiteDatabase;

import com.google.gson.Gson;

import java.util.List;

import bd.com.ipay.ipayskeleton.Model.BusinessContact.Outlets;
import bd.com.ipay.ipayskeleton.Model.CommunicationPOJO.Profile.BasicInfo.UserAddress;
import bd.com.ipay.ipayskeleton.Model.CommunicationPOJO.Profile.BasicInfo.UserAddressList;

public class BusinessAccountEntry {
    public String mobileNumber;
    public String businessName;
    public String companyName;
    public String email;
    public int businessType;
    public String profilePictureUrl;
    public String profilePictureUrlMedium;
    public String profilePictureUrlHigh;
    public int businessId;
    private UserAddressList addressList;
    private List<Outlets> outlets;

    public BusinessAccountEntry(String mobileNumber, String businessName, String companyName, String email,
                                int businessType, String profilePictureUrl, String profilePictureUrlMedium,
                                String profilePictureUrlHigh, int businessId, UserAddressList addressList,List<Outlets> outlets) {
        this.mobileNumber = mobileNumber;
        this.businessName = businessName;
        this.companyName = companyName;
        this.email = email;
        this.businessType = businessType;
        this.profilePictureUrl = profilePictureUrl;
        this.profilePictureUrlMedium = profilePictureUrlMedium;
        this.profilePictureUrlHigh = profilePictureUrlHigh;
        this.businessId = businessId;
        this.addressList = addressList;
        this.outlets = outlets;
    }

    public String getMobileNumber() {
        return mobileNumber;
    }

    public void setMobileNumber(String mobileNumber) {
        this.mobileNumber = mobileNumber;
    }

    public String getBusinessName() {
        return businessName;
    }

    public void setBusinessName(String businessName) {
        this.businessName = businessName;
    }

    public String getCompanyName() {
        return companyName;
    }

    public void setCompanyName(String companyName) {
        this.companyName = companyName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public int getBusinessType() {
        return businessType;
    }

    public void setBusinessType(int businessType) {
        this.businessType = businessType;
    }

    public String getProfilePictureUrl() {
        return profilePictureUrl;
    }

    public void setProfilePictureUrl(String profilePictureUrl) {
        this.profilePictureUrl = profilePictureUrl;
    }

    public int getBusinessId() {
        return businessId;
    }

    public void setBusinessId(int businessId) {
        this.businessId = businessId;
    }

    public String getProfilePictureUrlMedium() {
        return profilePictureUrlMedium;
    }

    public void setProfilePictureUrlMedium(String profilePictureUrlMedium) {
        this.profilePictureUrlMedium = profilePictureUrlMedium;
    }

    public String getProfilePictureUrlHigh() {
        return profilePictureUrlHigh;
    }

    public void setProfilePictureUrlHigh(String profilePictureUrlHigh) {
        this.profilePictureUrlHigh = profilePictureUrlHigh;
    }

    public UserAddressList getAddressList() {
        return addressList;
    }

    public void setAddressList(UserAddressList addressList) {
        this.addressList = addressList;
    }

    public List<Outlets> getOutlets() {
        return outlets;
    }

    public void setOutlets(List<Outlets> outlets) {
        this.outlets = outlets;
    }

    public String getAddressString() {
        String addressString = null;
        if (addressList != null) {
            if (addressList.getOFFICE() != null) {
                List<UserAddress> office = addressList.getOFFICE();
                if (office != null) {
                    addressString = office.get(0).getAddressLine1();

                    if (!office.get(0).getAddressLine2().isEmpty())
                        addressString += office.get(0).getAddressLine2();
                }
            }
        }
        return addressString;
    }

    public String getOutLetString() {
        String outletString = null;
        if (outlets != null) {
            outletString = new Gson().toJson(outlets);
        }
        return outletString;
    }

    public String getThanaString() {
        String thanaString = null;
        if (addressList != null) {
            if (addressList.getOFFICE() != null) {
                List<UserAddress> office = addressList.getOFFICE();
                if (office != null) {
                    thanaString = office.get(0).getThana();
                }
            }
        }
        return thanaString;
    }

    public String getDistrictString() {
        String districtString = null;
        if (addressList != null) {
            if (addressList.getOFFICE() != null) {
                List<UserAddress> office = addressList.getOFFICE();
                if (office != null) {
                    districtString = office.get(0).getDistrict();
                }
            }
        }
        return districtString;
    }

    @Override
    public String toString() {
        return "BusinessAccountEntry{" +
                "mobileNumber='" + mobileNumber + '\'' +
                ", businessName='" + businessName + '\'' +
                ", email='" + email + '\'' +
                ", businessType=" + businessType +
                ", profilePictureUrl='" + profilePictureUrl + '\'' +
                ", profilePictureUrlMedium='" + profilePictureUrlMedium + '\'' +
                ", profilePictureUrlHigh='" + profilePictureUrlHigh + '\'' +
                ", businessId=" + businessId +
                '}';
    }
}
