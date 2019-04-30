
package bd.com.ipay.ipayskeleton.Model.CommunicationPOJO.SaveBill;

import java.io.Serializable;
import java.util.List;
import android.os.Parcel;
import android.os.Parcelable;
import android.os.Parcelable.Creator;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class SavedBill implements Serializable
{

    @SerializedName("id")
    @Expose
    private int id;
    @SerializedName("version")
    @Expose
    private String version;
    @SerializedName("shortName")
    @Expose
    private String shortName;
    @SerializedName("isScheduledToo")
    @Expose
    private Boolean isScheduledToo;
    @SerializedName("providerCode")
    @Expose
    private String providerCode;
    @SerializedName("dateOfBillPayment")
    @Expose
    private int dateOfBillPayment;
    @SerializedName("skipNumberOfMonths")
    @Expose
    private int skipNumberOfMonths;
    @SerializedName("lastPaid")
    @Expose
    private String lastPaid;
    @SerializedName("paidForOthers")
    @Expose
    private Boolean paidForOthers;
    @SerializedName("metaData")
    @Expose
    private MetaData metaData;
    @SerializedName("billParams")
    @Expose
    private List<BillParam> billParams = null;
//    public final static Creator<SavedBill> CREATOR = new Creator<SavedBill>() {
//
//
//        @SuppressWarnings({
//            "unchecked"
//        })
//        public SavedBill createFromParcel(Parcel in) {
//            return new SavedBill(in);
//        }
//
//        public SavedBill[] newArray(int size) {
//            return (new SavedBill[size]);
//        }
//
//    }
//    ;
//    private final static long serialVersionUID = 2720671897152056552L;
//
//    protected SavedBill(Parcel in) {
//        this.id = ((int) in.readValue((Long.class.getClassLoader())));
//        this.version = ((String) in.readValue((String.class.getClassLoader())));
//        this.shortName = ((String) in.readValue((String.class.getClassLoader())));
//        this.isScheduledToo = ((Boolean) in.readValue((Boolean.class.getClassLoader())));
//        this.providerCode = ((String) in.readValue((String.class.getClassLoader())));
//        this.dateOfBillPayment = ((int) in.readValue((int.class.getClassLoader())));
//        this.skipNumberOfMonths = ((int) in.readValue((int.class.getClassLoader())));
//        this.lastPaid = ((String) in.readValue((String.class.getClassLoader())));
//        this.paidForOthers = ((Boolean) in.readValue((Boolean.class.getClassLoader())));
//        this.metaData = ((MetaData) in.readValue((MetaData.class.getClassLoader())));
//        in.readList(this.billParams, (BillParam.class.getClassLoader()));
//    }

    /**
     * No args constructor for use in serialization
     * 
     */
    public SavedBill() {
    }

    /**
     * 
     * @param id
     * @param paidForOthers
     * @param billParams
     * @param lastPaid
     * @param dateOfBillPayment
     * @param skipNumberOfMonths
     * @param isScheduledToo
     * @param providerCode
     * @param shortName
     * @param metaData
     * @param version
     */
    public SavedBill(int id, String version, String shortName, Boolean isScheduledToo, String providerCode, int dateOfBillPayment, int skipNumberOfMonths,String lastPaid, Boolean paidForOthers, MetaData metaData, List<BillParam> billParams) {
        super();
        this.id = id;
        this.version = version;
        this.shortName = shortName;
        this.isScheduledToo = isScheduledToo;
        this.providerCode = providerCode;
        this.dateOfBillPayment = dateOfBillPayment;

        this.skipNumberOfMonths = skipNumberOfMonths;
        this.lastPaid = lastPaid;
        this.paidForOthers = paidForOthers;
        this.metaData = metaData;
        this.billParams = billParams;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public String getShortName() {
        return shortName;
    }

    public void setShortName(String shortName) {
        this.shortName = shortName;
    }

    public Boolean getIsScheduledToo() {
        return isScheduledToo;
    }

    public void setIsScheduledToo(Boolean isScheduledToo) {
        this.isScheduledToo = isScheduledToo;
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

    public String getLastPaid() {
        return lastPaid;
    }

    public void setLastPaid(String lastPaid) {
        this.lastPaid = lastPaid;
    }

    public Boolean getPaidForOthers() {
        return paidForOthers;
    }

    public void setPaidForOthers(Boolean paidForOthers) {
        this.paidForOthers = paidForOthers;
    }

    public MetaData getMetaData() {
        return metaData;
    }

    public void setMetaData(MetaData metaData) {
        this.metaData = metaData;
    }

    public List<BillParam> getBillParams() {
        return billParams;
    }

    public void setBillParams(List<BillParam> billParams) {
        this.billParams = billParams;
    }

    public int getSkipNumberOfMonths() {
        return skipNumberOfMonths;
    }

    public void setSkipNumberOfMonths(int skipNumberOfMonths) {
        this.skipNumberOfMonths = skipNumberOfMonths;
    }

//    public void writeToParcel(Parcel dest, int flags) {
//        dest.writeValue(id);
//        dest.writeValue(version);
//        dest.writeValue(shortName);
//        dest.writeValue(isScheduledToo);
//        dest.writeValue(providerCode);
//        dest.writeValue(dateOfBillPayment);
//
//        dest.writeValue(skipNumberOfMonths);
//        dest.writeValue(lastPaid);
//        dest.writeValue(paidForOthers);
//        dest.writeValue(metaData);
//        dest.writeList(billParams);
//    }

    public int describeContents() {
        return  0;
    }

    @Override
    public String toString() {
        return "SavedBill{" +
                "id=" + id +
                ", version='" + version + '\'' +
                ", shortName='" + shortName + '\'' +
                ", isScheduledToo=" + isScheduledToo +
                ", providerCode='" + providerCode + '\'' +
                ", dateOfBillPayment=" + dateOfBillPayment +
                ", skipNumberOfMonths=" + skipNumberOfMonths +
                ", lastPaid='" + lastPaid + '\'' +
                ", paidForOthers=" + paidForOthers +
                ", metaData=" + metaData +
                ", billParams=" + billParams +
                '}';
    }
}
