
package bd.com.ipay.ipayskeleton.Model.CommunicationPOJO.IPDC;

import android.os.Parcel;

import bd.com.ipay.ipayskeleton.Model.CommunicationPOJO.Notification.Notification;

public class ScheduledPaymentInfoPercel_1 implements Notification
{

    private int id;
    private int updateId;
    private String receiverAccountId;
    private String customerAccountId;
    private ReceiverInfoParcel receiverInfo;
    private CustomerInfo customerInfo;
    private int installmentNumber;
    private Long loanAmount;
    private String product;
    private String center;
    private Long startDate;
    private String schedulerType;
    private int status;
    private Long createdAt;
    private Long updatedAt;
    private Long statusUpdatedAt;
    private String description;
    private Long amountPaid;
    private Long totalAmount;

    public ScheduledPaymentInfoPercel_1() {

    }

    public ScheduledPaymentInfoPercel_1(int id, int updateId, String receiverAccountId, String customerAccountId, ReceiverInfoParcel receiverInfo, CustomerInfo customerInfo, int installmentNumber, Long loanAmount, String product, String center, Long startDate, String schedulerType, int status, Long createdAt, Long updatedAt, Long statusUpdatedAt, String description, Long amountPaid, Long totalAmount) {
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

    public ReceiverInfoParcel getReceiverInfo() {
        return receiverInfo;
    }

    public void setReceiverInfo(ReceiverInfoParcel receiverInfo) {
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

    public Long getLoanAmount() {
        return loanAmount;
    }

    public void setLoanAmount(Long loanAmount) {
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

    public Long getStartDate() {
        return startDate;
    }

    public void setStartDate(Long startDate) {
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

    public Long getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Long createdAt) {
        this.createdAt = createdAt;
    }

    public Long getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Long updatedAt) {
        this.updatedAt = updatedAt;
    }

    public Long getStatusUpdatedAt() {
        return statusUpdatedAt;
    }

    public void setStatusUpdatedAt(Long statusUpdatedAt) {
        this.statusUpdatedAt = statusUpdatedAt;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Long getAmountPaid() {
        return amountPaid;
    }

    public void setAmountPaid(Long amountPaid) {
        this.amountPaid = amountPaid;
    }

    public Long getTotalAmount() {
        return totalAmount;
    }

    public void setTotalAmount(Long totalAmount) {
        this.totalAmount = totalAmount;
    }

    protected ScheduledPaymentInfoPercel_1(Parcel in) {
        id = in.readInt();
        updateId = in.readInt();
        receiverAccountId = in.readString();
        customerAccountId = in.readString();
        receiverInfo = in.readParcelable(ReceiverInfo.class.getClassLoader());
        customerInfo = in.readParcelable(CustomerInfo.class.getClassLoader());
        installmentNumber = in.readInt();
        if (in.readByte() == 0) {
            loanAmount = null;
        } else {
            loanAmount = in.readLong();
        }
        product = in.readString();
        center = in.readString();

        if (in.readByte() == 0) {
            startDate = null;
        } else {
            startDate = in.readLong();
        }

        schedulerType = in.readString();
        status = in.readInt();
        if (in.readByte() == 0) {
            createdAt = null;
        } else {
            createdAt = in.readLong();
        }

        if (in.readByte() == 0) {
            updatedAt = null;
        } else {
            updatedAt = in.readLong();
        }


        if (in.readByte() == 0) {
            statusUpdatedAt = null;
        } else {
            statusUpdatedAt = in.readLong();
        }

        description = in.readString();

        if (in.readByte() == 0) {
            amountPaid = null;
        } else {
            amountPaid = in.readLong();
        }


        if (in.readByte() == 0) {
            totalAmount = null;
        } else {
            totalAmount = in.readLong();
        }


    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(id);
        dest.writeInt(updateId);
        dest.writeString(receiverAccountId);
        dest.writeString(customerAccountId);
        if (loanAmount == null) {
            dest.writeByte((byte) 0);
        } else {
            dest.writeByte((byte) 1);
            dest.writeLong(loanAmount);
        }

        dest.writeParcelable(receiverInfo, flags);
        dest.writeString(product);
        dest.writeString(center);

        if (startDate == null) {
            dest.writeByte((byte) 0);
        } else {
            dest.writeByte((byte) 1);
            dest.writeLong(startDate);
        }


        dest.writeInt(status);

        if (createdAt == null) {
            dest.writeByte((byte) 0);
        } else {
            dest.writeByte((byte) 1);
            dest.writeLong(createdAt);
        }

        if (updatedAt == null) {
            dest.writeByte((byte) 0);
        } else {
            dest.writeByte((byte) 1);
            dest.writeLong(updatedAt);
        }

        if (statusUpdatedAt == null) {
            dest.writeByte((byte) 0);
        } else {
            dest.writeByte((byte) 1);
            dest.writeLong(statusUpdatedAt);
        }

        dest.writeString(description);

        if (amountPaid == null) {
            dest.writeByte((byte) 0);
        } else {
            dest.writeByte((byte) 1);
            dest.writeLong(amountPaid);
        }

        if (totalAmount == null) {
            dest.writeByte((byte) 0);
        } else {
            dest.writeByte((byte) 1);
            dest.writeLong(totalAmount);
        }
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<ScheduledPaymentInfoPercel_1> CREATOR = new Creator<ScheduledPaymentInfoPercel_1>() {
        @Override
        public ScheduledPaymentInfoPercel_1 createFromParcel(Parcel in) {
            return new ScheduledPaymentInfoPercel_1(in);
        }

        @Override
        public ScheduledPaymentInfoPercel_1[] newArray(int size) {
            return new ScheduledPaymentInfoPercel_1[size];
        }
    };

    @Override
    public String getNotificationTitle() {
        return "Schedule Payment Request";
    }

    @Override
    public String getName() {
        System.out.println("Test  "+receiverInfo.toString());
        return receiverInfo.getName();
    }

    @Override
    public String getImageUrl() {
        return "/image/d1aa4c30-0e67-4376-b14b-2ac173c26ba0.jpg";
    }

    @Override
    public long getTime() {
        return updatedAt;
    }

    @Override
    public int getNotificationType() {
        return 7;
    }

    @Override
    public String toString() {
        return "ScheduledPaymentInfoPercel{" +
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
}
