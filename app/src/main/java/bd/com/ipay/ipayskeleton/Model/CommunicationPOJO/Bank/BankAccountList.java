
package bd.com.ipay.ipayskeleton.Model.CommunicationPOJO.Bank;

import android.content.Context;
import android.content.res.Resources;
import android.os.Parcel;
import android.os.Parcelable;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.List;

public class BankAccountList implements Parcelable {

    @SerializedName("bankAccountId")
    @Expose
    private Long bankAccountId;
    @SerializedName("branchRoutingNumber")
    @Expose
    private String branchRoutingNumber;
    @SerializedName("accountName")
    @Expose
    private String accountName;
    @SerializedName("accountNumber")
    @Expose
    private String accountNumber;
    @SerializedName("accountStatus")
    @Expose
    private Long accountStatus;
    @SerializedName("verificationStatus")
    @Expose
    private String verificationStatus;
    @SerializedName("bankName")
    @Expose
    private String bankName;
    @SerializedName("branchName")
    @Expose
    private String branchName;
    @SerializedName("bankCode")
    @Expose
    private String bankCode;
    @SerializedName("bankDocuments")
    @Expose
    private List<BankDocument> bankDocuments = null;

    public BankAccountList() {
    }

    public BankAccountList(Long bankAccountId, String branchRoutingNumber, String accountName, String accountNumber, Long accountStatus, String verificationStatus, String bankName, String branchName, String bankCode, List<BankDocument> bankDocuments) {
        super();
        this.bankAccountId = bankAccountId;
        this.branchRoutingNumber = branchRoutingNumber;
        this.accountName = accountName;
        this.accountNumber = accountNumber;
        this.accountStatus = accountStatus;
        this.verificationStatus = verificationStatus;
        this.bankName = bankName;
        this.branchName = branchName;
        this.bankCode = bankCode;
        this.bankDocuments = bankDocuments;
    }


    protected BankAccountList(Parcel in) {
        if (in.readByte() == 0) {
            bankAccountId = null;
        } else {
            bankAccountId = in.readLong();
        }
        branchRoutingNumber = in.readString();
        accountName = in.readString();
        accountNumber = in.readString();
        if (in.readByte() == 0) {
            accountStatus = null;
        } else {
            accountStatus = in.readLong();
        }
        verificationStatus = in.readString();
        bankName = in.readString();
        branchName = in.readString();
        bankCode = in.readString();
        bankDocuments = in.createTypedArrayList(BankDocument.CREATOR);
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        if (bankAccountId == null) {
            dest.writeByte((byte) 0);
        } else {
            dest.writeByte((byte) 1);
            dest.writeLong(bankAccountId);
        }
        dest.writeString(branchRoutingNumber);
        dest.writeString(accountName);
        dest.writeString(accountNumber);
        if (accountStatus == null) {
            dest.writeByte((byte) 0);
        } else {
            dest.writeByte((byte) 1);
            dest.writeLong(accountStatus);
        }
        dest.writeString(verificationStatus);
        dest.writeString(bankName);
        dest.writeString(branchName);
        dest.writeString(bankCode);
        dest.writeTypedList(bankDocuments);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<BankAccountList> CREATOR = new Creator<BankAccountList>() {
        @Override
        public BankAccountList createFromParcel(Parcel in) {
            return new BankAccountList(in);
        }

        @Override
        public BankAccountList[] newArray(int size) {
            return new BankAccountList[size];
        }
    };

    public Long getBankAccountId() {
        return bankAccountId;
    }

    public void setBankAccountId(Long bankAccountId) {
        this.bankAccountId = bankAccountId;
    }

    public String getBranchRoutingNumber() {
        return branchRoutingNumber;
    }

    public void setBranchRoutingNumber(String branchRoutingNumber) {
        this.branchRoutingNumber = branchRoutingNumber;
    }

    public String getAccountName() {
        return accountName;
    }

    public void setAccountName(String accountName) {
        this.accountName = accountName;
    }

    public String getAccountNumber() {
        return accountNumber;
    }

    public void setAccountNumber(String accountNumber) {
        this.accountNumber = accountNumber;
    }

    public Long getAccountStatus() {
        return accountStatus;
    }

    public void setAccountStatus(Long accountStatus) {
        this.accountStatus = accountStatus;
    }

    public String getVerificationStatus() {
        return verificationStatus;
    }

    public void setVerificationStatus(String verificationStatus) {
        this.verificationStatus = verificationStatus;
    }

    public String getBankName() {
        return bankName;
    }

    public void setBankName(String bankName) {
        this.bankName = bankName;
    }

    public String getBranchName() {
        return branchName;
    }

    public void setBranchName(String branchName) {
        this.branchName = branchName;
    }

    public String getBankCode() {
        return bankCode;
    }

    public void setBankCode(String bankCode) {
        this.bankCode = bankCode;
    }

    public List<BankDocument> getBankDocuments() {
        return bankDocuments;
    }

    public void setBankDocuments(List<BankDocument> bankDocuments) {
        this.bankDocuments = bankDocuments;
    }

    public int getBankIcon(Context context) {
        Resources resources = context.getResources();
        int resourceId;
        if (bankCode != null)
            resourceId = resources.getIdentifier("ic_bank" + getBankCode(), "drawable",
                    context.getPackageName());
        else
            resourceId = resources.getIdentifier("ic_bank" + "111", "drawable",
                    context.getPackageName());
        return resourceId;
        //return resources.getDrawable(resourceId);
    }

}
