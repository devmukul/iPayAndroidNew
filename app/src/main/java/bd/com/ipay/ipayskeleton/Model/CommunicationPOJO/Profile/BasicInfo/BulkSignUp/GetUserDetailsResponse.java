
package bd.com.ipay.ipayskeleton.Model.CommunicationPOJO.Profile.BasicInfo.BulkSignUp;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

public class GetUserDetailsResponse implements Serializable
{

    @SerializedName("bankAccountName")
    @Expose
    private String bankAccountName;
    @SerializedName("bankAccountNumber")
    @Expose
    private String bankAccountNumber;
    @SerializedName("bankInfoChecked")
    @Expose
    private boolean bankInfoChecked;
    @SerializedName("bankName")
    @Expose
    private String bankName;
    @SerializedName("basicInfoChecked")
    @Expose
    private boolean basicInfoChecked;
    @SerializedName("branchName")
    @Expose
    private String branchName;
    @SerializedName("createdAt")
    @Expose
    private Long createdAt;
    @SerializedName("district")
    @Expose
    private String district;
    @SerializedName("dob")
    @Expose
    private String dob;
    @SerializedName("email")
    @Expose
    private String email;
    @SerializedName("fatherMobile")
    @Expose
    private String fatherMobile;
    @SerializedName("fatherName")
    @Expose
    private String fatherName;
    @SerializedName("gender")
    @Expose
    private String gender;
    @SerializedName("id")
    @Expose
    private Long id;
    @SerializedName("message")
    @Expose
    private String message;
    @SerializedName("mobileNumber")
    @Expose
    private String mobileNumber;
    @SerializedName("motherMobile")
    @Expose
    private String motherMobile;
    @SerializedName("motherName")
    @Expose
    private String motherName;
    @SerializedName("name")
    @Expose
    private String name;
    @SerializedName("nid")
    @Expose
    private String nid;
    @SerializedName("occupation")
    @Expose
    private String occupation;
    @SerializedName("organizationName")
    @Expose
    private String organizationName;
    @SerializedName("passport")
    @Expose
    private String passport;
    @SerializedName("permanentAddress")
    @Expose
    private String permanentAddress;
    @SerializedName("presentAddress")
    @Expose
    private String presentAddress;
    @SerializedName("status")
    @Expose
    private String status;
    @SerializedName("thana")
    @Expose
    private String thana;
    private final static long serialVersionUID = 1782816351533053339L;

    /**
     * No args constructor for use in serialization
     * 
     */
    public GetUserDetailsResponse() {
    }

    /**
     * 
     * @param organizationName
     * @param id
     * @param createdAt
     * @param name
     * @param gender
     * @param thana
     * @param district
     * @param permanentAddress
     * @param basicInfoChecked
     * @param bankAccountName
     * @param occupation
     * @param motherName
     * @param status
     * @param fatherMobile
     * @param nid
     * @param bankName
     * @param motherMobile
     * @param message
     * @param presentAddress
     * @param email
     * @param dob
     * @param branchName
     * @param bankAccountNumber
     * @param mobileNumber
     * @param passport
     * @param fatherName
     * @param bankInfoChecked
     */
    public GetUserDetailsResponse(String bankAccountName, String bankAccountNumber, boolean bankInfoChecked, String bankName, boolean basicInfoChecked, String branchName, Long createdAt, String district, String dob, String email, String fatherMobile, String fatherName, String gender, Long id, String message, String mobileNumber, String motherMobile, String motherName, String name, String nid, String occupation, String organizationName, String passport, String permanentAddress, String presentAddress, String status, String thana) {
        super();
        this.bankAccountName = bankAccountName;
        this.bankAccountNumber = bankAccountNumber;
        this.bankInfoChecked = bankInfoChecked;
        this.bankName = bankName;
        this.basicInfoChecked = basicInfoChecked;
        this.branchName = branchName;
        this.createdAt = createdAt;
        this.district = district;
        this.dob = dob;
        this.email = email;
        this.fatherMobile = fatherMobile;
        this.fatherName = fatherName;
        this.gender = gender;
        this.id = id;
        this.message = message;
        this.mobileNumber = mobileNumber;
        this.motherMobile = motherMobile;
        this.motherName = motherName;
        this.name = name;
        this.nid = nid;
        this.occupation = occupation;
        this.organizationName = organizationName;
        this.passport = passport;
        this.permanentAddress = permanentAddress;
        this.presentAddress = presentAddress;
        this.status = status;
        this.thana = thana;
    }

    public String getBankAccountName() {
        return bankAccountName;
    }

    public void setBankAccountName(String bankAccountName) {
        this.bankAccountName = bankAccountName;
    }

    public String getBankAccountNumber() {
        return bankAccountNumber;
    }

    public void setBankAccountNumber(String bankAccountNumber) {
        this.bankAccountNumber = bankAccountNumber;
    }

    public boolean getBankInfoChecked() {
        return bankInfoChecked;
    }

    public void setBankInfoChecked(boolean bankInfoChecked) {
        this.bankInfoChecked = bankInfoChecked;
    }

    public String getBankName() {
        return bankName;
    }

    public void setBankName(String bankName) {
        this.bankName = bankName;
    }

    public boolean getBasicInfoChecked() {
        return basicInfoChecked;
    }

    public void setBasicInfoChecked(boolean basicInfoChecked) {
        this.basicInfoChecked = basicInfoChecked;
    }

    public String getBranchName() {
        return branchName;
    }

    public void setBranchName(String branchName) {
        this.branchName = branchName;
    }

    public Long getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Long createdAt) {
        this.createdAt = createdAt;
    }

    public String getDistrict() {
        return district;
    }

    public void setDistrict(String district) {
        this.district = district;
    }

    public String getDob() {
        return dob;
    }

    public void setDob(String dob) {
        this.dob = dob;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getFatherMobile() {
        return fatherMobile;
    }

    public void setFatherMobile(String fatherMobile) {
        this.fatherMobile = fatherMobile;
    }

    public String getFatherName() {
        return fatherName;
    }

    public void setFatherName(String fatherName) {
        this.fatherName = fatherName;
    }

    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getMobileNumber() {
        return mobileNumber;
    }

    public void setMobileNumber(String mobileNumber) {
        this.mobileNumber = mobileNumber;
    }

    public String getMotherMobile() {
        return motherMobile;
    }

    public void setMotherMobile(String motherMobile) {
        this.motherMobile = motherMobile;
    }

    public String getMotherName() {
        return motherName;
    }

    public void setMotherName(String motherName) {
        this.motherName = motherName;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getNid() {
        return nid;
    }

    public void setNid(String nid) {
        this.nid = nid;
    }

    public String getOccupation() {
        return occupation;
    }

    public void setOccupation(String occupation) {
        this.occupation = occupation;
    }

    public String getOrganizationName() {
        return organizationName;
    }

    public void setOrganizationName(String organizationName) {
        this.organizationName = organizationName;
    }

    public String getPassport() {
        return passport;
    }

    public void setPassport(String passport) {
        this.passport = passport;
    }

    public String getPermanentAddress() {
        return permanentAddress;
    }

    public void setPermanentAddress(String permanentAddress) {
        this.permanentAddress = permanentAddress;
    }

    public String getPresentAddress() {
        return presentAddress;
    }

    public void setPresentAddress(String presentAddress) {
        this.presentAddress = presentAddress;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getThana() {
        return thana;
    }

    public void setThana(String thana) {
        this.thana = thana;
    }

}
