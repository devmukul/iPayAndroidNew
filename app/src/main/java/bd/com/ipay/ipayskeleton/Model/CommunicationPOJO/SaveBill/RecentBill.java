
package bd.com.ipay.ipayskeleton.Model.CommunicationPOJO.SaveBill;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.io.Serializable;
import java.util.List;

public class RecentBill implements Serializable {

    private String shortName;
    private boolean isScheduledToo;
    private boolean isSaved;
    private String providerCode;
    private int dateOfBillPayment;
    private int skipNumberOfMonths;
    private String lastPaid;
    private boolean paidForOthers;
    private String metaData;
    private String paramId;
    private String paramLabel;
    private String paramValue;
    private String amount;
    private String amountType;
    private String locationCode;

    public RecentBill(String shortName, boolean isScheduledToo, boolean isSaved, String providerCode, int dateOfBillPayment, int skipNumberOfMonths, String lastPaid, boolean paidForOthers, String metaData, String paramId, String paramLabel, String paramValue, String amount, String amountType, String locationCode) {
        this.shortName = shortName;
        this.isScheduledToo = isScheduledToo;
        this.isSaved = isSaved;
        this.providerCode = providerCode;
        this.dateOfBillPayment = dateOfBillPayment;
        this.skipNumberOfMonths = skipNumberOfMonths;
        this.lastPaid = lastPaid;
        this.paidForOthers = paidForOthers;
        this.metaData = metaData;
        this.paramId = paramId;
        this.paramLabel = paramLabel;
        this.paramValue = paramValue;
        this.amount = amount;
        this.amountType = amountType;
        this.locationCode = locationCode;
    }

    public RecentBill() {
    }

    public String getShortName() {
        return shortName;
    }

    public void setShortName(String shortName) {
        this.shortName = shortName;
    }

    public boolean getScheduledToo() {
        return isScheduledToo;
    }

    public void setScheduledToo(boolean scheduledToo) {
        isScheduledToo = scheduledToo;
    }

    public boolean getSaved() {
        return isSaved;
    }

    public void setSaved(boolean saved) {
        isSaved = saved;
    }

    public String getProviderCode() {
        return providerCode;
    }

    public void setProviderCode(String providerCode) {
        this.providerCode = providerCode;
    }

    public int getDateOfBillPayment() {
        return dateOfBillPayment;
    }

    public void setDateOfBillPayment(int dateOfBillPayment) {
        this.dateOfBillPayment = dateOfBillPayment;
    }

    public int getSkipNumberOfMonths() {
        return skipNumberOfMonths;
    }

    public void setSkipNumberOfMonths(int skipNumberOfMonths) {
        this.skipNumberOfMonths = skipNumberOfMonths;
    }

    public String getLastPaid() {
        return lastPaid;
    }

    public void setLastPaid(String lastPaid) {
        this.lastPaid = lastPaid;
    }

    public boolean getPaidForOthers() {
        return paidForOthers;
    }

    public void setPaidForOthers(boolean paidForOthers) {
        this.paidForOthers = paidForOthers;
    }

    public String getMetaData() {
        return metaData;
    }

    public void setMetaData(String metaData) {
        this.metaData = metaData;
    }

    public String getParamId() {
        return paramId;
    }

    public void setParamId(String paramId) {
        this.paramId = paramId;
    }

    public String getParamLabel() {
        return paramLabel;
    }

    public void setParamLabel(String paramLabel) {
        this.paramLabel = paramLabel;
    }

    public String getParamValue() {
        return paramValue;
    }

    public void setParamValue(String paramValue) {
        this.paramValue = paramValue;
    }

    public String getAmount() {
        return amount;
    }

    public void setAmount(String amount) {
        this.amount = amount;
    }

    public String getAmountType() {
        return amountType;
    }

    public void setAmountType(String amountType) {
        this.amountType = amountType;
    }

    public String getLocationCode() {
        return locationCode;
    }

    public void setLocationCode(String locationCode) {
        this.locationCode = locationCode;
    }

    @Override
    public String toString() {
        return "RecentBill{" +
                "shortName='" + shortName + '\'' +
                ", isScheduledToo=" + isScheduledToo +
                ", isSaved=" + isSaved +
                ", providerCode='" + providerCode + '\'' +
                ", dateOfBillPayment=" + dateOfBillPayment +
                ", skipNumberOfMonths=" + skipNumberOfMonths +
                ", lastPaid='" + lastPaid + '\'' +
                ", paidForOthers=" + paidForOthers +
                ", metaData='" + metaData + '\'' +
                ", paramId='" + paramId + '\'' +
                ", paramLabel='" + paramLabel + '\'' +
                ", paramValue='" + paramValue + '\'' +
                ", amount='" + amount + '\'' +
                ", amountType='" + amountType + '\'' +
                '}';
    }
}
