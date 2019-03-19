
package bd.com.ipay.ipayskeleton.Model.CommunicationPOJO.IPDC;

import java.io.Serializable;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class ScheduledPaymentInfo implements Serializable
{

    @SerializedName("amountPaid")
    @Expose
    private Long amountPaid;
    @SerializedName("center")
    @Expose
    private String center;
    @SerializedName("createdAt")
    @Expose
    private Long createdAt;
    @SerializedName("customerAccountId")
    @Expose
    private String customerAccountId;
    @SerializedName("customerInfo")
    @Expose
    private UserInfo customerInfo;
    @SerializedName("description")
    @Expose
    private String description;
    @SerializedName("id")
    @Expose
    private Long id;
    @SerializedName("installmentAmount")
    @Expose
    private Long installmentAmount;
    @SerializedName("installmentNumber")
    @Expose
    private Long installmentNumber;
    @SerializedName("product")
    @Expose
    private String product;
    @SerializedName("receiverAccountId")
    @Expose
    private String receiverAccountId;
    @SerializedName("receiverInfo")
    @Expose
    private ReceiverInfo receiverInfo;
    @SerializedName("schedulerType")
    @Expose
    private String schedulerType;
    @SerializedName("startDate")
    @Expose
    private Long startDate;
    @SerializedName("status")
    @Expose
    private Long status;
    @SerializedName("statusUpdatedAt")
    @Expose
    private Long statusUpdatedAt;
    @SerializedName("totalAmount")
    @Expose
    private Long totalAmount;
    @SerializedName("updatedAt")
    @Expose
    private Long updatedAt;
    private final static long serialVersionUID = 2849313800637187539L;

    /**
     * No args constructor for use in serialization
     * 
     */
    public ScheduledPaymentInfo() {
    }

    /**
     * 
     * @param startDate
     * @param status
     * @param receiverAccountId
     * @param schedulerType
     * @param receiverInfo
     * @param customerInfo
     * @param updatedAt
     * @param center
     * @param id
     * @param installmentNumber
     * @param product
     * @param statusUpdatedAt
     * @param createdAt
     * @param description
     * @param customerAccountId
     * @param totalAmount
     * @param amountPaid
     * @param installmentAmount
     */
    public ScheduledPaymentInfo(Long amountPaid, String center, Long createdAt, String customerAccountId, UserInfo customerInfo, String description, Long id, Long installmentAmount, Long installmentNumber, String product, String receiverAccountId, ReceiverInfo receiverInfo, String schedulerType, Long startDate, Long status, Long statusUpdatedAt, Long totalAmount, Long updatedAt) {
        super();
        this.amountPaid = amountPaid;
        this.center = center;
        this.createdAt = createdAt;
        this.customerAccountId = customerAccountId;
        this.customerInfo = customerInfo;
        this.description = description;
        this.id = id;
        this.installmentAmount = installmentAmount;
        this.installmentNumber = installmentNumber;
        this.product = product;
        this.receiverAccountId = receiverAccountId;
        this.receiverInfo = receiverInfo;
        this.schedulerType = schedulerType;
        this.startDate = startDate;
        this.status = status;
        this.statusUpdatedAt = statusUpdatedAt;
        this.totalAmount = totalAmount;
        this.updatedAt = updatedAt;
    }

    public Long getAmountPaid() {
        return amountPaid;
    }

    public void setAmountPaid(Long amountPaid) {
        this.amountPaid = amountPaid;
    }

    public String getCenter() {
        return center;
    }

    public void setCenter(String center) {
        this.center = center;
    }

    public Long getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Long createdAt) {
        this.createdAt = createdAt;
    }

    public String getCustomerAccountId() {
        return customerAccountId;
    }

    public void setCustomerAccountId(String customerAccountId) {
        this.customerAccountId = customerAccountId;
    }

    public UserInfo getCustomerInfo() {
        return customerInfo;
    }

    public void setCustomerInfo(UserInfo customerInfo) {
        this.customerInfo = customerInfo;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getInstallmentAmount() {
        return installmentAmount;
    }

    public void setInstallmentAmount(Long installmentAmount) {
        this.installmentAmount = installmentAmount;
    }

    public Long getInstallmentNumber() {
        return installmentNumber;
    }

    public void setInstallmentNumber(Long installmentNumber) {
        this.installmentNumber = installmentNumber;
    }

    public String getProduct() {
        return product;
    }

    public void setProduct(String product) {
        this.product = product;
    }

    public String getReceiverAccountId() {
        return receiverAccountId;
    }

    public void setReceiverAccountId(String receiverAccountId) {
        this.receiverAccountId = receiverAccountId;
    }

    public ReceiverInfo getReceiverInfo() {
        return receiverInfo;
    }

    public void setReceiverInfo(ReceiverInfo receiverInfo) {
        this.receiverInfo = receiverInfo;
    }

    public String getSchedulerType() {
        return schedulerType;
    }

    public void setSchedulerType(String schedulerType) {
        this.schedulerType = schedulerType;
    }

    public Long getStartDate() {
        return startDate;
    }

    public void setStartDate(Long startDate) {
        this.startDate = startDate;
    }

    public Long getStatus() {
        return status;
    }

    public void setStatus(Long status) {
        this.status = status;
    }

    public Long getStatusUpdatedAt() {
        return statusUpdatedAt;
    }

    public void setStatusUpdatedAt(Long statusUpdatedAt) {
        this.statusUpdatedAt = statusUpdatedAt;
    }

    public Long getTotalAmount() {
        return totalAmount;
    }

    public void setTotalAmount(Long totalAmount) {
        this.totalAmount = totalAmount;
    }

    public Long getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Long updatedAt) {
        this.updatedAt = updatedAt;
    }

}
