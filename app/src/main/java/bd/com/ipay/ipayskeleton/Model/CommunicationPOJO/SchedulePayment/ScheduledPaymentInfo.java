package bd.com.ipay.ipayskeleton.Model.CommunicationPOJO.SchedulePayment;

import android.os.Parcel;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

import bd.com.ipay.ipayskeleton.Model.CommunicationPOJO.Notification.Notification;

public class ScheduledPaymentInfo implements Serializable, Notification {

    @SerializedName("id")
    @Expose
    private int id;
    @SerializedName("updateId")
    @Expose
    private int updateId;
    @SerializedName("receiverAccountId")
    @Expose
    private String receiverAccountId;
    @SerializedName("customerAccountId")
    @Expose
    private String customerAccountId;
    @SerializedName("receiverInfo")
    @Expose
    private ReceiverInfo receiverInfo;
    @SerializedName("customerInfo")
    @Expose
    private CustomerInfo customerInfo;
    @SerializedName("installmentNumber")
    @Expose
    private int installmentNumber;
    @SerializedName("loanAmount")
    @Expose
    private double loanAmount;
    @SerializedName("product")
    @Expose
    private String product;
    @SerializedName("center")
    @Expose
    private String center;
    @SerializedName("startDate")
    @Expose
    private long startDate;
    @SerializedName("schedulerType")
    @Expose
    private String schedulerType;
    @SerializedName("status")
    @Expose
    private int status;
    @SerializedName("createdAt")
    @Expose
    private long createdAt;
    @SerializedName("updatedAt")
    @Expose
    private long updatedAt;
    @SerializedName("statusUpdatedAt")
    @Expose
    private long statusUpdatedAt;
    @SerializedName("description")
    @Expose
    private String description;
    @SerializedName("amountPaid")
    @Expose
    private double amountPaid;
    @SerializedName("totalAmount")
    @Expose
    private double totalAmount;
    public final static Creator<ScheduledPaymentInfo> CREATOR = new Creator<ScheduledPaymentInfo>() {


        @SuppressWarnings({
                "unchecked"
        })
        public ScheduledPaymentInfo createFromParcel(Parcel in) {
            return new ScheduledPaymentInfo(in);
        }

        public ScheduledPaymentInfo[] newArray(int size) {
            return (new ScheduledPaymentInfo[size]);
        }

    }
            ;
    private final static long serialVersionUID = 7520374914656922206L;

    protected ScheduledPaymentInfo(Parcel in) {
        this.id = ((int) in.readValue((int.class.getClassLoader())));
        this.updateId = ((int) in.readValue((int.class.getClassLoader())));
        this.receiverAccountId = ((String) in.readValue((String.class.getClassLoader())));
        this.customerAccountId = ((String) in.readValue((String.class.getClassLoader())));
        this.receiverInfo = ((ReceiverInfo) in.readValue((ReceiverInfo.class.getClassLoader())));
        this.customerInfo = ((CustomerInfo) in.readValue((CustomerInfo.class.getClassLoader())));
        this.installmentNumber = ((int) in.readValue((int.class.getClassLoader())));
        this.loanAmount = ((double) in.readValue((double.class.getClassLoader())));
        this.product = ((String) in.readValue((String.class.getClassLoader())));
        this.center = ((String) in.readValue((String.class.getClassLoader())));
        this.startDate = ((long) in.readValue((long.class.getClassLoader())));
        this.schedulerType = ((String) in.readValue((String.class.getClassLoader())));
        this.status = ((int) in.readValue((int.class.getClassLoader())));
        this.createdAt = ((long) in.readValue((long.class.getClassLoader())));
        this.updatedAt = ((long) in.readValue((long.class.getClassLoader())));
        this.statusUpdatedAt = ((long) in.readValue((long.class.getClassLoader())));
        this.description = ((String) in.readValue((String.class.getClassLoader())));
        this.amountPaid = ((double) in.readValue((double.class.getClassLoader())));
        this.totalAmount = ((double) in.readValue((double.class.getClassLoader())));
    }

    /**
     * No args constructor for use in serialization
     *
     */
    public ScheduledPaymentInfo() {
    }

    /**
     *
     * @param startDate
     * @param loanAmount
     * @param status
     * @param receiverAccountId
     * @param updateId
     * @param receiverInfo
     * @param schedulerType
     * @param customerInfo
     * @param updatedAt
     * @param id
     * @param installmentNumber
     * @param product
     * @param center
     * @param statusUpdatedAt
     * @param description
     * @param createdAt
     * @param customerAccountId
     * @param totalAmount
     * @param amountPaid
     */
    public ScheduledPaymentInfo(int id, int updateId, String receiverAccountId, String customerAccountId, ReceiverInfo receiverInfo, CustomerInfo customerInfo, int installmentNumber, double loanAmount, String product, String center, long startDate, String schedulerType, int status, long createdAt, long updatedAt, long statusUpdatedAt, String description, double amountPaid, double totalAmount) {
        super();
        this.id = id;
        this.updateId = updateId;
        this.receiverAccountId = receiverAccountId;
        this.customerAccountId = customerAccountId;
        this.receiverInfo = receiverInfo;
        this.customerInfo = customerInfo;
        this.installmentNumber = installmentNumber;
        this.loanAmount = loanAmount;
        this.product = product;
        this.center = center;
        this.startDate = startDate;
        this.schedulerType = schedulerType;
        this.status = status;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.statusUpdatedAt = statusUpdatedAt;
        this.description = description;
        this.amountPaid = amountPaid;
        this.totalAmount = totalAmount;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getUpdateId() {
        return updateId;
    }

    public void setUpdateId(int updateId) {
        this.updateId = updateId;
    }

    public String getReceiverAccountId() {
        return receiverAccountId;
    }

    public void setReceiverAccountId(String receiverAccountId) {
        this.receiverAccountId = receiverAccountId;
    }

    public String getCustomerAccountId() {
        return customerAccountId;
    }

    public void setCustomerAccountId(String customerAccountId) {
        this.customerAccountId = customerAccountId;
    }

    public ReceiverInfo getReceiverInfo() {
        return receiverInfo;
    }

    public void setReceiverInfo(ReceiverInfo receiverInfo) {
        this.receiverInfo = receiverInfo;
    }

    public CustomerInfo getCustomerInfo() {
        return customerInfo;
    }

    public void setCustomerInfo(CustomerInfo customerInfo) {
        this.customerInfo = customerInfo;
    }

    public int getInstallmentNumber() {
        return installmentNumber;
    }

    public void setInstallmentNumber(int installmentNumber) {
        this.installmentNumber = installmentNumber;
    }

    public double getLoanAmount() {
        return loanAmount;
    }

    public void setLoanAmount(double loanAmount) {
        this.loanAmount = loanAmount;
    }

    public String getProduct() {
        return product;
    }

    public void setProduct(String product) {
        this.product = product;
    }

    public String getCenter() {
        return center;
    }

    public void setCenter(String center) {
        this.center = center;
    }

    public long getStartDate() {
        return startDate;
    }

    public void setStartDate(long startDate) {
        this.startDate = startDate;
    }

    public String getSchedulerType() {
        return schedulerType;
    }

    public void setSchedulerType(String schedulerType) {
        this.schedulerType = schedulerType;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public long getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(long createdAt) {
        this.createdAt = createdAt;
    }

    public long getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(long updatedAt) {
        this.updatedAt = updatedAt;
    }

    public long getStatusUpdatedAt() {
        return statusUpdatedAt;
    }

    public void setStatusUpdatedAt(long statusUpdatedAt) {
        this.statusUpdatedAt = statusUpdatedAt;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public double getAmountPaid() {
        return amountPaid;
    }

    public void setAmountPaid(double amountPaid) {
        this.amountPaid = amountPaid;
    }

    public double getTotalAmount() {
        return totalAmount;
    }

    public void setTotalAmount(double totalAmount) {
        this.totalAmount = totalAmount;
    }

    public void writeToParcel(Parcel dest, int flags) {
        dest.writeValue(id);
        dest.writeValue(updateId);
        dest.writeValue(receiverAccountId);
        dest.writeValue(customerAccountId);
        dest.writeValue(receiverInfo);
        dest.writeValue(customerInfo);
        dest.writeValue(installmentNumber);
        dest.writeValue(loanAmount);
        dest.writeValue(product);
        dest.writeValue(center);
        dest.writeValue(startDate);
        dest.writeValue(schedulerType);
        dest.writeValue(status);
        dest.writeValue(createdAt);
        dest.writeValue(updatedAt);
        dest.writeValue(statusUpdatedAt);
        dest.writeValue(description);
        dest.writeValue(amountPaid);
        dest.writeValue(totalAmount);
    }

    public int describeContents() {
        return  0;
    }

    @Override
    public String toString() {
        return "ScheduledPaymentInfo{" +
                "id=" + id +
                ", updateId=" + updateId +
                ", receiverAccountId='" + receiverAccountId + '\'' +
                ", customerAccountId='" + customerAccountId + '\'' +
                ", receiverInfo=" + receiverInfo +
                ", customerInfo=" + customerInfo +
                ", installmentNumber=" + installmentNumber +
                ", loanAmount=" + loanAmount +
                ", product='" + product + '\'' +
                ", center='" + center + '\'' +
                ", startDate=" + startDate +
                ", schedulerType='" + schedulerType + '\'' +
                ", status=" + status +
                ", createdAt=" + createdAt +
                ", updatedAt=" + updatedAt +
                ", statusUpdatedAt=" + statusUpdatedAt +
                ", description='" + description + '\'' +
                ", amountPaid=" + amountPaid +
                ", totalAmount=" + totalAmount +
                '}';
    }

    @Override
    public String getNotificationTitle() {
        return "Schedule Payment Request";
    }

    @Override
    public String getName() {
        return receiverInfo.getName();
    }

    @Override
    public String getImageUrl() {
        return receiverInfo.getProfilePictures().get(0).getUrl();
    }

    @Override
    public long getTime() {
        return updatedAt;
    }

    @Override
    public int getNotificationType() {
        return 7;
    }
}
