
package bd.com.ipay.ipayskeleton.Model.CommunicationPOJO.SchedulePayment;

import java.io.Serializable;
import android.os.Parcel;
import android.os.Parcelable;
import android.os.Parcelable.Creator;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class InstallmentInfoList implements Serializable, Parcelable
{

    @SerializedName("id")
    @Expose
    private int id;
    @SerializedName("amount")
    @Expose
    private double amount;
    @SerializedName("triggerDate")
    @Expose
    private long triggerDate;
    @SerializedName("status")
    @Expose
    private int status;
    @SerializedName("comment")
    @Expose
    private String comment;
    public final static Creator<InstallmentInfoList> CREATOR = new Creator<InstallmentInfoList>() {


        @SuppressWarnings({
            "unchecked"
        })
        public InstallmentInfoList createFromParcel(Parcel in) {
            return new InstallmentInfoList(in);
        }

        public InstallmentInfoList[] newArray(int size) {
            return (new InstallmentInfoList[size]);
        }

    }
    ;
    private final static long serialVersionUID = -5563466544846903235L;

    protected InstallmentInfoList(Parcel in) {
        this.id = ((int) in.readValue((int.class.getClassLoader())));
        this.amount = ((double) in.readValue((double.class.getClassLoader())));
        this.triggerDate = ((long) in.readValue((long.class.getClassLoader())));
        this.status = ((int) in.readValue((int.class.getClassLoader())));
        this.comment = ((String) in.readValue((String.class.getClassLoader())));
    }

    /**
     * No args constructor for use in serialization
     * 
     */
    public InstallmentInfoList() {
    }

    /**
     * 
     * @param amount
     * @param id
     * @param triggerDate
     * @param status
     * @param comment
     */
    public InstallmentInfoList(int id, double amount, long triggerDate, int status, String comment) {
        super();
        this.id = id;
        this.amount = amount;
        this.triggerDate = triggerDate;
        this.status = status;
        this.comment = comment;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public double getAmount() {
        return amount;
    }

    public void setAmount(double amount) {
        this.amount = amount;
    }

    public long getTriggerDate() {
        return triggerDate;
    }

    public void setTriggerDate(long triggerDate) {
        this.triggerDate = triggerDate;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public void writeToParcel(Parcel dest, int flags) {
        dest.writeValue(id);
        dest.writeValue(amount);
        dest.writeValue(triggerDate);
        dest.writeValue(status);
        dest.writeValue(comment);
    }

    public int describeContents() {
        return  0;
    }

    @Override
    public String toString() {
        return "InstallmentInfoList{" +
                "id=" + id +
                ", amount=" + amount +
                ", triggerDate=" + triggerDate +
                ", status=" + status +
                ", comment='" + comment + '\'' +
                '}';
    }
}
